package org.unipd.nbeghin.climbtheworld.ui.card;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.SimpleTimeZone;

import org.json.JSONException;
import org.json.JSONObject;
import org.unipd.nbeghin.climbtheworld.ClimbApplication;
import org.unipd.nbeghin.climbtheworld.R;
import org.unipd.nbeghin.climbtheworld.models.AskCollaborationNotification;
import org.unipd.nbeghin.climbtheworld.models.AskCompetitionNotification;
import org.unipd.nbeghin.climbtheworld.models.AskTeamDuelNotification;
import org.unipd.nbeghin.climbtheworld.models.Building;
import org.unipd.nbeghin.climbtheworld.models.Climbing;
import org.unipd.nbeghin.climbtheworld.models.Collaboration;
import org.unipd.nbeghin.climbtheworld.models.Competition;
import org.unipd.nbeghin.climbtheworld.models.Group;
import org.unipd.nbeghin.climbtheworld.models.Notification;
import org.unipd.nbeghin.climbtheworld.models.NotificationType;
import org.unipd.nbeghin.climbtheworld.models.TeamDuel;
import org.unipd.nbeghin.climbtheworld.models.User;
import org.unipd.nbeghin.climbtheworld.util.FacebookUtils;
import org.unipd.nbeghin.climbtheworld.util.ModelsUtil;
import org.unipd.nbeghin.climbtheworld.util.ParseUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.fima.cardsui.objects.Card;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

public class NotificationCard extends Card {

	SharedPreferences pref = ClimbApplication.getContext().getSharedPreferences("UserSession", 0);
	final Notification notification;
	ImageButton acceptBtn;
	ImageButton cancelBtn;
	TextView text;
	ProgressBar progressBar;
	boolean enabled;

	public NotificationCard(Notification notification, boolean enabled) {
		super(notification.getId());
		this.notification = notification;
		this.enabled = enabled;
	}

	private String setMessage() {
		switch (notification.getType()) {
		case INVITE_IN_GROUP:
			return ClimbApplication.getContext().getString(R.string.message_group_invite, notification.getSender(), notification.getGroupName());
		case ASK_COLLABORATION:
			return ClimbApplication.getContext().getString(R.string.message_collaboration, notification.getSender(), ((AskCollaborationNotification) notification).getBuilding_name());
		case ASK_COMPETITION:
			return ClimbApplication.getContext().getString(R.string.message_competition, notification.getSender(), ((AskCompetitionNotification) notification).getBuilding_name());
		case ASK_TEAM_COMPETITION_CHALLENGER:
			return ClimbApplication.getContext().getString(R.string.message_team_challenge, notification.getSender(), ((AskTeamDuelNotification) notification).getBuilding_name());
		case ASK_TEAM_COMPETITION_TEAM:
			return ClimbApplication.getContext().getString(R.string.message_team_member, notification.getSender(), ((AskTeamDuelNotification) notification).getBuilding_name());

		default:
			return "";
		}
	}

	@Override
	public View getCardContent(final Context context) {
		View view = LayoutInflater.from(context).inflate(R.layout.notification_card, null);
		cancelBtn = ((ImageButton) view.findViewById(R.id.cancelButton));
		acceptBtn = (ImageButton) view.findViewById(R.id.acceptButton);
		text = ((TextView) view.findViewById(R.id.text));
		progressBar = (ProgressBar) view.findViewById(R.id.progressBarNotf);
		progressBar.setIndeterminate(true);

		text.setText(setMessage());
		/*
		 * if (enabled){ acceptBtn.setVisibility(View.VISIBLE); acceptBtn.setEnabled(true); cancelBtn.setVisibility(View.VISIBLE); cancelBtn.setEnabled(true); view.setBackgroundColor(Color.parseColor("#fffb94")); }else{ acceptBtn.setVisibility(View.INVISIBLE); acceptBtn.setEnabled(false); cancelBtn.setVisibility(View.INVISIBLE); cancelBtn.setEnabled(false); view.setBackgroundColor(Color.parseColor("#ffffff")); }
		 */

		acceptBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {

				boolean busy = ClimbApplication.BUSY;
				if (FacebookUtils.isOnline(context) && !busy) {
					acceptBtn.setVisibility(View.GONE);
					cancelBtn.setVisibility(View.GONE);
					progressBar.setVisibility(View.VISIBLE);
					ClimbApplication.BUSY = true;
					NotificationType type = notification.getType();
					switch (type) {
					case INVITE_IN_GROUP:
						ParseQuery<ParseObject> query = ParseQuery.getQuery("Group");
						query.whereEqualTo("name", notification.getGroupName());
						query.findInBackground(new FindCallback<ParseObject>() {
							public void done(List<ParseObject> group, ParseException e) {
								if (e == null) {
									if (group.size() == 0) {
										Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.group_not_exists), Toast.LENGTH_SHORT).show();
										text.setText("Request expired");

									} else {
										SharedPreferences pref = context.getSharedPreferences("UserSession", 0);
										JSONObject members = group.get(0).getJSONObject("members");
										if (members.has(pref.getString("FBid", ""))) {
											Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.group_already_part, notification.getGroupName()), Toast.LENGTH_SHORT).show();
											text.setText("Already a member");
										} else {
											updateGroup(group.get(0));
											text.setText("Request Accepted");
										}

									}
									deleteRequest(String.valueOf(notification.getId()));

									cancelBtn.setEnabled(false);
									acceptBtn.setEnabled(false);
									notification.setRead(true);

								} else {
									Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();

									Log.d("Connection problem", "Error: " + e.getMessage());

								}
								ClimbApplication.BUSY = false;
								progressBar.setVisibility(View.GONE);

							}

						});
						break;
					case ASK_COLLABORATION:

						final AskCollaborationNotification current = ((AskCollaborationNotification) notification);

						Collaboration collabs = ClimbApplication.getCollaborationByBuildingAndUser(current.getBuilding_id(), pref.getInt("local_id", -1));
						Competition compet = ClimbApplication.getCompetitionByBuildingAndUser(current.getBuilding_id(), pref.getInt("local_id", -1));
						TeamDuel duel = ClimbApplication.getTeamDuelByBuildingAndUser(current.getBuilding_id(), pref.getInt("local_id", -1));
						
						final Building building = ClimbApplication.getBuildingById(current.getBuilding_id());
						final User me = ClimbApplication.getUserById(pref.getInt("local_id", -1));

						if(me.getLevel() >= building.getBase_level()){

						// stessa per team
						if (collabs == null && compet == null && duel == null) {

							// pick collaboration from Parse (use the id in the notification)
							ParseQuery<ParseObject> queryColl = ParseQuery.getQuery("Collaboration");
							queryColl.whereEqualTo("objectId", current.getCollaborationId());
							queryColl.findInBackground(new FindCallback<ParseObject>() {

								@Override
								public void done(List<ParseObject> collabs, ParseException e) {
									if (e == null) {
										if (collabs.size() == 0) {// this collaboration has been deleted by someone else
											Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.collaboration_not_exists), Toast.LENGTH_SHORT).show();
											text.setText(ClimbApplication.getContext().getString(R.string.collaboration_not_exists));
											deleteRequest(String.valueOf(notification.getId()));

											cancelBtn.setEnabled(false);
											acceptBtn.setEnabled(false);
											notification.setRead(true);
											ClimbApplication.BUSY = false;
											progressBar.setVisibility(View.GONE);

										} else {
											// collaboration object found in parse
											final ParseObject collaborationParse = collabs.get(0);
											JSONObject collaborators = collaborationParse.getJSONObject("collaborators");
											JSONObject stairs = collaborationParse.getJSONObject("stairs");

											boolean meIn = collaborators.has(pref.getString("FBid", ""));

											if (!meIn) {// I am not already part of the collaboration

												int n_collaborators = collaborators.length();
												if (n_collaborators < ClimbApplication.N_MEMBERS_PER_GROUP) {

													final Climbing climb = ClimbApplication.getClimbingForBuilding(building.get_id());
													if (climb != null && (climb.getGame_mode() != 0 /* || climb.getId_mode().equalsIgnoreCase("paused") */)) {
														Toast.makeText(context, ClimbApplication.getContext().getString(R.string.building_occupied), Toast.LENGTH_SHORT).show();
														text.setText(ClimbApplication.getContext().getString(R.string.building_occupied));
													} else {

														try {// Collaboration is not full, I can add myself
															collaborators.put(pref.getString("FBid", ""), pref.getString("username", ""));
															stairs.put(pref.getString("FBid", ""), 0);
														} catch (JSONException e1) {
															// TODO Auto-generated
															// catch block
															e1.printStackTrace();
														}
														collaborationParse.put("collaborators", collaborators);
														collaborationParse.put("stairs", stairs);
														collaborationParse.saveInBackground(new SaveCallback() {

															@Override
															public void done(ParseException e) {
																if (e == null) {
																	int my_stairs = 0;
																	if (climb == null) {

																		final Climbing climbing = new Climbing();
																		climbing.setBuilding(building);
																		climbing.setCompleted(0);
																		climbing.setCompleted_steps(0);
																		climbing.setCreated(new Date().getTime());
																		climbing.setGame_mode(1);
																		climbing.setId_mode(collaborationParse.getObjectId());
																		climbing.setModified(new Date().getTime());
																		climbing.setPercentage(0);
																		climbing.setRemaining_steps(building.getSteps());
																		climbing.setUser(me);
																		climbing.setSaved(true);
																		ClimbApplication.climbingDao.create(climbing);
																		my_stairs = climbing.getCompleted_steps();
																		final ParseObject climbingParse = new ParseObject("Climbing");
																		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
																		df.setTimeZone(new SimpleTimeZone(0, "GMT"));
																		climbingParse.put("building", climbing.getBuilding().get_id());
																		try {
																			climbingParse.put("created", df.parse(df.format(climbing.getCreated())));
																			climbingParse.put("modified", df.parse(df.format(climbing.getModified())));
																			climbingParse.put("completedAt", df.parse(df.format(climbing.getCompleted())));
																		} catch (java.text.ParseException ex) {
																			// TODO
																			// Auto-generated
																			// catch block
																			ex.printStackTrace();
																		}
																		climbingParse.put("completed_steps", climbing.getCompleted_steps());
																		climbingParse.put("remaining_steps", climbing.getRemaining_steps());
																		climbingParse.put("percentage", String.valueOf(climbing.getPercentage()));
																		climbingParse.put("users_id", climbing.getUser().getFBid());
																		climbingParse.put("game_mode", climbing.getGame_mode());
																		climbingParse.put("id_mode", climbing.getId_mode());
																		climbingParse.saveInBackground(new SaveCallback() {

																			@Override
																			public void done(ParseException e) {
																				if (e == null) {
																					climbing.setId_online(climbingParse.getObjectId());
																					climbing.setSaved(true);
																					ClimbApplication.climbingDao.update(climbing);
																				} else {
																					climbing.setSaved(false);
																					ClimbApplication.climbingDao.update(climbing);
																					Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
																					Log.d("Connection problem", "Error: " + e.getMessage());
																				}

																			}
																		});
																	} else {
																		climb.setGame_mode(1);
																		climb.setId_mode(collaborationParse.getObjectId());
																		if (climb.getPercentage() >= 1.00) {
																			climb.setPercentage(0);
																			climb.setCompleted_steps(0);
																			climb.setRemaining_steps(building.getSteps());
																		}
																		ClimbApplication.climbingDao.update(climb);
																		my_stairs = climb.getCompleted_steps();
																		ParseQuery<ParseObject> query = ParseQuery.getQuery("Climbing");
																		query.whereEqualTo("objectId", climb.getId_online());
																		// query.whereEqualTo("building", building.get_id());
																		// query.whereEqualTo("users_id", pref.getString("FBid", ""));
																		query.findInBackground(new FindCallback<ParseObject>() {

																			@Override
																			public void done(List<ParseObject> climbs, ParseException e) {
																				if (e == null) {
																					ParseObject climbParse = climbs.get(0);
																					climbParse.put("game_mode", 1);
																					climbParse.put("id_mode", climb.getId_mode());
																					climbParse.put("percentage", String.valueOf(climb.getPercentage()));
																					climbParse.put("completed_steps", climb.getCompleted_steps());
																					climbParse.put("remaining_steps", climb.getRemaining_steps());
																					// climbParse.saveEventually();
																					ParseUtils.saveClimbing(climbParse, climb);
																					//
																				} else {
																					climb.setSaved(false);
																					ClimbApplication.climbingDao.update(climb);
																					Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
																					Log.d("Connection problem", "Error: " + e.getMessage());
																				}
																			}
																		});
																	}

																	// salvo collaboration in
																	// locale
																	Collaboration collaborationLocal = new Collaboration();
																	collaborationLocal.setId(collaborationParse.getObjectId());
																	collaborationLocal.setBuilding(ClimbApplication.getBuildingById(current.getBuilding_id()));
																	collaborationLocal.setMy_stairs(my_stairs);
																	collaborationLocal.setOthers_stairs(0);
																	collaborationLocal.setSaved(true);
																	collaborationLocal.setLeaved(false);
																	collaborationLocal.setUser(me);
																	collaborationLocal.setCompleted(false);
																	collaborationLocal.setAmICreator(false);
																	ClimbApplication.collaborationDao.create(collaborationLocal);

																	text.setText(ClimbApplication.getContext().getString(R.string.accept_req));

																	deleteRequest(String.valueOf(notification.getId()));

																	cancelBtn.setEnabled(false);
																	acceptBtn.setEnabled(false);
																	notification.setRead(true);
																	ClimbApplication.BUSY = false;
																	progressBar.setVisibility(View.GONE);

																} else {
																	Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
																	Log.d("Connection problem", "Error: " + e.getMessage());
																	ClimbApplication.BUSY = false;
																	progressBar.setVisibility(View.GONE);

																}
															}
														});
													}

												} else {
													text.setText(ClimbApplication.getContext().getString(R.string.collaboration_completed));
													Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.collaboration_completed), Toast.LENGTH_SHORT).show();
													deleteRequest(String.valueOf(notification.getId()));

													cancelBtn.setEnabled(false);
													acceptBtn.setEnabled(false);
													notification.setRead(true);
													ClimbApplication.BUSY = false;
													progressBar.setVisibility(View.GONE);

												}
											} else {
												text.setText(ClimbApplication.getContext().getString(R.string.collaboration_already_part));
												deleteRequest(String.valueOf(notification.getId()));

												cancelBtn.setEnabled(false);
												acceptBtn.setEnabled(false);
												notification.setRead(true);
												ClimbApplication.BUSY = false;
												progressBar.setVisibility(View.GONE);

											}

										}
										// deleteRequest(String.valueOf(notification.getId()));
										//
										// cancelBtn.setEnabled(false);
										// acceptBtn.setEnabled(false);
										// notification.setRead(true);
									} else {
										Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
										Log.d("Connection problem", "Error: " + e.getMessage());
										ClimbApplication.BUSY = false;
										progressBar.setVisibility(View.GONE);

									}
								}
							});

						} else {
							// I'm already part of another collaboration/competition/team duel
							text.setText(ClimbApplication.getContext().getString(R.string.unable_part));
							deleteRequest(String.valueOf(notification.getId()));

							cancelBtn.setEnabled(false);
							acceptBtn.setEnabled(false);
							notification.setRead(true);
							ClimbApplication.BUSY = false;
							progressBar.setVisibility(View.GONE);

						}
					}else{
						// I'm already part of another collaboration/competition/team duel
						text.setText(ClimbApplication.getContext().getString(R.string.no_base_level, building.getBase_level(), building.getName()));
						deleteRequest(String.valueOf(notification.getId()));

						cancelBtn.setEnabled(false);
						acceptBtn.setEnabled(false);
						notification.setRead(true);
						ClimbApplication.BUSY = false;
						progressBar.setVisibility(View.GONE);
					}
						break;
					case ASK_COMPETITION:
						final AskCompetitionNotification current1 = ((AskCompetitionNotification) notification);

						Collaboration collabs1 = ClimbApplication.getCollaborationByBuildingAndUser(current1.getBuilding_id(), pref.getInt("local_id", -1));
						Competition compet1 = ClimbApplication.getCompetitionByBuildingAndUser(current1.getBuilding_id(), pref.getInt("local_id", -1));
						TeamDuel duel1 = ClimbApplication.getTeamDuelByBuildingAndUser(current1.getBuilding_id(), pref.getInt("local_id", -1));
						
						final User me_1 = ClimbApplication.getUserById(pref.getInt("local_id", -1));
						final Building building_1 = ClimbApplication.getBuildingById(current1.getBuilding_id());
						
						if(me_1.getLevel() >= building_1.getBase_level()){
						
						if (collabs1 == null && compet1 == null && duel1 == null) {

							// look for competition object in Parse
							ParseQuery<ParseObject> queryComp = ParseQuery.getQuery("Competition");
							queryComp.whereEqualTo("objectId", current1.getCompetitionId());
							queryComp.findInBackground(new FindCallback<ParseObject>() {

								@Override
								public void done(List<ParseObject> compets, ParseException e) {
									if (e == null) {
										if (compets.size() == 0) {
											Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.competition_not_exists), Toast.LENGTH_SHORT).show();
											text.setText(ClimbApplication.getContext().getString(R.string.competition_not_exists));
											deleteRequest(String.valueOf(notification.getId()));
											cancelBtn.setEnabled(false);
											acceptBtn.setEnabled(false);
											notification.setRead(true);
											ClimbApplication.BUSY = false;
											progressBar.setVisibility(View.GONE);

										} else {
											// se c'e la competizione
											final ParseObject collaborationParse = compets.get(0);
											JSONObject collaborators = collaborationParse.getJSONObject("competitors");
											JSONObject stairs = collaborationParse.getJSONObject("stairs");
											
											

											boolean meIn = collaborators.has(pref.getString("FBid", ""));

											if (!meIn) {

												int n_collaborators = collaborators.length();
												if (n_collaborators < ClimbApplication.N_MEMBERS_PER_GROUP) {

													
													final Climbing climb = ClimbApplication.getClimbingForBuilding(building_1.get_id());

													if (ModelsUtil.hasSomeoneWon(ModelsUtil.fromJsonToChart(stairs), building_1.getSteps())) {
														Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.competition_end), Toast.LENGTH_SHORT).show();
														text.setText(ClimbApplication.getContext().getString(R.string.competition_end));
														deleteRequest(String.valueOf(notification.getId()));
														cancelBtn.setEnabled(false);
														acceptBtn.setEnabled(false);
														notification.setRead(true);
														ClimbApplication.BUSY = false;
														progressBar.setVisibility(View.GONE);
													} else {

														if (climb != null && (climb.getGame_mode() != 0 /* || climb.getId_mode().equalsIgnoreCase("paused") */)) {
															Toast.makeText(context, ClimbApplication.getContext().getString(R.string.building_occupied), Toast.LENGTH_SHORT).show();
															text.setText(ClimbApplication.getContext().getString(R.string.building_occupied));
														} else {

															try {// mi aggiungo alla
																	// competizione
																collaborators.put(pref.getString("FBid", ""), pref.getString("username", ""));
																stairs.put(pref.getString("FBid", ""), 0);
															} catch (JSONException e1) {
																// TODO Auto-generated
																// catch block
																e1.printStackTrace();
															}
															collaborationParse.put("competitors", collaborators);
															collaborationParse.put("stairs", stairs);
															collaborationParse.saveInBackground(new SaveCallback() {

																@Override
																public void done(ParseException e) {
																	if (e == null) {
																		int my_stairs = 0;
																		/*
																		 * if (climb != null) { climb.setId_mode("paused"); ClimbApplication.climbingDao.update(climb); ParseQuery<ParseObject> query = ParseQuery.getQuery("Climbing"); if(climb.getId_online() != null && !climb.getId_online().equals("")){ query.whereEqualTo("objectId", climb.getId_online()); }else{ query.whereEqualTo("building", building.get_id()); query.whereEqualTo("users_id", pref.getString("FBid", ""));
																		 * query.whereEqualTo("game_mode", 0); } query.findInBackground(new FindCallback<ParseObject>() {
																		 * 
																		 * @Override public void done(List<ParseObject> climbs, ParseException e) { if (e == null) { ParseObject c = climbs.get(0); c.put("id_mode", climb.getId_mode()); c.saveEventually(); } else { climb.setSaved(false); ClimbApplication.climbingDao.update(climb); Toast.makeText(ClimbApplication.getContext(), "Connection problem", Toast.LENGTH_SHORT).show(); Log.d("Connection problem", "Error: " + e.getMessage()); } } }); }
																		 */

																		final Climbing climbing = new Climbing();
																		climbing.setBuilding(building_1);
																		climbing.setCompleted(0);
																		climbing.setCompleted_steps(0);
																		climbing.setCreated(new Date().getTime());
																		climbing.setGame_mode(2);
																		climbing.setModified(new Date().getTime());
																		climbing.setPercentage(0);
																		climbing.setRemaining_steps(building_1.getSteps());
																		climbing.setUser(me_1);
																		climbing.setSaved(true);
																		climbing.setId_mode(collaborationParse.getObjectId());

																		ClimbApplication.climbingDao.create(climbing);

																		final ParseObject climbingParse = new ParseObject("Climbing");
																		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
																		df.setTimeZone(new SimpleTimeZone(0, "GMT"));
																		climbingParse.put("building", climbing.getBuilding().get_id());
																		try {
																			climbingParse.put("created", df.parse(df.format(climbing.getCreated())));
																			climbingParse.put("modified", df.parse(df.format(climbing.getModified())));
																			climbingParse.put("completedAt", df.parse(df.format(climbing.getCompleted())));
																		} catch (java.text.ParseException ex) {
																			// TODO Auto-generated
																			// catch block
																			ex.printStackTrace();
																		}
																		climbingParse.put("completed_steps", climbing.getCompleted_steps());
																		climbingParse.put("remaining_steps", climbing.getRemaining_steps());
																		climbingParse.put("percentage", String.valueOf(climbing.getPercentage()));
																		climbingParse.put("users_id", climbing.getUser().getFBid());
																		climbingParse.put("game_mode", climbing.getGame_mode());
																		climbingParse.put("id_mode", climbing.getId_mode());
																		climbingParse.put("checked", climbing.isChecked());
																		climbingParse.saveInBackground(new SaveCallback() {

																			@Override
																			public void done(ParseException e) {
																				if (e == null) {
																					climbing.setId_online(climbingParse.getObjectId());
																					climbing.setSaved(true);
																					ClimbApplication.climbingDao.update(climbing);

																				} else {
																					climbing.setSaved(false);
																					ClimbApplication.climbingDao.update(climbing);
																					Toast.makeText(context, ClimbApplication.getContext().getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
																					Log.e("1 Connection Problem", e.getMessage());
																				}
																			}
																		});

																		// salvo collaboration in
																		// locale
																		Competition competitionLocal = new Competition();
																		competitionLocal.setId_online(collaborationParse.getObjectId());
																		competitionLocal.setBuilding(ClimbApplication.getBuildingById(current1.getBuilding_id()));
																		competitionLocal.setMy_stairs(0);
																		competitionLocal.setCurrent_position(0);
																		competitionLocal.setSaved(true);
																		competitionLocal.setLeaved(false);
																		competitionLocal.setUser(me_1);
																		competitionLocal.setCompleted(false);
																		competitionLocal.setAmICreator(false);
																		competitionLocal.setVictory_time(collaborationParse.getDate("victory_time").getTime());
																		competitionLocal.setChecks(collaborationParse.getInt("checks"));
																		competitionLocal.setWinner_id(collaborationParse.getString("winner_id"));
																		ClimbApplication.competitionDao.create(competitionLocal);

																		text.setText(ClimbApplication.getContext().getString(R.string.accept_req));
																		deleteRequest(String.valueOf(notification.getId()));

																		cancelBtn.setEnabled(false);
																		acceptBtn.setEnabled(false);
																		notification.setRead(true);
																		ClimbApplication.BUSY = false;
																		progressBar.setVisibility(View.GONE);

																	} else {
																		Competition competitionLocal = new Competition();
																		competitionLocal.setBuilding(ClimbApplication.getBuildingById(current1.getBuilding_id()));
																		competitionLocal.setSaved(false);
																		competitionLocal.setLeaved(true);
																		competitionLocal.setUser(me_1);
																		competitionLocal.setCompleted(false);
																		competitionLocal.setVictory_time(0);
																		competitionLocal.setChecks(0);
																		competitionLocal.setWinner_id("0");
																		ClimbApplication.competitionDao.create(competitionLocal);
																		Toast.makeText(context, ClimbApplication.getContext().getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
																		Log.e("2 Connection Problem adding me in competition", "Error: " + e.getMessage());
																		ClimbApplication.BUSY = false;
																		progressBar.setVisibility(View.GONE);

																	}
																}

															});

														}
													}
												} else {
													text.setText(ClimbApplication.getContext().getString(R.string.competition_completed));
													Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.competition_completed), Toast.LENGTH_SHORT).show();

													deleteRequest(String.valueOf(notification.getId()));

													cancelBtn.setEnabled(false);
													acceptBtn.setEnabled(false);
													notification.setRead(true);
													ClimbApplication.BUSY = false;
													progressBar.setVisibility(View.GONE);

												}

											} else {
												text.setText(ClimbApplication.getContext().getString(R.string.competition_already_part));
												deleteRequest(String.valueOf(notification.getId()));

												cancelBtn.setEnabled(false);
												acceptBtn.setEnabled(false);
												notification.setRead(true);
												ClimbApplication.BUSY = false;
												progressBar.setVisibility(View.GONE);

											}

										}
									} else {
										Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
										Log.d("Connection problem", "Error: " + e.getMessage());
										ClimbApplication.BUSY = false;
										progressBar.setVisibility(View.GONE);

									}

								}
							});

						} else {
							text.setText(ClimbApplication.getContext().getString(R.string.unable_part));
							deleteRequest(String.valueOf(notification.getId()));

							cancelBtn.setEnabled(false);
							acceptBtn.setEnabled(false);
							notification.setRead(true);
							ClimbApplication.BUSY = false;
							progressBar.setVisibility(View.GONE);

						}
					} else {
						text.setText(ClimbApplication.getContext().getString(R.string.no_base_level, building_1.getBase_level(), building_1.getName()));
						deleteRequest(String.valueOf(notification.getId()));

						cancelBtn.setEnabled(false);
						acceptBtn.setEnabled(false);
						notification.setRead(true);
						ClimbApplication.BUSY = false;
						progressBar.setVisibility(View.GONE);

					}
						
						
						break;
					case ASK_TEAM_COMPETITION_CHALLENGER:
						final AskTeamDuelNotification current2 = ((AskTeamDuelNotification) notification);

						Collaboration collabs2 = ClimbApplication.getCollaborationByBuildingAndUser(current2.getBuilding_id(), pref.getInt("local_id", -1));
						Competition compet2 = ClimbApplication.getCompetitionByBuildingAndUser(current2.getBuilding_id(), pref.getInt("local_id", -1));
						TeamDuel duel2 = ClimbApplication.getTeamDuelByBuildingAndUser(current2.getBuilding_id(), pref.getInt("local_id", -1));
						
						final User me_2 = ClimbApplication.getUserById(pref.getInt("local_id", -1));
						final Building building_2 = ClimbApplication.getBuildingById(current2.getBuilding_id());
						
						if(me_2.getLevel() >= building_2.getBase_level()){
						
						if (collabs2 == null && compet2 == null && duel2 == null) {

							// look for team duel object in Parse
							ParseQuery<ParseObject> queryComp = ParseQuery.getQuery("TeamDuel");
							queryComp.whereEqualTo("objectId", current2.getTeamDuelId());
							queryComp.findInBackground(new FindCallback<ParseObject>() {

								@Override
								public void done(List<ParseObject> duels, ParseException e) {
									if (e == null) {
										if (duels.size() == 0) {
											Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.team_duel_not_exists), Toast.LENGTH_SHORT).show();
											text.setText(ClimbApplication.getContext().getString(R.string.team_duel_not_exists));
											deleteRequest(String.valueOf(notification.getId()));
											cancelBtn.setEnabled(false);
											acceptBtn.setEnabled(false);
											notification.setRead(true);
											ClimbApplication.BUSY = false;
											progressBar.setVisibility(View.GONE);

										} else {
											// if team duel exists
											final ParseObject teamDuelParse = duels.get(0);
											JSONObject challenger = teamDuelParse.getJSONObject("challenger");
											JSONObject challenger_stairs = teamDuelParse.getJSONObject("challenger_stairs");
											JSONObject challenger_team = teamDuelParse.getJSONObject("challenger_team");
											JSONObject creator_stairs = teamDuelParse.getJSONObject("creator_stairs");

											boolean meIn = challenger_stairs.has(pref.getString("FBid", "")) && creator_stairs.has(pref.getString("FBid", ""));

											if (!meIn) {

												int n_collaborators = challenger.length();
												if (n_collaborators < 1) {

													
													final Climbing climb = ClimbApplication.getClimbingForBuilding(building_2.get_id());

													if (climb != null && (climb.getGame_mode() != 0 /* || climb.getId_mode().equalsIgnoreCase("paused") */)) {
														Toast.makeText(context, ClimbApplication.getContext().getString(R.string.building_occupied), Toast.LENGTH_SHORT).show();
														text.setText(ClimbApplication.getContext().getString(R.string.building_occupied));
													} else {

														try {// add myself as challenger
															challenger.put(pref.getString("FBid", ""), pref.getString("username", ""));
															challenger_stairs.put(pref.getString("FBid", ""), 0);
														} catch (JSONException e1) {
															// TODO Auto-generated
															// catch block
															e1.printStackTrace();
														}
														teamDuelParse.put("challenger", challenger);
														teamDuelParse.put("challenger_stairs", challenger_stairs);
														// teamDuelParse.put("challenger_team", challenger_team);
														teamDuelParse.saveInBackground(new SaveCallback() {

															@Override
															public void done(ParseException e) {
																if (e == null) {
																	int my_stairs = 0;

																	final Climbing climbing = new Climbing();
																	climbing.setBuilding(building_2);
																	climbing.setCompleted(0);
																	climbing.setCompleted_steps(0);
																	climbing.setCreated(new Date().getTime());
																	climbing.setGame_mode(3);
																	climbing.setModified(new Date().getTime());
																	climbing.setPercentage(0);
																	climbing.setRemaining_steps(building_2.getSteps());
																	climbing.setUser(me_2);
																	climbing.setSaved(true);
																	climbing.setId_mode(teamDuelParse.getObjectId());

																	ClimbApplication.climbingDao.create(climbing);

																	final ParseObject climbingParse = new ParseObject("Climbing");
																	DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
																	df.setTimeZone(new SimpleTimeZone(0, "GMT"));
																	climbingParse.put("building", climbing.getBuilding().get_id());
																	try {
																		climbingParse.put("created", df.parse(df.format(climbing.getCreated())));
																		climbingParse.put("modified", df.parse(df.format(climbing.getModified())));
																		climbingParse.put("completedAt", df.parse(df.format(climbing.getCompleted())));
																	} catch (java.text.ParseException ex) {
																		// TODO Auto-generated
																		// catch block
																		ex.printStackTrace();
																	}
																	climbingParse.put("completed_steps", climbing.getCompleted_steps());
																	climbingParse.put("remaining_steps", climbing.getRemaining_steps());
																	climbingParse.put("percentage", String.valueOf(climbing.getPercentage()));
																	climbingParse.put("users_id", climbing.getUser().getFBid());
																	climbingParse.put("game_mode", climbing.getGame_mode());
																	climbingParse.put("id_mode", climbing.getId_mode());
																	climbingParse.put("checked", climbing.isChecked());
																	climbingParse.saveInBackground(new SaveCallback() {

																		@Override
																		public void done(ParseException e) {
																			if (e == null) {
																				climbing.setId_online(climbingParse.getObjectId());
																				climbing.setSaved(true);
																				ClimbApplication.climbingDao.update(climbing);

																			} else {
																				climbing.setSaved(false);
																				ClimbApplication.climbingDao.update(climbing);
																				Toast.makeText(context, ClimbApplication.getContext().getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
																				Log.e("1 Connection Problem", e.getMessage());
																			}
																		}
																	});
																	JSONObject creator = teamDuelParse.getJSONObject("creator");
																	Iterator<String> it = creator.keys();
																	String creator_name = "";
																	try {
																		if (it.hasNext())
																			creator_name = creator.getString(it.next());
																	} catch (JSONException e1) {
																		// TODO Auto-generated catch block
																		e1.printStackTrace();
																	}

																	// save team duel locally
																	TeamDuel teamDuelLocal = new TeamDuel();
																	teamDuelLocal.setId_online(teamDuelParse.getObjectId());
																	teamDuelLocal.setBuilding(ClimbApplication.getBuildingById(current2.getBuilding_id()));
																	teamDuelLocal.setMy_steps(0);
																	teamDuelLocal.setSaved(true);
																	teamDuelLocal.setUser(me_2);
																	teamDuelLocal.setCreator_name(creator_name);
																	teamDuelLocal.setChallenger_name(pref.getString("username", ""));
																	teamDuelLocal.setMygroup(Group.CHALLENGER);
																	teamDuelLocal.setCompleted(false);
																	teamDuelLocal.setCreator(false);
																	teamDuelLocal.setChallenger(true);
																	teamDuelLocal.setDeleted(false);
																	teamDuelLocal.setReadyToPlay(false);
																	teamDuelLocal.setSteps_my_group(0);
																	teamDuelLocal.setSteps_other_group(0);
																	teamDuelLocal.setVictory_time(teamDuelParse.getDate("victory_time").getTime());
																	teamDuelLocal.setChecks(teamDuelParse.getInt("checks"));
																	teamDuelLocal.setWinner_id(teamDuelParse.getString("winner_id"));
																	ClimbApplication.teamDuelDao.create(teamDuelLocal);

																	text.setText(ClimbApplication.getContext().getString(R.string.accept_req));
																	deleteRequest(String.valueOf(notification.getId()));

																	cancelBtn.setEnabled(false);
																	acceptBtn.setEnabled(false);
																	notification.setRead(true);
																	ClimbApplication.BUSY = false;
																	progressBar.setVisibility(View.GONE);

																} else {
																	TeamDuel teamDuelLocal = new TeamDuel();
																	teamDuelLocal.setBuilding(ClimbApplication.getBuildingById(current2.getBuilding_id()));
																	teamDuelLocal.setCompleted(true);
																	teamDuelLocal.setSaved(false);
																	teamDuelLocal.setDeleted(true);
																	teamDuelLocal.setUser(me_2);
																	ClimbApplication.teamDuelDao.create(teamDuelLocal);
																	Toast.makeText(context, ClimbApplication.getContext().getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
																	Log.e("2 Connection Problem adding me in team duel", "Error: " + e.getMessage());
																	ClimbApplication.BUSY = false;
																	progressBar.setVisibility(View.GONE);

																}
															}

														});

													}

												} else {
													text.setText(ClimbApplication.getContext().getString(R.string.team_duel_already_chosen));
													Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.team_duel_already_chosen), Toast.LENGTH_SHORT).show();

													deleteRequest(String.valueOf(notification.getId()));

													cancelBtn.setEnabled(false);
													acceptBtn.setEnabled(false);
													notification.setRead(true);
													ClimbApplication.BUSY = false;
													progressBar.setVisibility(View.GONE);

												}
											} else {
												text.setText(ClimbApplication.getContext().getString(R.string.team_duel_already));
												deleteRequest(String.valueOf(notification.getId()));

												cancelBtn.setEnabled(false);
												acceptBtn.setEnabled(false);
												notification.setRead(true);
												ClimbApplication.BUSY = false;
												progressBar.setVisibility(View.GONE);

											}

										}
									} else {
										Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
										Log.d("Connection problem", "Error: " + e.getMessage());
										ClimbApplication.BUSY = false;
										progressBar.setVisibility(View.GONE);

									}

								}
							});

						} else {
							text.setText(ClimbApplication.getContext().getString(R.string.unable_part));
							deleteRequest(String.valueOf(notification.getId()));

							cancelBtn.setEnabled(false);
							acceptBtn.setEnabled(false);
							notification.setRead(true);
							ClimbApplication.BUSY = false;
							progressBar.setVisibility(View.GONE);

						}
					} else {
						text.setText(ClimbApplication.getContext().getString(R.string.no_base_level, building_2.getBase_level(), building_2.getName()));
						deleteRequest(String.valueOf(notification.getId()));

						cancelBtn.setEnabled(false);
						acceptBtn.setEnabled(false);
						notification.setRead(true);
						ClimbApplication.BUSY = false;
						progressBar.setVisibility(View.GONE);

					}
						break;
					case ASK_TEAM_COMPETITION_TEAM:
						final AskTeamDuelNotification current3 = ((AskTeamDuelNotification) notification);

						Collaboration collabs3 = ClimbApplication.getCollaborationByBuildingAndUser(current3.getBuilding_id(), pref.getInt("local_id", -1));
						Competition compet3 = ClimbApplication.getCompetitionByBuildingAndUser(current3.getBuilding_id(), pref.getInt("local_id", -1));
						TeamDuel duel3 = ClimbApplication.getTeamDuelByBuildingAndUser(current3.getBuilding_id(), pref.getInt("local_id", -1));
						
						final User me_3 = ClimbApplication.getUserById(pref.getInt("local_id", -1));
						final Building building_3 = ClimbApplication.getBuildingById(current3.getBuilding_id());
						
						if(me_3.getLevel() >= building_3.getBase_level()){
						
						if (collabs3 == null && compet3 == null && duel3 == null) {

							// look for team duel object in Parse
							ParseQuery<ParseObject> queryComp = ParseQuery.getQuery("TeamDuel");
							queryComp.whereEqualTo("objectId", current3.getTeamDuelId());
							queryComp.findInBackground(new FindCallback<ParseObject>() {

								@Override
								public void done(List<ParseObject> duels, ParseException e) {
									if (e == null) {
										if (duels.size() == 0) {
											Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.team_duel_not_exists), Toast.LENGTH_SHORT).show();
											text.setText(ClimbApplication.getContext().getString(R.string.team_duel_not_exists));
											deleteRequest(String.valueOf(notification.getId()));
											cancelBtn.setEnabled(false);
											acceptBtn.setEnabled(false);
											notification.setRead(true);
											ClimbApplication.BUSY = false;
											progressBar.setVisibility(View.GONE);

										} else {
											// if team duel exists
											final ParseObject teamDuelParse = duels.get(0);
											JSONObject stairs;
											JSONObject team;
											JSONObject challenger_stairs;
											if (current3.isSenderCreator()) {
												team = teamDuelParse.getJSONObject("creator_team");
												stairs = teamDuelParse.getJSONObject("creator_stairs");
												challenger_stairs = teamDuelParse.getJSONObject("challenger_stairs");
											} else {
												team = teamDuelParse.getJSONObject("challenger_team");
												stairs = teamDuelParse.getJSONObject("challenger_stairs");
												challenger_stairs = teamDuelParse.getJSONObject("creator_stairs");
											}

											boolean meIn = stairs.has(pref.getString("FBid", "")) && challenger_stairs.has(pref.getString("FBid", ""));

											if (!meIn) {

												int n_collaborators = team.length();
												if (n_collaborators < ClimbApplication.N_MEMBERS_PER_GROUP_TEAM - 1) {

													
													final Climbing climb = ClimbApplication.getClimbingForBuildingAndUser(building_3.get_id(), me_3.get_id());// ClimbApplication.getClimbingForBuilding(building.get_id());

													if (climb != null && (climb.getGame_mode() != 0 /* || climb.getId_mode().equalsIgnoreCase("paused") */)) {
														Toast.makeText(context, ClimbApplication.getContext().getString(R.string.building_occupied), Toast.LENGTH_SHORT).show();
														text.setText(ClimbApplication.getContext().getString(R.string.building_occupied));

													} else {

														try {// add myself as challenger
															team.put(pref.getString("FBid", ""), pref.getString("username", ""));
															stairs.put(pref.getString("FBid", ""), 0);
														} catch (JSONException e1) {
															// TODO Auto-generated
															// catch block
															e1.printStackTrace();
														}
														if (current3.isSenderCreator()) {
															teamDuelParse.put("creator_team", team);
															teamDuelParse.put("creator_stairs", stairs);
														} else {
															teamDuelParse.put("challenger_team", team);
															teamDuelParse.put("challenger_stairs", stairs);
														}
														teamDuelParse.saveInBackground(new SaveCallback() {

															@Override
															public void done(ParseException e) {
																if (e == null) {
																	int my_stairs = 0;

																	final Climbing climbing = new Climbing();
																	climbing.setBuilding(building_3);
																	climbing.setCompleted(0);
																	climbing.setCompleted_steps(0);
																	climbing.setCreated(new Date().getTime());
																	climbing.setGame_mode(3);
																	climbing.setModified(new Date().getTime());
																	climbing.setPercentage(0);
																	climbing.setRemaining_steps(building_3.getSteps());
																	climbing.setUser(me_3);
																	climbing.setSaved(true);
																	climbing.setId_mode(teamDuelParse.getObjectId());
																	ClimbApplication.climbingDao.create(climbing);

																	final ParseObject climbingParse = new ParseObject("Climbing");
																	DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
																	df.setTimeZone(new SimpleTimeZone(0, "GMT"));
																	climbingParse.put("building", climbing.getBuilding().get_id());
																	try {
																		climbingParse.put("created", df.parse(df.format(climbing.getCreated())));
																		climbingParse.put("modified", df.parse(df.format(climbing.getModified())));
																		climbingParse.put("completedAt", df.parse(df.format(climbing.getCompleted())));
																	} catch (java.text.ParseException ex) {
																		// TODO Auto-generated
																		// catch block
																		ex.printStackTrace();
																	}
																	climbingParse.put("completed_steps", climbing.getCompleted_steps());
																	climbingParse.put("remaining_steps", climbing.getRemaining_steps());
																	climbingParse.put("percentage", String.valueOf(climbing.getPercentage()));
																	climbingParse.put("users_id", climbing.getUser().getFBid());
																	climbingParse.put("game_mode", climbing.getGame_mode());
																	climbingParse.put("id_mode", climbing.getId_mode());
																	climbingParse.put("checked", climbing.isChecked());

																	climbingParse.saveInBackground(new SaveCallback() {

																		@Override
																		public void done(ParseException e) {
																			if (e == null) {
																				climbing.setId_online(climbingParse.getObjectId());
																				climbing.setSaved(true);
																				ClimbApplication.climbingDao.update(climbing);

																			} else {
																				climbing.setSaved(false);
																				ClimbApplication.climbingDao.update(climbing);
																				Toast.makeText(context, ClimbApplication.getContext().getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
																				Log.e("1 Connection Problem", e.getMessage());
																			}
																		}
																	});
																	// salva creator nome localm
																	JSONObject challenger = teamDuelParse.getJSONObject("challenger");
																	Iterator<String> it = challenger.keys();
																	String challenger_name = "";
																	try {
																		if (it.hasNext())
																			challenger_name = challenger.getString(it.next());
																	} catch (JSONException e1) {
																		// TODO Auto-generated catch block
																		e1.printStackTrace();
																	}
																	JSONObject creator = teamDuelParse.getJSONObject("creator");
																	Iterator<String> it1 = creator.keys();
																	String creator_name = "";
																	try {
																		if (it1.hasNext())
																			creator_name = creator.getString(it1.next());
																	} catch (JSONException e1) {
																		// TODO Auto-generated catch block
																		e1.printStackTrace();
																	}

																	// save team duel locally
																	TeamDuel teamDuelLocal = new TeamDuel();
																	teamDuelLocal.setId_online(teamDuelParse.getObjectId());
																	teamDuelLocal.setBuilding(ClimbApplication.getBuildingById(current3.getBuilding_id()));
																	teamDuelLocal.setMy_steps(0);
																	teamDuelLocal.setSaved(true);
																	teamDuelLocal.setUser(me_3);
																	teamDuelLocal.setReadyToPlay(false);
																	teamDuelLocal.setCreator_name(creator_name);
																	teamDuelLocal.setChallenger_name(challenger_name);
																	teamDuelLocal.setCreator(false);
																	teamDuelLocal.setChallenger(false);
																	if (current3.isSenderCreator())
																		teamDuelLocal.setMygroup(Group.CREATOR);
																	else
																		teamDuelLocal.setMygroup(Group.CHALLENGER);
																	teamDuelLocal.setCompleted(false);
																	teamDuelLocal.setDeleted(false);
																	teamDuelLocal.setSteps_my_group(0);
																	teamDuelLocal.setSteps_other_group(0);
																	teamDuelLocal.setVictory_time(teamDuelParse.getDate("victory_time").getTime());
																	teamDuelLocal.setChecks(teamDuelParse.getInt("checks"));
																	teamDuelLocal.setWinner_id(teamDuelParse.getString("winner_id"));
																	ClimbApplication.teamDuelDao.create(teamDuelLocal);

																	text.setText(ClimbApplication.getContext().getString(R.string.accept_req));
																	deleteRequest(String.valueOf(notification.getId()));

																	cancelBtn.setEnabled(false);
																	acceptBtn.setEnabled(false);
																	notification.setRead(true);
																	ClimbApplication.BUSY = false;
																	progressBar.setVisibility(View.GONE);

																} else {
																	TeamDuel teamDuelLocal = new TeamDuel();
																	teamDuelLocal.setBuilding(ClimbApplication.getBuildingById(current3.getBuilding_id()));
																	teamDuelLocal.setCompleted(true);
																	teamDuelLocal.setSaved(false);
																	teamDuelLocal.setDeleted(true);
																	teamDuelLocal.setUser(me_3);
																	ClimbApplication.teamDuelDao.create(teamDuelLocal);
																	Toast.makeText(context, ClimbApplication.getContext().getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
																	Log.e("2 Connection Problem adding me in team duel", "Error: " + e.getMessage());
																	ClimbApplication.BUSY = false;
																}
															}

														});

													}

												} else {
													text.setText(ClimbApplication.getContext().getString(R.string.team_duel_completed));
													Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.team_duel_completed), Toast.LENGTH_SHORT).show();

													deleteRequest(String.valueOf(notification.getId()));

													cancelBtn.setEnabled(false);
													acceptBtn.setEnabled(false);
													notification.setRead(true);
													ClimbApplication.BUSY = false;
													progressBar.setVisibility(View.GONE);

												}
											} else {
												text.setText(ClimbApplication.getContext().getString(R.string.team_duel_already_part));
												deleteRequest(String.valueOf(notification.getId()));

												cancelBtn.setEnabled(false);
												acceptBtn.setEnabled(false);
												notification.setRead(true);
												ClimbApplication.BUSY = false;
												progressBar.setVisibility(View.GONE);

											}

										}
									} else {
										Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
										Log.d("Connection problem", "Error: " + e.getMessage());
										ClimbApplication.BUSY = false;
										progressBar.setVisibility(View.GONE);

									}

								}
							});

						} else {
							text.setText(ClimbApplication.getContext().getString(R.string.unable_part));
							deleteRequest(String.valueOf(notification.getId()));

							cancelBtn.setEnabled(false);
							acceptBtn.setEnabled(false);
							notification.setRead(true);
							ClimbApplication.BUSY = false;
							progressBar.setVisibility(View.GONE);

						}
					} else {
						text.setText(ClimbApplication.getContext().getString(R.string.no_base_level, building_3.getBase_level(), building_3.getName()));
						deleteRequest(String.valueOf(notification.getId()));

						cancelBtn.setEnabled(false);
						acceptBtn.setEnabled(false);
						notification.setRead(true);
						ClimbApplication.BUSY = false;
						progressBar.setVisibility(View.GONE);

					}
						break;

					}

				}
				if (busy) {
					Toast.makeText(context, ClimbApplication.getContext().getString(R.string.wait_notification), Toast.LENGTH_SHORT).show();
				}
			}
		});

		cancelBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {

				boolean busy = ClimbApplication.BUSY;
				if (FacebookUtils.isOnline(context) && !busy) {
					acceptBtn.setVisibility(View.GONE);
					cancelBtn.setVisibility(View.GONE);
					progressBar.setVisibility(View.VISIBLE);
					ClimbApplication.BUSY = true;

					deleteRequest(String.valueOf(notification.getId()));
					text.setText(ClimbApplication.getContext().getString(R.string.delete_req));
					cancelBtn.setEnabled(false);
					acceptBtn.setEnabled(false);
					notification.setRead(true);
					ClimbApplication.BUSY = false;
					progressBar.setVisibility(View.GONE);

				}
				if (busy) {
					Toast.makeText(context, ClimbApplication.getContext().getString(R.string.wait_notification), Toast.LENGTH_SHORT).show();
				}

			}
		});

		return view;
	}

	private void updateGroup(ParseObject group) {
		JSONObject members = group.getJSONObject("members");
		// final String groupName = group.getString("name");

		// List<String> members = group.getList("members");

		if (members.length() < 5) {

			try {
				members.put(pref.getString("FBid", ""), pref.getString("username", ""));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			group.put("members", members);
			group.saveEventually();

			/*
			 * JSONObject member = new JSONObject(); try { member.put("FBid", pref.getString("FBid", "")); member.put("name", pref.getString("username", "")); } catch (JSONException e) { // TODO Auto-generated catch block e.printStackTrace(); } members.put(member); group.saveEventually();
			 * 
			 * ParseQuery<ParseUser> user = ParseUser.getQuery(); user.whereEqualTo("FBid", pref.getString("FBid", "")); Log.d("FBid da cercare", "cerco " + pref.getString("FBid", "")); user.findInBackground(new FindCallback<ParseUser>() { public void done(List<ParseUser> users, ParseException e) { if (e == null) { System.out.println(users.size()); ParseUser me = users.get(0); me.addAllUnique("Groups", Arrays.asList(groupName)); me.saveEventually(); } else {
			 * 
			 * } } });
			 */

		} else {
			Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.group_completed), Toast.LENGTH_SHORT).show();
		}

	}

	private void deleteRequest(String inRequestId) {
		// Create a new request for an HTTP delete with the
		// request ID as the Graph path.
		Request request = new Request(Session.getActiveSession(), inRequestId, null, HttpMethod.DELETE, new Request.Callback() {

			@Override
			public void onCompleted(Response response) {
				// Show a confirmation of the deletion
				// when the API call completes successfully.
				//Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.delete_req), Toast.LENGTH_SHORT).show();
			}
		});
		// Execute the request asynchronously.
		Request.executeBatchAsync(request);
		ClimbApplication.notifications.remove(notification);
	}

	@Override
	public boolean convert(View convertCardView) {
		// TODO Auto-generated method stub
		return false;
	}
}
