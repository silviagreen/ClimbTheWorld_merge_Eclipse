package org.unipd.nbeghin.climbtheworld;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.unipd.nbeghin.climbtheworld.models.Building;
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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageInstaller.Session;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.facebook.RequestBatch;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

/**
 * Class called during login in asynchronus threads, to let user wait for his game progress and other data to be downloaded ad saved locally.
 * 
 * @author Silvia
 * 
 */
public class MyAsync {

	final SharedPreferences pref = ClimbApplication.getContext().getSharedPreferences("UserSession", 0);

	// Session session;
	private ParseUser user;
	private Activity activity;
	private ProgressDialog PD;
	private User me;
	private boolean inside;

	MyAsync(/* ParseUser user, */Activity activity, ProgressDialog PD, boolean inside) {
		// session = session;
		// this.user = user;
		this.inside = inside;
		this.activity = activity;
		this.PD = PD;
		me = ClimbApplication.getUserById(activity.getSharedPreferences("UserSession", 0).getInt("local_id", -1));
	}

	protected Void execute() {
		ClimbApplication.BUSY = true;
		// PD.setMessage("load friends");
		
		RequestBatch requestBatch = ClimbApplication.loadFriendsFromFacebook();
		// Execute the batch of requests asynchronously
		if (inside)
			requestBatch.executeAsync();
		else
			requestBatch.executeAndWait();
		ParseUtils.saveNewDayStats();//updateCurrentUserData();
		// NetworkRequestAsyncTask.setMessage("load badge");
		saveBadges();
		// NetworkRequestAsyncTask.setMessage("load microgoal");
		loadMicrogoalFromParse();
		// NetworkRequestAsyncTask.setMessage("load progress");
		loadProgressFromParse();

		// updateFacebookSession(session, session.getState());
		return null;
	}

	/**
	 * Saved user's badges locally
	 */
	private void saveBadges() {
		Log.d("SettingsActivity", "saveBadges");
		ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.whereEqualTo("FBid", pref.getString("FBid", ""));
		query.getFirstInBackground(new GetCallback<ParseUser>() {

			@Override
			public void done(ParseUser user, ParseException e) {
				if (e == null) {
					JSONArray badges = user.getJSONArray("badges");
					if (badges != null && badges.length() > 0) {
						for (int i = 0; i < badges.length(); i++) {
							try {
								JSONObject badge = badges.getJSONObject(i);
								int badge_id = badge.getInt("badge_id");
								int obj_id = badge.getInt("obj_id");
								int user_id = pref.getInt("local_id", -1);
								UserBadge userBadge = ClimbApplication.getUserBadgeForUserAndBadge(badge_id, obj_id, user_id);
								if (userBadge == null) {
									UserBadge ub = new UserBadge();
									ub.setBadge(ClimbApplication.getBadgeById(badge_id));
									ub.setObj_id(obj_id);
									ub.setUser(ClimbApplication.getUserById(user_id));
									ub.setPercentage(badge.getDouble("percentage"));
									ClimbApplication.userBadgeDao.create(ub);
								} else {
									userBadge.setPercentage(badge.getDouble("percentage"));
									ClimbApplication.userBadgeDao.update(userBadge);
								}
							} catch (JSONException ex) {
								// TODO Auto-generated catch block
								ex.printStackTrace();
							}
						}
						ClimbApplication.refreshUserBadge();
					} else {
						badges = new JSONArray();
						user.put("badges", badges);
						// user.saveEventually();
						ParseUtils.saveUserInParse(user);
					}
				} else {
					Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
					Log.e("saveBadges", e.getMessage());
				}

			}
		});

	}

	/**
	 * Saves user's microgoal progress locally
	 */
	private void loadMicrogoalFromParse() {
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Microgoal");
		query.whereEqualTo("user_id", pref.getString("FBid", ""));
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> microgoals, ParseException e) {
				if (e == null) {
					// System.out.println("microgoal :" + microgoals.size());
					for (ParseObject microgoal : microgoals) {
						// System.out.println("cerco microgoal per building " + microgoal.getInt("building"));
						Microgoal current_microgoal = ClimbApplication.getMicrogoalByUserAndBuilding(pref.getInt("local_id", -1), microgoal.getInt("building"));
						if (current_microgoal == null) {
							Building building = ClimbApplication.getBuildingById(microgoal.getInt("building"));
							User user = ClimbApplication.getUserByFBId(microgoal.getString("user_id"));
							current_microgoal = new Microgoal();
							current_microgoal.setDeleted(false);
							current_microgoal.setDone_steps(microgoal.getInt("done_steps"));
							current_microgoal.setSaved(true);
							current_microgoal.setStory_id(microgoal.getInt("story_id"));
							current_microgoal.setTot_steps(microgoal.getInt("tot_steps"));
							current_microgoal.setUser(user);
							current_microgoal.setBuilding(building);
							current_microgoal.setReward(5);
							ClimbApplication.microgoalDao.create(current_microgoal);
						} else {
							current_microgoal.setDone_steps(microgoal.getInt("done_steps"));
							current_microgoal.setTot_steps(microgoal.getInt("tot_steps"));
							current_microgoal.setSaved(true);
							ClimbApplication.microgoalDao.update(current_microgoal);
						}
					}
				} else {
					Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
					Log.e("loadMicrogoalFromParse", e.getMessage());
				}
				ClimbApplication.refreshMicrogoals();
			}

		});
	}

	/**
	 * Saves user's climbing locally
	 */
	private void loadProgressFromParse() {// date non salvate
		Log.d("setting activity", "loadProgressFromParse");
		final SharedPreferences pref = ClimbApplication.getContext().getSharedPreferences("UserSession", 0);
		ClimbApplication.refreshClimbings();
		ClimbApplication.refreshCollaborations();
		ClimbApplication.refreshCompetitions();
		ClimbApplication.refreshTeamDuels();
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Climbing");
		query.whereEqualTo("users_id", pref.getString("FBid", ""));
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> climbings, ParseException e) {
				if (e == null) {
					// save results locally
					for (ParseObject climb : climbings) {
						int idx = climbings.indexOf(climb);
						boolean last = idx == (climbings.size() - 1);
						Climbing localClimb = null;
						boolean pausedExists = false;
						List<Climbing> climbs = ClimbApplication.getClimbingListForBuildingAndUser(climb.getInt("building"), pref.getInt("local_id", -1));
						if (climbs.size() > 0) {
							for (Climbing climbing : climbs) {
								if (climbing.getGame_mode() == climb.getInt("game_mode"))
									localClimb = climbing;
							}

							if (climbs.size() >= 2)
								pausedExists = true;

						}
						if (localClimb == null) {
							// save new climbing locally
							Climbing c = new Climbing();
							c.setBuilding(ClimbApplication.getBuildingById(climb.getInt("building")));
							c.setCompleted(climb.getDate("completedAt").getTime());
							c.setCompleted_steps(climb.getInt("completed_steps"));
							c.setCreated(climb.getDate("created").getTime());
							c.setModified(climb.getDate("modified").getTime());
							c.setPercentage(Float.valueOf(climb.getString("percentage")));
							c.setRemaining_steps(climb.getInt("remaining_steps"));
							c.setUser(ClimbApplication.getUserById(pref.getInt("local_id", -1)));
							c.setSaved(true);
							c.setId_mode(climb.getString("id_mode"));
							c.setGame_mode(climb.getInt("game_mode"));
							c.setId_online(climb.getObjectId());
							ClimbApplication.climbingDao.create(c);

							switch (c.getGame_mode()) {
							case 1:
								loadCollaborationsFromParse(c.getId_mode(), last);
								break;
							case 2:
								loadCompetitionsFromParse(c.getId_mode(), last);
								break;
							case 3:
								loadTeamDuelsFromParse(c.getId_mode(), last, c, climb, pausedExists);
								break;
							default:
								break;
							}
						} else {
							long localTime = localClimb.getModified();
							long parseTime = climb.getDate("modified").getTime();
							if (localTime < parseTime) { // parseTime � piu
															// recente
								localClimb.setCompleted(climb.getDate("completedAt").getTime());
								localClimb.setCompleted_steps(climb.getInt("completed_steps"));
								localClimb.setCreated(climb.getDate("created").getTime());
								localClimb.setModified(climb.getDate("modified").getTime());
								localClimb.setPercentage(Float.valueOf(climb.getString("percentage")));
								localClimb.setRemaining_steps(climb.getInt("remaining_steps"));
								localClimb.setGame_mode(climb.getInt("game_mode"));
								localClimb.setSaved(true);
								localClimb.setId_mode(climb.getString("id_mode"));
								localClimb.setId_online(climb.getObjectId());
								ClimbApplication.climbingDao.update(localClimb);
							}
							switch (localClimb.getGame_mode()) {
							case 1:
								loadCollaborationsFromParse(localClimb.getId_mode(), last);
								break;
							case 2:
								loadCompetitionsFromParse(localClimb.getId_mode(), last);
								break;
							case 3:

								loadTeamDuelsFromParse(localClimb.getId_mode(), last, localClimb, climb, pausedExists);
								break;
							default:
								break;
							}
						}
						if (last) {
							synchronized (ClimbApplication.lock) {
								ClimbApplication.lock.notify();
								ClimbApplication.BUSY = false;
							}
						}
					}

					ClimbApplication.refreshClimbings();
					ClimbApplication.refreshCollaborations();
					ClimbApplication.refreshCompetitions();
					ClimbApplication.refreshTeamDuels();

				} else {
					Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
					Log.e("loadProgressFromParse", e.getMessage());
				}

				if (climbings.size() == 0) {
					synchronized (ClimbApplication.lock) {
						ClimbApplication.lock.notify();
						ClimbApplication.BUSY = false;
					}

				}

			}
		});

	}

	private int sumOthersStep(JSONObject others_step) {
		int sum = 0;
		Iterator keys = others_step.keys();
		while (keys.hasNext()) {
			try {
				sum += ((Integer) others_step.get((String) keys.next()));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return sum;
	}

	/**
	 * Downloads the team duel with the given id and saves it locally
	 * 
	 * @param id
	 *            the id of the team duel object to download
	 * @param last
	 *            true if this is the last operation of the asynctask, false otherwise
	 */
	private void loadTeamDuelsFromParse(final String id, final boolean last, final Climbing c, final ParseObject climb, final boolean pausedExists) {
		final SharedPreferences pref = ClimbApplication.getContext().getSharedPreferences("UserSession", 0);
		ParseQuery<ParseObject> query = ParseQuery.getQuery("TeamDuel");
		query.whereEqualTo("objectId", id);
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> duels, ParseException e) {
				if (e == null) {
					boolean created = false;
					if (duels.size() == 0) {
						if (!pausedExists) {
							TeamDuel local_duel = ClimbApplication.getTeamDuelById(id);
							ClimbApplication.teamDuelDao.delete(local_duel);
							c.setId_mode("");
							c.setGame_mode(0);
							ClimbApplication.climbingDao.update(c);
							climb.put("id_mode", "");
							climb.put("game_mode", 0);
							ParseUtils.saveClimbing(climb, c);
						} else {
							TeamDuel local_duel = ClimbApplication.getTeamDuelById(id);
							ClimbApplication.teamDuelDao.delete(local_duel);
							ParseUtils.deleteClimbing(climb, c);
							ClimbApplication.climbingDao.delete(c);

						}
					} else {
						ParseObject duel = duels.get(0);
						TeamDuel local_duel = ClimbApplication.getTeamDuelById(duel.getObjectId());
						if (local_duel == null) {
							local_duel = new TeamDuel();
							created = true;
						}
						User me = ClimbApplication.getUserById(pref.getInt("local_id", -1));
						local_duel.setId_online(duel.getObjectId());
						local_duel.setBuilding(ClimbApplication.getBuildingById(duel.getInt("building")));
						local_duel.setUser(me);
						local_duel.setDeleted(false);
						local_duel.setCompleted(duel.getBoolean("completed"));
						JSONObject challenger = duel.getJSONObject("challenger");
						JSONObject creator = duel.getJSONObject("creator");
						JSONObject creator_stairs = duel.getJSONObject("creator_stairs");
						JSONObject challenger_stairs = duel.getJSONObject("challenger_stairs");
						try {
							Iterator<String> it;
							if (creator.has(pref.getString("FBid", ""))) {
								local_duel.setCreator(true);
								local_duel.setMygroup(Group.CREATOR);
								local_duel.setSteps_my_group(ModelsUtil.sum(creator_stairs));
								local_duel.setSteps_other_group(ModelsUtil.sum(challenger_stairs));
								if (challenger.length() > 0) {
									it = challenger.keys();
									if (it.hasNext())
										local_duel.setChallenger_name(challenger.getString(it.next()));
									else
										local_duel.setChallenger_name("");
								}
								local_duel.setChallenger(false);
								local_duel.setCreator_name(me.getName());
								local_duel.setMy_steps(creator_stairs.getInt(me.getFBid()));

							} else if (challenger.has(pref.getString("FBid", ""))) {
								local_duel.setCreator(false);
								local_duel.setChallenger_name(me.getName());
								local_duel.setChallenger(true);
								if (creator.length() > 0) {
									it = creator.keys();
									if (it.hasNext())
										local_duel.setCreator_name(creator.getString(it.next()));
									else
										local_duel.setCreator_name("");
								}
								local_duel.setMygroup(Group.CHALLENGER);
								local_duel.setSteps_my_group(ModelsUtil.sum(challenger_stairs));
								local_duel.setSteps_my_group(ModelsUtil.sum(creator_stairs));
								local_duel.setMy_steps(challenger_stairs.getInt(me.getFBid()));
								local_duel.setChallenger_name(me.getName());

							} else {
								if (creator_stairs.has(pref.getString("FBid", ""))) {
									local_duel.setCreator(false);
									local_duel.setMygroup(Group.CREATOR);
									local_duel.setSteps_my_group(ModelsUtil.sum(creator_stairs));
									local_duel.setSteps_other_group(ModelsUtil.sum(challenger_stairs));
									if (challenger.length() > 0) {
										it = challenger.keys();
										if (it.hasNext())
											local_duel.setChallenger_name(challenger.getString(it.next()));
										else
											local_duel.setChallenger_name("");
									}
									local_duel.setChallenger(false);

									if (creator.length() > 0) {
										it = creator.keys();
										if (it.hasNext())
											local_duel.setCreator_name(creator.getString(it.next()));
										else
											local_duel.setCreator_name("");
									}
									local_duel.setMy_steps(creator_stairs.getInt(me.getFBid()));
								} else {
									local_duel.setCreator(false);
									if (challenger.length() > 0) {
										it = challenger.keys();
										if (it.hasNext())
											local_duel.setChallenger_name(challenger.getString(it.next()));
										else
											local_duel.setChallenger_name("");
									}
									local_duel.setChallenger(false);
									if (creator.length() > 0) {
										it = creator.keys();
										if (it.hasNext())
											local_duel.setCreator_name(creator.getString(it.next()));
										else
											local_duel.setCreator_name("");
									}
									local_duel.setMygroup(Group.CHALLENGER);
									local_duel.setSteps_my_group(ModelsUtil.sum(challenger_stairs));
									local_duel.setSteps_my_group(ModelsUtil.sum(creator_stairs));
									local_duel.setMy_steps(challenger_stairs.getInt(me.getFBid()));
								}
							}
						} catch (JSONException ex) {
							ex.printStackTrace();
						}
						if (challenger_stairs.length() == ClimbApplication.N_MEMBERS_PER_GROUP_TEAM && creator_stairs.length() == ClimbApplication.N_MEMBERS_PER_GROUP_TEAM)
							local_duel.setReadyToPlay(true);
						else
							local_duel.setReadyToPlay(false);
						local_duel.setSaved(true);
						if (created)
							ClimbApplication.teamDuelDao.create(local_duel);
						else
							ClimbApplication.teamDuelDao.update(local_duel);
					}
				} else {
					Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
					Log.e("loadTeamDuelsFromParse", e.getMessage());
				}
				if (last) {
					synchronized (ClimbApplication.lock) {
						ClimbApplication.lock.notify();
						ClimbApplication.BUSY = false;
					}
				}
			}

		});

	}

	/**
	 * Downloads the collaboration with the given id and saves it locally
	 * 
	 * @param id
	 *            the id of the collaboration object to download
	 * @param last
	 *            true if this is the last operation of the asynctask, false otherwise
	 */
	private void loadCollaborationsFromParse(String id, final boolean last) {
		final SharedPreferences pref = ClimbApplication.getContext().getSharedPreferences("UserSession", 0);
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Collaboration");
		query.whereEqualTo("objectId", id);
		// System.out.println("loadCollabortion: " + id);
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> collabs, ParseException e) {
				if (e == null) {
					User me = ClimbApplication.getUserById(pref.getInt("local_id", -1));
					ParseObject collaboration = collabs.get(0);
					JSONObject others_steps = collaboration.getJSONObject("stairs");
					boolean completed = collaboration.getBoolean("completed");
					Collaboration local_collab = ClimbApplication.getCollaborationById(collaboration.getObjectId());
					if (local_collab == null) {
						// crea nuova collaborazione
						Collaboration coll = new Collaboration();
						coll.setBuilding(ClimbApplication.getBuildingById(collaboration.getInt("building")));
						coll.setId(collaboration.getObjectId());
						coll.setLeaved(false);
						coll.setMy_stairs(collaboration.getInt("my_stairs"));
						coll.setOthers_stairs(sumOthersStep(others_steps));
						coll.setSaved(true);
						coll.setUser(me);
						JSONObject creator = collaboration.getJSONObject("creator");
						if (creator.has(me.getFBid()))
							coll.setAmICreator(true);
						else
							coll.setAmICreator(false);
						ClimbApplication.collaborationDao.create(coll);

					} else {// update collaborazione esistente
						if (local_collab.getMy_stairs() < collaboration.getInt("my_stairs"))
							local_collab.setMy_stairs(collaboration.getInt("my_stairs"));
						JSONObject others = collaboration.getJSONObject("stairs");
						local_collab.setOthers_stairs(sumOthersStep(others));
						local_collab.setSaved(true);
						local_collab.setCompleted(collaboration.getBoolean("completed"));
						ClimbApplication.collaborationDao.update(local_collab);

					}

				} else {
					Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
					Log.e("loadCollaborationsFromParse", e.getMessage());
				}
				if (last) {
					synchronized (ClimbApplication.lock) {

						ClimbApplication.lock.notify();
						ClimbApplication.BUSY = false;
					}
				}
			}
		});
	}

	/**
	 * Downloads the competition with the given id and saves it locally
	 * 
	 * @param id
	 *            the id of the competition object to download
	 * @param last
	 *            true if this is the last operation of the asynctask, false otherwise
	 */
	private void loadCompetitionsFromParse(String id, final boolean last) {
		final SharedPreferences pref = ClimbApplication.getContext().getSharedPreferences("UserSession", 0);
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Competition");
		query.whereEqualTo("objectId", id);
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> compets, ParseException e) {
				if (e == null) {
					ParseObject competition = compets.get(0);
					JSONObject others_steps = competition.getJSONObject("stairs");
					boolean completed = competition.getBoolean("completed");
					Competition local_compet = ClimbApplication.getCompetitionById(competition.getObjectId());
					User me = ClimbApplication.getUserById(pref.getInt("local_id", -1));
					if (local_compet == null) {
						// crea nuova collaborazione
						Competition comp = new Competition();
						comp.setBuilding(ClimbApplication.getBuildingById(competition.getInt("building")));
						comp.setId_online(competition.getObjectId());
						comp.setLeaved(false);
						comp.setMy_stairs(competition.getInt("my_stairs"));
						// setcurrentposition
						comp.setCurrent_position(ModelsUtil.getMyPosition(pref.getString("FBid", ""), ModelsUtil.fromJsonToSortedMap(competition.getJSONObject("stairs"))));
						comp.setSaved(true);
						comp.setUser(me);
						JSONObject creator = competition.getJSONObject("creator");
						if (creator.has(me.getFBid()))
							comp.setAmICreator(true);
						else
							comp.setAmICreator(false);
						ClimbApplication.competitionDao.create(comp);

					} else {// update collaborazione esistente
						if (local_compet.getMy_stairs() < competition.getInt("my_stairs"))
							local_compet.setMy_stairs(competition.getInt("my_stairs"));
						JSONObject others = competition.getJSONObject("stairs");
						// setcurrentposition
						local_compet.setCurrent_position(ModelsUtil.getMyPosition(pref.getString("FBid", ""), ModelsUtil.fromJsonToSortedMap(competition.getJSONObject("stairs"))));
						local_compet.setSaved(true);
						ClimbApplication.competitionDao.update(local_compet);

					}

				} else {
					Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
					Log.e("loadCompetitionsFromParse", e.getMessage());
				}
				if (last) {
					synchronized (ClimbApplication.lock) {

						ClimbApplication.lock.notify();
						ClimbApplication.BUSY = false;
					}
				}
			}
		});
	}
}
