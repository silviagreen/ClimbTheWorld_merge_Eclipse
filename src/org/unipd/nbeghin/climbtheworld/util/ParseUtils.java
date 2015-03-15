package org.unipd.nbeghin.climbtheworld.util;

import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.unipd.nbeghin.climbtheworld.ClimbApplication;
import org.unipd.nbeghin.climbtheworld.models.Climbing;
import org.unipd.nbeghin.climbtheworld.models.Collaboration;
import org.unipd.nbeghin.climbtheworld.models.Competition;
import org.unipd.nbeghin.climbtheworld.models.Microgoal;
import org.unipd.nbeghin.climbtheworld.models.TeamDuel;
import org.unipd.nbeghin.climbtheworld.models.User;
import org.unipd.nbeghin.climbtheworld.models.UserBadge;

import android.content.SharedPreferences;
import android.util.Log;

import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
/**
 * Class containing methods to be used when saving object in Parse
 * DO NOT USE PARSE METHOD saveEventually/deleteEventually:
 * 
 * ParseCommandCache.runLoop() is the thread that processes any items from previous calls to saveEventually that haven't yet been saved to Parse's servers. 
 * If you haven't been calling saveEventually, then this thread will be asleep, and will only be awakened when you do call it.
 * This thread causes UI to block.
 * @author Silvia
 *
 */
public class ParseUtils {

	public static void saveClimbing(final ParseObject p_climbing, final Climbing l_climbing){
		
			
				p_climbing.saveInBackground(new SaveCallback() {
					
					@Override
					public void done(ParseException ex) {
						if(ex == null){
							//no problems
							l_climbing.setSaved(true);
							ClimbApplication.climbingDao.update(l_climbing);
							Log.i(getClass().getName(), "Climbing correctly saved in Parse");
						}else{
							l_climbing.setSaved(false);
							ClimbApplication.climbingDao.update(l_climbing);
							//Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.connection_problem2), Toast.LENGTH_SHORT).show();
							//ClimbApplication.showConnectionProblemsToast();
							Log.e(getClass().getName(), ex.getMessage());
						}
						
					}
				});
				
			
	
		
	}
	
	public static void saveMicrogoal(ParseObject p_microgoal, final Microgoal l_microgoal){ System.out.println("save " + p_microgoal.getObjectId());
		p_microgoal.saveInBackground(new SaveCallback() {
			
			@Override
			public void done(ParseException ex) {
				if(ex == null){
					//no problems
					Log.i(getClass().getName(), "Microgoal correctly saved in Parse");
					l_microgoal.setSaved(true);
					ClimbApplication.microgoalDao.update(l_microgoal);
				}else{
					
					
					if (ex.getCode() == ParseException.OBJECT_NOT_FOUND) {
						if (l_microgoal.getDone_steps() == l_microgoal.getTot_steps())
							ClimbApplication.microgoalDao.delete(l_microgoal);
						else {
							System.out.println("mg not found utils");
							ParseObject mg = new ParseObject("Microgoal");
							mg.put("story_id", l_microgoal.getStory_id());
							mg.put("building", l_microgoal.getBuilding().get_id());
							mg.put("done_steps", l_microgoal.getDone_steps());
							mg.put("tot_steps", l_microgoal.getTot_steps());
							mg.put("user_id", l_microgoal.getUser().getFBid());
							mg.saveInBackground(new SaveCallback() {
								
								@Override
								public void done(ParseException e) {
									if(e == null){
										Log.i(getClass().getName(), "Microgoal correctly saved in Parse");
										l_microgoal.setSaved(true);
										ClimbApplication.microgoalDao.update(l_microgoal);
									}else{
										l_microgoal.setSaved(false);
										ClimbApplication.microgoalDao.update(l_microgoal);
										Log.e(getClass().getName(), e.getMessage());
									}
									
								}
							});
						}
					} else {
						l_microgoal.setSaved(false);
						ClimbApplication.microgoalDao.update(l_microgoal);
						Log.e(getClass().getName(), ex.getMessage());
					}
					
					
					
				}
				
			}
		});
	}
	
	public static void saveUserInParse(final ParseUser user){
	
		
				System.out.println("save user in parse");
				user.saveInBackground(new SaveCallback() {
					
					@Override
					public void done(ParseException e) {
						if(e == null){
							Log.i(getClass().getName(), "User correctly saved in Parse");
						}else{
							//Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.connection_problem2), Toast.LENGTH_SHORT).show();
							//ClimbApplication.showConnectionProblemsToast();
							Log.e(getClass().getName(), e.getMessage());
						}
					}
				});

	}
	
	public static void saveBadgesInParse(final ParseUser user, final List<UserBadge> userbadges){
		
		
		user.saveInBackground(new SaveCallback() {
			
			@Override
			public void done(ParseException e) {
				if(e == null){
					Log.i(getClass().getName(), "Badge correctly saved in Parse");
					for(UserBadge userbadge : userbadges){
						userbadge.setSaved(true);
						ClimbApplication.userBadgeDao.update(userbadge);
					}
				}else{
					for(UserBadge userbadge : userbadges){
						userbadge.setSaved(true);
						ClimbApplication.userBadgeDao.update(userbadge);
					}
					Log.e(getClass().getName(), e.getMessage());
				}
			}
		});

}
	
	public static void saveCollaboration(ParseObject p_collaboration, final Collaboration l_collaboration){
		p_collaboration.saveInBackground(new SaveCallback() {
			
			@Override
			public void done(ParseException ex) {
				if(ex == null){
					//no problems
					Log.i(getClass().getName(), "Collaboration correctly saved in Parse");
					if(l_collaboration.isLeaved() || l_collaboration.isCompleted())
						ClimbApplication.collaborationDao.delete(l_collaboration);
					else{
						l_collaboration.setSaved(true);
						ClimbApplication.collaborationDao.update(l_collaboration);
					}
				}else{
					l_collaboration.setSaved(false);
					ClimbApplication.collaborationDao.update(l_collaboration);
					//Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.connection_problem2), Toast.LENGTH_SHORT).show();
					//ClimbApplication.showConnectionProblemsToast();
					Log.e(getClass().getName(), ex.getMessage());
				}
				
			}
		});
	}
	
	public static void saveCompetition(ParseObject p_competition, final Competition l_competition){
		p_competition.saveInBackground(new SaveCallback() {
			
			@Override
			public void done(ParseException ex) {
				if(ex == null){
					//no problems
					Log.i(getClass().getName(), "Competition correctly saved in Parse");
					if(l_competition.isLeaved() || l_competition.isCompleted())
						ClimbApplication.competitionDao.delete(l_competition);
					else{
						l_competition.setSaved(true);
						ClimbApplication.competitionDao.update(l_competition);
					}
				}else{
					l_competition.setSaved(false);
					ClimbApplication.competitionDao.update(l_competition);
					//Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.connection_problem2), Toast.LENGTH_SHORT).show();
					//ClimbApplication.showConnectionProblemsToast();
					Log.e(getClass().getName(), ex.getMessage());
				}
				
			}
		});
	}
	
	public static void saveTeamDuel(ParseObject p_teamduel, final TeamDuel l_teamduel){
		p_teamduel.saveInBackground(new SaveCallback() {
			
			@Override
			public void done(ParseException ex) {
				if(ex == null){
					//no problems
					Log.i(getClass().getName(), "Team Duel correctly saved in Parse");
					if(l_teamduel.isCompleted())
						ClimbApplication.teamDuelDao.delete(l_teamduel);
					else{
						l_teamduel.setSaved(true);
						ClimbApplication.teamDuelDao.update(l_teamduel);
					}
					
				}else{
					l_teamduel.setSaved(false);
					ClimbApplication.teamDuelDao.update(l_teamduel);
					Log.e(getClass().getName(), ex.getMessage());
				}
				
			}
		});
	}
	
	public static void deleteClimbing(ParseObject p_climbing, final Climbing l_climbing){
		p_climbing.deleteInBackground(new DeleteCallback() {
			
			@Override
			public void done(ParseException e) {
				if(e == null){
					Log.i(getClass().getName(), "Climbing correctly deleted in Parse");
					ClimbApplication.climbingDao.delete(l_climbing);
				}else{
					l_climbing.setDeleted(true);
					l_climbing.setSaved(false);
					ClimbApplication.climbingDao.update(l_climbing);
					//Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.connection_problem2), Toast.LENGTH_SHORT).show();
					//ClimbApplication.showConnectionProblemsToast();
					Log.e(getClass().getName(), e.getMessage());
				}
				
			}
		});
	}
	
	public static void deleteCollaboration(ParseObject p_collaboration, final Collaboration l_collaboration){
		p_collaboration.deleteInBackground(new DeleteCallback() {
			
			@Override
			public void done(ParseException ex) {
				if(ex == null){
					Log.i(getClass().getName(), "Competition correctly deleted in Parse");
					ClimbApplication.collaborationDao.delete(l_collaboration);
				}else{
					l_collaboration.setLeaved(true);
					l_collaboration.setSaved(false);
					ClimbApplication.collaborationDao.update(l_collaboration);
					//Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.connection_problem2), Toast.LENGTH_SHORT).show();
					//ClimbApplication.showConnectionProblemsToast();
					Log.e(getClass().getName(), ex.getMessage());
				}
				
			}
		});
	}
	
	public static void deleteCompetition(ParseObject p_competition, final Competition l_competition){
		p_competition.deleteInBackground(new DeleteCallback() {
			
			@Override
			public void done(ParseException ex) {
				if(ex == null){
					Log.i(getClass().getName(), "Competition correctly deleted in Parse");
					ClimbApplication.competitionDao.delete(l_competition);
				}else{
					l_competition.setLeaved(true);
					l_competition.setSaved(false);
					ClimbApplication.competitionDao.update(l_competition);
					//Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.connection_problem2), Toast.LENGTH_SHORT).show();
					//ClimbApplication.showConnectionProblemsToast();
					Log.e(getClass().getName(), ex.getMessage());
				}
				
			}
		});
	}
	
	public static void deleteTeamDuel(ParseObject p_team, final TeamDuel l_teamDuel){
		p_team.deleteInBackground(new DeleteCallback() {
			
			@Override
			public void done(ParseException e) {
				if(e == null){
					Log.i(getClass().getName(), "Team duel correctly deleted in Parse");
					ClimbApplication.teamDuelDao.delete(l_teamDuel);
				}else{
					l_teamDuel.setDeleted(true);
					l_teamDuel.setSaved(false);
					ClimbApplication.teamDuelDao.update(l_teamDuel);
					//Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.connection_problem2), Toast.LENGTH_SHORT).show();
					//ClimbApplication.showConnectionProblemsToast();
					Log.e(getClass().getName(), e.getMessage());

				}
				
			}
		});
	}
	
	public static void deleteMicrogoal(ParseObject p_microgoal, final Microgoal microgoal){ System.out.println("delete " + p_microgoal.getObjectId());
		p_microgoal.deleteInBackground(new DeleteCallback() {
			
			@Override
			public void done(ParseException e) {
				if(e == null){
					Log.i(getClass().getName(), "Microgoal correctly deleted in Parse");
					ClimbApplication.microgoalDao.delete(microgoal);
				}else{
					microgoal.setDeleted(true);
					microgoal.setSaved(false);
					ClimbApplication.microgoalDao.update(microgoal);
					Log.e(getClass().getName(), e.getMessage());

				}
				
			}
		});
	}
	public static void saveNewDayStats(){
		ParseUser.getCurrentUser().fetchInBackground(new GetCallback<ParseUser>() {
			  public void done(ParseUser object, ParseException e) {
				    if (e == null) {
    	SharedPreferences pref = ClimbApplication.getContext().getSharedPreferences("UserSession", 0);
		User me = ClimbApplication.getUserByFBId(pref.getString("FBid", "none"));
		ParseUser user = ParseUser.getCurrentUser();
		if (me.getBegin_date() != null && ClimbApplication.are24hPassed(me.getBegin_date())) {
			System.out.println("sono passate 24 ore");
			me.setMean(ClimbApplication.calculateNewMean((long) me.getMean(), me.getN_measured_days(), (me.getCurrent_steps_value())));
			me.setCurrent_steps_value(0);
			me.setN_measured_days(me.getN_measured_days() + 1);
			me.setBegin_date(String.valueOf(new Date().getTime()));
			// ClimbApplication.userDao.update(currentUser);
			
			
			
			if (user != null) {
				JSONObject stats = user.getJSONObject("mean_daily_steps");
				try {
					stats.put("mean", me.getMean());
					stats.put("n_days", me.getN_measured_days());
					stats.put("current_value", me.getCurrent_steps_value());
					stats.put("begin_date", me.getBegin_date());
					user.put("mean_daily_steps", stats);
					user.put("height", me.getHeight());
					// user.saveEventually();
					
					ParseUtils.saveUserInParse(user);
				} catch (JSONException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}
			}
		}else{
			System.out.println("non sono passate 24 ore");
			me.setHeight(user.getDouble("height"));
			JSONObject stats = user.getJSONObject("mean_daily_steps");
			if (stats != null && stats.length() > 0) {
			try {
				me.setBegin_date(String.valueOf(stats.getLong("begin_date")));
				me.setMean(stats.getLong("mean"));
				me.setN_measured_days(stats.getInt("n_days"));
				me.setCurrent_steps_value(stats.getInt("current_value"));
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			}
		}
		me.setLevel(user.getInt("level"));
		me.setXP(user.getInt("XP"));
		ClimbApplication.setDoneTutorial(me);
		ClimbApplication.userDao.update(me);
		
		//updateCurrentUserData();
				    } else {
			    		//Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.connection_problem2), Toast.LENGTH_SHORT).show();
					//ClimbApplication.showConnectionProblemsToast();
			    	Log.e(getClass().getName(), e.getMessage());
			    }
			  }
			});
	}
	
	public static void updateCurrentUserData(){
		ParseUser.getCurrentUser().fetchInBackground(new GetCallback<ParseUser>() {
			  public void done(ParseUser object, ParseException e) {
				    if (e == null) {
				    	SharedPreferences pref = ClimbApplication.getContext().getSharedPreferences("UserSession", 0);
				    	User me = ClimbApplication.getUserByFBId(pref.getString("FBid", "none"));
						ParseUser user = ParseUser.getCurrentUser();
						me.setLevel(user.getInt("level"));
						me.setXP(user.getInt("XP"));
						ClimbApplication.setDoneTutorial(me);

						me.setHeight(user.getDouble("height"));
						JSONObject stats = user.getJSONObject("mean_daily_steps");
						if (stats != null && stats.length() > 0) {
						try {
							me.setBegin_date(String.valueOf(stats.getLong("begin_date")));
							me.setMean(stats.getLong("mean"));
							me.setN_measured_days(stats.getInt("n_days"));
							me.setCurrent_steps_value(stats.getInt("current_value"));
						} catch (JSONException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						}
						ClimbApplication.userDao.update(me);
						
				    } else {
				    		//Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.connection_problem2), Toast.LENGTH_SHORT).show();
						//ClimbApplication.showConnectionProblemsToast();
				    	Log.e(getClass().getName(), e.getMessage());
				    }
				  }
				});
	}
}
