package org.unipd.nbeghin.climbtheworld;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SimpleTimeZone;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.unipd.nbeghin.climbtheworld.db.DbHelper;
import org.unipd.nbeghin.climbtheworld.db.PreExistingDbLoader;
import org.unipd.nbeghin.climbtheworld.models.Badge;
import org.unipd.nbeghin.climbtheworld.models.Building;
import org.unipd.nbeghin.climbtheworld.models.BuildingText;
import org.unipd.nbeghin.climbtheworld.models.BuildingTour;
import org.unipd.nbeghin.climbtheworld.models.Climbing;
import org.unipd.nbeghin.climbtheworld.models.Collaboration;
import org.unipd.nbeghin.climbtheworld.models.Competition;
import org.unipd.nbeghin.climbtheworld.models.GameNotification;
import org.unipd.nbeghin.climbtheworld.models.Microgoal;
import org.unipd.nbeghin.climbtheworld.models.MicrogoalText;
import org.unipd.nbeghin.climbtheworld.models.Notification;
import org.unipd.nbeghin.climbtheworld.models.Photo;
import org.unipd.nbeghin.climbtheworld.models.TeamDuel;
import org.unipd.nbeghin.climbtheworld.models.Tour;
import org.unipd.nbeghin.climbtheworld.models.TourText;
import org.unipd.nbeghin.climbtheworld.models.User;
import org.unipd.nbeghin.climbtheworld.models.UserBadge;
import org.unipd.nbeghin.climbtheworld.util.LogUtils;
import org.unipd.nbeghin.climbtheworld.util.ParseUtils;

import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
//import android.support.multidex.MultiDex;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.RequestBatch;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

/*
 * Base class to maintain global application state
 * 
 * Called when the application is starting, before any activity, service, or receiver objects (excluding content providers) 
 * have been created. Implementations should be as quick as possible (for example using lazy initialization of state) 
 * since the time spent in this function directly impacts the performance of starting the first activity, 
 * service, or receiver in a process. If you override this method, be sure to call super.onCreate().
 */
public class ClimbApplication extends Application {
	
	private static HashMap<Integer, Bitmap> bitmaps = new HashMap<Integer, Bitmap>();
	
	public static final boolean DEBUG = true;
	public static boolean logEnabled = true; 
	
	public static final List<String> PERMISSIONS = Arrays.asList("user_friends,public_profile");

	public static final int N_MEMBERS_PER_GROUP = 6;  
	public static final int N_MEMBERS_PER_GROUP_TEAM = 2; // REAL: 5 friends + me = 6
															// TEST: 2 frends + me = 3
	public static int PERCENTAGE_MICROGOAL = 25;
	public static boolean BUSY = false;
	public static Object lock = new Object();

	private static boolean activityVisible;

	public static boolean seen = false;

	// current application language
	public static String language;
	
	public static int bonus_notification = 0;
	public static Multimap<String, String> notifications_inbox_contents = ArrayListMultimap.create();

	public static List<Building> buildings;
	public static List<Climbing> climbings; // list of loaded climbings
	public static List<Tour> tours; // list of loaded tours
	public static List<Notification> notifications;
	public static List<Collaboration> collaborations;
	public static List<Competition> competitions;
	public static List<TeamDuel> teamDuels;
	public static List<Badge> badges;
	public static List<UserBadge> userBadges;
	public static List<BuildingText> buildingTexts;
	public static List<TourText> tourTexts;
	public static List<MicrogoalText> microgoalTexts;
	public static List<Microgoal> microgoals;

	public static List<JSONObject> invitableFriends = new ArrayList<JSONObject>();

	private ActionBar ab; // reference to action bar

	// DAOs
	public static RuntimeExceptionDao<Building, Integer> buildingDao; // DAO for buildings
	public static RuntimeExceptionDao<Collaboration, Integer> collaborationDao;
	public static RuntimeExceptionDao<Competition, Integer> competitionDao;
	public static RuntimeExceptionDao<TeamDuel, Integer> teamDuelDao;
	public static RuntimeExceptionDao<Climbing, Integer> climbingDao; // DAO for climbings
	public static RuntimeExceptionDao<Tour, Integer> tourDao; // DAO for tours
	public static RuntimeExceptionDao<BuildingTour, Integer> buildingTourDao; // DAO for building_tours
	public static RuntimeExceptionDao<Photo, Integer> photoDao;
	public static RuntimeExceptionDao<User, Integer> userDao;
	public static RuntimeExceptionDao<Badge, Integer> badgeDao;
	public static RuntimeExceptionDao<UserBadge, Integer> userBadgeDao;
	public static RuntimeExceptionDao<BuildingText, Integer> buildingTextDao;
	public static RuntimeExceptionDao<TourText, Integer> tourTextDao;
	public static RuntimeExceptionDao<MicrogoalText, Integer> microgoalTextDao;
	public static RuntimeExceptionDao<Microgoal, Integer> microgoalDao;

	public static User currentUser;

	public static Toast connection_toast;

	public static final String settings_file = "ClimbTheWorldPreferences";
	public static final String settings_detected_sampling_rate = "samplingRate";

	public static final String counter_mode = "couter_mode"; // intent
	public static final String building_text_intent_object = "org.unipd.nbeghin.climbtheworld.intents.object.buildingText"; // intent key for sending building id
	public static final String duel_intent_object = "org.unipd.nbeghin.climbtheworld.intents.object.teamDuel";

	static DbHelper dbHelper;
	private static Context sContext;
	public static GraphUser user;

	private static ClimbApplication singleton;

	public ClimbApplication getInstance() {
		return singleton;
	}
	
	public static void addBitmap(int id, Bitmap bitmap){
		bitmaps.put(id, bitmap);
	}
	
	public static Bitmap getBitmap(int id){
		return bitmaps.get(id);
	}

	public static Context getContext() {
		return sContext;
	}

	public static void setCurrentUser(User user) {
		currentUser = user;
	}
	
//	@Override
//	protected void attachBaseContext(Context base) {
//		  super.attachBaseContext(base);
//		  MultiDex.install(this);
//		}
	
	public static void setDoneTutorial(User user){
		SharedPreferences pref = getContext().getSharedPreferences("UserSession", 0);
		if(user.getLevel() >= 1){
			pref.edit().putBoolean("done_tutorial", true).commit();
		}else
			pref.edit().putBoolean("done_tutorial", false).commit();
	}

	public static boolean isActivityVisible() {
		return activityVisible;
	}

	public static void activityResumed() {
		activityVisible = true;
	}

	public static void activityPaused() {
		activityVisible = false;
	}

	public static List<JSONObject> getInvitableFriends() {
		return invitableFriends;
	}

	public static void setInvitableFriend(List<JSONObject> obj) {
		invitableFriends = obj;
		Log.d("invitabili", String.valueOf(obj.size()));
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("ClimbApplication", "onCreate");
		
		
		sContext = getApplicationContext();
		if(notifications == null)
			ClimbApplication.notifications = new ArrayList<Notification>();
		//LOG-------------------------------------------------
				//LINE
				String line = "OPEN APP at " + new Date().toString();
				LogUtils.initGameLog(sContext, "BEGINS NOW");
				LogUtils.writeGameUpdate(sContext, line);
				//
				//---------------------------------------------------
		
		emptyMultimapNotification();
		
		SharedPreferences pref = sContext.getSharedPreferences("UserSession", 0);
		Editor edit = pref.edit();
		edit.putBoolean("openedFirst", true);
		edit.commit();
		
		singleton = this;
		language = getString(R.string.language);
		
		// Parse initialize
		Parse.initialize(this, "e9wlYQPdpXlFX3XQc9Lq0GJFecuYrDSzwVNSovvd", "QVII1Qhy8pXrjAZiL07qaTKbaWpkB87zc88UMWv2");
		ParseFacebookUtils.initialize(getString(R.string.app_id));
		
		loadDb();

	}
	
	public static void emptyMultimapNotification(){
		notifications_inbox_contents.clear();
		notifications_inbox_contents.put("BONUS", "0");
		notifications_inbox_contents.put("LEVEL", "0");
	}
	
	public static void updateLevelNotification(int level){ System.out.println("notifico livello " + level);
		notifications_inbox_contents.asMap().remove("LEVEL");
		notifications_inbox_contents.put("LEVEL", String.valueOf(level));
		System.out.println(notifications_inbox_contents.toString());
	}
	
	public static void updateBonusNotification(int bonus){ System.out.println("notifico bonus " + bonus);
		int new_val = Integer.valueOf(Iterables.get(notifications_inbox_contents.get("BONUS"), 0)) + bonus;
		notifications_inbox_contents.asMap().remove("BONUS");
		notifications_inbox_contents.put("BONUS", String.valueOf(new_val));
		System.out.println(notifications_inbox_contents.toString());

	}
	
	public static void addBadgeNotification(String message){
		notifications_inbox_contents.put("BADGE", message);
		//System.out.println(notifications_inbox_contents.toString());

	}
	
	public static int getGameNotification(){
		for(int i = 0; i < notifications.size(); i++){
			if(notifications.get(i) instanceof GameNotification)
				return i;
		}
		return -1;
	}

	/**
	 * Return the XP points given the current level
	 * 
	 * @param l
	 *            The current level
	 * @return the necessary number of XP points to access level l
	 */
	public static int levelToXP(int l) {
		return (25 * l * l - 20 * l);
	}

	/**
	 * Return the current level given the number of XP points
	 * 
	 * @param points
	 *            number of XP points
	 * @return the level where the user is with points XP
	 */
	public static int XPtoLevel(int points) {
		//return ((int) Math.floor(20 + Math.sqrt(400 + 100 * points)) / 50);
		return ((int) Math.ceil(2 + Math.sqrt(points - 4)) / 5);
	}

	/**
	 * Returns the new level where the user will be with the current amount of XP points.
	 * 
	 * @param xp
	 *            the current amount of XP points of the user
	 * @param precLevel
	 *            the level where the user is
	 * @return the level where the user will be with xp points
	 */
	public static int levelUp(int xp, int precLevel) {
		int newLevel;
		if (precLevel < 20) {
			newLevel = XPtoLevel(xp);
		} else {
			if (xp >= 9500)
				newLevel = precLevel + 1;
			else
				newLevel = precLevel;
		}
		return newLevel;
	}

	/**
	 * Return the number of XP point the user can earn after doing a given number of steps. The number of XP points changes according to the chosen difficulty.
	 * 
	 * @param steps
	 *            the amount of steps the user has done
	 * @return the amount of XP point the user has earned after doing steps steps.
	 */
	public static int XPforStep(int steps, boolean tutorial) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
		int difficulty = Integer.parseInt(settings.getString("difficulty", "10"));
		if(tutorial) difficulty = 1;
		double r_steps = (double) steps / (double) difficulty;
		switch (difficulty) {
		case 100:// easy
			return (int) Math.floor(r_steps);
		case 1:// hard
			return ((int) Math.floor(r_steps)) * 3;
		default: // normal
			return ((int) Math.floor(r_steps)) * 2;
		}
	}

	/**
	 * Load db and setup DAOs NB: extracts DB from assets/databases/ClimbTheWorld.zip
	 */
	private void loadDb() {
		Log.d("Load normal db", "inizio");
		PreExistingDbLoader preExistingDbLoader = new PreExistingDbLoader(getApplicationContext()); // extract
																									// db // from
																									// zip
		SQLiteDatabase db = preExistingDbLoader.getReadableDatabase();
		db.close(); // close connection to extracted db
		dbHelper = DbHelper.getInstance(getApplicationContext()); // instance new db
															// connection to
															// now-standard db
		buildingDao = dbHelper.getBuildingDao(); // create building DAO
		userDao = dbHelper.getUserDao();

		climbingDao = dbHelper.getClimbingDao(); // create climbing DAO
		tourDao = dbHelper.getTourDao(); // create tour DAO
		buildingTourDao = dbHelper.getBuildingTourDao(); // create building tour
															// DAO
		photoDao = dbHelper.getPhotoDao();
		collaborationDao = dbHelper.getCollaborationDao();
		competitionDao = dbHelper.getCompetitionDao();
		teamDuelDao = dbHelper.getTeamDuelDao();
		badgeDao = dbHelper.getBadgeDao();
		userBadgeDao = dbHelper.getUserBadgeDao();
		buildingTextDao = dbHelper.getBuildingTextDao();
		tourTextDao = dbHelper.getTourTextDao();
		microgoalTextDao = dbHelper.getMicrogoalTextDao();
		microgoalDao = dbHelper.getMicrogoalDao();
		refresh(); // loads all buildings and tours
	}

	/**
	 * Reload all buildings
	 */
	public static void refreshBuildings() {
		buildings = buildingDao.queryForAll();
	}

	public static void refreshBadges() {
		badges = badgeDao.queryForAll();
	}

	public static void refreshBuildingTexts() {
		QueryBuilder<BuildingText, Integer> query = buildingTextDao.queryBuilder();
		Where<BuildingText, Integer> where = query.where();
		try {
			where.eq("language", language);
			PreparedQuery<BuildingText> preparedQuery = query.prepare();
			buildingTexts = buildingTextDao.query(preparedQuery);
		} catch (SQLException e) {
			e.printStackTrace();

		}
	}

	public static void refreshMicrogoalTexts() {
		QueryBuilder<MicrogoalText, Integer> query = microgoalTextDao.queryBuilder();
		Where<MicrogoalText, Integer> where = query.where();
		try {
			where.eq("language", language);
			PreparedQuery<MicrogoalText> preparedQuery = query.prepare();
			microgoalTexts = microgoalTextDao.query(preparedQuery);
		} catch (SQLException e) {
			e.printStackTrace();

		}
	}

	/**
	 * Reload all text according to the current phone language
	 */
	public static void refreshMicrogoals() {
		SharedPreferences pref = sContext.getSharedPreferences("UserSession", 0);
		QueryBuilder<Microgoal, Integer> query = microgoalDao.queryBuilder();
		Where<Microgoal, Integer> where = query.where();
		try {
			where.eq("user_id", pref.getInt("local_id", -1));
			PreparedQuery<Microgoal> preparedQuery = query.prepare();
			microgoals = microgoalDao.query(preparedQuery);
		} catch (SQLException e) {
			e.printStackTrace();

		}
	}

	public static void refreshTourTexts() {
		QueryBuilder<TourText, Integer> query = tourTextDao.queryBuilder();
		Where<TourText, Integer> where = query.where();
		try {
			where.eq("language", language);
			PreparedQuery<TourText> preparedQuery = query.prepare();
			tourTexts = tourTextDao.query(preparedQuery);
		} catch (SQLException e) {
			e.printStackTrace();

		}
	}

	public static void refreshUserBadge() {
		SharedPreferences pref = sContext.getSharedPreferences("UserSession", 0);
		QueryBuilder<UserBadge, Integer> query = userBadgeDao.queryBuilder();
		Where<UserBadge, Integer> where = query.where();
		try {
			where.eq("user_id", pref.getInt("local_id", -1));
			PreparedQuery<UserBadge> preparedQuery = query.prepare();
			userBadges = userBadgeDao.query(preparedQuery);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
	}

	public static void refreshCollaborations() {
		SharedPreferences pref = sContext.getSharedPreferences("UserSession", 0);
		QueryBuilder<Collaboration, Integer> query = collaborationDao.queryBuilder();
		Where<Collaboration, Integer> where = query.where();
		// the name field must be equal to "foo"
		try {

			where.eq("user_id", pref.getInt("local_id", -1));
			PreparedQuery<Collaboration> preparedQuery = query.prepare();
			List<Collaboration> colls = collaborationDao.query(preparedQuery);
			collaborations = colls;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
	}

	public static void refreshCompetitions() {
		SharedPreferences pref = sContext.getSharedPreferences("UserSession", 0);
		QueryBuilder<Competition, Integer> query = competitionDao.queryBuilder();
		Where<Competition, Integer> where = query.where();
		// the name field must be equal to "foo"
		try {

			where.eq("user_id", pref.getInt("local_id", -1));
			PreparedQuery<Competition> preparedQuery = query.prepare();
			List<Competition> colls = competitionDao.query(preparedQuery);
			competitions = colls;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
	}

	public static void refreshClimbings() {
		SharedPreferences pref = sContext.getSharedPreferences("UserSession", 0);
		QueryBuilder<Climbing, Integer> query = climbingDao.queryBuilder();
		Where<Climbing, Integer> where = query.where();
		// the name field must be equal to "foo"
		try {

			where.eq("users_id", pref.getInt("local_id", -1));
			PreparedQuery<Climbing> preparedQuery = query.prepare();
			List<Climbing> climbs = climbingDao.query(preparedQuery);
			climbings = climbs;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

	}

	public static void refreshTeamDuels() {
		SharedPreferences pref = sContext.getSharedPreferences("UserSession", 0);

		QueryBuilder<TeamDuel, Integer> query = teamDuelDao.queryBuilder();
		Where<TeamDuel, Integer> where = query.where();
		// the name field must be equal to "foo"
		try {

			where.eq("user_id", pref.getInt("local_id", -1));
			PreparedQuery<TeamDuel> preparedQuery = query.prepare();
			List<TeamDuel> duels = teamDuelDao.query(preparedQuery);
			teamDuels = duels;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
	}

	/**
	 * Queries
	 */

	public static List<User> getUsers(){
		QueryBuilder<User, Integer> query = userDao.queryBuilder();
		try {
			PreparedQuery<User> preparedQuery = query.prepare();
			return userDao.query(preparedQuery);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}
	
	public static Collaboration getCollaborationByBuildingAndUser(int building_id, int user_id) {
		SharedPreferences pref = sContext.getSharedPreferences("UserSession", 0);
		QueryBuilder<Collaboration, Integer> query = collaborationDao.queryBuilder();
		Where<Collaboration, Integer> where = query.where();

		try {
			where.eq("building_id", building_id);
			where.and();
			where.eq("user_id", user_id);
			PreparedQuery<Collaboration> preparedQuery = query.prepare();
			List<Collaboration> collabs = collaborationDao.query(preparedQuery);
			if (collabs.size() == 0)
				return null;
			else
				return collabs.get(0);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

	public static Competition getCompetitionByBuildingAndUser(int building_id, int user_id) {
		SharedPreferences pref = sContext.getSharedPreferences("UserSession", 0);
		QueryBuilder<Competition, Integer> query = competitionDao.queryBuilder();
		Where<Competition, Integer> where = query.where();

		try {
			where.eq("building_id", building_id);
			where.and();
			where.eq("completed", 0);
			where.and();
			where.eq("user_id", user_id);
			PreparedQuery<Competition> preparedQuery = query.prepare();
			List<Competition> collabs = competitionDao.query(preparedQuery);
			if (collabs.size() == 0)
				return null;
			else
				return collabs.get(0);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

	public static TeamDuel getTeamDuelByBuildingAndUser(int building_id, int user_id) {
		SharedPreferences pref = sContext.getSharedPreferences("UserSession", 0);
		QueryBuilder<TeamDuel, Integer> query = teamDuelDao.queryBuilder();
		Where<TeamDuel, Integer> where = query.where();

		try {
			where.eq("building_id", building_id);
			where.and();
			where.eq("user_id", user_id);
			PreparedQuery<TeamDuel> preparedQuery = query.prepare();
			List<TeamDuel> duels = teamDuelDao.query(preparedQuery);
			if (duels.size() == 0)
				return null;
			else
				return duels.get(0);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Reload all tours
	 */
	public static void refreshTours() {
		tours = tourDao.queryForAll();
	}

	/**
	 * Reload buildings and tours
	 */
	public static void refresh() {
		refreshBuildings();
		refreshTours();
		refreshBadges();
		refreshBuildingTexts();
		refreshTourTexts();
		refreshMicrogoalTexts();
	}

	public void onBtnShowGallery(View v) {
		Intent intent = new Intent(sContext, GalleryActivity.class);
		intent.putExtra("building_id", 1);
		startActivity(intent);
	}

	public static List<BuildingTour> getBuildingsForTour(int tour_id) {
		Map<String, Object> conditions = new HashMap<String, Object>();
		conditions.put("tour_id", tour_id);
		return buildingTourDao.queryForFieldValuesArgs(conditions); // get all
																	// buildings
																	// associated
																	// to a tour
	}

	public static int getBuildingImageResource(Building building) {
		return sContext.getResources().getIdentifier(building.getPhoto(), "drawable", sContext.getPackageName());
	}

	public static List<Integer> getBuildingPhotosForTour(int tour_id) {
		List<Integer> images = new ArrayList<Integer>();
		List<BuildingTour> buildingsTour = getBuildingsForTour(tour_id);
		for (BuildingTour buildingTour : buildingsTour) {
			images.add(getBuildingImageResource(buildingTour.getBuilding()));
		}
		return images;
	}

	/**
	 * Check and return if a climbing exists for given building Returns null if no climbing exists yet
	 * 
	 * @param building_id
	 *            building ID
	 * @return Climbing
	 */
	public static Climbing getClimbingForBuilding(int building_id) {
		Map<String, Object> conditions = new HashMap<String, Object>();
		conditions.put("building_id", building_id); // filter for building ID
		List<Climbing> climbings = climbingDao.queryForFieldValuesArgs(conditions);
		if (climbings.size() == 0)
			return null;
		return climbings.get(0);
	}

	public static Climbing getClimbingForBuildingAndUser(int building_id, int user_id) {
		QueryBuilder<Climbing, Integer> query = climbingDao.queryBuilder();
		Where<Climbing, Integer> where = query.where();
		try {
			where.eq("building_id", building_id);
			// and
			where.and();
			where.eq("users_id", user_id);
			PreparedQuery<Climbing> preparedQuery = query.prepare();
			List<Climbing> climbings = climbingDao.query(preparedQuery);
			if (climbings.size() == 0)
				return null;
			return climbings.get(0);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

	public static Climbing getClimbingForParseId(String id) {
		QueryBuilder<Climbing, Integer> query = climbingDao.queryBuilder();
		Where<Climbing, Integer> where = query.where();
		// the name field must be equal to "foo"
		try {
			where.eq("online_id", id);
			PreparedQuery<Climbing> preparedQuery = query.prepare();
			List<Climbing> climbings = climbingDao.query(preparedQuery);
			if (climbings.size() == 0)
				return null;
			return climbings.get(0);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

	public static Climbing getClimbingForBuildingAndUserNotPaused(int building_id, int user_id) {

		QueryBuilder<Climbing, Integer> query = climbingDao.queryBuilder();
		Where<Climbing, Integer> where = query.where();
		try {
			where.eq("building_id", building_id);
			// and
			where.and();
			where.eq("users_id", user_id);
			where.and();
			where.ne("id_mode", "paused");
			PreparedQuery<Climbing> preparedQuery = query.prepare();
			List<Climbing> climbings = climbingDao.query(preparedQuery);
			if (climbings.size() == 0)
				return null;
			return climbings.get(0);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

	public static Climbing getClimbingForBuildingAndUserPaused(int building_id, int user_id) {
		QueryBuilder<Climbing, Integer> query = climbingDao.queryBuilder();
		Where<Climbing, Integer> where = query.where();
		try {
			where.eq("building_id", building_id);
			// and
			where.and();
			where.eq("users_id", user_id);
			where.and();
			where.eq("id_mode", "paused");
			PreparedQuery<Climbing> preparedQuery = query.prepare();
			List<Climbing> climbings = climbingDao.query(preparedQuery);
			if (climbings.size() == 0)
				return null;
			return climbings.get(0);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public static List<Climbing> getClimbingListForBuildingAndUser(int building_id, int user_id) {

		QueryBuilder<Climbing, Integer> query = climbingDao.queryBuilder();
		Where<Climbing, Integer> where = query.where();
		try {
			where.eq("building_id", building_id);
			// and
			where.and();
			where.eq("users_id", user_id);
			PreparedQuery<Climbing> preparedQuery = query.prepare();
			List<Climbing> climbings = climbingDao.query(preparedQuery);
			return climbings;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public static UserBadge getUserBadgeForUserAndBadge(int badge_id, int obj_id, int user_id) {

		QueryBuilder<UserBadge, Integer> query = userBadgeDao.queryBuilder(); 
		Where<UserBadge, Integer> where = query.where();
		try {
			where.eq("badge_id", badge_id);
			where.and();
			where.eq("user_id", user_id);
			where.and();
			where.eq("obj_id", obj_id);
			PreparedQuery<UserBadge> preparedQuery = query.prepare(); 
			List<UserBadge> userBadges = userBadgeDao.query(preparedQuery); System.out.println("Userbadges size " + userBadges.size());
			if (userBadges.isEmpty())
				return null;
			return userBadges.get(0);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public static TeamDuel getTeamDuelForUserAndId(String id, int user_id) {

		QueryBuilder<TeamDuel, Integer> query = teamDuelDao.queryBuilder(); 
		Where<TeamDuel, Integer> where = query.where();
		try {
			where.eq("id_online", id);
			where.and();
			where.eq("user_id", user_id);
			PreparedQuery<TeamDuel> preparedQuery = query.prepare(); 
			List<TeamDuel> userBadges = teamDuelDao.query(preparedQuery); 
			if (userBadges.isEmpty())
				return null;
			return userBadges.get(0);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public static Collaboration getCollaborationForUserAndId(String id, int user_id) {

		QueryBuilder<Collaboration, Integer> query = collaborationDao.queryBuilder(); 
		Where<Collaboration, Integer> where = query.where();
		try {
			where.eq("id_online", id);
			where.and();
			where.eq("user_id", user_id);
			PreparedQuery<Collaboration> preparedQuery = query.prepare(); 
			List<Collaboration> userBadges = collaborationDao.query(preparedQuery); 
			if (userBadges.isEmpty())
				return null;
			return userBadges.get(0);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public static Competition getCompetitionForUserAndId(String id, int user_id) {

		QueryBuilder<Competition, Integer> query = competitionDao.queryBuilder(); 
		Where<Competition, Integer> where = query.where();
		try {
			where.eq("id_online", id);
			where.and();
			where.eq("user_id", user_id);
			PreparedQuery<Competition> preparedQuery = query.prepare(); 
			List<Competition> userBadges = competitionDao.query(preparedQuery); 
			if (userBadges.isEmpty())
				return null;
			return userBadges.get(0);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	
	public static Collaboration getCollaborationForBuilding(int building_id) {
		Map<String, Object> conditions = new HashMap<String, Object>();
		conditions.put("building_id", building_id); // filter for building ID
		List<Collaboration> collaborations = collaborationDao.queryForFieldValuesArgs(conditions);
		if (collaborations.size() == 0)
			return null;
		return collaborations.get(0);
	}

	public static TeamDuel getTeamDuelById(String id) {
		Map<String, Object> conditions = new HashMap<String, Object>();
		conditions.put("id_online", id);
		List<TeamDuel> duels = teamDuelDao.queryForFieldValuesArgs(conditions);
		if (duels.size() == 0)
			return null;
		return duels.get(0);
	}

	public static Collaboration getCollaborationById(String id) {
		Map<String, Object> conditions = new HashMap<String, Object>();
		conditions.put("id_online", id); // filter for building ID
		List<Collaboration> collaborations = collaborationDao.queryForFieldValuesArgs(conditions);
		if (collaborations.size() == 0)
			return null;
		return collaborations.get(0);
	}

	public static Competition getCompetitionById(String id) {
		Map<String, Object> conditions = new HashMap<String, Object>();
		conditions.put("id_online", id); // filter for building ID
		List<Competition> collaborations = competitionDao.queryForFieldValuesArgs(conditions);
		if (collaborations.size() == 0)
			return null;
		return collaborations.get(0);
	}

	public static User getUserById(int id) {
		Map<String, Object> conditions = new HashMap<String, Object>();
		conditions.put("_id", id); // filter for building ID
		List<User> users = userDao.queryForFieldValuesArgs(conditions);
		if (users.size() == 0)
			return null;
		return users.get(0);
	}

	public static User getUserByFBId(String id) {
		Map<String, Object> conditions = new HashMap<String, Object>();
		conditions.put("FBid", id); // filter for building ID
		List<User> users = userDao.queryForFieldValuesArgs(conditions);
		if (users.size() == 0)
			return null;
		return users.get(0);
	}

	public static Building getBuildingById(int id) {
		Map<String, Object> conditions = new HashMap<String, Object>();
		conditions.put("_id", id); // filter for building ID
		List<Building> buildings = buildingDao.queryForFieldValuesArgs(conditions);
		if (buildings.size() == 0)
			return null;
		return buildings.get(0);
	}

	public static BuildingText getBuildingTextById(int id) {
		Map<String, Object> conditions = new HashMap<String, Object>();
		conditions.put("_id", id); // filter for building ID
		List<BuildingText> buildings = buildingTextDao.queryForFieldValuesArgs(conditions);
		if (buildings.size() == 0)
			return null;
		return buildings.get(0);
	}
	
	public static BuildingText getBuildingTextByBuildingId(int id) {
		Map<String, Object> conditions = new HashMap<String, Object>();
		conditions.put("building_id", id); // filter for building ID
		List<BuildingText> buildings = buildingTextDao.queryForFieldValuesArgs(conditions);
		if (buildings.size() == 0)
			return null;
		return buildings.get(0);
	}

	public static Tour getTourById(int id) {
		Map<String, Object> conditions = new HashMap<String, Object>();
		conditions.put("_id", id); // filter for building ID
		List<Tour> tours = tourDao.queryForFieldValuesArgs(conditions);
		if (tours.size() == 0)
			return null;
		return tours.get(0);
	}

	public static List<Tour> getToursByBuilding(int building_id) {
		QueryBuilder<BuildingTour, Integer> orderQb = buildingTourDao.queryBuilder();
		try {
			orderQb.where().eq("building_id", building_id);
			QueryBuilder<Tour, Integer> accountQb = tourDao.queryBuilder();
			List<Tour> results = accountQb.join(orderQb).query();
			return results;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public static Badge getBadgeById(int id) {
		Map<String, Object> conditions = new HashMap<String, Object>();
		conditions.put("_id", id); // filter for building ID
		List<Badge> userBadges = badgeDao.queryForFieldValuesArgs(conditions);
		if (userBadges.size() == 0)
			return null;
		return userBadges.get(0);
	}

	public static Badge getBadgeByCategory(int category) {
		Map<String, Object> conditions = new HashMap<String, Object>();
		conditions.put("category", category); // filter for building ID
		List<Badge> userBadges = badgeDao.queryForFieldValuesArgs(conditions);
		if (userBadges.size() == 0)
			return null;
		return userBadges.get(0);
	}

	public static List<UserBadge> getUserBadgeByUser(int user_id) {
		Map<String, Object> conditions = new HashMap<String, Object>();
		conditions.put("user_id", user_id); // filter for building ID
		List<UserBadge> userBadges = userBadgeDao.queryForFieldValuesArgs(conditions);
		return userBadges;
	}

	public static int lookForBadge(int badge_id, int obj_id, JSONArray array) {
		for (int i = 0; i < array.length(); i++) {
			try {
				JSONObject obj = array.getJSONObject(i);
				if (obj.getInt("badge_id") == badge_id && obj.getInt("obj_id") == obj_id)
					return i;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return -1;
			}

		}
		return -1;
	}

	public static BuildingText getBuildingTextByBuilding(int building_id) {
		QueryBuilder<BuildingText, Integer> query = buildingTextDao.queryBuilder();
		Where<BuildingText, Integer> where = query.where();
		try {
			where.eq("language", language);
			where.and();
			where.eq("building_id", building_id);
			PreparedQuery<BuildingText> preparedQuery = query.prepare();
			List<BuildingText> userBadges = buildingTextDao.query(preparedQuery);
			if (userBadges.isEmpty())
				return null;
			return userBadges.get(0);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public static int areThereUserBadges(int user_id) {
		QueryBuilder<UserBadge, Integer> query = userBadgeDao.queryBuilder();
		Where<UserBadge, Integer> where = query.where();
		try {
			where.eq("user_id", user_id);
			PreparedQuery<UserBadge> preparedQuery = query.prepare();
			List<UserBadge> userBadges = userBadgeDao.query(preparedQuery);
			return userBadges.size();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
	}

	public static Microgoal getMicrogoalByUserAndBuilding(int user_id, int building_id) {
		QueryBuilder<Microgoal, Integer> query = microgoalDao.queryBuilder();
		Where<Microgoal, Integer> where = query.where();
		try {
			where.eq("user_id", user_id);
			where.and();
			where.eq("building_id", building_id);
			where.and();
			where.eq("deleted", 0);
			PreparedQuery<Microgoal> preparedQuery = query.prepare();
			List<Microgoal> microgoals = microgoalDao.query(preparedQuery);
			if (microgoals.size() == 0)
				return null;
			else
				return microgoals.get(0);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public static MicrogoalText getMicrogoalTextByStory(int story_id) {
		QueryBuilder<MicrogoalText, Integer> query = microgoalTextDao.queryBuilder();
		Where<MicrogoalText, Integer> where = query.where();
		try {
			where.eq("story_id", story_id);
			PreparedQuery<MicrogoalText> preparedQuery = query.prepare();
			List<MicrogoalText> microgoals = microgoalTextDao.query(preparedQuery);
			if (microgoals.size() == 0)
				return null;
			else
				return microgoals.get(0);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Choose a random microgoal
	 * 
	 * @return the story_id of the choosen microgoal
	 * @throws SQLException
	 */
	public static int getRandomStoryId() throws SQLException {
		QueryBuilder<MicrogoalText, Integer> qb = microgoalTextDao.queryBuilder();
		qb.selectRaw("MAX(story_id)");
		// the results will contain 1 string values for the max
		String[] values = microgoalTextDao.queryRaw(qb.prepareStatementString()).getFirstResult();
		int max = Integer.valueOf(values[0]);
		Random rand = new Random();
		int randomNum = rand.nextInt((max - 1) + 1) + 1;
		return randomNum;
	}

	/**
	 * Returns true if 24 hours are passed from a given date, false otherwise.
	 * 
	 * @param date
	 *            the given date
	 * @return true if 24 hours are passed from a given date, false otherwise
	 */
	public static boolean are24hPassed(String date) {
		// try {
		Date today = new Date();
		// Date dateRef = new SimpleDateFormat("MMMM d, yyyy", Locale.ITALIAN).parse(date);
		long millDiff = today.getTime() - Long.valueOf(date);// dateRef.getTime();
		long hours = TimeUnit.MILLISECONDS.toHours(millDiff);
		if (hours < 24)
			return false;
		else
			return true;
		// } catch (ParseException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// return false;
		// }
	}
	public static boolean needUpdate(String date){	 
		 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		 
		 Date today = null;
		 Date check = null;
		try {
			today = sdf.parse(sdf.format(new Date()));
			check = sdf.parse(sdf.format(new Date(Long.valueOf(date))));

		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		 
		 if(check.before(today))
			 return true;
		 else
			 return false;
		 
	 }

	public static long daysPassed(String date) {
		Date today = new Date();
		// Date dateRef = new SimpleDateFormat("MMMM d, yyyy", Locale.ITALIAN).parse(date);
		long millDiff = today.getTime() - Long.valueOf(date);// dateRef.getTime();
		return TimeUnit.MILLISECONDS.toDays(millDiff);
	}

	/**
	 * Calculates the new mean value
	 * 
	 * @param old_mean
	 *            the previous mean value
	 * @param n
	 *            number of elements of the previous mean
	 * @param new_val
	 *            new value to add in new the mean value
	 * @return the new mean value
	 */
	public static double calculateNewMean(long old_mean, int n, int new_val) {
		long sum_old_mean = old_mean * n;
		System.out.println("sum_old_mean " + sum_old_mean);
		System.out.println("new_val " + new_val);
		System.out.println("n " + n);
		return (sum_old_mean + new_val) / (n + 1);
	}

	/**
	 * Returns the multiple of 5 nearest to a given number
	 * 
	 * @param n
	 *            the given number
	 * @return the multiple of 5 nearest to n
	 */
	public static long roundUpMultiple5(double n) {
		return Math.round(((n + 4) / 5) * 5);
	}

	/**
	 * Returns the multiple of 10 nearest to a given number
	 * 
	 * @param n
	 *            the given number
	 * @return the multiple of 10 nearest to n
	 */
	public static long roundUpMultiple10(double n) {
		return Math.round(((n + 5) / 10) * 10);
	}

	/**
	 * OLD VERSION - CURRENTLY UNUSED
	 * Calculates the number of steps to be done in a microgoal
	 * 
	 * @param remainingSteps
	 *            remaining steps to do to complete a given climbing
	 * @param current_mean
	 *            average daily steps of a given user
	 * @return number of steps to be done in order to complete a microgoal
	 */
	public static int generateStepsToDo(int remainingSteps, double current_mean, int difficulty) {

		if (current_mean >= remainingSteps)
			return remainingSteps;
		else {
			int stepsToDo = (int) (roundUpMultiple10(current_mean)) * difficulty;
			if (stepsToDo >= remainingSteps)
				return remainingSteps;
			else
				return stepsToDo;
		}
	}
	
	/**
	 * versione con fisso 25%
	 * @param tot_steps
	 * @param done_steps
	 * @return
	 */
	public static int generateStepsToDo_(int tot_steps, int done_steps){
		int remaining_steps = tot_steps - done_steps;
		int remaining_perc =(int) (((double) remaining_steps / (double) tot_steps) * 100);
		System.out.println("remaining steps " + remaining_steps);
		System.out.println("remaining perc " + remaining_perc);

		if(remaining_perc >= 35)
			return (int) Math.ceil((double) (tot_steps*25)/ (double) 100);
		else
			return remaining_steps;
	}
	
	public static int generateStepsToDo(int tot_steps, int done_steps){
		//calculate interval 
		int percentage = 5;
		if(tot_steps < 6000)
			percentage = 25;
		else if(tot_steps >= 6000 && tot_steps <= 10000 )
			percentage = 15;
		
		int remaining_steps = tot_steps - done_steps;
		int remaining_perc =(int) (((double) remaining_steps / (double) tot_steps) * 100);

		if(remaining_perc >= 35)
			return (int) Math.ceil((double) (tot_steps*percentage)/ (double) 100);
		else
			return remaining_steps;
	}
	

	/**
	 * Saves the current logged in user (and its data) in the cloud.
	 * 
	 * @param fbUser
	 * @param session
	 */
	public static void saveUserToParse(GraphUser fbUser, Session session) {
		// create new parse user
		SharedPreferences pref = getContext().getSharedPreferences("UserSession", 0);
		ParseUser user = new ParseUser();
		user.setUsername(fbUser.getName());
		user.setPassword("");
		user.put("FBid", fbUser.getId());
		user.put("level", currentUser.getLevel());
		user.put("XP", currentUser.getXP());
		user.put("height", 0);
		JSONArray badges = new JSONArray();
		List<UserBadge> ubs = ClimbApplication.getUserBadgeByUser(currentUser.get_id());
		for (UserBadge ub : ubs) {
			JSONObject badge = new JSONObject();
			try {
				badge.put("badge_id", ub.getBadge().get_id());
				badge.put("obj_id", ub.getObj_id());
				badge.put("percentage", ub.getPercentage());
				badges.put(badge);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}
		user.put("badges", badges);
		user.put("mean_daily_steps", new JSONObject());
		user.signUpInBackground(new SignUpCallback() {
			public void done(ParseException e) {
				if (e == null) {
					saveProgressToParse();
				} else {
					Toast.makeText(getContext(), getContext().getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
					Log.e("signUpInBackground", e.getMessage());
					// Sign up didn't succeed. Look at the ParseException
					// to figure out what went wrong
				}
			}
		});
	}

	/**
	 * Saves current user's climbings and microgoal in parse
	 */
	static void saveProgressToParse() {
		ClimbApplication.refreshClimbings();
		for (Climbing climbing : ClimbApplication.climbings) {
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			df.setTimeZone(new SimpleTimeZone(0, "GMT"));
			ParseObject climb = new ParseObject("Climbing");
			climb.put("building", climbing.getBuilding().get_id());
			try {
				climb.put("created", df.parse(df.format(climbing.getCreated())));
				climb.put("modified", df.parse(df.format(climbing.getModified())));
				climb.put("completedAt", df.parse(df.format(climbing.getCompleted())));
			} catch (java.text.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			climb.put("completed_steps", climbing.getCompleted_steps());
			climb.put("remaining_steps", climbing.getRemaining_steps());
			climb.put("percentage", String.valueOf(climbing.getPercentage()));
			climb.put("users_id", climbing.getUser().getFBid());
			climb.put("game_mode", climbing.getGame_mode());
			// climb.saveEventually();
			ParseUtils.saveClimbing(climb, climbing);

		}
		ClimbApplication.refreshMicrogoals();
		for (Microgoal micro : ClimbApplication.microgoals) {
			ParseObject m = new ParseObject("Microgoal");
			m.put("user_id", micro.getUser().getFBid());
			m.put("building", micro.getBuilding().get_id());
			m.put("story_id", micro.getStory_id());
			m.put("tot_steps", micro.getTot_steps());
			m.put("done_steps", micro.getDone_steps());
			// m.saveEventually();
			ParseUtils.saveMicrogoal(m, micro);
		}
		synchronized (ClimbApplication.lock) {
			ClimbApplication.lock.notify();
			ClimbApplication.BUSY = false;
		}
	}

	/**
	 * Checks if current fb logged in user exists in Parse and, if it does, logs him in parse and downloads/updates its progress and data with the one in the cloud.
	 * 
	 * @param fbUser
	 * @param session
	 * @param PD
	 * @param activity
	 */
	public static void userExists(final GraphUser fbUser, final Session session, final ProgressDialog PD, final Activity activity) {
		final User me = ClimbApplication.currentUser;// ClimbApplication.getUserByFBId(fbUser.getId());
		ParseQuery<ParseUser> sameFBid = ParseUser.getQuery();
		sameFBid.whereEqualTo("FBid", fbUser.getId());
		sameFBid.findInBackground(new FindCallback<ParseUser>() {
			public void done(List<ParseUser> results, ParseException e) {
				if (results.isEmpty()) {// user not saved in Parse
					ClimbApplication.saveUserToParse(fbUser, session);
				} else {// user already saved in Parse
					ParseUser user = results.get(0);
					ParseUser.logInInBackground(user.getUsername(), "", new LogInCallback() {
						public void done(ParseUser user, ParseException e) {
							if (user != null) {
								// Hooray! The user is logged in.
								me.setLevel(user.getInt("level"));
								me.setXP(user.getInt("XP"));
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
								new MyAsync(activity, PD, true).execute();//download user's progresses, badges
							} else {
								// Signup failed. Look at the ParseException to
								// see what happened.
								Toast.makeText(getContext(), getContext().getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
								Log.e("userExists", e.getMessage());
							}
						}
					});
				}
			}
		});
	}

	/*
	 * Now that user_friends is granted, load /me/invitable_friends to get friends who have not installed the game. 
	 * Also load /me/friends which returns friends that have installed the game (if using Platform v2.0).
	 */
	public static RequestBatch loadFriendsFromFacebook() {
		Log.d("ClimbApplication", "loadFriendsFromFacebook");
		final Session session = Session.getActiveSession();
		
//		final List<String> PERMISSIONS = Arrays.asList("publish_actions, user_friends");
//		List<String> permissions = Session.getActiveSession().getPermissions();
//
//		if (!new HashSet<String>(permissions).containsAll(PERMISSIONS)) {
//			Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(this, PERMISSIONS);
//			Session.getActiveSession().requestNewPublishPermissions(newPermissionsRequest);
//			Log.w("FBShare", "has permission");
//		}

		RequestBatch requestBatch = new RequestBatch();

		// Get a list of friends who have _not installed_ the game.
		Request invitableFriendsRequest = Request.newGraphPathRequest(session, "/me/invitable_friends", new Request.Callback() {

			@Override
			public void onCompleted(Response response) {

				FacebookRequestError error = response.getError();
				if (error != null) {
					Log.e("ClimbApplication", error.toString());
					// handleError(error, true);
				} else if (session == Session.getActiveSession()) {
					if (response != null) {
						// Get the result
						GraphObject graphObject = response.getGraphObject();
						JSONArray dataArray = (JSONArray) graphObject.getProperty("data");

						List<JSONObject> invitableFriends = new ArrayList<JSONObject>();
						if (dataArray.length() > 0) {
							// Ensure the user has at least one friend ...

							for (int i = 0; i < dataArray.length(); i++) { 
								invitableFriends.add(dataArray.optJSONObject(i));
							}
						}

						setInvitableFriend(invitableFriends);

					}
				}
			}

		});
		Bundle invitableParams = new Bundle();
		invitableParams.putString("fields", "id,first_name,last_name,picture");
		invitableFriendsRequest.setParameters(invitableParams);
		requestBatch.add(invitableFriendsRequest);

		return requestBatch;
	}

	public static double fromStepsToMeters(int steps) {
		return (double) steps * Building.average_step_height / (double) 100;
	}

	public static void showConnectionProblemsToast() {
		if (connection_toast == null) {
			// connection_toast = Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.connection_problem2), Toast.LENGTH_SHORT);
			// connection_toast.show();
		}
	}
	
	public static void setTextTipStyle(TextView text_tip){
		text_tip.setTextColor(Color.parseColor("#FFFFFF"));
		text_tip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		text_tip.setPadding(12, 12, 12, 12);
	}
	
	

}
