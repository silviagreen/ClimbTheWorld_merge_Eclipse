package org.unipd.nbeghin.climbtheworld.db;

import org.unipd.nbeghin.climbtheworld.models.Alarm;
import org.unipd.nbeghin.climbtheworld.models.Badge;
import org.unipd.nbeghin.climbtheworld.models.Building;
import org.unipd.nbeghin.climbtheworld.models.BuildingText;
import org.unipd.nbeghin.climbtheworld.models.BuildingTour;
import org.unipd.nbeghin.climbtheworld.models.Climbing;
import org.unipd.nbeghin.climbtheworld.models.Collaboration;
import org.unipd.nbeghin.climbtheworld.models.Competition;
import org.unipd.nbeghin.climbtheworld.models.Microgoal;
import org.unipd.nbeghin.climbtheworld.models.MicrogoalText;
import org.unipd.nbeghin.climbtheworld.models.Photo;
import org.unipd.nbeghin.climbtheworld.models.TeamDuel;
import org.unipd.nbeghin.climbtheworld.models.Tour;
import org.unipd.nbeghin.climbtheworld.models.TourText;
import org.unipd.nbeghin.climbtheworld.models.User;
import org.unipd.nbeghin.climbtheworld.models.UserBadge;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;

public class DbHelper extends OrmLiteSqliteOpenHelper {
	private static DbHelper mInstance = null;
	
	public static final String							DATABASE_NAME			= "ClimbTheWorld";
	public static final int								DATABASE_VERSION		= 1;
	private RuntimeExceptionDao<Building, Integer>		buildingRuntimeDao		= null;
	private RuntimeExceptionDao<Tour, Integer>			tourRuntimeDao			= null;
	private RuntimeExceptionDao<Climbing, Integer>		climbingRuntimeDao		= null;
	private RuntimeExceptionDao<BuildingTour, Integer>	buildingTourRuntimeDao	= null;
	private RuntimeExceptionDao<Photo, Integer>			photoRuntimeDao			= null;
	private RuntimeExceptionDao<User, Integer>			userRuntimeDao			= null;
	private RuntimeExceptionDao<Collaboration, Integer> 	collaborationRuntimeDao	= null;
	private RuntimeExceptionDao<Competition, Integer> 	competitionRuntimeDao 	= null;
	private RuntimeExceptionDao<TeamDuel, Integer>		teamDuelRuntimeDao		= null;
	private RuntimeExceptionDao<Badge, Integer> 			badgeRuntimeDao			= null;
	private RuntimeExceptionDao<UserBadge, Integer>		userBadgeRuntimeDao		= null;
	private RuntimeExceptionDao<BuildingText, Integer>   buildingTextDao			= null;
	private RuntimeExceptionDao<TourText, Integer> 		tourTextDao				= null;
	private RuntimeExceptionDao<MicrogoalText, Integer> 	microgoalTextDao			= null;
	private RuntimeExceptionDao<Microgoal, Integer> 		microgoalDao				= null;
	private RuntimeExceptionDao<Alarm, Integer> alarmRuntimeDao = null;

	private DbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	public static DbHelper getInstance(Context ctx) {
        /** 
         * si usa l'application context per far sì di non fuoriuscire
         * dal contesto di un'activity
         * http://android-developers.blogspot.nl/2009/01/avoiding-memory-leaks.html)
         */
        if (mInstance == null) {
        	System.out.println("Istanza dbHelper null");
            mInstance = new DbHelper(ctx.getApplicationContext());
        }
        else{
        	System.out.println("Istanza dbHelper NOT null");
        }
        return mInstance;
    }

	@Override
	public void onCreate(SQLiteDatabase arg0, ConnectionSource arg1) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, ConnectionSource arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
	}

	public RuntimeExceptionDao<Building, Integer> getBuildingDao() {
		if (buildingRuntimeDao == null) {
			buildingRuntimeDao = getRuntimeExceptionDao(Building.class);
		}
		return buildingRuntimeDao;
	}

	public RuntimeExceptionDao<Tour, Integer> getTourDao() {
		if (tourRuntimeDao == null) {
			tourRuntimeDao = getRuntimeExceptionDao(Tour.class);
		}
		return tourRuntimeDao;
	}

	public RuntimeExceptionDao<BuildingTour, Integer> getBuildingTourDao() {
		if (buildingTourRuntimeDao == null) {
			buildingTourRuntimeDao = getRuntimeExceptionDao(BuildingTour.class);
		}
		return buildingTourRuntimeDao;
	}

	public RuntimeExceptionDao<Climbing, Integer> getClimbingDao() {
		if (climbingRuntimeDao == null) {
			climbingRuntimeDao = getRuntimeExceptionDao(Climbing.class);
		}
		return climbingRuntimeDao;
	}

	public RuntimeExceptionDao<Photo, Integer> getPhotoDao() {
		if (photoRuntimeDao == null) {
			photoRuntimeDao = getRuntimeExceptionDao(Photo.class);
		}
		return photoRuntimeDao;
	}
	
	public RuntimeExceptionDao<User, Integer> getUserDao(){
		if(userRuntimeDao == null){
			userRuntimeDao = getRuntimeExceptionDao(User.class);
		}
		return userRuntimeDao;
	}
	
	public RuntimeExceptionDao<Collaboration, Integer> getCollaborationDao(){
		if(collaborationRuntimeDao == null){
			collaborationRuntimeDao = getRuntimeExceptionDao(Collaboration.class);
		}
		return collaborationRuntimeDao;
	}
	
	public RuntimeExceptionDao<Competition, Integer> getCompetitionDao(){
		if(competitionRuntimeDao == null){
			competitionRuntimeDao = getRuntimeExceptionDao(Competition.class);
		}
		return competitionRuntimeDao;
	}
	
	public RuntimeExceptionDao<TeamDuel, Integer> getTeamDuelDao(){
		if(teamDuelRuntimeDao == null){
			teamDuelRuntimeDao = getRuntimeExceptionDao(TeamDuel.class);
		}
		return teamDuelRuntimeDao;
	}
	
	public RuntimeExceptionDao<Badge, Integer> getBadgeDao(){
		if(badgeRuntimeDao == null)
			badgeRuntimeDao = getRuntimeExceptionDao(Badge.class);
		return badgeRuntimeDao;
	}
	
	public RuntimeExceptionDao<UserBadge, Integer> getUserBadgeDao(){
		if(userBadgeRuntimeDao == null)
			userBadgeRuntimeDao = getRuntimeExceptionDao(UserBadge.class);
		return userBadgeRuntimeDao;
	}
	
	public RuntimeExceptionDao<BuildingText, Integer> getBuildingTextDao(){
		if(buildingTextDao == null)
			buildingTextDao = getRuntimeExceptionDao(BuildingText.class);
		return buildingTextDao;
	}
	
	public RuntimeExceptionDao<TourText, Integer> getTourTextDao(){
		if(tourTextDao == null)
			tourTextDao = getRuntimeExceptionDao(TourText.class);
		return tourTextDao;
	}
	
	public RuntimeExceptionDao<MicrogoalText, Integer> getMicrogoalTextDao(){
		if(microgoalTextDao == null)
			microgoalTextDao = getRuntimeExceptionDao(MicrogoalText.class);
		return microgoalTextDao;
	}
	
	public RuntimeExceptionDao<Microgoal, Integer> getMicrogoalDao(){
		if(microgoalDao == null)
			microgoalDao = getRuntimeExceptionDao(Microgoal.class);
		return microgoalDao;
	}

    public String getDbPath() {
        return this.getReadableDatabase().getPath();
    }
    
	public RuntimeExceptionDao<Alarm, Integer> getAlarmDao() {
		if (alarmRuntimeDao == null) {
			System.out.println("alarm dao null");
			alarmRuntimeDao = getRuntimeExceptionDao(Alarm.class);
		}
		else{
			System.out.println("alarm dao NOT null");
		}
		return alarmRuntimeDao;
	}
    
	/**
	 * Close the database connections and clear any cached DAOs.
	 */
	@Override
	public void close() {
		super.close();
		buildingRuntimeDao = null;
		tourRuntimeDao = null;
		climbingRuntimeDao = null;
		buildingTourRuntimeDao = null;
		photoRuntimeDao = null;
		userRuntimeDao = null;
		collaborationRuntimeDao = null;
		competitionRuntimeDao = null;
		teamDuelRuntimeDao = null;
		badgeRuntimeDao = null;
		userBadgeRuntimeDao = null;
		buildingTextDao = null;
		tourTextDao = null;
		microgoalTextDao = null;
		microgoalDao = null;
		alarmRuntimeDao=null;

	}
}
