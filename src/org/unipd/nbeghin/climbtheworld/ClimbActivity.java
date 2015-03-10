package org.unipd.nbeghin.climbtheworld;

import java.lang.reflect.Type;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.unipd.nbeghin.climbtheworld.activity.recognition.ActivityRecognitionIntentService;
import org.unipd.nbeghin.climbtheworld.exceptions.ClimbingNotFound;
import org.unipd.nbeghin.climbtheworld.exceptions.NoFBSession;
import org.unipd.nbeghin.climbtheworld.listeners.AccelerometerSamplingRateDetect;
import org.unipd.nbeghin.climbtheworld.models.Alarm;
import org.unipd.nbeghin.climbtheworld.models.Badge;
import org.unipd.nbeghin.climbtheworld.models.Building;
import org.unipd.nbeghin.climbtheworld.models.BuildingText;
import org.unipd.nbeghin.climbtheworld.models.ChartMember;
import org.unipd.nbeghin.climbtheworld.models.Climbing;
import org.unipd.nbeghin.climbtheworld.models.Collaboration;
import org.unipd.nbeghin.climbtheworld.models.Competition;
import org.unipd.nbeghin.climbtheworld.models.GameModeType;
import org.unipd.nbeghin.climbtheworld.models.Group;
import org.unipd.nbeghin.climbtheworld.models.Microgoal;
import org.unipd.nbeghin.climbtheworld.models.MicrogoalText;
import org.unipd.nbeghin.climbtheworld.models.TeamDuel;
import org.unipd.nbeghin.climbtheworld.models.Tour;
import org.unipd.nbeghin.climbtheworld.models.User;
import org.unipd.nbeghin.climbtheworld.models.UserBadge;
import org.unipd.nbeghin.climbtheworld.receivers.StairsClassifierReceiver;
import org.unipd.nbeghin.climbtheworld.services.ActivityRecognitionRecordService;
import org.unipd.nbeghin.climbtheworld.services.NotificationClickedService;
import org.unipd.nbeghin.climbtheworld.services.NotificationDeletedReceiver;
import org.unipd.nbeghin.climbtheworld.services.SamplingClassifyService;
import org.unipd.nbeghin.climbtheworld.util.AlarmUtils;
import org.unipd.nbeghin.climbtheworld.util.FacebookUtils;
import org.unipd.nbeghin.climbtheworld.util.GeneralUtils;
import org.unipd.nbeghin.climbtheworld.util.GraphicsUtils;
import org.unipd.nbeghin.climbtheworld.util.LogUtils;
import org.unipd.nbeghin.climbtheworld.util.ModelsUtil;
import org.unipd.nbeghin.climbtheworld.util.ParseUtils;
import org.unipd.nbeghin.climbtheworld.util.StatUtils;
import org.unipd.nbeghin.climbtheworld.util.SystemUiHider;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VerticalSeekBar;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/**
 * Climbing activity: shows a given building and starts classifier. At start it calculates the sampling rate of the device it's run from (only once, after that it just saves the value in standard Android preferences).
 * 
 */
public class ClimbActivity extends ActionBarActivity implements Observer {

	private static boolean setInitialStar = false;

	public static final String SAMPLING_TYPE = "ACTION_SAMPLING"; // intent's action
	public static final String SAMPLING_TYPE_NON_STAIR = "NON_STAIR"; // classifier's output
	public static final String SAMPLING_DELAY = "DELAY"; // intent's action
	public static final String NOTIFICATION_GROUP = "ClimbActivity_play";

	public static int NOTIFICATION_ID_BADGE = 1; // badge
	public static int NOTIFICATION_ID_BONUS = 2; // bonus
	public static int NOTIFICATION_ID_LEVEL = 3; // level
	public static int NOTIFICATION_ID_INBOX = 4; // inbox

	public boolean current_win = false;

	private UiLifecycleHelper uiHelper;

	public static boolean samplingEnabled = false; // sentinel if sampling is running
	private static double detectedSamplingRate = 0; // detected sampling rate (after sampling rate detector)
	private static int samplingDelay; // current sampling delay (SensorManager)
	private double minimumSamplingRate = 13; // minimum sampling rate for using this app
	private Intent backgroundClassifySampler; // classifier background service intent
	private Intent backgroundSamplingRateDetector; // sampling rate detector service intent
//	private IntentFilter classifierFilter = new IntentFilter(ClassifierCircularBuffer.CLASSIFIER_ACTION); // intent filter (for BroadcastReceiver)
	private IntentFilter samplingRateDetectorFilter = new IntentFilter(AccelerometerSamplingRateDetect.SAMPLING_RATE_ACTION); // intent filter (for BroadcastReceiver)
//	private BroadcastReceiver classifierReceiver = new ClassifierReceiver(); // implementation of BroadcastReceiver for classifier service
	private BroadcastReceiver sampleRateDetectorReceiver = new SamplingRateDetectorReceiver(); // implementation of BroadcastReceiver for sampling rate detector

	private int previous_progress = 0;
	private int num_steps = 0; // number of currently detected steps
	private double percentage = 0.0; // current progress percentage
	private BuildingText buildingText;
	private Building building; // current building
	private Climbing climbing; // current climbing
	private Climbing soloClimb; // the 'paused' climbing
	private Microgoal microgoal; // the current microgoal
	private User currentUser; // the current user
	private VerticalSeekBar seekbarIndicator; // reference to vertical seekbar
	private int vstep_for_rstep = 1;
	private boolean used_bonus = false;
	private double percentage_bonus = 0.30f; // OLD: 0.50f
	private boolean climbedYesterday = false;
	private int new_steps = 0;
	private int difficulty;
	private int old_chart_position;
	private GameModeType old_game_mode;

	SharedPreferences pref;
	
	//application context
	private Context appContext;
	//reference to android preferences
	private SharedPreferences settings;

	// logic for social mode
	private GameModeType mode;

	private Collaboration collaboration;
	private Map<String, Integer> others_steps;
	ParseObject collab_parse;
	private int threshold;

	private Competition competition;
	private List<ChartMember> chart;
	ParseObject compet_parse;

	private TeamDuel teamDuel;
	private List<Integer> myTeamScores;
	ParseObject teamDuel_parse;
	JSONObject totScores;
	// Graphics for Social Mode
	private VerticalSeekBar secondSeekbar;
	private TextView current;

	private List<TextView> group_members = new ArrayList<TextView>();
	private List<TextView> group_steps = new ArrayList<TextView>();
	private List<TextView> group_minus = new ArrayList<TextView>();

	private boolean isCounterMode; // true if the game is on, false otherwise

	private Menu mymenu;
	private TextView chartHelpText;

	// number of virtual step for each real step
	/**
	 * Whether or not the system UI should be auto-hidden after {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = true;
	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise, will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = true;
	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = 0;
	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;
	
	private static boolean stepsInGamePeriod = false;


	// public static double getDetectedSamplingRate() {
	// return detectedSamplingRate;
	// }

	/**
	 * Handles classifier service intents (STAIR/NON_STAIR)
	 * 
	 */
//	public class ClassifierReceiver extends BroadcastReceiver {
//
//		private static final double tradeoffG = 0.001;
//		private static final double g = tradeoffG / (double) 100;
//		private List<Double> history = new ArrayList<Double>();
//		private static final int historySize = 10;
//
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			Double result = intent.getExtras().getDouble(ClassifierCircularBuffer.CLASSIFIER_NOTIFICATION_STATUS);
//
//			double correction = 0.0;
//			for (int indexHistory = 0; indexHistory < history.size(); indexHistory++) {
//				correction += (100 / Math.pow(2, indexHistory + 1)) * (double) history.get(indexHistory) * g;
//			}
//
//			if (Double.isNaN(correction)) {
//				correction = 0.0;
//			}
//
//			double finalClassification = result + correction;
//
//			if (result * finalClassification >= 0) {
//				if (history.size() == historySize) {
//					history.remove(historySize - 1);
//					history.add(0, (finalClassification > 0 ? 1.0 : -1.0));
//				} else {
//					history.add(0, (finalClassification > 0 ? 1.0 : -1.0));
//				}
//			} else {
//				history.clear();
//				history.add(result > 0 ? 1.0 : -1.0);
//			}
//
//			if (finalClassification > 0) {
//
//				if (climbedYesterday && percentage > 0.25f && percentage < 0.50f && used_bonus == false && building.get_id() != 6) {
//					// bonus at 25%
//					int rest = apply_percentage_bonus();
//					boolean winMicrogoal = microgoal.getDone_steps() >= microgoal.getTot_steps();
//					if (winMicrogoal){
//						apply_win_microgoal();
//						microgoal.setDone_steps(microgoal.getDone_steps() + rest);
//					}
//				} else { // standard, no bonus
//					num_steps += vstep_for_rstep; // increase the number of steps
//					new_steps += vstep_for_rstep;
//					previous_progress = new_steps;
//					if (!isCounterMode) { // increase the seekbar progress and update the microgoal progress only if game mode is on
//						microgoal.setDone_steps(microgoal.getDone_steps() + vstep_for_rstep);
//
//						// increase the seekbar progress
//						if (mode == GameModeType.SOCIAL_CLIMB) {
//							seekbarIndicator.setProgress(num_steps + sumOthersStep());
//							setThresholdText();
//						} else if (mode == GameModeType.TEAM_VS_TEAM) {
//							seekbarIndicator.setProgress(myTeamScore());
//							setThresholdText();
//						} else
//							seekbarIndicator.setProgress(num_steps);
//
//						percentage = (double) num_steps / (double) building.getSteps(); // increase the progress
//					}
//
//					boolean win = false; // user wins?
//					boolean winMicrogoal = false; // is current microgoal completed?
//					if (!isCounterMode) { // check win only if game mode is on
//						winMicrogoal = microgoal.getDone_steps() >= microgoal.getTot_steps();
//						if (winMicrogoal)
//							apply_win_microgoal();
//
//						if (mode == GameModeType.SOCIAL_CLIMB) {
//							// consider also my friends' steps
//							win = ((num_steps + sumOthersStep()) >= building.getSteps()) && (threshold - num_steps <= 0);
//							setThresholdText();
//						} else if (mode == GameModeType.TEAM_VS_TEAM) {
//							// consider my team's steps
//							win = ((myTeamScore()) >= building.getSteps());
//							setThresholdText();
//						} else
//							win = ((num_steps) >= building.getSteps()); // consider only my steps
//						if (win) { // ensure it did not exceed the number of steps
//									// (when multiple steps-at-once are detected)
//							num_steps = building.getSteps();
//							percentage = 1.00;
//							if (mode != GameModeType.SOCIAL_CHALLENGE && mode != GameModeType.TEAM_VS_TEAM)
//								current_win = true;
//
//						}
//					}
//					updateStats(); // update the view of current stats
//					if (win && !isCounterMode) {
//						new SaveProgressTask(true, true).execute(); // stopClassify(); // stop classifier service service
//						((ImageButton) findViewById(R.id.btnStartClimbing)).setImageResource(R.drawable.av_play); // set
//						findViewById(R.id.progressBarClimbing).startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_fade_out)); // hide
//						// progress
//						// bar
//						findViewById(R.id.progressBarClimbing).setVisibility(View.INVISIBLE);
//						apply_win();
//					}
//				}
//			}
//
//			//((TextView) findViewById(R.id.lblClassifierOutput)).setText(finalClassification > 0 ? "STAIR" : "NON_STAIR"); // debug: show currently detected classifier output
//			((TextView) findViewById(R.id.lblClassifierOutput)).setVisibility(View.GONE);
//		}
//	}
	
	public void refreshOnStep(){
		
		//l'utente ha fatto almeno uno scalino nel periodo di gioco corrente
		stepsInGamePeriod=true;
		Log.d(MainActivity.AppName,"ClimbActivity - STEP");
		
		if (climbedYesterday && percentage > 0.25f && percentage < 0.50f && used_bonus == false && building.get_id() != 6) {
			// bonus at 25%
			int rest = apply_percentage_bonus();
			boolean winMicrogoal = microgoal.getDone_steps() >= microgoal.getTot_steps();
			if (winMicrogoal){
				apply_win_microgoal();
				microgoal.setDone_steps(microgoal.getDone_steps() + rest);
			}
		} else { // standard, no bonus
			num_steps += vstep_for_rstep; // increase the number of steps
			new_steps += vstep_for_rstep;
			previous_progress = new_steps;
			if (!isCounterMode) { // increase the seekbar progress and update the microgoal progress only if game mode is on
				microgoal.setDone_steps(microgoal.getDone_steps() + vstep_for_rstep);

				// increase the seekbar progress
				if (mode == GameModeType.SOCIAL_CLIMB) {
					seekbarIndicator.setProgress(num_steps + sumOthersStep());
					setThresholdText();
				} else if (mode == GameModeType.TEAM_VS_TEAM) {
					seekbarIndicator.setProgress(myTeamScore());
					setThresholdText();
				} else
					seekbarIndicator.setProgress(num_steps);

				percentage = (double) num_steps / (double) building.getSteps(); // increase the progress
			}

			boolean win = false; // user wins?
			boolean winMicrogoal = false; // is current microgoal completed?
			if (!isCounterMode) { // check win only if game mode is on
				winMicrogoal = microgoal.getDone_steps() >= microgoal.getTot_steps();
				System.out.println("done_steps: " + microgoal.getDone_steps());
				System.out.println("tot_steps: " + microgoal.getTot_steps());
				if (winMicrogoal)
					apply_win_microgoal();

				if (mode == GameModeType.SOCIAL_CLIMB) {
					// consider also my friends' steps
					win = ((num_steps + sumOthersStep()) >= building.getSteps()) && (threshold - num_steps <= 0);
					setThresholdText();
				} else if (mode == GameModeType.TEAM_VS_TEAM) {
					// consider my team's steps
					win = ((myTeamScore()) >= building.getSteps());
					setThresholdText();
				} else
					win = ((num_steps) >= building.getSteps()); // consider only my steps
				if (win) { // ensure it did not exceed the number of steps
							// (when multiple steps-at-once are detected)
					num_steps = building.getSteps();
					percentage = 1.00;
					if (mode != GameModeType.SOCIAL_CHALLENGE && mode != GameModeType.TEAM_VS_TEAM)
						current_win = true;

				}
			}
			updateStats(); // update the view of current stats
			if (win && !isCounterMode) {
				
				//si recupera il prossimo alarm impostato
				int next_alarm_id = settings.getInt("alarm_id", -1);
				Alarm next_alarm = null;				
				if(next_alarm_id!=-1){
					next_alarm = AlarmUtils.getAlarm(appContext, next_alarm_id);
				}
				
				new SaveProgressTask(next_alarm, settings.getInt("artificialDayIndex", 0), true, true).execute(); // stopClassify(); // stop classifier service service
				((ImageButton) findViewById(R.id.btnStartClimbing)).setImageResource(R.drawable.av_play); // set
				findViewById(R.id.progressBarClimbing).startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_fade_out)); // hide
				// progress
				// bar
				findViewById(R.id.progressBarClimbing).setVisibility(View.INVISIBLE);
				apply_win();
			}
		}
		
	}
	
	
	public void printClassification(double finalClassification){
		((TextView) findViewById(R.id.lblClassifierOutput)).setText(finalClassification > 0 ? "STAIR" : "NON_STAIR"); // debug: show currently detected classifier output
	}

	private int apply_percentage_bonus() {
		Log.i(MainActivity.AppName, "Applying percentage bonus");
		percentage += percentage_bonus;
		int old_steps = num_steps;
		num_steps = (int) (((double) building.getSteps()) * percentage);
		int gained_steps = num_steps - old_steps;
		int remaining = microgoal.getTot_steps() - microgoal.getDone_steps();
		int rest_micro_steps = 0;
		if(gained_steps >= remaining)
			rest_micro_steps = gained_steps - remaining;
		microgoal.setDone_steps(microgoal.getDone_steps() + remaining);
		// stopClassify();
		used_bonus = true;
		Toast.makeText(getApplicationContext(), getString(R.string.bonus), Toast.LENGTH_LONG).show();
		enableRocket();
		double progress = ((double) ((microgoal.getTot_steps() - microgoal.getDone_steps()) + num_steps) * (double) 100) / (double) building.getSteps();
		//seekbarIndicator.nextStar((int) Math.round(progress));
		updateStats(); // update the view of current stats
		if (mode == GameModeType.SOCIAL_CLIMB) {
			seekbarIndicator.setProgress(num_steps + sumOthersStep());
			setThresholdText();
		} else
			seekbarIndicator.setProgress(num_steps); // increase the seekbar progress
		return rest_micro_steps;
	}

	private void apply_win() {
		Log.i(MainActivity.AppName, "Succesfully climbed building #" + building.get_id());
		// Toast.makeText(getApplicationContext(), getString(R.string.successfull_climb, building.getSteps(), building.getHeight(), building.getName()), Toast.LENGTH_LONG).show();
		// show completion text
		findViewById(R.id.lblWin).setVisibility(View.VISIBLE); // load and animate completed climbing test
		findViewById(R.id.lblWin).startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink));
		((ImageButton) findViewById(R.id.btnStartClimbing)).setImageResource(R.drawable.social_share);
		findViewById(R.id.btnAccessPhotoGallery).startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_fade_in));
		findViewById(R.id.btnAccessPhotoGallery).setVisibility(View.VISIBLE);
		((ImageButton) findViewById(R.id.btnAccessPhotoGallery)).setImageResource(R.drawable.ic_action_video);
		switch (climbing.getGame_mode()) {
		case 1:
			setThresholdText();
			break;
		case 2:
			break;
		default:
			break;
		}

		if (pref.getBoolean("first_open_3", true)) {
			if (!pref.getBoolean("done_tutorial", false)) {
				Intent intent = new Intent(this, OnBoardingActivity.class);
				intent.putExtra("source", "ClimbActivityVictory");
				startActivity(intent);
				pref.edit().putBoolean("first_open_3", false).commit();
			} else {
				pref.edit().putBoolean("first_open_3", false).commit();

			}
		}
	}

	private void apply_update() {
		Log.i("apply_update", "apply_update " + new_steps);
		findViewById(R.id.encouragment).setVisibility(View.VISIBLE);
		((TextView) findViewById(R.id.encouragment)).setText(getString(R.string.well_done));
		if (!isCounterMode && (!pref.getString("FBid", "none").equalsIgnoreCase("none") && !pref.getString("FBid", "none").equalsIgnoreCase("empty"))) {
			((ImageButton) findViewById(R.id.btnAccessPhotoGallery)).setImageResource(R.drawable.social_share);
			findViewById(R.id.btnAccessPhotoGallery).setVisibility(View.VISIBLE);
		}
		previous_progress = new_steps;
		new_steps = 0;

	}

	private void enableRocket() {
		Animation animSequential = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rocket);
		findViewById(R.id.imgRocket).startAnimation(animSequential);
	}

	private void win_microgoal_animation(int bonus) {
		
		//calculate interval 
		int percentage = 5;
		if(building.getSteps() < 6000)
			percentage = 25;
		else if(building.getSteps() >= 6000 && building.getSteps() <= 10000 )
			percentage = 15;
		
		
		
		int unit = (int) Math.ceil((double) (building.getSteps()*percentage)/ (double) 100);
		int final_pos = (int) Math.floor(((double) (num_steps)/ (double) unit));
		if(num_steps == building.getSteps()) final_pos += 1;
		System.out.println("unit " + unit);
		System.out.println("num_steps " + num_steps);
		System.out.println("final_pos " + final_pos);
		seekbarIndicator.setGoldStar(final_pos);
		/*
		 * final TextView bonus_microgoal = (TextView) findViewById(R.id.bonusMicrogoal); bonus_microgoal.setText(getString(R.string.microgoal_terminated2, bonus)); Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_slide_in_top); anim.setDuration(2000); anim.setAnimationListener(new AnimationListener() {
		 * 
		 * @Override public void onAnimationStart(Animation animation) { }
		 * 
		 * @Override public void onAnimationRepeat(Animation animation) { }
		 * 
		 * @Override public void onAnimationEnd(Animation animation) { Animation anim2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_slide_out_bottom); anim2.setDuration(2000); bonus_microgoal.startAnimation(anim2); } });
		 * 
		 * bonus_microgoal.startAnimation(anim);
		 */

		// int notification_icon = (Build.VERSION.SDK_INT < 11) ? R.drawable.ic_stat_star_dark : R.drawable.ic_stat_star_light;
		int notification_icon = (Build.VERSION.SDK_INT < 11) ? R.drawable.ic_stat_cup_dark : R.drawable.ic_stat_cup_light;
		if ((Build.VERSION.SDK_INT < 16))
			showNotificationBonus(String.valueOf(bonus), notification_icon, true); // getString(R.string.microgoal_terminated2, bonus)
		else {
			ClimbApplication.updateBonusNotification(bonus);
			showInboxNotification();
		}
	}

	private void apply_win_microgoal() {
		User me = currentUser;// ClimbApplication.getUserById(pref.getInt("local_id", -1));
		int multiplier = 1;
		switch (difficulty) {
		case 100:
			multiplier = 1;
			break;
		case 10:
			multiplier = 2;
			break;
		case 1:
			multiplier = 3;
			break;
		}
		int reward = microgoal.getReward() * multiplier;
		me.setXP(me.getXP() + reward);
		ClimbApplication.userDao.update(me);

		// Toast.makeText(this, getString(R.string.microgoal_terminated, reward), Toast.LENGTH_SHORT).show();
		win_microgoal_animation(reward);

		if (percentage >= 1.00)
			current_win = true;
		Microgoal old_microgoal = microgoal;
		if (percentage < 1.00 && !current_win)
			old_microgoal = createMicrogoal();

		if (!pref.getString("FBid", "none").equalsIgnoreCase("none") && !pref.getString("FBid", "none").equalsIgnoreCase("empty"))
			deleteMicrogoalInParse(old_microgoal);
		else {
			ClimbApplication.microgoalDao.delete(old_microgoal);
		}
	}

	/**
	 * Handles sampling rate detector service intents (STAIR/NON_STAIR)
	 * 
	 */
	public class SamplingRateDetectorReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			detectedSamplingRate = intent.getExtras().getDouble(AccelerometerSamplingRateDetect.SAMPLING_RATE); // get detected sampling rate from received intent
			samplingDelay = backgroundSamplingRateDetector.getExtras().getInt(SAMPLING_DELAY); // get used sampling delay from received intent
			Log.i(MainActivity.AppName, "Detected sampling rate: " + Double.toString(detectedSamplingRate) + "Hz");
			if (detectedSamplingRate >= minimumSamplingRate) { // sampling rate high enough
				SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit(); // get refence to android preferences
				editor.putFloat("detectedSamplingRate", (float) detectedSamplingRate); // store
																						// detected
																						// sampling
																						// rate
				editor.putInt("sensor_delay", samplingDelay); // store used
																// sampling
																// delay
				editor.apply(); // commit preferences
				Log.i(MainActivity.AppName, "Stored detected sampling rate of " + detectedSamplingRate + "Hz");
				Log.i(MainActivity.AppName, "Stored sampling delay of " + samplingDelay);
				stopService(backgroundSamplingRateDetector); // stop sampling
																// rate detector
																// service
				unregisterReceiver(this); // unregister listener
				setupByDetectedSamplingRate(); // setup app with detected
												// sampling rate
			} else { // sampling rate not high enough: try to decrease the
						// sampling delay
				if (backgroundSamplingRateDetector.getExtras().getInt(ClimbActivity.SAMPLING_DELAY) != SensorManager.SENSOR_DELAY_UI) { // decrease
																																		// sampling
																																		// delay
																																		// in
																																		// order
																																		// to
																																		// increase
																																		// sampling
																																		// rate
					Log.w(MainActivity.AppName, "Sampling rate not high enough: trying to decrease the sampling delay");
					stopService(backgroundSamplingRateDetector); // stop
																	// previous
																	// sampling
																	// rate
																	// detector
																	// service
					backgroundSamplingRateDetector.putExtra(SAMPLING_DELAY, SensorManager.SENSOR_DELAY_UI); // set
																											// new
																											// sampling
																											// delay
																											// (lower
																											// than
																											// the
																											// previous
																											// one)
					startService(backgroundSamplingRateDetector); // start new
																	// sampling
																	// rate
																	// detector
																	// service
				} else { // unable to determine a sampling rate high enough for
							// our purposes: stop
					Log.e(MainActivity.AppName, "Sampling rate not high enough for this application");
					unregisterReceiver(this); // unregister listener
					stopService(backgroundSamplingRateDetector); // stop
																	// sampling
																	// rate
																	// detector
																	// service
					((TextView) findViewById(R.id.lblSamplingRateDetected)).setText("TOO LOW: " + (int) detectedSamplingRate + " Hz");
					AlertDialog.Builder alert = new AlertDialog.Builder(getApplicationContext());
					alert.setTitle(getString(R.string.alert_title));
					alert.setMessage(getString(R.string.alert_message, minimumSamplingRate));
					alert.show();
				}
			}
		}
	}

	private final List<String> PERMISSION = Arrays.asList("publish_actions");
	private static final int REAUTH_ACTIVITY_CODE = 100;

	public void accessPhotoGallery(View v) {
		if (percentage >= 1.0) {
			Log.i(MainActivity.AppName, "Accessing gallery for building " + building.get_id());
			Intent intent = new Intent(this, GalleryActivity.class);
			intent.putExtra("gallery_building_id", building.get_id());
			startActivity(intent);
			finish();
		} else {
			if (FacebookUtils.isOnline(getApplicationContext())) {
				if (FacebookUtils.isLoggedIn()) {
					if (FacebookDialog.canPresentOpenGraphActionDialog(getApplicationContext(), FacebookDialog.OpenGraphActionDialogFeature.OG_ACTION_DIALOG)) {
						// OpenGraphObject object = OpenGraphObject.Factory.createForPost("unipdclimb:building");
						// object.setTitle(buildingText.getName());
						// List<String> list = new ArrayList<String>();
						// list.add("http://thumb1.shutterstock.com/display_pic_with_logo/711913/218203954/stock-vector-trophy-hand-holding-trophy-vector-218203954.jpg");
						// object.setImageUrls(list);
						// object.setType("unipdclimb:building");
						// object.setTitle("titolo");
						// object.setUrl("http://climbtheworld.parseapp.com/building.html");
						//
						// OpenGraphAction action = OpenGraphAction.Factory.createForPost("unipdclimb:climb");
						// action.setProperty("building", object);
						// action.setType("unipdclimb:climb");
						//
						// List<String> permissions = Session.getActiveSession().getPermissions();
						//
						// if (!new HashSet<String>(permissions).containsAll(PERMISSIONS)) {
						// Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(
						// this, PERMISSIONS);
						// Session.getActiveSession().requestNewPublishPermissions(newPermissionsRequest);
						// Log.w("FBShare", "has permission");
						// return;
						// }
						//
						// Request request = Request.newPostOpenGraphActionRequest(Session.getActiveSession(), action, new Request.Callback() {
						//
						// @Override
						// public void onCompleted(Response response) {
						// System.out.println("completed");
						// FacebookRequestError error = response.getError();
						// if (error != null){
						// Log.e("FacebookRequestError", "Error 1: "+error.getErrorMessage());
						// } else {
						// String actionId = null;
						// try {
						// JSONObject graphResponse = response
						// .getGraphObject().getInnerJSONObject();
						// actionId = graphResponse.getString("id");
						// } catch (JSONException e) {
						// }
						// Log.e("done", actionId);
						//
						// }
						// }
						// });
						//
						// RequestBatch requestBatch = new RequestBatch();
						// requestBatch.add(request);
						//
						// requestBatch.executeAsync();

						List<String> permissions = Session.getActiveSession().getPermissions();

						for (String p : permissions)
							System.out.println(p);

						if (!new HashSet<String>(permissions).containsAll(PERMISSION)) {
							Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(this, PERMISSION);
							Session.getActiveSession().requestNewPublishPermissions(newPermissionsRequest);
							Log.w("FBShare", "has permission");
						}

						FacebookDialog shareDialog = null;
						boolean win = percentage >= 1.00 ? true : false;

						switch (old_game_mode) {
						case SOLO_CLIMB:
							shareDialog = FacebookUtils.publishOpenGraphStory_SoloClimb(this, win, previous_progress, buildingText.getName(), building.getSteps());
							break;
						case SOCIAL_CLIMB:
							shareDialog = FacebookUtils.publishOpenGraphStory_SocialClimb(this, collab_parse.getJSONObject("collaborators"), win, previous_progress, buildingText.getName());
							break;

						case SOCIAL_CHALLENGE:
							shareDialog = FacebookUtils.publishOpenGraphStory_SocialChallenge(this, chart, win, previous_progress, buildingText.getName(), old_chart_position);
							break;

						case TEAM_VS_TEAM:
							int new_position = 0;
							if (teamDuel.getSteps_my_group() >= teamDuel.getSteps_other_group())
								new_position = 0;
							else
								new_position = 1;
							shareDialog = FacebookUtils.publishOpenGraphStory_TeamVsTeam(this, teamDuel.getMygroup(), teamDuel_parse.getJSONObject("creator_stairs"), teamDuel_parse.getJSONObject("challenger_stairs"), win, previous_progress, buildingText.getName(), old_chart_position, new_position);
							break;
						}

						uiHelper.trackPendingDialogCall(shareDialog.present());

					} else {

						FacebookUtils fb = new FacebookUtils(this);

						try {
							fb.postUpdateToWall(climbing, previous_progress, buildingText.getName(), pref.getString("FBid", ""));
						} catch (NoFBSession e) {
							Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
							startActivity(intent);

						}
					}
				}else{
					Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
					intent.putExtra("need_help", true);
					startActivity(intent);
				}
			}else{
				Toast.makeText(getApplicationContext(), getString(R.string.connect_to_post), Toast.LENGTH_SHORT).show();
			}

		}// close else
	}

	/**
	 * Update the stat panel
	 */
	private void updateStats() {
		// ((TextView)
		// findViewById(R.id.lblNumSteps)).setText(Integer.toString(num_steps) +
		// " of " + Integer.toString(building.getSteps()) + " (" + (new
		// DecimalFormat("#.##")).format(percentage * 100.00) + "%)");
		if (!isCounterMode)
			((TextView) findViewById(R.id.lblNumSteps)).setText(getString(R.string.stats_panel, Integer.toString(num_steps), Integer.toString(building.getSteps()), (new DecimalFormat("#.##")).format(percentage * 100.00)));
		else
			((TextView) findViewById(R.id.lblNumSteps)).setText(String.valueOf(num_steps));

	}

	/**
	 * Setup the activity with a given sampling rate and sampling delay
	 */
	private void setupByDetectedSamplingRate() {
		backgroundClassifySampler.putExtra(AccelerometerSamplingRateDetect.SAMPLING_RATE, detectedSamplingRate);
		backgroundClassifySampler.putExtra(SAMPLING_DELAY, samplingDelay);
		// if (climbing.getPercentage() < 100) {
		// Toast.makeText(getApplicationContext(),
		// "Start climbing some stairs!", Toast.LENGTH_LONG).show();
		// }
	}

	private void setThresholdText() {
		int stepsToThreshold = threshold - num_steps;
		if (stepsToThreshold <= 0)
			current.setText(getString(R.string.threashold_passed));
		else
			current.setText(getString(R.string.threshold) + ": " + stepsToThreshold);
	}
	
	public static boolean stepsInCurrentGamePeriod(){
		return stepsInGamePeriod;
	}

	/**
	 * Sets Graphics in case of social mode. Shows name and steps of user's friends
	 */
	private void setGraphicsSocialMode() {

		secondSeekbar = (VerticalSeekBar) findViewById(R.id.seekBarPosition2);
		secondSeekbar.setOnTouchListener(new OnTouchListener() { // disable
																	// user-driven
																	// seekbar
																	// changes
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return false;
			}
		});
		secondSeekbar.setMax(building.getSteps());
		secondSeekbar.hideStars();
		current = (TextView) findViewById(R.id.textPosition);
		for (int i = 1; i <= 6; i++) { //ClimbApplication.N_MEMBERS_PER_GROUP
			int idNome = getResources().getIdentifier("nome" + i, "id", getPackageName());
			int idPassi = getResources().getIdentifier("passi" + i, "id", getPackageName());
			int idMinus = getResources().getIdentifier("minus" + i, "id", getPackageName());
			group_members.add((TextView) findViewById(idNome));
			group_steps.add((TextView) findViewById(idPassi));
			group_minus.add((TextView) findViewById(idMinus));
		}

		switch (GameModeType.values()[climbing.getGame_mode()]) {
		case SOCIAL_CLIMB:
			secondSeekbar.setVisibility(View.GONE);
			for (int i = 0; i < group_members.size() - 1; i++) {
				group_members.get(i).setVisibility(View.VISIBLE);
				group_steps.get(i).setVisibility(View.VISIBLE);
				group_minus.get(i).setVisibility(View.VISIBLE);
			}
			group_members.get(group_members.size() - 1).setVisibility(View.VISIBLE);
			group_steps.get(group_steps.size() - 1).setVisibility(View.VISIBLE);
			current.setVisibility(View.VISIBLE);
			setThresholdText();

			break;
		case SOCIAL_CHALLENGE:
			secondSeekbar.setVisibility(View.INVISIBLE);
			current.setVisibility(View.VISIBLE);
			for (int i = 0; i < group_members.size(); i++) {
				group_members.get(i).setVisibility(View.VISIBLE);
				group_steps.get(i).setVisibility(View.VISIBLE);
				group_minus.get(i).setVisibility(View.VISIBLE);

			}

			break;
		case TEAM_VS_TEAM:
			secondSeekbar.setVisibility(View.VISIBLE);
			secondSeekbar.setProgressDrawable(getResources().getDrawable(R.drawable.progress_bar_red));
			seekbarIndicator.setProgressDrawable(getResources().getDrawable(R.drawable.progress_bar_green));
			current.setVisibility(View.VISIBLE);
			setThresholdText();
			for (int i = 0; i < 2; i++) {
				group_members.get(i).setVisibility(View.VISIBLE);
				group_steps.get(i).setVisibility(View.VISIBLE);
				group_minus.get(i).setVisibility(View.GONE);

			}
			for (int i = 2; i < group_members.size(); i++) {
				group_members.get(i).setVisibility(View.GONE);
				group_steps.get(i).setVisibility(View.GONE);
				group_minus.get(i).setVisibility(View.GONE);

			}

			break;
		case SOLO_CLIMB:
			secondSeekbar.setVisibility(View.GONE);
			current.setVisibility(View.GONE);
			for (int i = 0; i < group_members.size(); i++) {
				group_members.get(i).setVisibility(View.GONE);
				group_steps.get(i).setVisibility(View.GONE);
				group_minus.get(i).setVisibility(View.GONE);

			}

			break;

		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
			@Override
			public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
				Log.e("Activity", "Error: " + error.toString() + "\n" + error.getLocalizedMessage());
				error.printStackTrace();
			}

			@Override
			public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
				Log.i("Activity", "Success!");
			}
		});
	}

	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state, Exception exception) {
		}
	};

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_climb);
		
		//si recupera il contesto dell'applicazione
		appContext = ClimbActivity.this;
		//si ottiene il riferimento alle shared preferences
		settings = PreferenceManager.getDefaultSharedPreferences(appContext);
				
				
		setupActionBar();

		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);

		setInitialStar = false;

		// if (ClimbApplication.DEBUG) {
		// StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
		// .detectDiskReads()
		// .detectDiskWrites()
		// .detectNetwork() // or .detectAll() for all detectable problems
		// .penaltyLog()
		// .build());
		// StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
		// .detectLeakedSqlLiteObjects()
		// .detectLeakedClosableObjects()
		// .penaltyLog()
		// .penaltyDeath()
		// .build());
		// }

		pref = getSharedPreferences("UserSession", 0);
		SharedPreferences paused = getSharedPreferences("state", 0);
		Editor editor = paused.edit();
		editor.putBoolean("paused", false);
		editor.commit();
		currentUser = ClimbApplication.getUserById(pref.getInt("local_id", -1));
		final View controlsView = findViewById(R.id.fullscreen_content_controls);
		final View contentView = findViewById(R.id.lblReadyToClimb);
		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		// mSystemUiHider = SystemUiHider.getInstance(this, contentView,
		// HIDER_FLAGS);
		// mSystemUiHider.setup();
		// mSystemUiHider.setOnVisibilityChangeListener(new
		// SystemUiHider.OnVisibilityChangeListener() {
		// // Cached values.
		// int mControlsHeight;
		// int mShortAnimTime;
		//
		// @Override
		// @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
		// public void onVisibilityChange(boolean visible) {
		// if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
		// // If the ViewPropertyAnimator API is available
		// // (Honeycomb MR2 and later), use it to animate the
		// // in-layout UI controls at the bottom of the
		// // screen.
		// if (mControlsHeight == 0) {
		// mControlsHeight = controlsView.getHeight();
		// }
		// if (mShortAnimTime == 0) {
		// mShortAnimTime =
		// getResources().getInteger(android.R.integer.config_shortAnimTime);
		// }
		// controlsView.animate().translationY(visible ? 0 :
		// mControlsHeight).setDuration(mShortAnimTime);
		// } else {
		// // If the ViewPropertyAnimator APIs aren't
		// // available, simply show or hide the in-layout UI
		// // controls.
		// controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
		// }
		// if (visible && AUTO_HIDE) {
		// // Schedule a hide().
		// delayedHide(AUTO_HIDE_DELAY_MILLIS);
		// }
		// }
		// });
		// // Set up the user interaction to manually show or hide the system
		// UI.
		// // contentView.setOnClickListener(new View.OnClickListener() {
		// // @Override
		// // public void onClick(View view) {
		// // if (TOGGLE_ON_CLICK) {
		// // mSystemUiHider.toggle();
		// // } else {
		// // mSystemUiHider.show();
		// // }
		// // }
		// // });
		// mSystemUiHider.show();
		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		// findViewById(R.id.btnStartClimbing).setOnTouchListener(mDelayHideTouchListener);

		// app-specific logic
		seekbarIndicator = (VerticalSeekBar) findViewById(R.id.seekBarPosition); // get reference to vertical seekbar (only once for performance-related reasons)
		seekbarIndicator.setOnTouchListener(new OnTouchListener() { // disable user-driven seekbar changes
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return false;
			}
		});
		checkUserStats(); // user's stats about average daily steps
		chartHelpText = (TextView) findViewById(R.id.textHelp);
		chartHelpText.setVisibility(View.GONE);
		isCounterMode = getIntent().getBooleanExtra(ClimbApplication.counter_mode, false);
		if (!isCounterMode) { // game mode on
			int building_id = getIntent().getIntExtra(ClimbApplication.building_text_intent_object, 0); // get building id from received intent
			try {
				// get building text ID from intent
				if (building_id == 0)
					throw new Exception("ERROR: unable to get intent data"); // no building id found in received intent
				buildingText = ClimbApplication.buildingTextDao.queryForId(building_id);
				System.out.println("BUILDING_ID " + building_id);
				building = buildingText.getBuilding();
				
				setup_from_building(); // load building info
				backgroundClassifySampler = new Intent(this, SamplingClassifyService.class); // instance (without starting) background classifier
			} catch (Exception e) {
				
				e.printStackTrace();
			}
			setGraphicsSocialMode();
		} else { // game mode off (only engine to count user's steps)
			//LOG-------------------------------------------------
			//LINE
			String line = "CURRENT GAME SESSION: COUNTER MODE";
			LogUtils.writeGameUpdate(getApplicationContext(), line);
			//
			//---------------------------------------------------
			backgroundClassifySampler = new Intent(this, SamplingClassifyService.class);
			ImageView photo = ((ImageView) findViewById(R.id.buildingPhoto));
			TextView name = ((TextView) findViewById(R.id.lblBuildingName));
			TextView n_steps = ((TextView) findViewById(R.id.lblNumSteps));
			TextView height = ((TextView) findViewById(R.id.lblHeight));
			seekbarIndicator.setVisibility(View.INVISIBLE);
			// photo.setVisibility(View.INVISIBLE);
			photo.setImageResource(R.drawable.heart);
			photo.setScaleType(ScaleType.CENTER);
			name.setVisibility(View.INVISIBLE);
			n_steps.setVisibility(View.VISIBLE);
			height.setVisibility(View.INVISIBLE);
			n_steps.setText(String.valueOf(0));

			current = (TextView) findViewById(R.id.textPosition);
			current.setVisibility(View.INVISIBLE);
			for (int i = 1; i <= 6; i++) {//ClimbApplication.N_MEMBERS_PER_GROUP
				int idNome = getResources().getIdentifier("nome" + i, "id", getPackageName());
				int idPassi = getResources().getIdentifier("passi" + i, "id", getPackageName());
				int idMinus = getResources().getIdentifier("minus" + i, "id", getPackageName());
				group_members.add((TextView) findViewById(idNome));
				group_steps.add((TextView) findViewById(idPassi));
				group_minus.add((TextView) findViewById(idMinus));
				group_members.get(i - 1).setVisibility(View.INVISIBLE);
				group_steps.get(i - 1).setVisibility(View.INVISIBLE);
				group_minus.get(i - 1).setVisibility(View.INVISIBLE);
			}
			difficulty = 1; // 1rstep = 1 vstep

		}

	}

	@Override
	public void onStart() {
		super.onStart();
		if (pref.getBoolean("first_open_2", true)) {
			if (!pref.getBoolean("done_tutorial", false)) {
				Intent intent = new Intent(this, OnBoardingActivity.class);
				intent.putExtra("source", "ClimbActivity");
				startActivity(intent);
				pref.edit().putBoolean("first_open_2", false).commit();
			} else {
				pref.edit().putBoolean("first_open_2", false).commit();
			}
		}
	}

	protected void onNewIntent(Intent intent) {
		int building_id = intent.getIntExtra(ClimbApplication.building_text_intent_object, 0); // get
		// building
		// id
		// from
		// received
		// intent
		Log.i(MainActivity.AppName, "New intent building id: " + building_id);
	}

	/**
	 * Setup view with a given building and create/load an associated climbing
	 */
	private void setup_from_building() {
		int imageId = getApplicationContext().getResources().getIdentifier(building.getPhoto(), "drawable", getApplicationContext().getPackageName()); // get
																																						// building's
																																						// photo
																																						// resource
																																						// ID
		if (imageId > 0){
			
			DisplayImageOptions options = new DisplayImageOptions.Builder()
			.showImageForEmptyUri(R.drawable.ic_action_help_dark)
			.showImageOnFail(R.drawable.ic_action_cancel)
			.resetViewBeforeLoading(true)
			.imageScaleType(ImageScaleType.EXACTLY)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.displayer(new FadeInBitmapDisplayer(300))
			.cacheInMemory(true)
			.cacheOnDisc(true)
			.build();
			
			ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).threadPoolSize(3).defaultDisplayImageOptions(options).build();
			ImageLoader.getInstance().init(config);
			
			ImageLoader.getInstance().displayImage("drawable://"+imageId, ((ImageView)findViewById(R.id.buildingPhoto)), options, new ImageLoadingListener() {
				
				@Override
				public void onLoadingStarted(String arg0, View arg1) {
					
				}
				
				@Override
				public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
					
				}
				
				@Override
				public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
										
				}
				
				@Override
				public void onLoadingCancelled(String arg0, View arg1) {
										
				}
			});
		
		}
		// set building info
		((TextView) findViewById(R.id.lblBuildingName)).setText(buildingText.getName() + "\n (" + buildingText.getLocation() + ")"); // building's
		// location
		((TextView) findViewById(R.id.lblNumSteps)).setText(getString(R.string.num_steps, Integer.toString(building.getSteps()))); // building's
		// steps
		((TextView) findViewById(R.id.lblHeight)).setText(Integer.toString(building.getHeight()) + "mt"); // building's height (in mt)
		loadPreviousClimbing(); // get previous climbing for this building
		mode = GameModeType.values()[climbing.getGame_mode()]; // setup game mode
		old_game_mode = mode;
		// loadSocialMode();
		new LoadSocialModeTask().execute();

	}

	static boolean in_progress = true;

	private class LoadSocialModeTask extends AsyncTask<Void, Void, Void> {
		ProgressDialog dialog;

		public LoadSocialModeTask() {
			dialog = new ProgressDialog(ClimbActivity.this);
		}

		protected void onPreExecute() {
			dialog.setTitle(getString(R.string.title_climb_dialog));
			dialog.setMessage(getString(R.string.message_climb_dialog));
			dialog.setCancelable(false);
			dialog.show();
		}

		protected Void doInBackground(Void... urls) {
			loadSocialMode();
			synchronized (ClimbApplication.lock) {
				while (in_progress) {
					try {
						ClimbApplication.lock.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}

			return null;
		}

		@Override
		protected void onPostExecute(Void success) {
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
			//AUTOMATIC UPDATE
			if(climbing.getGame_mode() != 0)
				timer.schedule(updates, 3000, 5000); //execute in every 10 ms
		
		}
	}

	/**
	 * Load the object with info about the current social mode
	 */
	private void loadSocialMode() {
		switch (mode) {
		case SOCIAL_CHALLENGE:
			//LOG-------------------------------------------------
			//LINE
			String line = "CURRENT SESSION GAME MODE: SOCIAL CHALLENGE";
			LogUtils.writeGameUpdate(getApplicationContext(), line);
			//
			//---------------------------------------------------
			mode = GameModeType.SOCIAL_CHALLENGE;
			loadCompetition();
			break;
		case SOCIAL_CLIMB:
			//LOG-------------------------------------------------
			//LINE
			line = "CURRENT SESSION GAME MODE: SOCIAL CLIMB";
			LogUtils.writeGameUpdate(getApplicationContext(), line);
			//
			//---------------------------------------------------
			mode = GameModeType.SOCIAL_CLIMB;
			loadCollaboration();
			break;
		case TEAM_VS_TEAM:
			//LOG-------------------------------------------------
			//LINE
			line = "CURRENT SESSION GAME MODE: TEAM VS TEAM";
			LogUtils.writeGameUpdate(getApplicationContext(), line);
			//
			//---------------------------------------------------
			mode = GameModeType.TEAM_VS_TEAM;
			loadTeamDuel();
			break;
		case SOLO_CLIMB:
			if(percentage < 1.0){
				//LOG-------------------------------------------------
				//LINE
				line = "CURRENT SESSION GAME MODE: SOLO CLIMB";
				LogUtils.writeGameUpdate(getApplicationContext(), line);
				//
				//---------------------------------------------------
			}
			synchronized (ClimbApplication.lock) {
				in_progress = false;
				ClimbApplication.lock.notifyAll();
			}
			break;
		}
	}

	/**
	 * Loads the Collaboration object corresponding to the current climbing
	 */
	private void loadCollaboration() {
		collaboration = ClimbApplication.getCollaborationById(climbing.getId_mode());// MainActivity.getCollaborationForBuilding(building.get_id());
		others_steps = new HashMap<String, Integer>();
		if (collaboration == null) {
			Toast.makeText(this, getString(R.string.no_collaboration), Toast.LENGTH_SHORT).show();
			synchronized (ClimbApplication.lock) {
				in_progress = false;
				ClimbApplication.lock.notifyAll();
			}

		} else {
			collaboration.setMy_stairs(climbing.getCompleted_steps());
			updateOthers(false, true);
		}

	}

	/**
	 * Loads the TeamDuel object corresponding to the current climbing
	 */
	private void loadTeamDuel() {
		teamDuel = ClimbApplication.getTeamDuelById(climbing.getId_mode());
		myTeamScores = new ArrayList<Integer>();
		if (teamDuel == null) {
			Toast.makeText(this, getString(R.string.no_team_duel), Toast.LENGTH_SHORT).show();
			synchronized (ClimbApplication.lock) {
				in_progress = false;
				ClimbApplication.lock.notifyAll();
			}
		} else {
			teamDuel.setMy_steps(climbing.getCompleted_steps());
			teamDuel.setSteps_my_group(teamDuel.getSteps_my_group() + climbing.getCompleted_steps());
			updateTeams(false, true);
		}
	}

	/**
	 * Calculates the score (number of steps) done by the team of the current user
	 * 
	 * @return number of steps done by the team of the current user
	 */
	private int myTeamScore() {
		int scoreTot = 0;
		for (Integer score : myTeamScores)
			scoreTot += score;
		return scoreTot + num_steps;
	}

	/**
	 * Loads the Competition object corresponding to the current climbing
	 */
	private void loadCompetition() {
		competition = ClimbApplication.getCompetitionById(climbing.getId_mode());// MainActivity.getCompetitionByBuilding(building.get_id());
		if (competition == null) {
			Toast.makeText(this, getString(R.string.no_competition), Toast.LENGTH_SHORT).show();
			synchronized (ClimbApplication.lock) {
				in_progress = false;
				ClimbApplication.lock.notifyAll();
			}
		} else {
			competition.setMy_stairs(climbing.getCompleted_steps());
			updateChart(false, true);
		}
	}

	/**
	 * Update graphics to let user see the latest update about my friends' steps.
	 */
	private void updateOthers(final boolean isUpdate, final boolean isOpening) {
		Log.i("ClimbActivity", "updateothers");
		if (!(mode == GameModeType.SOLO_CLIMB) && FacebookUtils.isOnline(this)) { // If is online
			// look for current collaboration in Parse
			ParseQuery<ParseObject> query = ParseQuery.getQuery("Collaboration");
			query.whereEqualTo("objectId", collaboration.getId());
			query.findInBackground(new FindCallback<ParseObject>() {

				@Override
				public void done(List<ParseObject> collabs, ParseException e) {
					if (e == null) { // if everything has gone ok
						if (collabs == null || collabs.size() == 0) {
							// no collaboration found
							showMessage(getString(R.string.collab_no_available));
							Log.e("loadCollaboration", "Collaboration " + collaboration.getId() + " not present in Parse");
							timer.cancel();
						} else {

							// collaboration found
							SharedPreferences pref = getApplicationContext().getSharedPreferences("UserSession", 0);
							collab_parse = collabs.get(0);

							JSONObject creator = collab_parse.getJSONObject("creator");
							Iterator<String> it_creator = creator.keys();
							String creator_fbid = it_creator.next();
							if (creator_fbid.equalsIgnoreCase(pref.getString("FBid", ""))) {
								if (!collaboration.getAmICreator()) {
									collaboration.setAmICreator(true);
									ClimbApplication.collaborationDao.update(collaboration);
								}
							} else {
								if (collaboration.getAmICreator()) {
									collaboration.setAmICreator(false);
									ClimbApplication.collaborationDao.update(collaboration);
								}
							}

							JSONObject others = collab_parse.getJSONObject("stairs");
							JSONObject othersName = collab_parse.getJSONObject("collaborators");

							if (othersName.has(currentUser.getFBid())) {

								try {
									String fbid = pref.getString("FBid", "");
									if(!fbid.equalsIgnoreCase("none") && !fbid.equalsIgnoreCase("empty")) others.put(fbid, num_steps);
								} catch (JSONException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								collab_parse.put("stairs", others);
								collaboration.setMy_stairs(num_steps);
								collaboration.setOthers_stairs(ModelsUtil.sum(others) - num_steps);
								// compet_parse.saveEventually();
								ParseUtils.saveCollaboration(collab_parse, collaboration);

								Iterator members = others.keys();
								int i = 0;
								while (members.hasNext()) {
									String key = (String) members.next();
									if (!key.equalsIgnoreCase(pref.getString("FBid", ""))) {
										String name = "";
										int steps = -1;
										try {
											name = (String) othersName.getString(key);
											steps = (Integer) others.getInt(key);
										} catch (JSONException e2) {
											// TODO Auto-generated catch block
											e2.printStackTrace();
										}
										group_members.get(i).setText(name);
										others_steps.put(key, steps);
										group_steps.get(i).setVisibility(View.VISIBLE);
										group_steps.get(i).setText(String.valueOf(steps));
										group_members.get(i).setClickable(false);
										group_members.get(i).setVisibility(View.VISIBLE);
										group_members.get(i).setTextAppearance(getApplicationContext(), R.style.chartNormal);
										group_members.get(i).setBackgroundColor(Color.parseColor("#dcdcdc"));
										if (collaboration.getAmICreator()) {
											final Integer idx = new Integer(i);
											final ChartMember member = new ChartMember(key, steps);
											group_minus.get(i).setVisibility(View.VISIBLE);
											group_minus.get(i).setClickable(true);
											group_minus.get(i).setOnClickListener(new OnClickListener() {

												@Override
												public void onClick(View arg0) {
													if (FacebookUtils.isOnline(getApplicationContext())) {
														removeFromCollaboration(member);
														group_members.get(idx).setVisibility(View.GONE);
														group_steps.get(idx).setVisibility(View.GONE);
														group_minus.get(idx).setVisibility(View.GONE);
													} else {
														showMessage(getString(R.string.connection_problem));
													}

												}
											});

										} else {
											group_minus.get(i).setVisibility(View.GONE);
										}
										i++;
									}
								}
								if (i < group_members.size() && i <= ClimbApplication.N_MEMBERS_PER_GROUP) {
									group_steps.get(i).setVisibility(View.INVISIBLE);
									group_minus.get(i).setVisibility(View.INVISIBLE);
									group_members.get(i).setClickable(true);
									group_members.get(i).setText("  +");
									group_members.get(i).setVisibility(View.VISIBLE);
									group_members.get(i).setBackgroundColor(getResources().getColor(R.color.light_teal));
									group_members.get(i).setTextAppearance(getApplicationContext(), R.style.chartButton);
									group_members.get(i).setOnClickListener(new OnClickListener() {

										@Override
										public void onClick(View arg0) {
											Bundle params = new Bundle();
											params.putString("data", "{\"idCollab\":\"" + collaboration.getId() + "\"," + "\"idBuilding\":\"" + building.get_id() + "\"," + "\"nameBuilding\":\""
													+ building.getName() + "\", \"type\": \"1\"}");
											params.putString("message", "Please, help me!!!!");
											if (Session.getActiveSession() != null && Session.getActiveSession().isOpened()) {

												WebDialog requestsDialog = (new WebDialog.RequestsDialogBuilder(ClimbActivity.this, Session.getActiveSession(), params)).setOnCompleteListener(new OnCompleteListener() {

													@Override
													public void onComplete(Bundle values, FacebookException error) {
														if (error != null) {
															if (error instanceof FacebookOperationCanceledException) {
																showMessage(getString(R.string.request_cancelled));
															} else {
																showMessage(getString(R.string.network_error));
															}
														} else {
															final String requestId = values.getString("request");
															if (requestId != null) {
																showMessage(getString(R.string.request_sent));
															} else {
																showMessage(getString(R.string.request_cancelled));

															}
														}
													}

												}).build();
												requestsDialog.show();
											} else {
												showMessage(getString(R.string.not_logged));
											}
										}
									});
									i++;
								}
								int new_seekbar_progress = num_steps + sumOthersStep();
								seekbarIndicator.setProgress(new_seekbar_progress);
								double progress = ((double) (new_seekbar_progress + (microgoal.getTot_steps() - microgoal.getDone_steps())) * (double) 100) / (double) (building.getSteps());
								//seekbarIndicator.nextStar((int) Math.round(progress));
								for (; i < group_members.size(); i++) {
									group_members.get(i).setClickable(false);
									group_members.get(i).setVisibility(View.INVISIBLE);
									group_steps.get(i).setVisibility(View.INVISIBLE);
									group_minus.get(i).setVisibility(View.INVISIBLE);
								}

								if ((num_steps + sumOthersStep() >= building.getSteps()) && (num_steps < threshold)) {
									// if I and my friend have finished climbing
									// this building, but I didn't pass the
									// threshold
									current_win = true;
									timer.cancel();
									socialPenalty();
									if (microgoal != null && (isUpdate || isOpening))
										deleteMicrogoalInParse(microgoal);

								} else if ((num_steps + sumOthersStep() >= building.getSteps()) && (num_steps >= threshold)) {
									percentage = 1.0;
									current_win = true;
									System.out.println("ho vinto");
									if (isUpdate || isOpening) {
										if (microgoal != null)
											deleteMicrogoalInParse(microgoal);
										updatePoints(false, true);
										// saveBadges(true);
										saveCollaborationData();
									}
									timer.cancel();
								}

							} else {
								showMessage(getString(R.string.kicked_out));
								timer.cancel();
								ClimbApplication.collaborationDao.delete(collaboration);
								apply_removed_from_collaboration();
								// reset graphics
								current.setVisibility(View.GONE);
								for (int i = 0; i < group_members.size(); i++) {
									group_members.get(i).setVisibility(View.GONE);
									group_steps.get(i).setVisibility(View.GONE);
									group_minus.get(i).setVisibility(View.GONE);

								}
							}
						}

					} else if (!(mode == GameModeType.SOCIAL_CLIMB)) {
						showMessage(getString(R.string.connection_problem));
						showOfflineSocialMember();
						Log.e("loadCollaboration", e.getMessage());
					}
					if (isUpdate) {
						//resetUpdating();
					}

					synchronized (ClimbApplication.lock) {
						in_progress = false;
						ClimbApplication.lock.notifyAll();
					}
				}
			});

		} else {
			showMessage(getString(R.string.check_connection));
			showOfflineSocialMember();
			if (isUpdate) {
				//resetUpdating();
			}
			synchronized (ClimbApplication.lock) {
				in_progress = false;
				ClimbApplication.lock.notifyAll();
			}
		}

	}

	private int sumOthersStep() {
		int sum = 0;
		Set<String> keys = others_steps.keySet();
		for (String key : keys) {
			sum += others_steps.get(key);
		}
		return sum;
	}

	/**
	 * Saves the new Climbing object in Parse
	 */
	private void saveClimbingToParse(final Climbing climbing) {
		SharedPreferences pref = getApplicationContext().getSharedPreferences("UserSession", 0);
		// save climbing to parse
		if (!pref.getString("FBid", "none").equals("none")) {
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			df.setTimeZone(new SimpleTimeZone(0, "GMT"));
			final ParseObject climb = new ParseObject("Climbing");
			climb.put("building", climbing.getBuilding().get_id());
			if (climbing.getId_mode() != null)
				climb.put("id_mode", climbing.getId_mode());
			else
				climb.put("id_mode", "");

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
			climb.put("checked", climbing.isChecked());
			climb.saveInBackground(new SaveCallback() {

				@Override
				public void done(ParseException e) {
					if (e == null) {
						climbing.setId_online(climb.getObjectId());
						climbing.setSaved(true);
						ClimbApplication.climbingDao.update(climbing);
					} else {
						climbing.setSaved(false);
						ClimbApplication.climbingDao.update(climbing);
						// Toast.makeText(getApplicationContext(),
						// getString(R.string.connection_problem2),
						// Toast.LENGTH_SHORT).show();
					}

				}
			});
		}
	}

	private void deleteClimbingInParse(final Climbing climbing) {
		// if (FacebookUtils.isOnline(this)) {
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Climbing");
		query.whereEqualTo("users_id", climbing.getUser().getFBid());
		query.whereEqualTo("building", climbing.getBuilding().get_id());
		query.whereEqualTo("id_mode", climbing.getId_mode());
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> climbs, ParseException e) {
				if (e == null) {
					System.out.println("climb size " + climbs.size());
					if (climbs.size() != 0) {
						ParseUtils.deleteClimbing(climbs.get(0), climbing);
						// climbs.get(0).deleteEventually();
						// ClimbApplication.climbingDao.delete(climbing);
					}
				} else {
					climbing.setSaved(false);
					climbing.setDeleted(true);
					ClimbApplication.climbingDao.update(climbing);
					Toast.makeText(getApplicationContext(), getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
					Log.e("updateClimbingInParse", e.getMessage());
				}
			}

		});
		// }
	}

	/**
	 * Update the given Climbing object in Parse
	 * 
	 * @param myclimbing
	 *            the given Climbing object
	 * 
	 */
	private void updateClimbingInParse(final Climbing myclimbing, boolean paused) {
		Log.i("ClimbActivity", "updateClimbingInParse");
		final SharedPreferences pref = getApplicationContext().getSharedPreferences("UserSession", 0);
		if (FacebookUtils.isOnline(this)) {
			ParseQuery<ParseObject> query = ParseQuery.getQuery("Climbing");
			/*
			 * query.whereEqualTo("users_id", myclimbing.getUser().getFBid()); query.whereEqualTo("building", myclimbing.getBuilding().get_id()); if(!paused) query.whereEqualTo("id_mode", myclimbing.getId_mode()); else query.whereEqualTo("id_mode", "paused");
			 * 
			 * System.out.println("CERCO: " + myclimbing.getId_mode());
			 */
			System.out.println(myclimbing.getId_online() == null);
			if (!(myclimbing.getId_online() == null) && !myclimbing.getId_online().equalsIgnoreCase(""))
				query.whereEqualTo("objectId", myclimbing.getId_online());
			else {
				query.whereEqualTo("users_id", myclimbing.getUser().getFBid());
				query.whereEqualTo("building", myclimbing.getBuilding().get_id());
				query.whereEqualTo("game_mode", myclimbing.getGame_mode());
			}
			query.findInBackground(new FindCallback<ParseObject>() {

				@Override
				public void done(List<ParseObject> climbs, ParseException e) {
					if (e == null && climbs.size() > 0) {
						ParseObject c = climbs.get(0);
						DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
						df.setTimeZone(new SimpleTimeZone(0, "GMT"));
						try {
							c.put("created", df.parse(df.format(myclimbing.getCreated())));
							c.put("modified", df.parse(df.format(myclimbing.getModified())));
							c.put("completedAt", df.parse(df.format(myclimbing.getCompleted())));
						} catch (java.text.ParseException ex) {
							// TODO Auto-generated catch block
							ex.printStackTrace();
						}
						c.put("completed_steps", myclimbing.getCompleted_steps());
						c.put("remaining_steps", myclimbing.getRemaining_steps());
						c.put("percentage", String.valueOf(myclimbing.getPercentage()));
						if (myclimbing.getId_mode() == null)
							c.put("id_mode", "");
						else
							c.put("id_mode", myclimbing.getId_mode());
						c.put("game_mode", myclimbing.getGame_mode());
						c.put("checked", myclimbing.isChecked());
						// c.saveEventually();
						ParseUtils.saveClimbing(c, myclimbing);
						// myclimbing.setSaved(true);
						// ClimbApplication.climbingDao.update(myclimbing);
					} else {
						myclimbing.setSaved(false);
						ClimbApplication.climbingDao.update(myclimbing);
						Toast.makeText(getApplicationContext(), getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
						if(e!=null)Log.e("updateClimbingInParse", e.getMessage());
						else Log.e("updateClimbingInParse", "climbs.size = 0");
					}
				}
			});
		} else {
			myclimbing.setSaved(false);
			ClimbApplication.climbingDao.update(myclimbing);
			// Toast.makeText(getApplicationContext(),
			// getString(R.string.connection_problem2), Toast.LENGTH_SHORT)
			// .show();

		}
	}

	private void saveMicrogoalInParse() {
		ParseObject mg = new ParseObject("Microgoal");
		mg.put("user_id", microgoal.getUser().getFBid());
		mg.put("story_id", microgoal.getStory_id());
		mg.put("building", microgoal.getBuilding().get_id());
		mg.put("tot_steps", microgoal.getTot_steps());
		mg.put("done_steps", microgoal.getDone_steps());
		// mg.saveEventually();
		// microgoal.setSaved(true);
		// ClimbApplication.microgoalDao.update(microgoal);
		ParseUtils.saveMicrogoal(mg, microgoal);
	}

	private void deleteMicrogoalInParse(final Microgoal microgoal) {
		Log.i("CLimbActivity", "deleteMicrogoalInParse");
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Microgoal");
		query.whereEqualTo("user_id", microgoal.getUser().getFBid());
		query.whereEqualTo("building", microgoal.getBuilding().get_id());
		query.getFirstInBackground(new GetCallback<ParseObject>() {

			@Override
			public void done(ParseObject m, ParseException e) {
				if (e == null) {
					if (m != null)
						// m.deleteEventually();
						ParseUtils.deleteMicrogoal(m, microgoal);
					else
						ClimbApplication.microgoalDao.delete(microgoal);
					// System.out.println("parse");
					// System.out.println("% " + percentage);
					// System.out.println("fine???? " + current_win);
					// if(percentage < 1.00 && !current_win) createMicrogoal();

				} else {
					if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
						ClimbApplication.microgoalDao.delete(microgoal);
					} else {
						microgoal.setDeleted(true);
						microgoal.setSaved(false);
						ClimbApplication.microgoalDao.update(microgoal);
					}
					// if(percentage < 1.00 && !current_win) createMicrogoal();
					// Toast.makeText(getApplicationContext(),
					// getString(R.string.connection_problem2),
					// Toast.LENGTH_SHORT).show();
					Log.e("delete microgoal", e.getMessage());
				}

			}
		});
	}

	private void updateMicrogoalInParse() {
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Microgoal");
		query.whereEqualTo("user_id", microgoal.getUser().getFBid());
		query.whereEqualTo("building", microgoal.getBuilding().get_id());
		query.getFirstInBackground(new GetCallback<ParseObject>() {

			@Override
			public void done(ParseObject m, ParseException e) {
				if (e == null) {
					m.put("tot_steps", microgoal.getTot_steps());
					m.put("done_steps", microgoal.getDone_steps());
					ParseUtils.saveMicrogoal(m, microgoal);
					// m.saveEventually();
					// microgoal.setSaved(true);
					// ClimbApplication.microgoalDao.update(microgoal);
				} else {
					microgoal.setSaved(false);
					ClimbApplication.microgoalDao.update(microgoal);
					// Toast.makeText(getApplicationContext(),
					// getString(R.string.connection_problem2),
					// Toast.LENGTH_SHORT).show();
					Log.e("update microgoal", e.getMessage());
				}

			}

		});
	}

	private Microgoal createMicrogoal() {
		Log.i("ClimbActivity", "createMicrogoal");
		if (difficulty == 0) {
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			difficulty = Integer.parseInt(settings.getString("difficulty", "10"));
		}
		Microgoal old_microgoal = microgoal;
		updateUserStats(true);
		// double newCurrentMean = ClimbApplication.calculateNewMean((long) currentUser.getMean(), currentUser.getN_measured_days(), (currentUser.getCurrent_steps_value()));
		// int tot_steps = ClimbApplication.generateStepsToDo(climbing.getRemaining_steps(), /* currentUser.getMean() */newCurrentMean, difficulty);
		int tot_steps = ClimbApplication.generateStepsToDo(building.getSteps(), num_steps);
		int story_id;
		try {
			User me = ClimbApplication.getUserById(pref.getInt("local_id", -1));
			story_id = ClimbApplication.getRandomStoryId();
			microgoal = new Microgoal();
			microgoal.setUser(ClimbApplication.getUserById(pref.getInt("local_id", -1)));
			microgoal.setBuilding(building);
			microgoal.setDeleted(false);
			microgoal.setDone_steps(0);
			microgoal.setTot_steps(tot_steps);
			microgoal.setStory_id(story_id);
			microgoal.setReward(5);
			double progress = ((double) (tot_steps + num_steps) * (double) 100) / (double) building.getSteps();
			//seekbarIndicator.nextStar((int) Math.round(progress));
			// if (!pref.getString("FBid", "none").equalsIgnoreCase("none") && !pref.getString("FBid", "none").equalsIgnoreCase("empty"))
			if (!me.getFBid().equalsIgnoreCase("empty") && !me.getFBid().equalsIgnoreCase("none")) {
				microgoal.setSaved(false);
				ClimbApplication.microgoalDao.create(microgoal);
				saveMicrogoalInParse();
			} else {
				microgoal.setSaved(true);
				ClimbApplication.microgoalDao.create(microgoal);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return old_microgoal;
	}

	/**
	 * Check (and load) if a climbing exists for the given building
	 * 
	 * @throws ClimbingNotFound
	 */
	private void loadPreviousClimbing() {
		SharedPreferences pref = getApplicationContext().getSharedPreferences("UserSession", 0);

		List<Climbing> climbs = ClimbApplication.getClimbingListForBuildingAndUser(building.get_id(), pref.getInt("local_id", -1));
		System.out.println(pref.getInt("local_id", -1));
		System.out.println("climbs "+ climbs.size());
		if (climbs.size() == 1) {
			climbing = climbs.get(0);
		} else if (climbs.size() == 2) {
			for (Climbing c : climbs) {
				if (c.getGame_mode() == 0)
					soloClimb = c;
				else if (c.getGame_mode() == 2 || c.getGame_mode() == 3)
					climbing = c;
			}
		}

		if (climbing == null) { // no climbing found
			Log.i(MainActivity.AppName, "No previous climbing found");
			num_steps = 0;
			percentage = 0;
			climbing = new Climbing(); // create a new empty climbing for this
										// building
			climbing.setBuilding(building);
			climbing.setCompleted(0);
			climbing.setRemaining_steps(building.getSteps());
			climbing.setCompleted_steps(num_steps);
			climbing.setCreated(new Date().getTime());
			climbing.setModified(new Date().getTime());
			climbing.setGame_mode(0);
			String FBid = pref.getString("FBid", ""); System.out.println("FBid " + pref.getString("FBid", ""));
			if (FBid.equals("") || FBid.equals("none") || FBid.equalsIgnoreCase("empty"))
				climbing.setUser(ClimbApplication.getUserById(pref.getInt("local_id", -1)));
			else
				climbing.setUser(ClimbApplication.getUserByFBId(FBid));

			ClimbApplication.climbingDao.create(climbing);

			if (!pref.getString("FBid", "none").equalsIgnoreCase("none") && !pref.getString("FBid", "none").equalsIgnoreCase("empty"))
				saveClimbingToParse(climbing);
			else {
				climbing.setSaved(true);
				ClimbApplication.climbingDao.update(climbing);
			}

			Log.i(MainActivity.AppName, "Created new climbing #" + climbing.get_id());
			// current_win = false;
			if (percentage < 1.00 && !current_win)
				createMicrogoal();

		} else {
			num_steps = climbing.getCompleted_steps();
			percentage = climbing.getPercentage();
			if (percentage >= 1.00)
				current_win = true;
			microgoal = ClimbApplication.getMicrogoalByUserAndBuilding(pref.getInt("local_id", -1), building.get_id());
			if (microgoal == null && percentage < 1.00 && !current_win)
				createMicrogoal();

			Log.i(MainActivity.AppName, "Loaded existing climbing (#" + climbing.get_id() + ")");
		}
		climbing.addObserver(this);
		threshold = building.getSteps() / ClimbApplication.N_MEMBERS_PER_GROUP;
		seekbarIndicator.setMax(building.getSteps());
		if (mode == GameModeType.SOCIAL_CLIMB)
			seekbarIndicator.setProgress(climbing.getCompleted_steps() + sumOthersStep());
		else if (mode == GameModeType.TEAM_VS_TEAM) {
			seekbarIndicator.setProgress(climbing.getCompleted_steps() + myTeamScore());
			secondSeekbar.setMax(building.getSteps());
		} else
			seekbarIndicator.setProgress(climbing.getCompleted_steps());

		// per le competizioni setta la seconda seek bar

		updateStats();
		if (percentage >= 1.00) { // building already climbed
			findViewById(R.id.lblReadyToClimb).setVisibility(View.GONE);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
			((TextView) findViewById(R.id.lblWin)).setText(getString(R.string.already_climb, sdf.format(new Date(climbing.getCompleted()))));
			apply_win();
		} else { // building to be completed
			// animate "ready to climb" text
			Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_slide_in_top);
			Animation arrowAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.arrow);
			anim.setDuration(2500);
			findViewById(R.id.lblReadyToClimb).startAnimation(anim);
			findViewById(R.id.imgArrow).startAnimation(arrowAnim);
			findViewById(R.id.lblReadyToClimb).setVisibility(View.VISIBLE);
			
			

		}
	}
	
	final Handler handler = new Handler();
    Timer timer = new Timer();
    TimerTask updates = new TimerTask() {       
        @Override
        public void run() {
            handler.post(new Runnable() {
                public void run() {       
                    try {
                    	switch (climbing.getGame_mode()) {
						case 1:
							updateOthers(true, false);
							break;
						case 2:
							updateChart(true, false);
							break;
						case 3:
							updateTeams(true, false);
							break;
						}
                        updateTeams(true, false);                     
                        //yout method here
                    } catch (Exception e) {
                    }
                }
            });
        }
    };
    
   
    

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
//		if (!setInitialStar) {
//			seekbarIndicator.setTotalHeight();
//			if (microgoal != null) {
//				double progress = ((double) ((microgoal.getTot_steps() - microgoal.getDone_steps()) + climbing.getCompleted_steps()) * (double) 100) / (double) building.getSteps();
//				seekbarIndicator.nextStar((int) Math.round(progress));
//			}
//			setInitialStar = true;
//		}
		//int unit = (int) Math.floor(((double )building.getSteps() / (double)100) * 25);
		//seekbarIndicator.setInitialGoldenStars(num_steps/unit);
		
		//calculate interval 
		if(!isCounterMode){ System.out.println("gioco");
				int percentage = 5;
				if(building.getSteps() < 6000)
					percentage = 25;
				else if(building.getSteps() >= 6000 && building.getSteps() <= 10000 )
					percentage = 15;
				
				
				int unit = (int) Math.ceil((double) (building.getSteps()*percentage)/ (double) 100);
				double perc_unit = Math.ceil(((double) (unit*100)/ (double) building.getSteps()));
				int final_pos = (int) Math.floor(((double) (num_steps)/ (double) unit));
				if(num_steps == building.getSteps() && final_pos < 4) final_pos += 1;
				seekbarIndicator.setInitialGoldenStars(final_pos, perc_unit);
		}
		
		super.onWindowFocusChanged(hasFocus);
	}

	private void checkUserStats() {
		if (currentUser.getBegin_date() == null || currentUser.getBegin_date().equals("")) {
			currentUser.setBegin_date(String.valueOf((new Date()).getTime()));
			currentUser.setCurrent_steps_value(0);
			currentUser.setMean(0);
			currentUser.setN_measured_days(0);
			ClimbApplication.userDao.update(currentUser);
			ParseUser user = ParseUser.getCurrentUser();
			if (user != null) {
				JSONObject stats = new JSONObject();
				try {
					stats.put("begin_date", currentUser.getBegin_date());
					stats.put("mean", 0);
					stats.put("current_value", 0);
					stats.put("n_days", 1);
					user.put("mean_daily_steps", stats);
					user.put("height", currentUser.getHeight());
					// user.saveEventually();
					ParseUtils.saveUserInParse(user);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// Show the Up button in the action bar.
			// getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	private final boolean shouldUpRecreateTask(Activity from) {
		String action = from.getIntent().getAction();
		return action != null && action.equals("back");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.itemHelp:
			// Get width in dp
			DisplayMetrics metrics = new DisplayMetrics();
			Display display = getWindowManager().getDefaultDisplay();
			display.getMetrics(metrics);
			float logicalDensity = metrics.density;
			int dp = (int) (display.getWidth() / logicalDensity + 0.5);
			int n_icons = 2;
			if (dp < 360) { // only two icons
				n_icons = 2;
			} else {
				n_icons = 3;
			}
			boolean new_steps_done = new_steps != 0 ? true : false;
			HelpDialogActivity dialog = new HelpDialogActivity(this, R.style.Transparent, mode, percentage, new_steps_done, n_icons, samplingEnabled, isCounterMode);
			dialog.show();
			return true;
		case R.id.itemMicroGoal:
			onMicroGoalClicked();
			return true;
//		case R.id.itemUpdate:
//			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//			ImageView iv = (ImageView) inflater.inflate(R.layout.refresh, null);
//			Animation rotation = AnimationUtils.loadAnimation(this, R.anim.rotate_refresh);
//			rotation.setRepeatCount(Animation.INFINITE);
//			iv.startAnimation(rotation);
//			MenuItemCompat.setActionView(item, iv);
//			onUpdate();
//			return true;
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			// TODO: If Settings has multiple levels, Up should navigate up
			// that hierarchy.
			// NavUtils.navigateUpFromSameTask(this);
			if (samplingEnabled == false) {
				// finish();
				// SharedPreferences paused = getSharedPreferences("state", 0);
				//
				// Intent upIntent = NavUtils.getParentActivityIntent(this);
				// if ((NavUtils.shouldUpRecreateTask(this, upIntent)) || paused.getBoolean("paused", true)) {
				// System.out.println("ricrea");
				// Editor editor = paused.edit();
				// editor.putBoolean("paused", false);
				// editor.commit();
				// TaskStackBuilder.create(this).addNextIntentWithParentStack(upIntent).startActivities();
				// } else {
				// System.out.println("torna su");
				// // fillUpIntentWithExtras(upIntent);
				// NavUtils.navigateUpTo(this, upIntent);
				// }
				NavUtils.navigateUpFromSameTask(this);
				finish();
			} else { // disable back button if sampling is enabled
				Toast.makeText(getApplicationContext(), getString(R.string.sampling_enabled), Toast.LENGTH_SHORT).show();
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the system UI. This is to prevent the jarring behavior of controls going away while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};
	Handler mHideHandler = new Handler();

	// Runnable mHideRunnable = new Runnable() {
	// @Override
	// public void run() {
	// mSystemUiHider.hide();
	// }
	// };

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		// mHideHandler.removeCallbacks(mHideRunnable);
		// mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}

	/**
	 * onClick listener for starting/stopping classifier
	 * 
	 * @param v
	 */
	public void onBtnStartClimbing(View v) {

		if (percentage >= 1.00 || (current_win && percentage < 1.00)) { // already win
			if (FacebookUtils.isOnline(getApplicationContext())) {
				if (FacebookUtils.isLoggedIn()) {
					if (FacebookDialog.canPresentOpenGraphActionDialog(getApplicationContext(), FacebookDialog.OpenGraphActionDialogFeature.OG_ACTION_DIALOG)) {

						FacebookDialog shareDialog = null;
						boolean win = percentage >= 1.00 ? true : false;

						switch (old_game_mode) {
						case SOLO_CLIMB:
							shareDialog = FacebookUtils.publishOpenGraphStory_SoloClimb(this, win, previous_progress, buildingText.getName(), building.getSteps());
							break;
						case SOCIAL_CLIMB:
							shareDialog = FacebookUtils.publishOpenGraphStory_SocialClimb(this, collab_parse.getJSONObject("collaborators"), win, previous_progress, buildingText.getName());
							break;

						case SOCIAL_CHALLENGE:
							shareDialog = FacebookUtils.publishOpenGraphStory_SocialChallenge(this, chart, win, previous_progress, buildingText.getName(), old_chart_position);
							break;

						case TEAM_VS_TEAM:
							int new_position = 0;
							if (teamDuel.getSteps_my_group() >= teamDuel.getSteps_other_group())
								new_position = 0;
							else
								new_position = 1;
							shareDialog = FacebookUtils.publishOpenGraphStory_TeamVsTeam(this, teamDuel.getMygroup(), teamDuel_parse.getJSONObject("creator_stairs"), teamDuel_parse.getJSONObject("challenger_stairs"), win, previous_progress, buildingText.getName(), old_chart_position, new_position);
							break;
						}

						uiHelper.trackPendingDialogCall(shareDialog.present());

					} else {
						FacebookUtils fb = new FacebookUtils(this);
						try {
							fb.postToWall(climbing, buildingText.getName());
						} catch (NoFBSession e) {
							Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
							intent.putExtra("need_help", true);
							startActivity(intent);
						}

					}
				} else {
					Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
					intent.putExtra("need_help", true);
					startActivity(intent);
				}
			} else {
				Toast.makeText(getApplicationContext(), getString(R.string.connect_to_post), Toast.LENGTH_SHORT).show();
			}

		} else {
			
			//si recupera il prossimo alarm impostato
			int next_alarm_id = settings.getInt("alarm_id", -1);	
			Alarm next_alarm=null;
			
			if(next_alarm_id!=-1){
				next_alarm = AlarmUtils.getAlarm(appContext, settings.getInt("alarm_id", -1));
			}
			
			//si recupera l'indice del giorno corrente all'interno della settimana
			/////////		
	    	//PER TEST ALGORITMO
			int current_day_index = settings.getInt("artificialDayIndex", 0);
			///////// altrimenti l'indice del giorno è (Calendar.getInstance().get(Calendar.DAY_OF_WEEK))-1;
			
			if (samplingEnabled) { // if sampling is enabled stop the classifier
				boolean changes = false;
				if (new_steps != 0)
					changes = true;
				new SaveProgressTask(next_alarm,current_day_index, changes, true).execute(); // stopClassify();
				
				((ImageButton) findViewById(R.id.btnStartClimbing)).setImageResource(R.drawable.av_play); // set
				findViewById(R.id.progressBarClimbing).startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_fade_out)); // hide progress bar
				findViewById(R.id.progressBarClimbing).setVisibility(View.INVISIBLE);
				if (new_steps != 0) {
					apply_update();

				}
			} else { // if sampling is not enabled stop the classifier
				if (!isCounterMode)
					climbedYesterday = StatUtils.climbedYesterday(climbing.get_id());
				// FOR TESTING PURPOSES
				// climbedYesterday=true;
				startClassifyService(next_alarm,current_day_index);
				
//l'utente fa partire il gioco, generando un "periodo di gioco"
				
				//se il prossimo alarm settato è un evento di stop allora significa che 
				//si è all'interno di un intervallo attivo; se quest'ultimo è un semplice
				//"intervallo di esplorazione" (non è un "intervallo con scalini"), vuol
				//dire che il servizio di activity recognition è in esecuzione; in questo
				//caso tale servizio viene fermato	
				if(next_alarm!=null && !next_alarm.get_actionType() && !next_alarm.isStepsInterval(current_day_index)){
										
					Log.d(MainActivity.AppName,"START GAME IN ACTIVE INTERVAL - STOP ACTIVITY REC");
					
					//stop servizio di activity recognition (si tengono le variabili legate
					//all'eventuale valutazione dell'attività svolta in precedenza nell'intervallo
					//corrente, che è stato interrotto dall'inizio del gioco)					
					if(GeneralUtils.isActivityRecognitionServiceRunning(appContext)){
						appContext.stopService(new Intent(appContext, ActivityRecognitionRecordService.class));
					}
					
					Log.d(MainActivity.AppName,"START GAME IN ACTIVE INTERVAL - Total number of values: " + ActivityRecognitionIntentService.getValuesNumber(settings));
				   	Log.d(MainActivity.AppName,"START GAME IN ACTIVE INTERVAL - Number of activities: " + ActivityRecognitionIntentService.getActivitiesNumber(settings));	
				}
			}
		}
	}

	/**
	 * Make sure that all background services are stopped
	 */
//	private void stopAllServices() {
//		try {
//			unregisterReceiver(sampleRateDetectorReceiver);
//			stopService(backgroundSamplingRateDetector);
//		} catch (Exception e) {
//			Log.e(MainActivity.AppName, "Sampling rate service not running or unable to stop");
//		}
//		try {
//			unregisterReceiver(classifierReceiver);
//			stopService(backgroundClassifySampler);
//		} catch (Exception e) {
//			Log.e(MainActivity.AppName, "Classifier service not running or unable to stop");
//		}				
//	}
	
	/**
	 * Make sure that all background services are stopped
	 */
	private void stopAllServices() {
		try {
			unregisterReceiver(sampleRateDetectorReceiver);
			stopService(backgroundSamplingRateDetector);
		} catch (Exception e) {
			Log.e(MainActivity.AppName, "Sampling rate service not running or unable to stop");
		}
		try {
			//si recupera il prossimo alarm impostato
			int next_alarm_id = settings.getInt("alarm_id", -1);	
						
			//se questo alarm esiste
			if(next_alarm_id!=-1){
				Alarm next_alarm = AlarmUtils.getAlarm(appContext, next_alarm_id);			
				//si recupera l'indice del giorno corrente all'interno della settimana
				/////////		
		    	//PER TEST ALGORITMO
				int current_day_index = settings.getInt("artificialDayIndex", 0);
				///////// altrimenti l'indice del giorno è (Calendar.getInstance().get(Calendar.DAY_OF_WEEK))-1;
							
				if(next_alarm.get_actionType() || !next_alarm.get_actionType() && !next_alarm.isStepsInterval(current_day_index)){
					
					appContext.getPackageManager().setComponentEnabledSetting(new ComponentName(appContext, StairsClassifierReceiver.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
					stopService(backgroundClassifySampler);
				}
			}
			else{ //se non esiste un prossimo alarm, significa che l'algoritmo non è ancora
				  //stato configurato (oppure l'utente l'ha configurato impostando tutte
				  //fasce orarie rosse); in questo caso si ferma semplicemente il servizio
				appContext.getPackageManager().setComponentEnabledSetting(new ComponentName(appContext, StairsClassifierReceiver.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
				stopService(backgroundClassifySampler);
			}
						
			//unregisterReceiver(classifierReceiver);
			//stopService(backgroundClassifySampler);
		} catch (Exception e) {
			Log.e(MainActivity.AppName, "Classifier service not running or unable to stop");
		}
	}

	/**
	 * Called when Collabration ended and the user didn't pass the threshold. It modifies the game mode in 'Solo Climb' and deletes locally the Collaboration object
	 */
	// la collaborazione si � conclusa e non ho superato la soglia minima
	// quindi torno in social climb e elimino la collab localmente
	private void socialPenalty() {
		updatePoints(true, true);
		Toast.makeText(this.getApplicationContext(), getString(R.string.social_penalty), Toast.LENGTH_SHORT).show();
		((ImageButton) findViewById(R.id.btnStartClimbing)).setImageResource(R.drawable.social_share);
		mode = GameModeType.SOLO_CLIMB;
		climbing.setGame_mode(0);
		climbing.setId_mode("");
		updateClimbingInParse(climbing, false);
		saveCollaborationData();
		// ClimbApplication.collaborationDao.delete(collaboration);
	}

	/**
	 * Stop background classifier service
	 */
	public void stopClassify(Alarm next_alarm, int day_index, boolean changes, boolean stopServices) {
		Log.i("ClimbActivity", "stopClassify");
//		if (samplingEnabled && stopServices) {
//			stopService(backgroundClassifySampler); // stop background service
//			samplingEnabled = false;
//			unregisterReceiver(classifierReceiver); // unregister listener
//		}
		
		//se esiste un prossimo alarm
				if(next_alarm!=null){
					//se il prossimo alarm è di start oppure è di stop e definisce un semplice "intervallo
					//di esplorazione" allora si ferma il classificatore scalini/non_scalini
					if(next_alarm.get_actionType() || !next_alarm.get_actionType() && !next_alarm.isStepsInterval(day_index)){
						stopService(backgroundClassifySampler); // stop background service	
						//unregisterReceiver(classifierReceiver); // unregister listener
						appContext.getPackageManager().setComponentEnabledSetting(new ComponentName(appContext, StairsClassifierReceiver.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
						Log.d(MainActivity.AppName,"Climb - STOP CLASSIFY: is not a 'interval with steps'");
					}
					else{
						Log.d(MainActivity.AppName,"Climb - NO STOP CLASSIFY: is a 'interval with steps'");
					}
				}
				else{ //se non esiste un prossimo alarm si ferma semplicemente il classificatore 
					stopService(backgroundClassifySampler); // stop background service	
					appContext.getPackageManager().setComponentEnabledSetting(new ComponentName(appContext, StairsClassifierReceiver.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
				}
				
						
				StairsClassifierReceiver.setClimb(null);
				samplingEnabled = false;
		
				
				//l'utente ferma il gioco, ponendo fine al "periodo di gioco" iniziato
				//in precedenza
				
				//se il prossimo alarm è di stop significa che si è all'interno di un
				//intervallo di esplorazione/intervallo con scalini attivo
				if(next_alarm!=null && !next_alarm.get_actionType()){
				
					//nel periodo di gioco corrente l'utente ha fatto scalini (utile per un
					//intervallo di esplorazione)
					if(stepsInGamePeriod){
						
						Log.d(MainActivity.AppName,"STOP GAME IN ACTIVE INTERVAL - with steps");
						
						//si associa il fatto che nell'intervallo di esplorazione corrente
						//l'utente ha fatto almeno uno scalino (salvataggio nelle preferences:
						//utile per memorizzare questo fatto in caso di vari periodi di gioco 
						//all'interno dell'intervallo, mantenendo l'informazione anche se  
						//l'utente spegne il device)
						settings.edit().putBoolean("last_interval_with_steps", true).commit();
					
						//si resetta a 'false' il booleano che indica se l'utente ha fatto
						//almeno 1 scalino nel periodo di gioco corrente
						stepsInGamePeriod=false;
					}
					else{ 
						//nel periodo di gioco corrente l'utente non ha fatto scalini
						
						//se l'intervallo definito dall'alarm di stop è un semplice 
						//"intervallo di esplorazione" (quindi non è un "intervallo con scalini") e
						//se in nessuno dei periodi di gioco all'interno di questo intervallo
						//l'utente non ha fatto scalini, allora si ri-attiva il servizio di activity
						//recognition
						//TODO controllo livello batteria 
						
						//System.out.println("last interval with steps (default next alarm id): " + settings.getInt("last_interval_with_steps", next_alarm_id));
						//System.out.println("next alarm id: " + next_alarm_id);
						
						if(!next_alarm.isStepsInterval(day_index) && 
							!settings.getBoolean("last_interval_with_steps", false)){
														
							Log.d(MainActivity.AppName,"STOP GAME IN ACTIVE INTERVAL - START ACTIVITY REC");
							appContext.startService(new Intent(appContext, ActivityRecognitionRecordService.class));
							
						}
						//se si tratta di un "intervallo con scalini" si continua
						//l'esecuzione del classificatore scalini/non_scalini
					}
										
				/*						
					if(!next_alarm.isStepsInterval(current_day_index)){
						Log.d(MainActivity.AppName,"STOP GAME IN ACTIVE INTERVAL - START ACTIVITY REC");
						appContext.startService(new Intent(appContext, ActivityRecognitionRecordService.class));
					}*/
				}
				else{
					//se il prossimo alarm è di start significa che non si è all'interno di un
					//intervallo di esplorazione/intervallo con scalini attivo; quindi, in tal caso,
					//se l'utente ferma il gioco si resetta a 'false' il booleano che indica se 
					//l'utente ha fatto almeno 1 scalino nel periodo di gioco corrente
					stepsInGamePeriod=false;					
				}
				
				
				
		if (isCounterMode)
			updateUserStats(true);
		else
			updateUserStats(false);
		if (!isCounterMode && changes) {
			// update db

			climbing.setModified(new Date().getTime()); // update climbing last/ edit date
			climbing.setCompleted_steps(num_steps); // update completed steps
			climbing.setPercentage(percentage); // update progress percentage
			climbing.setRemaining_steps(building.getSteps() - num_steps); // update remaining steps
			if (percentage >= 1.00 && (mode != GameModeType.SOCIAL_CHALLENGE) && (mode != GameModeType.TEAM_VS_TEAM))
				climbing.setCompleted(new Date().getTime());
			if (percentage >= 1.00) {
				switch (mode) {
				case SOCIAL_CLIMB:
					old_game_mode = mode;
					updateOthers(false, false);
					climbing.setGame_mode(0);
					climbing.setId_mode("");
					ClimbApplication.climbingDao.update(climbing); // save to db
					if (!pref.getString("FBid", "none").equalsIgnoreCase("none") && !pref.getString("FBid", "none").equalsIgnoreCase("empty")) {
						updateClimbingInParse(climbing, false);
						if (microgoal != null)
							deleteMicrogoalInParse(microgoal);
					}
					break;
				case SOCIAL_CHALLENGE:
					if (!pref.getString("FBid", "none").equalsIgnoreCase("none") && !pref.getString("FBid", "none").equalsIgnoreCase("empty"))
						updateClimbingInParse(climbing, false);
					updateChart(false, false);
					// if(competition.isCompleted()) endCompetition(false);
					break;
				case TEAM_VS_TEAM:
					if (!pref.getString("FBid", "none").equalsIgnoreCase("none") && !pref.getString("FBid", "none").equalsIgnoreCase("empty"))
						updateClimbingInParse(climbing, false);
					updateTeams(false, false);
					// if(teamDuel.isCompleted()) endTeamCompetition(false);
					break;
				case SOLO_CLIMB:
					old_game_mode = mode;
					ClimbApplication.climbingDao.update(climbing); // save to db
					if (!pref.getString("FBid", "none").equalsIgnoreCase("none") && !pref.getString("FBid", "none").equalsIgnoreCase("empty")) {
						updateClimbingInParse(climbing, false);
						if (microgoal != null) {
							deleteMicrogoalInParse(microgoal);
						}
					} else {
						climbing.setSaved(true);
						ClimbApplication.climbingDao.update(climbing);
						ClimbApplication.microgoalDao.delete(microgoal);
					}
					break;
				}
				/*
				 * climbing.setGame_mode(0); climbing.setId_mode("");
				 */
			} else {

				if (!pref.getString("FBid", "none").equalsIgnoreCase("none") && !pref.getString("FBid", "none").equalsIgnoreCase("empty")) {
					ClimbApplication.climbingDao.update(climbing); // save to db
					updateClimbingInParse(climbing, false);
					updateMicrogoalInParse();

				} else {
					climbing.setSaved(true);
					ClimbApplication.climbingDao.update(climbing);
				}

			}

			if (mode == GameModeType.SOCIAL_CLIMB && collaboration != null)
				saveCollaborationData();
			else if (mode == GameModeType.SOCIAL_CHALLENGE && competition != null) {
				if (!competition.isCompleted())
					saveCompetitionData();
			} else if (mode == GameModeType.TEAM_VS_TEAM && teamDuel != null) {
				if (!teamDuel.isCompleted())
					saveTeamDuelData();
			}
			Log.i(MainActivity.AppName, "Updated climbing #" + climbing.get_id());

			if (mode == GameModeType.SOCIAL_CLIMB || mode == GameModeType.SOLO_CLIMB) {
				updatePoints(false, true);
				// saveBadges(true);
			}
			System.out.println("END");

		}

		// ClimbActivity.this.runOnUiThread(new Runnable() {
		// public void run() {
		// ((ImageButton) findViewById(R.id.btnStartClimbing)).setImageResource(R.drawable.av_play); // set
		// findViewById(R.id.progressBarClimbing).startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_fade_out)); // hide
		// // progress
		// // bar
		// findViewById(R.id.progressBarClimbing).setVisibility(View.INVISIBLE); }
		// });

	}

	private class SaveProgressTask extends AsyncTask<Void, Void, Void> {

		protected void onPostExecute(Void result) {

		}

		boolean changes;
		boolean stopServices;
		Alarm alarm;
		int day_index;
		
		public SaveProgressTask(Alarm alarm, int day_index, boolean changes, boolean stopServices) {
			this.changes = changes;
			this.stopServices = stopServices;
			this.alarm = alarm;
			this.day_index = day_index;
		}

		@Override
		protected Void doInBackground(Void... params) {
			stopClassify(alarm, day_index, changes, stopServices);
			return null;
		}
	}

	private void updateUserStats(boolean saveOnline) {
		Log.d("ClimbActivity", "UpdateUserStats");

		if (difficulty == 0) {
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()); // get
			difficulty = Integer.parseInt(settings.getString("difficulty", "10"));
		}
		if (isCounterMode)
			difficulty = 1;
		int real_steps = previous_progress / difficulty;
		//LOG-------------------------------------------------
		//LINE
		String line = "USER MADE : " + real_steps + " STEPS";
		LogUtils.writeGameUpdate(getApplicationContext(), line);
		Log.d("LOG", line);
		//
		//---------------------------------------------------
		currentUser.addObserver(this);
		ParseUser user = ParseUser.getCurrentUser();
		if (ClimbApplication.are24hPassed(currentUser.getBegin_date())) {
			currentUser.setMean(ClimbApplication.calculateNewMean((long) currentUser.getMean(), currentUser.getN_measured_days(), (currentUser.getCurrent_steps_value())));
			currentUser.setCurrent_steps_value(real_steps);
			currentUser.setN_measured_days(currentUser.getN_measured_days() + 1);
			currentUser.setBegin_date(String.valueOf(new Date().getTime()));
			// ClimbApplication.userDao.update(currentUser);
		} else {
			System.out.println("update current value with " + real_steps);

			currentUser.setCurrent_steps_value(currentUser.getCurrent_steps_value() + real_steps);
		}
		currentUser.setHeight(currentUser.getHeight() + ClimbApplication.fromStepsToMeters(real_steps));
		ClimbApplication.userDao.update(currentUser);

		if (user != null) {
			JSONObject stats = user.getJSONObject("mean_daily_steps");
			try {
				stats.put("mean", currentUser.getMean());
				stats.put("n_days", currentUser.getN_measured_days());
				stats.put("current_value", currentUser.getCurrent_steps_value());
				stats.put("begin_date", currentUser.getBegin_date());
				user.put("mean_daily_steps", stats);
				user.put("height", currentUser.getHeight());
				// user.saveEventually();
				if (saveOnline)
					ParseUtils.saveUserInParse(user);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Saves the data about current Collaboration object in Parse if possible, otherwise it remembers to save the updates when connection return available.
	 */
	private void saveCollaborationData() {
		Log.i("ClimbActivity", "saveCollaborationData");
		collaboration.setMy_stairs(climbing.getCompleted_steps());
		if (percentage >= 1.00 || current_win) {
			System.out.println("completato");
			collaboration.setCompleted(true);

		}
		ClimbApplication.collaborationDao.update(collaboration);
		if (collab_parse == null) {
			collaboration.setSaved(false);
			ClimbApplication.collaborationDao.update(collaboration);
			// Toast.makeText(getApplicationContext(),
			// getString(R.string.connection_problem2), Toast.LENGTH_SHORT)
			// .show();
		} else {
			SharedPreferences pref = getSharedPreferences("UserSession", 0);
			JSONObject stairs = collab_parse.getJSONObject("stairs");
			try {
				String fbid = pref.getString("FBid", "");
				if(!fbid.equalsIgnoreCase("none") && !fbid.equalsIgnoreCase("empty")) stairs.put(fbid, collaboration.getMy_stairs());
				collab_parse.put("stairs", stairs);
				if (percentage >= 1.00 || current_win) {
					collab_parse.put("completed", true);
					System.out.println("completato online");
				}
				// collab_parse.saveEventually();
				ParseUtils.saveCollaboration(collab_parse, collaboration);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// collaboration.setSaved(true);
		}
		// if (collaboration.isCompleted())
		// ClimbApplication.collaborationDao.delete(collaboration);
		// else
		// ClimbApplication.collaborationDao.update(collaboration);

	}

	private void saveCompetitionData() {
		Log.i("ClimbActivity", "saveCompetitionData");
		competition.setMy_stairs(climbing.getCompleted_steps());

		// if (current_win) {
		// competition.setCompleted(true);
		// }

		ClimbApplication.competitionDao.update(competition);

		if (compet_parse == null) {
			competition.setSaved(false);
			ClimbApplication.competitionDao.update(competition);

		} else {
			SharedPreferences pref = getSharedPreferences("UserSession", 0);
			JSONObject stairs = compet_parse.getJSONObject("stairs");
			try {
				String fbid = pref.getString("FBid", "");
				if(!fbid.equalsIgnoreCase("none") && !fbid.equalsIgnoreCase("empty")) stairs.put(pref.getString("FBid", ""), competition.getMy_stairs());
				compet_parse.put("stairs", stairs);
				System.out.println("salvo scalini");

				// if (percentage >= 1.00) {
				// compet_parse.put("completed", true);
				// System.out.println("completo online");
				// }

				// compet_parse.saveEventually();
				ParseUtils.saveCompetition(compet_parse, competition);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			// competition.setSaved(true);
		}
		// if (competition.isCompleted())
		// ClimbApplication.competitionDao.delete(competition);
		// else
		// ClimbApplication.competitionDao.update(competition);

	}

	private void saveTeamDuelData() {
		teamDuel.setMy_steps(climbing.getCompleted_steps());
		// if(current_win){
		// teamDuel.setCompleted(true);
		// }
		ClimbApplication.teamDuelDao.update(teamDuel);

		if (teamDuel_parse == null) {
			teamDuel.setSaved(false);
			ClimbApplication.teamDuelDao.update(teamDuel);
		} else {
			SharedPreferences pref = getSharedPreferences("UserSession", 0);
			JSONObject myteam;
			if (teamDuel.getMygroup() == Group.CHALLENGER)
				myteam = teamDuel_parse.getJSONObject("challenger_stairs");
			else
				myteam = teamDuel_parse.getJSONObject("creator_stairs");
			try {
				String fbid = pref.getString("FBid", "");
				if(!fbid.equalsIgnoreCase("none") && !fbid.equalsIgnoreCase("empty")) myteam.put(pref.getString("FBid", ""), teamDuel.getMy_steps());

				// if (percentage >= 1.00) {
				// System.out.println("elimino team duel");
				// teamDuel_parse.put("completed", true);
				// teamDuel.setCompleted(true);
				// }

				// teamDuel_parse.saveEventually();
				ParseUtils.saveTeamDuel(teamDuel_parse, teamDuel);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// teamDuel.setSaved(true);
		}
		// if (teamDuel.isCompleted())
		// ClimbApplication.teamDuelDao.delete(teamDuel);
		// else
		// ClimbApplication.teamDuelDao.update(teamDuel);
	}

	/**
	 * Start background classifier service
	 */
	public void startClassifyService(Alarm next_alarm, int day_index) {
		if (!isCounterMode) {
			if (building.get_id() != 6) {
				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()); // get
																														// reference
																														// to
																														// android
																														// preferences
				/* int */difficulty = Integer.parseInt(settings.getString("difficulty", "10")); // get
																								// difficulty
																								// from
																								// preferences
				switch (difficulty) { // set several parameters related to
										// difficulty
				case 100: // easy
					Log.i(MainActivity.AppName, "Selected difficulty: EASY");
					vstep_for_rstep = 100;
					break;
				case 1: // hard
					Log.i(MainActivity.AppName, "Selected difficulty: HARD");
					vstep_for_rstep = 1;
					break;
				default: // normal and default
					Log.i(MainActivity.AppName, "Selected difficulty: NORMAL");
					vstep_for_rstep = 10;
					break;
				}

				Log.i(MainActivity.AppName, "Using " + vstep_for_rstep + " steps for each real step");
			} else {
				Log.i(MainActivity.AppName, "Difficulty: HARD");
				difficulty = 1;
			}
		}

		//se esiste un prossimo alarm
				if(next_alarm!=null){
					//se il prossimo alarm è di stop e definisce la fine di un "intervallo con scalini",
					//allora il classificatore scalini/non_scalini è già in esecuzione; quindi non lo si
					//attiva nuovamente; se, invece, il prossimo alarm è di start (quindi non si è in
					//un intervallo attivo) oppure è di stop ma l'intervallo in questione è un "intervallo
					//di esplorazione", allora si deve attivare il classificatore scalini/non_scalini
					if(next_alarm.get_actionType() || !next_alarm.get_actionType() && !next_alarm.isStepsInterval(day_index)){
						
						Log.d(MainActivity.AppName, "It is not a 'interval with steps', START STAIRS CLASSIFIER");
						
						startService(backgroundClassifySampler); // start background service
						//registerReceiver(classifierReceiver, classifierFilter); // register listener	
						appContext.getPackageManager().setComponentEnabledSetting(new ComponentName(appContext, StairsClassifierReceiver.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
					}
				}
				else{ //se non esiste un prossimo alarm si fa partire semplicemente il classificatore 
					startService(backgroundClassifySampler); // start background service	
					appContext.getPackageManager().setComponentEnabledSetting(new ComponentName(appContext, StairsClassifierReceiver.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
				}
				
				
		//startService(backgroundClassifySampler); // start background service
		StairsClassifierReceiver.setClimb(this);//registerReceiver(classifierReceiver, classifierFilter); // register listener
		

		samplingEnabled = true;
		((ImageButton) findViewById(R.id.btnStartClimbing)).setImageResource(R.drawable.av_pause); // set
																									// button
																									// image
																									// to
																									// stop
																									// service
		findViewById(R.id.lblReadyToClimb).setVisibility(View.GONE);
		findViewById(R.id.encouragment).setVisibility(View.INVISIBLE);
		findViewById(R.id.btnAccessPhotoGallery).setVisibility(View.GONE);
		findViewById(R.id.progressBarClimbing).startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_fade_in));
		findViewById(R.id.progressBarClimbing).setVisibility(View.VISIBLE);
	}

	@Override
	protected void onResume() {
		Log.i(MainActivity.AppName, "ClimbActivity onResume");
		int building_id = getIntent().getIntExtra(ClimbApplication.building_text_intent_object, 0); // get
		// building
		// id
		// from
		// received
		// intent
		Log.i(MainActivity.AppName, "Building id: " + building_id);
		super.onResume();
		uiHelper.onResume();
		ClimbApplication.activityResumed();
	}

	//TODO Alarm da controllare
	@Override
	protected void onPause() {
		Log.i(MainActivity.AppName, "ClimbActivity onPause");
		super.onPause();
		uiHelper.onPause();
		ClimbApplication.activityPaused();
		SharedPreferences paused = getSharedPreferences("state", 0);
		Editor editor = paused.edit();
		editor.putBoolean("paused", true);
		editor.commit();
		if (samplingEnabled){
			
			//si recupera il prossimo alarm impostato
			int next_alarm_id = settings.getInt("alarm_id", -1);
			Alarm next_alarm = null;				
			if(next_alarm_id!=-1){
				next_alarm = AlarmUtils.getAlarm(appContext, next_alarm_id);
			}
			
			new SaveProgressTask(next_alarm, settings.getInt("artificialDayIndex", 0), true, false).execute(); // this.finish();
		}
			
			
	}

	

	//TODO Alarm controllare
	@Override
	protected void onDestroy() {
		Log.i(MainActivity.AppName, "ClimbActivity onDestroy");
		stopAllServices(); // make sure to stop all background services
		super.onDestroy();
		uiHelper.onDestroy();
		timer.cancel();
		if (samplingEnabled) {
			
			//si recupera il prossimo alarm impostato
			int next_alarm_id = settings.getInt("alarm_id", -1);
			Alarm next_alarm = null;				
			if(next_alarm_id!=-1){
				next_alarm = AlarmUtils.getAlarm(appContext, next_alarm_id);
			}
						
			new SaveProgressTask(next_alarm, settings.getInt("artificialDayIndex", 0), true, false).execute();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
	}

	@Override
	public void onBackPressed() {
		if (samplingEnabled == false) {
			// // super.onBackPressed();
			// SharedPreferences paused = getSharedPreferences("state", 0);
			//
			// Intent upIntent = NavUtils.getParentActivityIntent(this);
			// if ((NavUtils.shouldUpRecreateTask(this, upIntent)) || paused.getBoolean("paused", true)) {
			// System.out.println("ricrea btn");
			// Editor editor = paused.edit();
			// editor.putBoolean("paused", false);
			// editor.commit();
			// TaskStackBuilder.create(this).addNextIntentWithParentStack(upIntent).startActivities();
			// } else {
			// System.out.println("torna su btn");
			// // fillUpIntentWithExtras(upIntent);
			// NavUtils.navigateUpTo(this, upIntent);
			// }
			finish();

		} else { // disable back button if sampling is enabled
			Toast.makeText(getApplicationContext(), getString(R.string.sampling_enabled), Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d("Stopping...", "Stopping the activity");
		
	}

	/*
	 * // Working for all API levels
	 * 
	 * @Override public boolean onKeyDown(int keyCode, KeyEvent event) { if (keyCode == KeyEvent.KEYCODE_BACK) { SharedPreferences paused = getSharedPreferences("state", 0);
	 * 
	 * Intent upIntent = NavUtils.getParentActivityIntent(this); if ((NavUtils.shouldUpRecreateTask(this, upIntent) ) || paused.getBoolean("paused", true)) { System.out.println("ricrea"); Editor editor = paused.edit(); editor.putBoolean("paused", false); editor.commit(); TaskStackBuilder.create(this) .addNextIntentWithParentStack(upIntent) .startActivities(); } else { System.out.println("torna su"); //fillUpIntentWithExtras(upIntent); NavUtils.navigateUpTo(this, upIntent); } } return
	 * super.onKeyDown(keyCode, event); }
	 */

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.climb, menu);
		// We should save our menu so we can use it to reset our updater.
		mymenu = menu;
		if (isCounterMode || (climbing.getPercentage() >= 1.00 && (mode.equals(GameModeType.SOLO_CLIMB) || mode.equals(GameModeType.SOCIAL_CLIMB)))) {
			for (int i = 0; i < menu.size() - 1; i++)
				menu.getItem(i).setVisible(false);
		}
		if (!isCounterMode && mode.equals(GameModeType.SOLO_CLIMB))
			menu.getItem(0).setVisible(false); // hide update

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			supportInvalidateOptionsMenu();
		else
			onPrepareOptionsMenu(menu);

		return true;
	}

	/**
	 * Action for update button pressed
	 * 
	 * @param v
	 */
	public void onUpdate(/* MenuItem v */) {
		switch (climbing.getGame_mode()) {
		case 0:
			//resetUpdating();
			break;
		case 1:
			updateOthers(true, false);
			break;
		case 2:
			updateChart(true, false);
			break;
		case 3:
			updateTeams(true, false);
			break;

		}
	}

	public void resetUpdating() {
		// Get our refresh item from the menu
//		MenuItem m = mymenu.findItem(R.id.itemUpdate);
//		if (MenuItemCompat.getActionView(m) != null) {
//			// Remove the animation.
//			System.out.println("remove animation");
//
//			MenuItemCompat.getActionView(m).clearAnimation();
//			MenuItemCompat.setActionView(m, null);
//
//		}
		supportInvalidateOptionsMenu();
	}

	private void endCompetition(boolean saveOnline) {
		timer.cancel();
		Log.d("END COMPETITION", "fineeee");
		updatePoints(false, saveOnline);
		saveCompetitionData();
		if (soloClimb != null) {
			System.out.println("non scalato per la prima volta " + climbing.getId_mode());
			System.out.println("soloclimb: " + soloClimb.getId_mode());
			deleteClimbingInParse(climbing);
			/*
			 * if(climbing.getCompleted() != 0){ //ho gi� scalato l'edificio una volta, quindi lo lascio scalato per intero climbing.setCompleted_steps(building.getSteps()); climbing.setRemaining_steps(0); climbing.setPercentage(100);
			 * 
			 * }
			 */
			soloClimb.setGame_mode(0);
			soloClimb.setId_mode("");
			ClimbApplication.climbingDao.update(soloClimb);
			updateClimbingInParse(soloClimb, true);
		} else {
			System.out.println("scalato x prima volta");
			climbing.setGame_mode(0);
			climbing.setId_mode("");
			ClimbApplication.climbingDao.update(climbing);
			updateClimbingInParse(climbing, false);
		}
		if (microgoal != null)
			deleteMicrogoalInParse(microgoal);
		((ImageButton) findViewById(R.id.btnStartClimbing)).setImageResource(R.drawable.social_share);
		old_game_mode = mode;
		mode = GameModeType.SOLO_CLIMB;

	}

	private void endTeamCompetition(/* boolean penalty, */boolean saveOnline) {
		// updatePoints(penalty, saveOnline);
		timer.cancel();
		saveTeamDuelData();
		if (soloClimb != null) {
			deleteClimbingInParse(climbing);
			soloClimb.setGame_mode(0);
			soloClimb.setId_mode("");
			ClimbApplication.climbingDao.update(soloClimb);
			updateClimbingInParse(soloClimb, true);
		} else {
			System.out.println("scalato x prima volta");
			climbing.setGame_mode(0);
			climbing.setId_mode("");
			ClimbApplication.climbingDao.update(climbing);
			updateClimbingInParse(climbing, false);
			((ImageButton) findViewById(R.id.btnStartClimbing)).setImageResource(R.drawable.social_share);
		}
		if (microgoal != null)
			deleteMicrogoalInParse(microgoal);
		old_game_mode = mode;
		mode = GameModeType.SOLO_CLIMB;

	}

	// non ho superato la threshold
	private void socialTeamPenality() {
		// Toast.makeText(this.getApplicationContext(), getString(R.string.social_penalty), Toast.LENGTH_SHORT).show();
		showMessage(getString(R.string.social_penalty));
	}

	private void updateTeams(final boolean isUpdate, final boolean isOpening) {
		if (!(mode == GameModeType.SOLO_CLIMB) && FacebookUtils.isOnline(this)) {
			ParseQuery<ParseObject> query = ParseQuery.getQuery("TeamDuel");
			query.whereEqualTo("objectId", teamDuel.getId_online());
			query.findInBackground(new FindCallback<ParseObject>() {

				@Override
				public void done(List<ParseObject> tds, ParseException e) {
					if (e == null) {
						if (tds == null || tds.size() == 0) {
							showMessage(getString(R.string.team_duel_no_available));
							timer.cancel();
							// Toast.makeText(getApplicationContext(), getString(R.string.team_duel_no_available), Toast.LENGTH_SHORT).show();
							Log.e("updateTeams", "TeamDuel not present in Parse");
						} else {
							SharedPreferences pref = getApplicationContext().getSharedPreferences("UserSession", 0);
							teamDuel_parse = tds.get(0);
							JSONObject myTeam;
							JSONObject otherTeam;
							boolean completed = teamDuel_parse.getBoolean("completed");
							Date victory_time = teamDuel_parse.getDate("victory_time");
							Date last_update = teamDuel_parse.getUpdatedAt();

							if (teamDuel.getSteps_my_group() >= teamDuel.getSteps_other_group())
								old_chart_position = 0;
							else
								old_chart_position = 1;

							if (teamDuel.getMygroup() == Group.CHALLENGER) {
								myTeam = teamDuel_parse.getJSONObject("challenger_stairs");
								otherTeam = teamDuel_parse.getJSONObject("creator_stairs");
								// if(!completed){
								try {
									String fbid = pref.getString("FBid", "");
									if (myTeam.getInt(pref.getString("FBid", "")) < num_steps && !fbid.equalsIgnoreCase("none") && !fbid.equalsIgnoreCase("empty"))
										myTeam.put(pref.getString("FBid", ""), num_steps);
								} catch (JSONException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								teamDuel_parse.put("challenger_stairs", myTeam);
								// teamDuel_parse.saveEventually();
								// ParseUtils.saveTeamDuel(teamDuel_parse, teamDuel);
								// }
							} else {
								myTeam = teamDuel_parse.getJSONObject("creator_stairs");
								otherTeam = teamDuel_parse.getJSONObject("challenger_stairs");
								// if(!completed){
								try {
									String fbid = pref.getString("FBid", "");
									if (myTeam.getInt(pref.getString("FBid", "")) < num_steps && !fbid.equalsIgnoreCase("none") && !fbid.equalsIgnoreCase("empty"))
										myTeam.put(pref.getString("FBid", ""), num_steps);
								} catch (JSONException e1) {
									e1.printStackTrace();
								}
								teamDuel_parse.put("creator_stairs", myTeam);

							}
							int myGroupScore = ModelsUtil.sum(myTeam);
							int otherGroupScore = ModelsUtil.sum(otherTeam);
							DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

							teamDuel.setChecks(teamDuel_parse.getInt("checks"));
							teamDuel.setCompleted(teamDuel_parse.getBoolean("completed"));
							teamDuel.setVictory_time(victory_time.getTime());
							teamDuel.setWinner_id(teamDuel_parse.getString("winner_id"));
							ClimbApplication.teamDuelDao.update(teamDuel);

							boolean foundWinner = ModelsUtil.hasSomeoneWon(myGroupScore, otherGroupScore, building.getSteps());
							boolean updateChecks = false;
							System.out.println(foundWinner);

							if (foundWinner && !climbing.isChecked()) { // !victory_time.after(new Date(victory_time.getTime() - 5 * 24 * 3600 * 1000 )
								current_win = true;

								try {
									if (myGroupScore >= building.getSteps() && (victory_time.getTime() == 0 || victory_time.after(df.parse(df.format(climbing.getModified()))))) {
										teamDuel.setVictory_time(climbing.getModified());
										teamDuel.setWinner_id(new Integer((teamDuel.getMygroup()).ordinal()).toString());
									}
								} catch (java.text.ParseException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}

								teamDuel.setChecks(teamDuel.getChecks() + 1);
								teamDuel_parse.increment("checks", 1);
								climbing.setChecked(true);
								updateClimbingInParse(climbing, true);
							}else{
								updateChecks = true;
							}

							if (teamDuel.getChecks() >= (teamDuel_parse.getJSONObject("challenger_stairs").length() + teamDuel_parse.getJSONObject("creator_stairs").length())
									|| last_update.after(new Date(last_update.getTime() + 5 * 24 * 3600 * 1000))) {
								teamDuel.setCompleted(true);
								chartHelpText.setVisibility(View.GONE);
								if (last_update.after(new Date(last_update.getTime() + 5 * 24 * 3600 * 1000))) {
									// the higher score wins, no badge and no points
									if (myGroupScore > otherGroupScore) {
										teamDuel.setWinner_id(String.valueOf(teamDuel.getMygroup().ordinal()));
									} else if (myGroupScore < otherGroupScore)
										teamDuel.setWinner_id(String.valueOf((1 - teamDuel.getMygroup().ordinal())));

								}
							} else if (foundWinner) {
								chartHelpText.setVisibility(View.VISIBLE);
								chartHelpText.setText(getString(R.string.help_chart, (((teamDuel_parse.getJSONObject("challenger_stairs").length() + teamDuel_parse.getJSONObject("creator_stairs").length()) - teamDuel.getChecks()))));
							}

							try {
								teamDuel_parse.put("victory_time", df.parse(df.format(teamDuel.getVictory_time())));
							} catch (java.text.ParseException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							teamDuel_parse.put("winner_id", teamDuel.getWinner_id());
							
							teamDuel_parse.put("completed", teamDuel.isCompleted());

							ParseUtils.saveTeamDuel(teamDuel_parse, teamDuel);

							Iterator<String> keys = myTeam.keys();
							myTeamScores.clear();
							while (keys.hasNext()) {
								String key = keys.next();
								if (!key.equals(pref.getString("FBid", ""))) {
									try {
										myTeamScores.add(myTeam.getInt(key));
									} catch (JSONException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
								}
							}

							teamDuel.setSteps_my_group(myGroupScore);
							teamDuel.setSteps_other_group(otherGroupScore);
							teamDuel.setMy_steps(num_steps);
							ClimbApplication.teamDuelDao.update(teamDuel);
							if (teamDuel.getMygroup() == Group.CHALLENGER) {
								group_members.get(0).setText(getString(R.string.team_of) + " " + teamDuel.getChallenger_name());
								group_steps.get(0).setText(String.valueOf(teamDuel.getSteps_my_group()));
								group_members.get(1).setText(getString(R.string.team_of) + " " + teamDuel.getCreator_name());
								group_steps.get(1).setText(String.valueOf(teamDuel.getSteps_other_group()));
							} else {
								group_members.get(0).setText(getString(R.string.team_of) + " " + teamDuel.getCreator_name());
								group_steps.get(0).setText(String.valueOf(teamDuel.getSteps_my_group()));
								group_members.get(1).setText(getString(R.string.team_of) + " " + teamDuel.getChallenger_name());
								group_steps.get(1).setText(String.valueOf(teamDuel.getSteps_other_group()));
							}
							group_members.get(0).setBackgroundColor(Color.parseColor("#adeead"));
							group_steps.get(0).setBackgroundColor(Color.parseColor("#adeead"));
							group_members.get(1).setBackgroundColor(Color.parseColor("#ef9d9d"));
							group_steps.get(1).setBackgroundColor(Color.parseColor("#ef9d9d"));
							group_members.get(0).setVisibility(View.VISIBLE);
							group_members.get(1).setVisibility(View.VISIBLE);
							group_steps.get(0).setVisibility(View.VISIBLE);
							group_steps.get(1).setVisibility(View.VISIBLE);
							int new_seekbar_progress = myTeamScore();
							seekbarIndicator.setProgress(new_seekbar_progress);
							double progress = (((double) (new_seekbar_progress + (microgoal.getTot_steps() - microgoal.getDone_steps())) * (double) 100) / (double) building.getSteps());
							//seekbarIndicator.nextStar((int) Math.round(progress));
							secondSeekbar.setProgress(ModelsUtil.sum(otherTeam));

							if (/* myGroupScore >= building.getSteps() */teamDuel.isCompleted() && Integer.valueOf(teamDuel.getWinner_id()) == teamDuel.getMygroup().ordinal()) {
								showMessage(getString(R.string.your_team_won));
								chartHelpText.setText(getString(R.string.your_team_won));
								chartHelpText.setVisibility(View.VISIBLE);
								current_win = true;
								// Toast.makeText(getApplicationContext(), getString(R.string.your_team_won), Toast.LENGTH_SHORT).show();
								boolean penalty = false;
								percentage = 1.00;
								if (teamDuel.getMy_steps() < threshold) {
									socialTeamPenality();
									penalty = true;
								} else {
									apply_win();
								}
								endTeamCompetition(false);

								// if(isUpdate || isOpening){
								updatePoints(penalty, true);
								// saveBadges(true);
								// }

							} else if (/* otherGroupScore >= building.getSteps() */teamDuel.isCompleted() && Integer.valueOf(teamDuel.getWinner_id()) != teamDuel.getMygroup().ordinal()) {
								showMessage(getString(R.string.other_team_won));
								chartHelpText.setText(getString(R.string.other_team_won));
								chartHelpText.setVisibility(View.VISIBLE);
								current_win = true;
								// Toast.makeText(getApplicationContext(), getString(R.string.other_team_won), Toast.LENGTH_SHORT).show();
								boolean penalty = false;
								if (teamDuel.getMy_steps() < threshold) {
									socialTeamPenality();
									penalty = true;
								}
								endTeamCompetition(false);

								// if(isUpdate || isOpening){
								updatePoints(penalty, true);
								// }

							}

						}
					} else if (!(mode == GameModeType.TEAM_VS_TEAM)) {
						showMessage(getString(R.string.connection_problem));
						showOfflineSocialMember();

						// Toast.makeText(getApplicationContext(), getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
						Log.e("updateTeams", e.getMessage());
					}
					if (isUpdate) {
						//resetUpdating();
					}
					synchronized (ClimbApplication.lock) {
						in_progress = false;
						ClimbApplication.lock.notifyAll();
					}

				}

			});
		} else {
			showMessage(getString(R.string.check_connection));
			showOfflineSocialMember();

			// Toast.makeText(getApplicationContext(), getString(R.string.check_connection), Toast.LENGTH_SHORT).show();
			if (isUpdate) {
				//resetUpdating();
			}
			synchronized (ClimbApplication.lock) {
				in_progress = false;
				ClimbApplication.lock.notifyAll();
			}
		}

	}

	private void updateChart(final boolean isUpdate, final boolean isOpening) {
		System.out.println("update chart");
		if (!(mode == GameModeType.SOLO_CLIMB) && FacebookUtils.isOnline(this)) {
			ParseQuery<ParseObject> query = ParseQuery.getQuery("Competition");
			query.whereEqualTo("objectId", competition.getId_online());
			query.findInBackground(new FindCallback<ParseObject>() {

				@Override
				public void done(List<ParseObject> compets, ParseException e) {
					if (e == null) {
						if (compets == null || compets.size() == 0) {
							showMessage(getString(R.string.competition_no_available));
							Log.e("updatechart", "Competition not present in Parse");
							// delete this collaboration
							ClimbApplication.competitionDao.delete(competition);
							timer.cancel();
						} else {
							SharedPreferences pref = getApplicationContext().getSharedPreferences("UserSession", 0);
							compet_parse = compets.get(0);

							JSONObject creator = compet_parse.getJSONObject("creator");
							Iterator<String> it_creator = creator.keys();
							String creator_fbid = it_creator.next();
							if (creator_fbid.equalsIgnoreCase(pref.getString("FBid", ""))) {
								if (!competition.getAmICreator()) {
									competition.setAmICreator(true);
									ClimbApplication.competitionDao.update(competition);
								}
							} else {
								if (competition.getAmICreator()) {
									competition.setAmICreator(false);
									ClimbApplication.competitionDao.update(competition);
								}
							}

							JSONObject others = compet_parse.getJSONObject("stairs");
							JSONObject othersName = compet_parse.getJSONObject("competitors");
							// tirare giu data vittoria, checks e vincitore
							Date victory_time = compet_parse.getDate("victory_time");
							System.out.println("new victory time " + victory_time.toString());
							String winner_id = compet_parse.getString("winner_id");
							int checks = compet_parse.getInt("checks");
							Date last_update = compet_parse.getUpdatedAt();

							if (othersName.has(currentUser.getFBid())) {

								try {
									String fbid = pref.getString("FBid", "");
									if (others.getInt(pref.getString("FBid", "")) < num_steps && !fbid.equalsIgnoreCase("none") && !fbid.equalsIgnoreCase("empty"))
										others.put(pref.getString("FBid", ""), num_steps);
								} catch (JSONException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								compet_parse.put("stairs", others);
								competition.setMy_stairs(num_steps);
								competition.setChecks(checks); System.out.println("tiro giu checks online: " + competition.getChecks());

								chart = ModelsUtil.fromJsonToChart(others);
								old_chart_position = competition.getCurrent_position();
								competition.setCurrent_position(ModelsUtil.chartPosition(pref.getString("FBid", ""), chart));

								DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
								competition.setCompleted(compet_parse.getBoolean("completed"));
								competition.setVictory_time(victory_time.getTime());
								competition.setWinner_id(winner_id);
								ClimbApplication.competitionDao.update(competition);

								boolean foundWinner = ModelsUtil.hasSomeoneWon(chart, building.getSteps());
								boolean updateChecks = false;
								
								System.out.println("checked climb????" + climbing.isChecked());
								if (foundWinner && !climbing.isChecked()) {
									current_win = true;
									try {
										if (num_steps >= building.getSteps() && (victory_time.getTime() == 0 || victory_time.after(df.parse(df.format(climbing.getModified()))))) {
											competition.setVictory_time(climbing.getModified());
											competition.setWinner_id(pref.getString("FBid", ""));
											winner_id = competition.getWinner_id();
											victory_time = df.parse(df.format(competition.getVictory_time()));

										}
									} catch (java.text.ParseException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}

									competition.setChecks(competition.getChecks() + 1);
									compet_parse.increment("checks", 1);
									climbing.setChecked(true); System.out.println("nuovi check " + competition.getChecks());
									updateClimbingInParse(climbing, true);

								}else{
									updateChecks = true;
								}
								if (competition.getChecks() >= others.length() || last_update.after(new Date(last_update.getTime() + 5 * 24 * 3600 * 1000))) {
									competition.setCompleted(true);
									chartHelpText.setVisibility(View.GONE);
									if (last_update.after(new Date(last_update.getTime() + 5 * 24 * 3600 * 1000)))
										competition.setWinner_id(chart.get(0).getId());
								} else if (foundWinner) {
									chartHelpText.setVisibility(View.VISIBLE);
									chartHelpText.setText(getString(R.string.help_chart, (others.length() - competition.getChecks())));
								}
								try {
									compet_parse.put("victory_time", df.parse(df.format(competition.getVictory_time())));
								} catch (java.text.ParseException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								compet_parse.put("winner_id", competition.getWinner_id());
								
								compet_parse.put("completed", competition.isCompleted());

								// compet_parse.saveEventually();
								ParseUtils.saveCompetition(compet_parse, competition);

								Iterator members = others.keys();
								int i = 0;
								for (final ChartMember entry : chart) {
									String key = entry.getId();

									String name = "";
									int steps = -1;
									try {
										name = (String) othersName.getString(key);
										steps = entry.getScore();// (Integer)
																	// others.getInt(key);
									} catch (JSONException e2) {
										// TODO Auto-generated catch block
										e2.printStackTrace();
									}
									group_members.get(i).setText(name);
									group_steps.get(i).setVisibility(View.VISIBLE);
									group_steps.get(i).setText(String.valueOf(steps));
									group_members.get(i).setClickable(false);
									group_members.get(i).setVisibility(View.VISIBLE);
									group_members.get(i).setBackgroundColor(Color.parseColor("#dcdcdc"));
									group_steps.get(i).setBackgroundColor(Color.parseColor("#dcdcdc"));
									group_members.get(i).setTextAppearance(getApplicationContext(), R.style.chartNormal);
									final TextView currentName = group_members.get(i);
									final TextView currentSteps = group_steps.get(i);
									final TextView currentMinus = group_minus.get(i);
									if (competition.getAmICreator()) {
										group_minus.get(i).setVisibility(View.VISIBLE);
										group_minus.get(i).setClickable(true);
										group_minus.get(i).setOnClickListener(new OnClickListener() {
											@Override
											public void onClick(View arg0) {
												if (FacebookUtils.isOnline(getApplicationContext())) {
													removeFromCompetition(entry);
													currentName.setVisibility(View.GONE);
													currentSteps.setVisibility(View.GONE);
													currentMinus.setVisibility(View.GONE);
												} else {
													showMessage(getString(R.string.connection_problem));
												}
											}
										});

									} else {
										group_minus.get(i).setVisibility(View.INVISIBLE);
									}

									if (key.equalsIgnoreCase(pref.getString("FBid", ""))) {
										group_members.get(i).setBackgroundColor(Color.parseColor("#f7fe2e"));
										group_steps.get(i).setBackgroundColor(Color.parseColor("#f7fe2e"));
										group_minus.get(i).setVisibility(View.INVISIBLE);
										System.out.println("TEST");
										System.out.println(competition.isCompleted());

										if (competition.isCompleted() && winner_id.equalsIgnoreCase(pref.getString("FBid", ""))) {// if (steps >= building.getSteps()) {
											percentage = 1.0;
											current_win = true;
											endCompetition(false);

											// if(isUpdate || isOpening){
											updatePoints(false, true);
											// saveBadges(true);
											// }
											chartHelpText.setText(getString(R.string.competition_win));
											chartHelpText.setVisibility(View.VISIBLE);
											showMessage(getString(R.string.competition_win));
											// Toast.makeText(getApplicationContext(), getString(R.string.competition_win), Toast.LENGTH_SHORT).show();
										}
									} else {
										group_members.get(i).setBackgroundColor(Color.parseColor("#dcdcdc"));
										group_steps.get(i).setBackgroundColor(Color.parseColor("#dcdcdc"));
										if (competition.getAmICreator())
											group_minus.get(i).setVisibility(View.VISIBLE);

										if (competition.isCompleted() && winner_id.equalsIgnoreCase(key)/* steps >= building.getSteps() */) {
											current_win = true;
											// if(isUpdate || isOpening)
											updatePoints(true, true);
											endCompetition(true);
											chartHelpText.setText(getString(R.string.competition_lose, name));
											chartHelpText.setVisibility(View.VISIBLE);
											showMessage(getString(R.string.competition_lose, name));
											// Toast.makeText(getApplicationContext(), getString(R.string.competition_lose, name), Toast.LENGTH_SHORT).show();
										}
									}
									i++;

								}
								if (i < group_members.size() && i <= ClimbApplication.N_MEMBERS_PER_GROUP) {
									group_steps.get(i).setVisibility(View.INVISIBLE);
									group_minus.get(i).setVisibility(View.INVISIBLE);
									group_members.get(i).setClickable(true);
									group_members.get(i).setText("  +");
									group_members.get(i).setVisibility(View.VISIBLE);
									//group_members.get(i).setBackgroundColor(Color.parseColor("#dcdcdc"));
									group_members.get(i).setBackgroundColor(getResources().getColor(R.color.light_teal));
									group_members.get(i).setTextAppearance(getApplicationContext(), R.style.chartButton);
									group_members.get(i).setOnClickListener(new OnClickListener() {

										@Override
										public void onClick(View arg0) {
											Bundle params = new Bundle();
											params.putString("data", "{\"idCollab\":\"" + competition.getId_online() + "\"," + "\"idBuilding\":\"" + building.get_id() + "\"," + "\"nameBuilding\":\""
													+ building.getName() + "\", \"type\": \"2\"}");
											params.putString("message", "Please, help me!!!!");
											if (Session.getActiveSession() != null && Session.getActiveSession().isOpened()) {

												WebDialog requestsDialog = (new WebDialog.RequestsDialogBuilder(ClimbActivity.this, Session.getActiveSession(), params)).setOnCompleteListener(new OnCompleteListener() {

													@Override
													public void onComplete(Bundle values, FacebookException error) {
														if (error != null) {
															if (error instanceof FacebookOperationCanceledException) {
																showMessage(getString(R.string.request_cancelled));
																// Toast.makeText(ClimbActivity.this, getString(R.string.request_cancelled), Toast.LENGTH_SHORT).show();

															} else {
																showMessage(getString(R.string.network_error));
																// Toast.makeText(ClimbActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();

															}
														} else {
															final String requestId = values.getString("request");
															if (requestId != null) {
																showMessage(getString(R.string.request_sent));
																// Toast.makeText(ClimbActivity.this, getString(R.string.request_sent), Toast.LENGTH_SHORT).show();
															} else {
																showMessage(getString(R.string.request_cancelled));
																// Toast.makeText(ClimbActivity.this, getString(R.string.request_cancelled), Toast.LENGTH_SHORT).show();

															}
														}
													}

												}).build();
												requestsDialog.show();
											} else {
												showMessage(getString(R.string.not_logged));
												// Toast.makeText(ClimbActivity.this, getString(R.string.not_logged), Toast.LENGTH_SHORT).show();
											}
										}
									});
									i++;
								}
								for (; i < group_members.size(); i++) {
									group_members.get(i).setClickable(false);
									group_members.get(i).setVisibility(View.INVISIBLE);
									group_steps.get(i).setVisibility(View.INVISIBLE);
									group_minus.get(i).setVisibility(View.INVISIBLE);

								}
							} else {
								showMessage(getString(R.string.kicked_out));
								// Toast.makeText(getApplicationContext(), getString(R.string.kicked_out), Toast.LENGTH_SHORT).show();
								ClimbApplication.competitionDao.delete(competition);
								apply_removed_from_competition();
								timer.cancel();
								// reset graphics
								current.setVisibility(View.GONE);
								for (int i = 0; i < group_members.size(); i++) {
									group_members.get(i).setVisibility(View.GONE);
									group_steps.get(i).setVisibility(View.GONE);
									group_minus.get(i).setVisibility(View.GONE);

								}
							}
						}
					} else if (!(mode == GameModeType.SOCIAL_CLIMB)) {
						// Toast.makeText(getApplicationContext(), getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
						showMessage(getString(R.string.connection_problem));
						showOfflineSocialMember();
						Log.e("updateChart", e.getMessage());
					}
					if (isUpdate) {
						//resetUpdating();
					}
					synchronized (ClimbApplication.lock) {
						in_progress = false;
						ClimbApplication.lock.notifyAll();
					}
				}
			});
		} else {
			showMessage(getString(R.string.check_connection));
			showOfflineSocialMember();

			if (isUpdate) {
				//resetUpdating();
			}
			synchronized (ClimbApplication.lock) {
				in_progress = false;
				ClimbApplication.lock.notifyAll();
			}
		}

	}

	private void updatePoints(boolean penalty, boolean saveOnline) {
		if (difficulty == 0) {
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			difficulty = Integer.parseInt(settings.getString("difficulty", "10"));
		}
		int realSteps = new_steps / difficulty;
		System.out.println("update points " + difficulty);
		int newXP = 0;
		switch (difficulty) {
		case 1:// 5
			newXP = realSteps * 3;
			break;
		case 10:
			newXP = realSteps * 2;
			break;
		case 100:// 1
			newXP = realSteps;
			break;
		}
		if (mode == GameModeType.SOCIAL_CLIMB && percentage >= 1.00 && !penalty) {// 20%
																					// over
																					// the
																					// total
			int extra = (20 * building.getSteps()) / 100;
			newXP += extra;
			showMessage(getString(R.string.bonus_collaboration, extra));
		}
		if (mode == GameModeType.SOCIAL_CHALLENGE && percentage >= 1.00) {// 20%
																			// over
																			// the
																			// total
			int extra = (20 * building.getSteps()) / 100;
			newXP += extra;
			showMessage(getString(R.string.bonus_competition, extra));
		}
		if (mode == GameModeType.TEAM_VS_TEAM && percentage >= 1.00 && !penalty) {// 20%
																					// over
																					// the
																					// total
			int extra = (20 * building.getSteps()) / 100;
			newXP += extra;
			showMessage(getString(R.string.bonus_team_duel, extra));
		}
		final User me = currentUser;
		me.addObserver(this);
		int newLevel = ClimbApplication.levelUp(me.getXP() + newXP, me.getLevel());
		me.setXP(me.getXP() + newXP);
		if (newLevel != me.getLevel()) {
			me.setLevel(newLevel);
			// showMessage(getString(R.string.new_level));
			int notification_icon = (Build.VERSION.SDK_INT < 11) ? R.drawable.ic_stat_cup_dark : R.drawable.ic_stat_cup_light;
			if ((Build.VERSION.SDK_INT < 16))
				showNotificationNewLevel(newLevel, notification_icon);
			else {
				ClimbApplication.updateLevelNotification(newLevel);
				showInboxNotification();
			}

		}
		ClimbApplication.userDao.update(me);
		if (ParseUser.getCurrentUser() != null) {
			ParseUser currentUser = ParseUser.getCurrentUser();
			currentUser.put("XP", me.getXP());
			currentUser.put("level", me.getLevel());
			// currentUser.saveEventually();
			if (saveOnline)
				ParseUtils.saveUserInParse(currentUser);
		}
		/*
		 * ParseQuery<ParseUser> query = ParseUser.getQuery(); query.whereEqualTo("FBid", me.getFBid()); query.getFirstInBackground(new GetCallback<ParseUser>() {
		 * 
		 * @Override public void done(ParseUser user, ParseException e) { if(e == null){ user.put("XP", me.getXP()); user.put("level", me.getLevel()); user.saveEventually(); }else{ Toast.makeText(getApplicationContext(), "Connection Available. Your data will be saved during next connection" , Toast.LENGTH_SHORT).show(); Log.e("updatePoints", e.getMessage()); }
		 * 
		 * } });
		 */
	}

	private UserBadge checkBuildingBadge(User me) {
		Badge badge = ClimbApplication.getBadgeByCategory(0);
		UserBadge userbadge = ClimbApplication.getUserBadgeForUserAndBadge(badge.get_id(), building.get_id(), pref.getInt("local_id", -1));
		if (userbadge == null) {
			userbadge = new UserBadge();
			userbadge.setBadge(badge);
			userbadge.setObj_id(building.get_id());
			userbadge.setPercentage(percentage);
			userbadge.setUser(me);
			userbadge.setSaved(false);
			ClimbApplication.userBadgeDao.create(userbadge);
			if (percentage >= 1.00) {
				showMessage(getString(R.string.new_badge, buildingText.getName()));
				NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.unlock_win).setContentTitle("Climb The World").setContentText(getString(R.string.new_badge, buildingText.getName()));
				NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				// mId allows you to update the notification later on.
				mNotificationManager.notify(NOTIFICATION_ID_BADGE, mBuilder.build());
				// Toast.makeText(getApplicationContext(), , Toast.LENGTH_SHORT).show();
			}

		} else {
			double old_percentage = userbadge.getPercentage();
			if (userbadge.getPercentage() < percentage) {
				userbadge.setPercentage(percentage);
				userbadge.setSaved(false);
				ClimbApplication.userBadgeDao.update(userbadge);
			}
			if (old_percentage < 1.00 && percentage >= 1.00) {
				Toast.makeText(getApplicationContext(), getString(R.string.new_badge, buildingText.getName()), Toast.LENGTH_SHORT).show();
				System.out.println("notifico");
				NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.unlock_win).setContentTitle("Climb The World").setContentText(getString(R.string.new_badge, buildingText.getName()));
				NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				// mId allows you to update the notification later on.
				mNotificationManager.notify(NOTIFICATION_ID_BADGE, mBuilder.build());
			}
		}
		return userbadge;
	}

	private List<UserBadge> checkTourBadge(User me) {
		List<UserBadge> ris = new ArrayList<UserBadge>();
		Badge badge = ClimbApplication.getBadgeByCategory(1);
		List<Tour> tours = ClimbApplication.getToursByBuilding(building.get_id());
		for (Tour tour : tours) {
			UserBadge ub = ClimbApplication.getUserBadgeForUserAndBadge(badge.get_id(), tour.get_id(), me.get_id());
			if (ub == null) {
				ub = new UserBadge();
				ub.setBadge(badge);
				ub.setObj_id(tour.get_id());
				if (percentage >= 1.00)
					num_steps = building.getSteps();
				double percentage = ((double) tour.getDoneSteps(new_steps, building.get_id()) / (double) tour.getTotalSteps());
				// double percentage = ((double) (num_steps) / (double)
				// tour.getTotalSteps());
				ub.setPercentage(percentage);
				ub.setUser(me);
				ub.setSaved(false);
				ClimbApplication.userBadgeDao.create(ub);

			} else {
				if (percentage >= 1.00)
					num_steps = building.getSteps();
				double percentage = ((double) tour.getDoneSteps(new_steps, building.get_id()) / (double) tour.getTotalSteps());
				// double percentage = ((double) (num_steps) / (double)
				// tour.getTotalSteps());
				double old_percentage_tour = ub.getPercentage();
				ub.setPercentage(percentage);
				ub.setSaved(false);
				ClimbApplication.userBadgeDao.update(ub);
				if (percentage >= 1.00 && old_percentage_tour < 1.00)
					showMessage(getString(R.string.new_badge, tour.getTitle()));

			}
			ris.add(ub);

		}
		return ris;
	}

	private void saveBadges(final boolean saveOnline) {
		System.out.println("save badges");

		User me = ClimbApplication.getUserById(pref.getInt("local_id", -1));
		final List<UserBadge> updateUb = new ArrayList<UserBadge>();
		updateUb.add(checkBuildingBadge(me));
		updateUb.addAll(checkTourBadge(me));

		if (!pref.getString("FBid", "none").equalsIgnoreCase("none") && !pref.getString("FBid", "none").equalsIgnoreCase("empty")) {

			if (ParseUser.getCurrentUser() != null) {
				ParseUser user = ParseUser.getCurrentUser();
				for (final UserBadge ub : updateUb) {
					JSONObject newBadge = new JSONObject();
					try {
						JSONArray bs = user.getJSONArray("badges");
						int currentBadge = ClimbApplication.lookForBadge(ub.getBadge().get_id(), ub.getObj_id(), bs);
						if (currentBadge != -1) {
							bs = ModelsUtil.removeFromJSONArray(bs, currentBadge);
						}

						newBadge.put("badge_id", ub.getBadge().get_id());
						newBadge.put("obj_id", ub.getObj_id());
						newBadge.put("percentage", ub.getPercentage());
						bs.put(newBadge);
						user.put("badges", bs);
						// user.saveEventually();
						ub.setSaved(true);
						ClimbApplication.userBadgeDao.update(ub);
					} catch (JSONException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				if (saveOnline)
					ParseUtils.saveUserInParse(user);
			}
		}

		ClimbApplication.refreshUserBadge();
	}

	public void onMicroGoalClicked() {
		try {

			// Microgoal microgoal = ClimbApplication.getMicrogoalByUserAndBuilding(pref.getInt("local_id", -1), building.get_id());

			if (microgoal != null) {
				MicrogoalText texts = ModelsUtil.getMicrogoalTextByStory(microgoal.getStory_id());// ClimbApplication.getMicrogoalTextByStory(microgoal.getStory_id());

				final Dialog dialog = new Dialog(this, R.style.FullHeightDialog);
				dialog.setContentView(R.layout.dialog_micro_goal);
				dialog.setCancelable(true);
				LayoutParams params = dialog.getWindow().getAttributes();
				params.height = LayoutParams.WRAP_CONTENT;
				params.width = LayoutParams.MATCH_PARENT;
				dialog.getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

				JSONObject steps_obj = texts.getSteps();

				int checked_size = steps_obj.length();
				int steps_per_part = microgoal.getTot_steps() / checked_size;
				int resume = microgoal.getTot_steps() % checked_size;

				String steps[] = new String[checked_size];
				Boolean checked[] = new Boolean[checked_size];
				Integer climbs[] = new Integer[checked_size];
				Iterator<String> keys = steps_obj.keys();

				
				for (int k = 0; k < checked_size; k++) {
					int currents_steps = steps_per_part;
					int check_steps = steps_per_part;
					if (k == checked_size - 1 && resume != 0) {
						check_steps = (currents_steps * (k + 1) + resume) ;
						//						currents_steps += resume;
					} else {
						check_steps = steps_per_part * (k + 1);
					}
					steps[k] = String.format((steps_obj.getString(keys.next())), currents_steps);
					checked[k] = microgoal.getDone_steps() >= check_steps ? true : false;
					climbs[k] = currents_steps;
				}

				TableLayout layout = (TableLayout) dialog.findViewById(R.id.checkBoxesLayout);

				String intro = "";
				// Random rand = new Random();
				// int randomNum1 = rand.nextInt((10 - 1) + 1) + 1;
				// int randomNum2 = rand.nextInt((20 - randomNum1) + 1) +
				// randomNum1;
				int randomNum1 = Integer.valueOf(climbs[0]) / 5;

				if (checked_size == 1)
					intro = String.format(texts.getIntro(), randomNum1) /*+ getString(R.string.bonus_excluded)*/;
				else if (checked_size == 2) {
					int randomNum2 = Integer.valueOf(climbs[0] + climbs[1]) / 5;
					intro = String.format(texts.getIntro(), randomNum1, randomNum2) /*+ getString(R.string.bonus_excluded)*/;
				}

				// to set the message
				TextView message = (TextView) dialog.findViewById(R.id.tvmessagedialogtext);
				message.setText(intro);

				TextView reward = (TextView) dialog.findViewById(R.id.textReward);
				reward.setText(ClimbApplication.getContext().getString(R.string.reward_dialog, 100));

				if (steps.length == 1)
					((CheckBox) dialog.findViewById(R.id.checkBox2)).setVisibility(View.GONE);

				for (int i = 0; i < steps.length; i++) {
					/*
					 * TableRow row =new TableRow(activity); row.setId(i); row.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT ,LayoutParams.WRAP_CONTENT)); CheckBox checkBox = new CheckBox(activity); checkBox.setEnabled(false); checkBox.setId(i); checkBox.setText(steps[i]); checkBox.setChecked(checked[i]); row.addView(checkBox); layout.addView(row); checkBox.setWidth(LayoutParams.WRAP_CONTENT); checkBox.setHeight(LayoutParams.WRAP_CONTENT);
					 */
					CheckBox cb = (CheckBox) dialog.findViewById(ClimbApplication.getContext().getResources().getIdentifier("checkBox" + (i + 1), "id", this.getPackageName()));
					cb.setText(steps[i]);
					cb.setTextColor(ClimbApplication.getContext().getResources().getColor(R.color.black));
					cb.setChecked(checked[i]);
					cb.setClickable(false);
				}

				// add some action to the buttons
				Button acceptBtn = (Button) dialog.findViewById(R.id.bmessageDialogYes);
				acceptBtn.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						dialog.dismiss();
					}
				});
				// System.out.println("SHOOOOW");
				// DisplayMetrics metrics = this.getResources().getDisplayMetrics();
				// int width = metrics.widthPixels;
				// int height = metrics.heightPixels;
				// dialog.getWindow().setLayout((6 * width)/7, (4 * height)/5);

				ProgressBar pb = (ProgressBar) dialog.findViewById(R.id.progressBarDialog);
				TextView perc = (TextView) dialog.findViewById(R.id.textPercentageDialog);
				double percentage = Math.round(((double) microgoal.getDone_steps() / (double) microgoal.getTot_steps()) * 100);
				pb.setIndeterminate(false);
				if(percentage >= 100) percentage = 100;
				pb.setProgress((int) percentage);
				perc.setText(String.valueOf(percentage) + "%");

				dialog.show();
			} else {
				Toast.makeText(getApplicationContext(), getString(R.string.no_microgoal), Toast.LENGTH_SHORT).show();
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void removeFromCompetition(ChartMember member) {

		JSONObject stairs = compet_parse.getJSONObject("stairs");
		JSONObject competitors = compet_parse.getJSONObject("competitors");
		stairs.remove(member.getId());
		competitors.remove(member.getId());
		compet_parse.put("stairs", stairs);
		compet_parse.put("competitors", competitors);
		compet_parse.saveInBackground(new SaveCallback() {

			@Override
			public void done(ParseException e) {
				if (e != null) {
					Toast.makeText(getApplicationContext(), "Retry later", Toast.LENGTH_SHORT).show();
					Log.e("removeFromCompetition", e.getMessage());
				}

			}
		});
	}

	private void removeFromCollaboration(final ChartMember member) {

		JSONObject stairs = collab_parse.getJSONObject("stairs");
		JSONObject collaborators = collab_parse.getJSONObject("collaborators");
		stairs.remove(member.getId());
		collaborators.remove(member.getId());
		collab_parse.put("stairs", stairs);
		collab_parse.put("collaborators", collaborators);
		collab_parse.saveInBackground(new SaveCallback() {

			@Override
			public void done(ParseException e) {
				if (e != null) {
					Toast.makeText(getApplicationContext(), "Retry later", Toast.LENGTH_SHORT).show();
					Log.e("removeFromCollaboration", e.getMessage());
				} else {
					collaboration.setOthers_stairs(collaboration.getOthers_stairs() - member.getScore());
					ClimbApplication.collaborationDao.update(collaboration);
				}

			}
		});
	}

	private void apply_removed_from_competition() {
		((ImageButton) findViewById(R.id.btnStartClimbing)).setImageResource(R.drawable.av_play);
		mode = GameModeType.SOLO_CLIMB;
		if (soloClimb != null) {
			deleteClimbingInParse(climbing);
			soloClimb.setGame_mode(0);
			soloClimb.setId_mode("");
			ClimbApplication.climbingDao.update(soloClimb);
			updateClimbingInParse(soloClimb, true);
			climbing = soloClimb;
		} else {
			System.out.println("scalato x prima volta");
			climbing.setGame_mode(0);
			climbing.setId_mode("");
			ClimbApplication.climbingDao.update(climbing);
			updateClimbingInParse(climbing, false);
		}
	}

	private void apply_removed_from_collaboration() {
		((ImageButton) findViewById(R.id.btnStartClimbing)).setImageResource(R.drawable.av_play);
		mode = GameModeType.SOLO_CLIMB;
		climbing.setGame_mode(0);
		climbing.setId_mode("");
		updateClimbingInParse(climbing, false);
	}

	/**
	 * Shows a given message into a toast in the main thread
	 * 
	 * @param message
	 *            the message to be shown
	 */
	private void showMessage(final String message) {
		ClimbActivity.this.runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void showNotificationNewLevel(final int newLevel, final int drawable) {
		final int REQUEST_CODE_BASE = 1000;

		final int requestID2 = REQUEST_CODE_BASE + (int) System.currentTimeMillis();
		final Intent contentIntent = new Intent(ClimbActivity.this, NotificationClickedService.class);

		final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext()).setSmallIcon(drawable).setLargeIcon(BitmapFactory.decodeResource(getResources(), drawable)).setContentTitle("Climb The World");
		// PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
		// mBuilder.setContentIntent(contentIntent);
		mBuilder.setContentIntent(PendingIntent.getService(ClimbActivity.this, requestID2, contentIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT));

		ClimbActivity.this.runOnUiThread(new Runnable() {
			public void run() {

				mBuilder.setContentText(getString(R.string.new_level, newLevel));
				mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.new_level, newLevel)));
				NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

				mNotificationManager.notify(NOTIFICATION_ID_LEVEL, mBuilder.build());

			}
		});
	}

	private void showNotificationBonus(final String message, final int drawable, final boolean bonus) {
		final int REQUEST_CODE_BASE = 1000;

		final Intent deleteIntent = new Intent(ClimbActivity.this, NotificationDeletedReceiver.class); // new Intent("org.unipd.nbeghin.climbtheworld.services.NotificationDeletedReceiver");
		final Intent contentIntent = new Intent(ClimbActivity.this, NotificationClickedService.class);
		final int requestID = REQUEST_CODE_BASE + (int) System.currentTimeMillis();
		final int requestID2 = REQUEST_CODE_BASE + (int) System.currentTimeMillis();
		final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext()).setSmallIcon(drawable).setLargeIcon(BitmapFactory.decodeResource(getResources(), drawable)).setContentTitle(getString(R.string.notification_title, buildingText.getName()));
		// .addAction(R.drawable.ic_action_help_dark, "Test", PendingIntent.getService(ClimbActivity.this, requestID, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT))
		// .setContentIntent(PendingIntent.getService(ClimbActivity.this, requestID, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT))
		// .setContentIntent(PendingIntent.getBroadcast(ClimbActivity.this, requestID, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT))
		// .setDeleteIntent(PendingIntent.getBroadcast(ClimbActivity.this, requestID, deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT));

		// PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
		// mBuilder.setContentIntent(contentIntent);
		mBuilder.setContentIntent(PendingIntent.getService(ClimbActivity.this, requestID2, contentIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT));

		ClimbActivity.this.runOnUiThread(new Runnable() {
			public void run() {

				int id = (bonus == true) ? NOTIFICATION_ID_BONUS : NOTIFICATION_ID_BADGE;
				// Intent deleteIntent = new Intent(ClimbActivity.this, NotificationDeletedReceiver.class);

				if (!bonus) {
					mBuilder.setContentText(message);
					mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(message));
				} else {
					int bonus = Integer.valueOf(message);
					ClimbApplication.bonus_notification += bonus;
					mBuilder.setContentText(getString(R.string.notification_bonus, ClimbApplication.bonus_notification));
					mBuilder.setDeleteIntent(PendingIntent.getService(ClimbActivity.this, requestID, deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT))
					.setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.notification_bonus)
							+ ClimbApplication.bonus_notification));

				}

				// NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(getApplicationContext());

				NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				// mId allows you to update the notification later on.

				mNotificationManager.notify(id, mBuilder.build());
				if (!bonus)
					id++;
			}
		});
	}

	private void showInboxNotification() {

		final int REQUEST_CODE_BASE = 1000;
		final Intent deleteIntent = new Intent(ClimbActivity.this, NotificationDeletedReceiver.class); // new Intent("org.unipd.nbeghin.climbtheworld.services.NotificationDeletedReceiver");
		final Intent contentIntent = new Intent(ClimbActivity.this, NotificationClickedService.class);
		final int requestID = REQUEST_CODE_BASE + (int) System.currentTimeMillis();
		final int requestID2 = REQUEST_CODE_BASE + (int) System.currentTimeMillis();

		// Intent ci = new Intent(getApplicationContext(), MainActivity.class);
		// ci.setAction(Intent.ACTION_MAIN);
		// ci.addCategory(Intent.CATEGORY_LAUNCHER);
		// PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, /*new Intent()*/ ci, PendingIntent.FLAG_UPDATE_CURRENT);

		int notification_icon = (Build.VERSION.SDK_INT < 11) ? R.drawable.ic_stat_cup_dark : R.drawable.ic_stat_cup_light;
		int notification_size = ClimbApplication.notifications_inbox_contents.size();

		final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(notification_icon).setLargeIcon(BitmapFactory.decodeResource(getResources(), notification_icon)).setContentTitle("Climb the World")
		// .setContentText(getResources().getQuantityString(R.plurals.summary_inbox_text_2, notification_size, notification_size))
		.setContentText("Climb the World").setDeleteIntent(PendingIntent.getService(ClimbActivity.this, requestID, deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT))
		.setContentIntent(PendingIntent.getService(ClimbActivity.this, requestID2, contentIntent, PendingIntent.FLAG_CANCEL_CURRENT
				| PendingIntent.FLAG_ONE_SHOT));

		NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
		List<Spannable> events = new ArrayList<Spannable>();

		System.out.println("notifico " + ClimbApplication.notifications_inbox_contents.toString());

		// add bonus line
		int current_bonus = Integer.valueOf(Iterables.get(ClimbApplication.notifications_inbox_contents.get("BONUS"), 0));
		Spannable sb = new SpannableString(getString(R.string.notification_bonus, current_bonus));
		int end_index = sb.toString().indexOf("+");
		sb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, end_index - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		if (current_bonus > 0)
			events.add(sb);
		else
			notification_size -= 1;

		// add level line
		int current_level = Integer.valueOf(Iterables.get(ClimbApplication.notifications_inbox_contents.get("LEVEL"), 0));
		sb = new SpannableString(getString(R.string.new_level_inbox, current_level));
		end_index = sb.toString().indexOf("L");
		sb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, end_index - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		if (current_level > 0)
			events.add(sb);
		else
			notification_size -= 1;

		inboxStyle.setSummaryText(getResources().getQuantityString(R.plurals.summary_inbox_text_2, notification_size, notification_size));
		mBuilder.setContentText(getResources().getQuantityString(R.plurals.summary_inbox_text_2, notification_size, notification_size));

		// add badges lines
		Collection<String> badges = ClimbApplication.notifications_inbox_contents.get("BADGE");
		if (badges.size() > 0) {
			int n_current_missing_lines = 5 - events.size();
			Iterator<String> it = badges.iterator();
			int added_lines = 0;
			for (added_lines = 0; added_lines < n_current_missing_lines && it.hasNext(); added_lines++) {
				String b = (String) it.next();
				end_index = b.indexOf("!");
				b = b.replace("!", "");
				sb = new SpannableString(b);
				sb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, end_index, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				events.add(sb);
			}

			int remaining_lines = badges.size() - added_lines;
			if (remaining_lines > 0)
				inboxStyle.setSummaryText(getResources().getQuantityString(R.plurals.summary_inbox_text, remaining_lines, remaining_lines));

		}

		inboxStyle.setBigContentTitle(getString(R.string.notification_title, buildingText.getName()));
		for (int i = 0; i < events.size(); i++) {
			inboxStyle.addLine(events.get(i));
		}
		mBuilder.setStyle(inboxStyle);

		ClimbActivity.this.runOnUiThread(new Runnable() {
			public void run() {
				NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				mNotificationManager.notify(NOTIFICATION_ID_INBOX, mBuilder.build());
			}
		});

	}

	private void showOfflineSocialMember() {
		ClimbActivity.this.runOnUiThread(new Runnable() {
			public void run() {
				for (int i = 0; i < group_members.size(); i++) {
					group_members.get(i).setVisibility(View.GONE);
					group_steps.get(i).setVisibility(View.GONE);
					group_minus.get(i).setVisibility(View.GONE);

				}
				group_members.get(0).setVisibility(View.VISIBLE);
				group_members.get(0).setText("Go online to see your friends' results");
			}
		});
	}

	@Override
	public void onRestart() {
		super.onRestart();
		Log.i("ClimbActivity", "onRestart");
	}

	@Override
	public void update(Observable observable, Object data) {
		JSONArray modified_badges = new JSONArray();
		List<UserBadge> ubs = new ArrayList<UserBadge>();
		if (data instanceof Climbing) {
			// check building
			Badge badge = ClimbApplication.getBadgeByCategory(0); 
			System.out.println("badge id " + badge.get_id());
			System.out.println("building id" + building.get_id());
			System.out.println("user " + currentUser.get_id());
			UserBadge userbadge = ClimbApplication.getUserBadgeForUserAndBadge(badge.get_id(), building.get_id(), currentUser.get_id());
			saveBuildingBadge(userbadge, badge, (Climbing) data);// saves locally
			ubs.add(userbadge);
			try {
				modified_badges.put(new JSONObject(userbadge.toJSON()));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// check tour
			badge = ClimbApplication.getBadgeByCategory(1);
			List<Tour> tours = ClimbApplication.getToursByBuilding(building.get_id());

			for (Tour tour : tours) {
				UserBadge ub = ClimbApplication.getUserBadgeForUserAndBadge(badge.get_id(), tour.get_id(), currentUser.get_id());
				saveTourBadge(ub, badge, tour, (Climbing) data);
				ubs.add(ub);
				try {
					modified_badges.put(new JSONObject(userbadge.toJSON()));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		} else if (data instanceof User) {
			System.out.println("instance of user");
			// da usare per creare badge legati ad utente
		} else if (data instanceof String) {
			try {
				updateTrophies(ubs, modified_badges);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Saves the given badge progress locally
	 * 
	 * @param userbadge
	 *            the userbadge object to update
	 * @param badge
	 *            the badge category
	 * @param climbing
	 *            the climbing data to use to update the badge
	 */
	void saveBuildingBadge(UserBadge userbadge, Badge badge, Climbing climbing) {
		int notification_icon = (Build.VERSION.SDK_INT < 11) ? R.drawable.ic_stat_cup_dark : R.drawable.ic_stat_cup_light;

		if (userbadge == null) {// se prima non c'era lo creo
			userbadge = new UserBadge();
			userbadge.setBadge(badge);
			userbadge.setObj_id(climbing.getBuilding().get_id());
			userbadge.setPercentage(climbing.getPercentage());
			userbadge.setUser(climbing.getUser());
			userbadge.setSaved(false);
			ClimbApplication.userBadgeDao.create(userbadge);

			if (percentage >= 1.00) {
				// showMessage(getString(R.string.new_badge, buildingText.getName()));
				if (Build.VERSION.SDK_INT < 16)
					showNotificationBonus(getString(R.string.new_badge, userbadge.getName()), notification_icon, false);
				else {
					// show inbox notification
					ClimbApplication.addBadgeNotification(getString(R.string.new_badge_inbox, userbadge.getName()));
					showInboxNotification();
				}
			}
		} else {// altrimenti aggiorno quello gi� presente
			double old_percentage = userbadge.getPercentage();
			if (userbadge.getPercentage() < climbing.getPercentage()) {
				userbadge.setPercentage(climbing.getPercentage());
				userbadge.setSaved(false);
				ClimbApplication.userBadgeDao.update(userbadge);
			}
			if (old_percentage < 1.00 && percentage >= 1.00) {
				// showMessage(getString(R.string.new_badge, buildingText.getName()));
				if (Build.VERSION.SDK_INT < 16)
					showNotificationBonus(getString(R.string.new_badge, userbadge.getName()), notification_icon, false);
				else {
					// show inbox notification
					ClimbApplication.addBadgeNotification(getString(R.string.new_badge_inbox, userbadge.getName()));
					showInboxNotification();
				}
			}
		}
	}

	/**
	 * Saves the given badge progress locally
	 * 
	 * @param ub
	 *            the userbadge object to update
	 * @param badge
	 *            the badge category
	 * @param tour
	 *            the tour data to use to update the badge
	 * @param climbing
	 *            the climbing data to use to update the badge
	 */
	void saveTourBadge(UserBadge ub, Badge badge, Tour tour, Climbing climbing) {
		if (ub == null) {
			ub = new UserBadge();
			ub.setBadge(badge);
			ub.setObj_id(tour.get_id());
			if (percentage >= 1.00)
				num_steps = building.getSteps();
			double percentage = ((double) tour.getDoneSteps(new_steps, building.get_id()) / (double) tour.getTotalSteps());
			// double percentage = ((double) (num_steps) / (double)
			// tour.getTotalSteps());
			ub.setPercentage(climbing.getPercentage());
			ub.setUser(climbing.getUser());
			ub.setSaved(false);
			ClimbApplication.userBadgeDao.create(ub);

		} else {
			if (percentage >= 1.00)
				num_steps = building.getSteps();
			double percentage = ((double) tour.getDoneSteps(new_steps, building.get_id()) / (double) tour.getTotalSteps());
			// double percentage = ((double) (num_steps) / (double)
			// tour.getTotalSteps());
			double old_percentage_tour = ub.getPercentage();
			ub.setPercentage(climbing.getPercentage());
			ub.setSaved(false);
			ClimbApplication.userBadgeDao.update(ub);
			if (percentage >= 1.00 && old_percentage_tour < 1.00)
				showMessage(getString(R.string.new_badge, tour.getTitle()));

		}
	}

	void updateTrophies(List<UserBadge> ubs, JSONArray arrayToSave) throws JSONException {
		if (!pref.getString("FBid", "none").equalsIgnoreCase("none") && !pref.getString("FBid", "none").equalsIgnoreCase("empty")) {

			if (ParseUser.getCurrentUser() != null) {
				ParseUser user = ParseUser.getCurrentUser();
				JSONArray bs = user.getJSONArray("badges");

				Gson gson = new GsonBuilder().registerTypeAdapter(HashBasedTable.class, new JsonDeserializer<HashBasedTable>() {

					@SuppressWarnings({ "unchecked", "rawtypes" })
					@Override
					public HashBasedTable deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
						HashBasedTable<Integer, Integer, Double> table = HashBasedTable.create();
						JsonArray array = json.getAsJsonArray();
						for (int i = 0; i < array.size(); i++) {
							JsonObject object = array.get(i).getAsJsonObject();
							JsonElement element1 = object.get("badge_id");
							int badge_id = element1.getAsInt();
							JsonElement element2 = object.get("obj_id");
							int obj_id = element2.getAsInt();
							JsonElement element3 = object.get("percentage");
							double percentage = element3.getAsDouble();
							table.put(badge_id, obj_id, percentage);
						}
						return table;
					}
				})

				.setPrettyPrinting().create();
				HashBasedTable<Integer, Integer, Double> bs_editable = gson.fromJson(bs.toString(), HashBasedTable.class);

				for (int i = 0; i < arrayToSave.length(); i++) {
					JSONObject obj = arrayToSave.getJSONObject(i);
					bs_editable.put(obj.getInt("badge_id"), obj.getInt("obj_id"), obj.getDouble("percentage"));
				}

				bs = new JSONArray(gson.toJson(bs_editable));
				user.put("badges", bs);
				ParseUtils.saveBadgesInParse(user, ubs);
			}
		}

		ClimbApplication.refreshUserBadge();
	}
	
	

	public static boolean isGameActive(){
		return samplingEnabled;
	}
	
	
}
