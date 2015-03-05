package org.unipd.nbeghin.climbtheworld.services;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SimpleTimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.unipd.nbeghin.climbtheworld.ClimbApplication;
import org.unipd.nbeghin.climbtheworld.R;
import org.unipd.nbeghin.climbtheworld.models.Climbing;
import org.unipd.nbeghin.climbtheworld.models.Collaboration;
import org.unipd.nbeghin.climbtheworld.models.Competition;
import org.unipd.nbeghin.climbtheworld.models.Group;
import org.unipd.nbeghin.climbtheworld.models.Microgoal;
import org.unipd.nbeghin.climbtheworld.models.TeamDuel;
import org.unipd.nbeghin.climbtheworld.models.User;
import org.unipd.nbeghin.climbtheworld.models.UserBadge;
import org.unipd.nbeghin.climbtheworld.util.ModelsUtil;
import org.unipd.nbeghin.climbtheworld.util.ParseUtils;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class UpdateService extends IntentService {
	public static final String PARAM_IN_MSG = "imsg";
	public static final String PARAM_OUT_MSG = "omsg";

	public UpdateService() {
		super("UpdateService");
	}



	public boolean isOnline(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}

	private Collaboration getCollaborationByBuildingAndUser(int building_id, int user_id) {
		// per ogni edificio, una sola collaborazione
		QueryBuilder<Collaboration, Integer> query = ClimbApplication.collaborationDao.queryBuilder();
		Where<Collaboration, Integer> where = query.where();

		try {
			where.eq("building_id", building_id);
			where.and();
			where.eq("user_id", user_id);
			PreparedQuery<Collaboration> preparedQuery = query.prepare();
			List<Collaboration> collabs = ClimbApplication.collaborationDao.query(preparedQuery);
			if (collabs.size() == 0)
				return null;
			else
				return collabs.get(0);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	private Competition getCompetitionByBuildingAndUser(int building_id, int user_id) {
		// per ogni edificio, una sola competizione
		QueryBuilder<Competition, Integer> query = ClimbApplication.competitionDao.queryBuilder();
		Where<Competition, Integer> where = query.where();

		try {
			where.eq("building_id", building_id);
			where.and();
			where.eq("user_id", user_id);
			PreparedQuery<Competition> preparedQuery = query.prepare();
			List<Competition> collabs = ClimbApplication.competitionDao.query(preparedQuery);
			if (collabs.size() == 0)
				return null;
			else
				return collabs.get(0);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	private TeamDuel getTeamDuelByBuildingAndUser(int building_id, int user_id) {
		// per ogni edificio, una sola competizione
		QueryBuilder<TeamDuel, Integer> query = ClimbApplication.teamDuelDao.queryBuilder();
		Where<TeamDuel, Integer> where = query.where();

		try {
			where.eq("building_id", building_id);
			where.and();
			where.eq("user_id", user_id);
			PreparedQuery<TeamDuel> preparedQuery = query.prepare();
			List<TeamDuel> collabs = ClimbApplication.teamDuelDao.query(preparedQuery);
			if (collabs.size() == 0)
				return null;
			else
				return collabs.get(0);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	private void saveClimbings(final Context context, int mode) throws SQLException {
		// salvo tutti i climbing online
		QueryBuilder<Climbing, Integer> query1 = ClimbApplication.climbingDao.queryBuilder();
		Where<Climbing, Integer> where = query1.where();
		where.eq("saved", 0);
		where.and();
		where.eq("game_mode", mode);
		PreparedQuery<Climbing> preparedQuery = query1.prepare();
		List<Climbing> climbings = ClimbApplication.climbingDao.query(preparedQuery);
		Log.d("updateService", "Climbings: " + climbings.size());
		for (final Climbing climbing : climbings) {
			ParseQuery<ParseObject> query = ParseQuery.getQuery("Climbing");
			
			if(climbing.getId_online() != null && !climbing.getId_online().equalsIgnoreCase(""))
				query.whereEqualTo("objectId", climbing.getId_online());
			else{
				query.whereEqualTo("building", climbing.getBuilding().get_id());
				query.whereEqualTo("users_id", climbing.getUser().getFBid());
				query.whereEqualTo("game_mode", mode);
			}
			/*
			 * if(climbing.getGame_mode() == 3) query.whereEqualTo("game_mode",
			 * 3);
			 */

			query.findInBackground(new FindCallback<ParseObject>() {

				@Override
				public void done(List<ParseObject> climbs, ParseException e) {
					if (e == null) {
						if (climbs.size() == 0 && !climbing.isDeleted()) {
							final ParseObject climbOnline = new ParseObject("Climbing");
							climbOnline.put("building", climbing.getBuilding().get_id());
							climbOnline.put("users_id", climbing.getUser().getFBid());
							climbOnline.put("completed_steps", climbing.getCompleted_steps());
							climbOnline.put("remaining_steps", climbing.getRemaining_steps());
							DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
							df.setTimeZone(new SimpleTimeZone(0, "GMT"));
							try {
								climbOnline.put("modified", df.parse(df.format(climbing.getModified())));
								climbOnline.put("created", df.parse(df.format(climbing.getCreated())));
								climbOnline.put("completedAt", df.parse(df.format(climbing.getCompleted())));
							} catch (java.text.ParseException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							climbOnline.put("percentage", String.valueOf(climbing.getPercentage()));
							climbOnline.put("game_mode", climbing.getGame_mode());
							if (climbing.getGame_mode() != 0 && (climbing.getId_mode() == null || climbing.getId_mode().equals(""))) {
								switch (climbing.getGame_mode()) {
								case 1:
									Collaboration coll = getCollaborationByBuildingAndUser(climbing.getBuilding().get_id(), climbing.getUser().get_id());
									climbing.setId_mode(coll.getId());
									ClimbApplication.climbingDao.update(climbing);
									break;
								case 2:
									Competition comp = getCompetitionByBuildingAndUser(climbing.getBuilding().get_id(), climbing.getUser().get_id());
									climbing.setId_mode(comp.getId_online());
									ClimbApplication.climbingDao.update(climbing);
									break;
								case 3:
									TeamDuel duel = getTeamDuelByBuildingAndUser(climbing.getBuilding().get_id(), climbing.getUser().get_id());
									climbing.setId_mode(duel.getId_online());
									ClimbApplication.climbingDao.update(climbing);
								}
							}
							if(climbing.getId_mode() != null)//if (climbing.getGame_mode() != 0)
								climbOnline.put("id_mode", climbing.getId_mode());
							climbOnline.saveInBackground(new SaveCallback() {

								@Override
								public void done(ParseException e) {
									if (e == null) {
										climbing.setId_online(climbOnline.getObjectId());
										climbing.setSaved(true);
										ClimbApplication.climbingDao.update(climbing);
									} else {
										climbing.setSaved(false);
										ClimbApplication.climbingDao.update(climbing);
										// Toast.makeText(context,
										// getString(R.string.connection_problem2),
										// Toast.LENGTH_SHORT).show();
									}
								}
							});

						} else if (climbs.size() == 0 && climbing.isDeleted()) {
							ClimbApplication.climbingDao.delete(climbing);
						} else {
							if (!climbing.isDeleted()) {
								DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
								df.setTimeZone(new SimpleTimeZone(0, "GMT"));
								ParseObject climbOnline = climbs.get(0);
								climbOnline.put("completed_steps", climbing.getCompleted_steps());
								climbOnline.put("remaining_steps", climbing.getRemaining_steps());
								try {
									climbOnline.put("modified", df.parse(df.format(climbing.getModified())));
									climbOnline.put("created", df.parse(df.format(climbing.getCreated())));
									climbOnline.put("completedAt", df.parse(df.format(climbing.getCompleted())));
								} catch (java.text.ParseException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								climbOnline.put("percentage", String.valueOf(climbing.getPercentage()));
								climbOnline.put("game_mode", climbing.getGame_mode());
								if (climbing.getGame_mode() != 0 && (climbing.getId_mode() == null || climbing.getId_mode().equals(""))) {
									switch (climbing.getGame_mode()) {
									case 1:
										Collaboration coll = getCollaborationByBuildingAndUser(climbing.getBuilding().get_id(), climbing.getUser().get_id());
										if (coll != null) {
											climbing.setId_mode(coll.getId());
											climbOnline.put("id_mode", climbing.getId_mode());
										} else {
											climbing.setGame_mode(0);
											climbing.setId_mode("");
											climbOnline.put("id_mode", climbing.getId_mode());
											climbOnline.put("game_mode", climbing.getGame_mode());
										}
										ClimbApplication.climbingDao.update(climbing);
										break;
									case 2:
										Competition comp = getCompetitionByBuildingAndUser(climbing.getBuilding().get_id(), climbing.getUser().get_id());
										if (comp != null) {
											climbing.setId_mode(comp.getId_online());
											climbOnline.put("id_mode", climbing.getId_mode());
										} else {
											climbing.setGame_mode(0);
											climbing.setId_mode("");
											climbOnline.put("id_mode", climbing.getId_mode());
											climbOnline.put("game_mode", climbing.getGame_mode());
										}
										ClimbApplication.climbingDao.update(climbing);
										break;
									case 3:
										TeamDuel duel = getTeamDuelByBuildingAndUser(climbing.getBuilding().get_id(), climbing.getUser().get_id());
										if (duel != null) {
											climbing.setId_mode(duel.getId_online());
											climbOnline.put("id_mode", climbing.getId_mode());
										} else {
											climbing.setGame_mode(0);
											climbing.setId_mode("");
											climbOnline.put("id_mode", climbing.getId_mode());
											climbOnline.put("game_mode", climbing.getGame_mode());
										}
										ClimbApplication.climbingDao.update(climbing);
										break;
									}
								} else {
									if(climbing.getId_mode() != null)//if (climbing.getGame_mode() != 0)
										climbOnline.put("id_mode", climbing.getId_mode());
								}
								// climbOnline.saveEventually();
								// climbing.setSaved(true);
								// climbingDao.update(climbing);
								ParseUtils.saveClimbing(climbOnline, climbing);
							} else {
								ParseUtils.deleteClimbing(climbs.get(0), climbing);
								//climbs.get(0).deleteEventually();
								//ClimbApplication.climbingDao.delete(climbing);
							}
						}
					} else {
						//Toast.makeText(context, getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
						Log.e("updateService - climbings", e.getMessage());
					}
				}
			});
		}
	}

	private void saveCollaborations(final Context context) {
		// salvo tutti i climbing online
		Map<String, Object> conditions = new HashMap<String, Object>();
		conditions.put("saved", 0);
		final List<Collaboration> collaborations = ClimbApplication.collaborationDao.queryForFieldValuesArgs(conditions);
		Log.d("updateService", "Collaborations: " + collaborations.size());
		for (final Collaboration collaboration : collaborations) {
			ParseQuery<ParseObject> query = ParseQuery.getQuery("Collaboration");
			if (collaboration.getId() == null || collaboration.getId().equals("")) {
				query.whereEqualTo("collaborators." + collaboration.getUser().getFBid(), collaboration.getUser().getName());
				query.whereEqualTo("building", collaboration.getBuilding().get_id());
				//query.whereEqualTo("completed", false);
			} else
				query.whereEqualTo("objectId", collaboration.getId());
			query.findInBackground(new FindCallback<ParseObject>() {

				@Override
				public void done(List<ParseObject> collabs, ParseException e) {
					if (e == null) {
						if (collabs.size() == 0 && !collaboration.isLeaved()) {
							// collaborationDao.delete(collaboration);
							JSONObject stairs = new JSONObject();
							JSONObject collaborators = new JSONObject();
							final ParseObject collabParse = new ParseObject("Collaboration");

							try {
								collaborators.put(collaboration.getUser().getFBid(), collaboration.getUser().getName());
								stairs.put(collaboration.getUser().getFBid(), 0);
							} catch (JSONException ex) {
								// TODO Auto-generated catch block
								ex.printStackTrace();
							}

							collabParse.put("building", collaboration.getBuilding().get_id());
							collabParse.put("stairs", stairs);
							collabParse.put("collaborators", collaborators);
							collabParse.put("completed", false);
							JSONObject creator = new JSONObject();

							if (collaboration.getAmICreator()) {
								try {
									creator.put(collaboration.getUser().getFBid(), collaboration.getUser().getName());
								} catch (JSONException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							}
							collabParse.put("creator", creator);
							collabParse.saveInBackground(new SaveCallback() {

								@Override
								public void done(ParseException e) {
									if (e == null) {
										collaboration.setId(collabParse.getObjectId());
										collaboration.setSaved(true);
										ClimbApplication.collaborationDao.update(collaboration);

									} else {
										collaboration.setSaved(false);
										ClimbApplication.collaborationDao.update(collaboration);
										Log.e("load collab", e.getMessage());
									}
									if (collaborations.indexOf(collaboration) == (collaborations.size() - 1)) {
										try {
											saveClimbings(context, 2);
										} catch (SQLException ex) {
											// TODO Auto-generated catch block
											ex.printStackTrace();
										}
									}

								}
							});
						} else if (collabs.size() != 0) {
							ParseObject collabParse = collabs.get(0);
							JSONObject stairs = collabParse.getJSONObject("stairs");

							if (collaboration.isLeaved()) {
								JSONObject collaborators = collabParse.getJSONObject("collaborators");
								collaborators.remove(collaboration.getUser().getFBid());
								stairs.remove(collaboration.getUser().getFBid());
								collabParse.put("collaborators", collaborators);
								collabParse.put("stairs", stairs);
								ParseUtils.saveCollaboration(collabParse, collaboration);
								// collabParse.saveEventually();
								// collaboration.setSaved(true);
								// collaborationDao.update(collaboration);
							} else {
								if (stairs.has(collaboration.getUser().getFBid())) {
									try {
										stairs.put(collaboration.getUser().getFBid(), collaboration.getMy_stairs());
										collabParse.put("stairs", stairs);
										// collabParse.saveEventually();
										ParseUtils.saveCollaboration(collabParse, collaboration);
										collaboration.setId(collabParse.getObjectId());
										collaboration.setSaved(true);
										ClimbApplication.collaborationDao.update(collaboration);
									} catch (JSONException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
								} else {
									ClimbApplication.collaborationDao.delete(collaboration);
								}
							}
							if (collaborations.indexOf(collaboration) == (collaborations.size() - 1)) {
								try {
									saveClimbings(context, 1);
								} catch (SQLException ex) {
									// TODO Auto-generated catch block
									ex.printStackTrace();
								}
							}
						} else if (collabs.size() == 0 && collaboration.isLeaved()) {
							ClimbApplication.collaborationDao.delete(collaboration);
						}
					} else {
						//Toast.makeText(context, getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
						Log.e("updateService - collaborations", e.getMessage());
					}
				}

			});
		}
		if (collaborations.size() == 0) {
			try {
				saveClimbings(context, 1);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void saveCompetitions(final Context context) {
		Map<String, Object> conditions = new HashMap<String, Object>();
		conditions.put("saved", 0);
		final List<Competition> competitions = ClimbApplication.competitionDao.queryForFieldValuesArgs(conditions);
		Log.d("updateService", "Competitions: " + competitions.size());
		for (final Competition competition : competitions) {
			ParseQuery<ParseObject> query = ParseQuery.getQuery("Competition");
			if (competition.getId_online() == null || competition.getId_online().equals("")) {
				query.whereEqualTo("competitors." + competition.getUser().getFBid(), competition.getUser().getName());
				query.whereEqualTo("building", competition.getBuilding().get_id());
				//query.whereEqualTo("completed", false);
			} else
				query.whereEqualTo("objectId", competition.getId_online());
			query.findInBackground(new FindCallback<ParseObject>() {

				@Override
				public void done(List<ParseObject> collabs, ParseException e) {
					if (e == null) {
						if (collabs.size() == 0 && !competition.isLeaved()) {
							// competitionDao.delete(competition);
							final ParseObject comp = new ParseObject("Competition");
							JSONObject competitors = new JSONObject();
							JSONObject stairs = new JSONObject();
							try {
								competitors.put(competition.getUser().getFBid(), competition.getUser().getName());
								stairs.put(competition.getUser().getFBid(), 0);
							} catch (JSONException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							comp.put("competitors", competitors);
							comp.put("stairs", stairs);
							comp.put("building", competition.getBuilding().get_id());
							comp.put("completed", competition.isCompleted());
							JSONObject creator = new JSONObject();

							if (competition.getAmICreator()) {
								try {
									creator.put(competition.getUser().getFBid(), competition.getUser().getName());
								} catch (JSONException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							}
							comp.put("creator", creator); System.out.println("FROM SERVICE");
							comp.saveInBackground(new SaveCallback() {

								@Override
								public void done(ParseException e) {
									if (e == null) {
										competition.setId_online(comp.getObjectId());
										competition.setSaved(true);
										ClimbApplication.competitionDao.update(competition);
										if (competitions.indexOf(competition) == (competitions.size() - 1)) {
											try {
												saveClimbings(context, 2);
											} catch (SQLException ex) {
												// TODO Auto-generated catch
												// block
												ex.printStackTrace();
											}
										}

									} else {
										competition.setSaved(false);
										ClimbApplication.competitionDao.update(competition);
										Log.e("load comp", e.getMessage());
									}

								}
							});
						} else if (collabs.size() != 0) {
							ParseObject collabParse = collabs.get(0);
							JSONObject stairs = collabParse.getJSONObject("stairs");

							if (competition.isLeaved()) {
								JSONObject collaborators = collabParse.getJSONObject("competitors");
								collaborators.remove(competition.getUser().getFBid());
								stairs.remove(competition.getUser().getFBid());
								collabParse.put("competitors", collaborators);
								collabParse.put("stairs", stairs); System.out.println("FROM SERVICE");
								ParseUtils.saveCompetition(collabParse, competition);
								// collabParse.saveEventually();
								// competition.setSaved(true);
								ClimbApplication.competitionDao.delete(competition);
							} else {
								try {
									if (stairs.has(competition.getUser().getFBid())) {
										stairs.put(competition.getUser().getFBid(), competition.getMy_stairs());
										collabParse.put("stairs", stairs);
										collabParse.put("completed", competition.isCompleted());
										// collabParse.saveEventually(); 
										System.out.println("FROM SERVICE");
										ParseUtils.saveCompetition(collabParse, competition);
										competition.setId_online(collabParse.getObjectId());
										competition.setSaved(true); 
										ClimbApplication.competitionDao.update(competition);
										if (competitions.indexOf(competition) == (competitions.size() - 1)) {
											try {
												saveClimbings(context, 2);
											} catch (SQLException ex) {
												// TODO Auto-generated catch
												// block
												ex.printStackTrace();
											}
										}
									} else {
										ClimbApplication.competitionDao.delete(competition);
									}
								} catch (JSONException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							}

						} else if (collabs.size() == 0 && competition.isLeaved()) {
							ClimbApplication.competitionDao.delete(competition);
						}
					} else {
						//Toast.makeText(context, getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
						Log.e("UpdateService - competitors", e.getMessage());
					}
				}

			});
		}
		if (competitions.size() == 0) {
			try {
				saveClimbings(context, 2);
			} catch (SQLException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
		}
	}

	private void saveTeamDuels(final Context context) {
		Map<String, Object> conditions = new HashMap<String, Object>();
		conditions.put("saved", 0);
		final List<TeamDuel> duels = ClimbApplication.teamDuelDao.queryForFieldValuesArgs(conditions);
		Log.d("updateService", "Duels: " + duels.size());
		for (final TeamDuel duel : duels) {
			ParseQuery<ParseObject> main_query = ParseQuery.getQuery("TeamDuel");
			if (duel.getId_online() == null || duel.getId_online().equals("")) {
				if (duel.getMygroup() == Group.CHALLENGER) {
					if(duel.isChallenger())
						main_query.whereEqualTo("challenger." + duel.getUser().getFBid(), duel.getUser().getName());
					else
						main_query.whereEqualTo("challenger_team." + duel.getUser().getFBid(), duel.getUser().getName());
				} else {
					if(duel.isCreator())
						main_query.whereEqualTo("creator." + duel.getUser().getFBid(), duel.getUser().getName());
					else
						main_query.whereEqualTo("creator_team." + duel.getUser().getFBid(), duel.getUser().getName());
				}
				main_query.whereEqualTo("building", duel.getBuilding().get_id());
				main_query.whereEqualTo("completed", false);
			} else
				main_query.whereEqualTo("objectId", duel.getId_online());
			main_query.findInBackground(new FindCallback<ParseObject>() {

				@Override
				public void done(List<ParseObject> duelsParse, ParseException e) {
					if (e == null) {
						if (duelsParse.size() == 0 && !duel.isDeleted()) {
							// inserisci
							final ParseObject newDuelParse = new ParseObject("TeamDuel");
							JSONObject creator = new JSONObject();
							JSONObject creator_team = new JSONObject();
							JSONObject creator_stairs = new JSONObject();
							JSONObject challenger = new JSONObject();
							JSONObject challenger_team = new JSONObject();
							JSONObject challenger_stairs = new JSONObject();
							if (duel.isCreator()) {
								try {
									creator.put(duel.getUser().getFBid(), duel.getUser().getName());
									creator_stairs.put(duel.getUser().getFBid(), 0);
								} catch (JSONException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							} else if (duel.isChallenger()) {
								try {
									challenger.put(duel.getUser().getFBid(), duel.getUser().getName());
									challenger_stairs.put(duel.getUser().getFBid(), 0);
								} catch (JSONException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							} else {
								if (duel.getMygroup() == Group.CHALLENGER) {
									try {
										challenger_team.put(duel.getUser().getFBid(), duel.getUser().getName());
										challenger_stairs.put(duel.getUser().getFBid(), 0);
									} catch (JSONException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
								} else {
									try {
										creator_team.put(duel.getUser().getFBid(), duel.getUser().getName());
										creator_stairs.put(duel.getUser().getFBid(), 0);
									} catch (JSONException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
								}
							}
							newDuelParse.put("creator", creator);
							newDuelParse.put("challenger", challenger);
							newDuelParse.put("challenger_team", challenger_team);
							newDuelParse.put("creator_team", creator_team);
							newDuelParse.put("challenger_stairs", challenger_stairs);
							newDuelParse.put("creator_stairs", creator_stairs);
							newDuelParse.put("building", duel.getBuilding().get_id());
							newDuelParse.put("completed", duel.isCompleted());
							newDuelParse.saveInBackground(new SaveCallback() {

								@Override
								public void done(ParseException e) {
									if (e == null) { 
										duel.setId_online(newDuelParse.getObjectId());
										duel.setSaved(true);
										ClimbApplication.teamDuelDao.update(duel);
										if (duels.indexOf(duel) == (duels.size() - 1)) {
											try {
												saveClimbings(context, 3);
											} catch (SQLException ex) {
												// TODO Auto-generated catch
												// block
												ex.printStackTrace();
											}
										}

									} else {
										duel.setSaved(false);
										ClimbApplication.teamDuelDao.update(duel);
										Log.e("load duels", e.getMessage());
									}

								}
							});

						} else if (duelsParse.size() != 0) {
							ParseObject duelParse = duelsParse.get(0);
							if (duel.isDeleted()) {
								// elimina
								ParseUtils.deleteTeamDuel(duelParse, duel);
								//duelParse.deleteEventually();
								//ClimbApplication.teamDuelDao.delete(duel);
							} else {
								// aggiorna
								JSONObject creator = duelParse.getJSONObject("creator");//new JSONObject();
								JSONObject creator_team = duelParse.getJSONObject("creator_team");//new JSONObject();
								JSONObject creator_stairs = duelParse.getJSONObject("creator_stairs");//new JSONObject();
								JSONObject challenger = duelParse.getJSONObject("challenger");//new JSONObject();
								JSONObject challenger_team = duelParse.getJSONObject("challenger_team");//new JSONObject();
								JSONObject challenger_stairs = duelParse.getJSONObject("challenger_stairs");//new JSONObject();
								if (duel.isCreator()) {
									try {
										creator.put(duel.getUser().getFBid(), duel.getUser().getName());
										creator_stairs.put(duel.getUser().getFBid(), duel.getMy_steps());
									} catch (JSONException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
								} else if (duel.isChallenger()) {
									try {
										challenger.put(duel.getUser().getFBid(), duel.getUser().getName());
										challenger_stairs.put(duel.getUser().getFBid(), duel.getMy_steps());
									} catch (JSONException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
								} else {
									if (duel.getMygroup() == Group.CHALLENGER) {
										try {
											challenger_team.put(duel.getUser().getFBid(), duel.getUser().getName());
											challenger_stairs.put(duel.getUser().getFBid(), duel.getMy_steps());
										} catch (JSONException e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
										}
									} else {
										try {
											creator_team.put(duel.getUser().getFBid(), duel.getUser().getName());
											creator_stairs.put(duel.getUser().getFBid(), duel.getMy_steps());
										} catch (JSONException e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
										}
									}
								}
								duelParse.put("creator", creator);
								duelParse.put("challenger", challenger);
								duelParse.put("challenger_team", challenger_team);
								duelParse.put("creator_team", creator_team);
								duelParse.put("challenger_stairs", challenger_stairs);
								duelParse.put("creator_stairs", creator_stairs);
								duelParse.put("building", duel.getBuilding().get_id());
								duelParse.put("completed", duel.isCompleted());
								// duelParse.saveEventually();
								ParseUtils.saveTeamDuel(duelParse, duel);
								duel.setId_online(duelParse.getObjectId());
								duel.setSaved(true);
								ClimbApplication.teamDuelDao.update(duel);
								if (duels.indexOf(duel) == (duels.size() - 1)) {
									try {
										saveClimbings(context, 3);
									} catch (SQLException ex) {
										// TODO Auto-generated catch block
										ex.printStackTrace();
									}
								}

							}

						} else if (duelsParse.size() == 0 && duel.isDeleted()) {
							ClimbApplication.teamDuelDao.delete(duel);
						}
					} else {
					//	Toast.makeText(context, getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
						Log.e("UpdateService - team duels", e.getMessage());
					}

				}

			});
		}
		if (duels.size() == 0) {
			try {
				System.out.println("save climbs 3");
				saveClimbings(context, 3);
			} catch (SQLException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
		}

	}

	private void saveUsersData(final Context ctx) {
		Map<String, Object> conditions = new HashMap<String, Object>();
		conditions.put("saved", 0);
		final List<UserBadge> badges = ClimbApplication.userBadgeDao.queryForFieldValuesArgs(conditions);
		for (final UserBadge badge : badges) {
			ParseQuery<ParseUser> query = ParseUser.getQuery();
			query.whereEqualTo("FBid", badge.getUser().getFBid());
			query.getFirstInBackground(new GetCallback<ParseUser>() {

				@Override
				public void done(ParseUser parseUser, ParseException e) {
					if (e == null) {
						JSONArray badgeParse = parseUser.getJSONArray("badges");
						int currentBadge = ClimbApplication.lookForBadge(badge.getBadge().get_id(), badge.getObj_id(), badgeParse);
						if (currentBadge != -1) {
							badgeParse = ModelsUtil.removeFromJSONArray(badgeParse, currentBadge);
						}
						JSONObject newBadge = new JSONObject();
						try {
							newBadge.put("badge_id", badge.getBadge().get_id());
							newBadge.put("obj_id", badge.getObj_id());
							newBadge.put("percentage", badge.getPercentage());
							badgeParse.put(newBadge);
							parseUser.put("badges", badgeParse);
							parseUser.put("XP", badge.getUser().getXP());
							parseUser.put("level", badge.getUser().getLevel());
							// parseUser.saveEventually();
							ParseUtils.saveUserInParse(parseUser);
							badge.setSaved(true);
							ClimbApplication.userBadgeDao.update(badge);
						} catch (JSONException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}

					} else {
						//Toast.makeText(ctx, getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
						Log.e("UpdateService - save badges", e.getMessage());
					}
				}
			});
		}
	}

	private void saveUsers(final Context context) {
		Map<String, Object> conditions = new HashMap<String, Object>();
		final List<User> users = ClimbApplication.getUsers();//userDao.queryForFieldValuesArgs(conditions);
		for (final User user : users) {
			ParseQuery<ParseUser> query = ParseUser.getQuery();
			query.whereEqualTo("FBid", user.getFBid());
			query.getFirstInBackground(new GetCallback<ParseUser>() {

				@Override
				public void done(ParseUser parseUser, ParseException e) {
					if (e == null) {
						parseUser.put("XP", user.getXP());
						parseUser.put("level", user.getLevel());
						parseUser.put("height", user.getHeight());
						JSONObject stats = parseUser.getJSONObject("mean_daily_steps");
						try {
							stats.put("begin_date", user.getBegin_date());
							stats.put("mean", user.getMean());
							stats.put("current_value", user.getCurrent_steps_value());
							stats.put("n_days", user.getN_measured_days());
						} catch (JSONException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						parseUser.put("mean_daily_steps", stats);
						// parseUser.saveEventually();
						ParseUtils.saveUserInParse(parseUser);
					} else {
				//		Toast.makeText(context, getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
						Log.e("UpdateService - save users", e.getMessage());
					}
				}
			});
		}
	}

	private void saveMicrogoals(final Context context, final int deleted) throws SQLException {
//		Map<String, Object> conditions = new HashMap<String, Object>();
//		conditions.put("saved", 0);
//		conditions.put("deleted", deleted);
		QueryBuilder<Microgoal, Integer> queryLocal = ClimbApplication.microgoalDao.queryBuilder();
		Where<Microgoal, Integer> where = queryLocal.where();
	
			where.eq("saved", 0);
			// and
			where.and();
			where.eq("deleted", deleted);
			PreparedQuery<Microgoal> preparedQuery = queryLocal.prepare();
			final List<Microgoal> microgoals = ClimbApplication.microgoalDao.query(preparedQuery);
		//final List<Microgoal> microgoals = ClimbApplication.microgoalDao.queryForFieldValuesArgs(conditions);
		
		
		for (final Microgoal microgoal : microgoals) {
			System.out.println(deleted + " Microgoals " + microgoals.size());
			ParseQuery<ParseObject> query = ParseQuery.getQuery("Microgoal");
			query.whereEqualTo("user_id", microgoal.getUser().getFBid());
			query.whereEqualTo("building", microgoal.getBuilding().get_id());
			query.getFirstInBackground(new GetCallback<ParseObject>() {

				@Override
				public void done(ParseObject mg, ParseException e) {
					if (e == null) {
						if (microgoal.getDeleted()) {
							if (mg != null)
								//mg.deleteEventually();
								ParseUtils.deleteMicrogoal(mg, microgoal);
							//ClimbApplication.microgoalDao.delete(microgoal);
						} else if (!microgoal.getDeleted()) {
							if (mg == null) {
								System.out.println("mg null");
								mg = new ParseObject("Microgoal");
								mg.put("story_id", microgoal.getStory_id());
								mg.put("building", microgoal.getBuilding().get_id());
								mg.put("done_steps", microgoal.getDone_steps());
								mg.put("tot_steps", microgoal.getTot_steps());
								mg.put("user_id", microgoal.getUser().getFBid());
							} else {
								System.out.println("update");
								mg.put("done_steps", microgoal.getDone_steps());
								mg.put("tot_steps", microgoal.getTot_steps());
							}
							ParseUtils.saveMicrogoal(mg, microgoal);
							// mg.saveEventually();
							// microgoal.setSaved(true);
							// microgoalDao.update(microgoal);
						}
					} else {
						if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
							if (microgoal.getDone_steps() == microgoal.getTot_steps() || microgoal.getDeleted())
								ClimbApplication.microgoalDao.delete(microgoal);
							else {
								System.out.println("mg not found");
								mg = new ParseObject("Microgoal");
								mg.put("story_id", microgoal.getStory_id());
								mg.put("building", microgoal.getBuilding().get_id());
								mg.put("done_steps", microgoal.getDone_steps());
								mg.put("tot_steps", microgoal.getTot_steps());
								mg.put("user_id", microgoal.getUser().getFBid());
								ParseUtils.saveMicrogoal(mg, microgoal);
							}
						} else {
							//Toast.makeText(context, getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
							Log.e("UpdateService - save microgoals", e.getMessage());
						}
					}
					
					if(microgoals.indexOf(microgoal) == microgoals.size() -1 && deleted == 1){
						try {
							saveMicrogoals(context, 0);
						} catch (SQLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}

				}

			});
		}
		if(microgoals.size() == 0 && deleted == 1){
			saveMicrogoals(context, 0);		
		}
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// PreExistingDbLoader preExistingDbLoader = new
		// PreExistingDbLoader(this); // extract db from zip
		// Log.d("Load normal db", "fine service");
		// SQLiteDatabase db = preExistingDbLoader.getReadableDatabase();
		// db.close(); // close connection to extracted db
		// dbHelper = new DbHelper(getApplicationContext());
		// climbingDao = dbHelper.getClimbingDao(); // create climbing DAO
		// collaborationDao = dbHelper.getCollaborationDao();
		// competitionDao = dbHelper.getCompetitionDao();
		// teamDuelDao = dbHelper.getTeamDuelDao();
		// userDao = dbHelper.getUserDao();
		// userBadgesDao = dbHelper.getUserBadgeDao();
		// microgoalDao = dbHelper.getMicrogoalDao();
		if (isOnline(this)) {
			saveUsersData(this);
			saveCollaborations(this);
			saveCompetitions(this);
			saveTeamDuels(this);
			try {
				saveMicrogoals(this, 1);
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			saveUsers(this);
			try {
				saveClimbings(this, 0);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	
}