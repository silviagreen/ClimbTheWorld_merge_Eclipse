package org.unipd.nbeghin.climbtheworld;

import it.sephiroth.android.library.tooltip.TooltipManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.unipd.nbeghin.climbtheworld.adapters.PagerAdapter;
import org.unipd.nbeghin.climbtheworld.fragments.BadgesFragment;
import org.unipd.nbeghin.climbtheworld.fragments.BuildingsFragment;
import org.unipd.nbeghin.climbtheworld.fragments.NotificationFragment;
import org.unipd.nbeghin.climbtheworld.fragments.ToursFragment;
import org.unipd.nbeghin.climbtheworld.models.AskCollaborationNotification;
import org.unipd.nbeghin.climbtheworld.models.AskCompetitionNotification;
import org.unipd.nbeghin.climbtheworld.models.AskTeamDuelNotification;
import org.unipd.nbeghin.climbtheworld.models.GameNotification;
import org.unipd.nbeghin.climbtheworld.models.InviteNotification;
import org.unipd.nbeghin.climbtheworld.models.Notification;
import org.unipd.nbeghin.climbtheworld.models.NotificationType;
import org.unipd.nbeghin.climbtheworld.models.User;
import org.unipd.nbeghin.climbtheworld.util.FacebookUtils;
import org.unipd.nbeghin.climbtheworld.util.GraphicsUtils;
import org.unipd.nbeghin.climbtheworld.util.LogUtils;
import org.unipd.nbeghin.climbtheworld.util.ModelsUtil;
import org.unipd.nbeghin.climbtheworld.R;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.facebook.widget.WebDialog;

/**
 * Main activity
 * 
 */
@SuppressLint("NewApi")
public class MainActivity extends ActionBarActivity implements NetworkRequests, ActionBar.TabListener, TooltipManager.onTooltipClosingCallback {
	private static final String APP_TITLE = "Climb the world";
	public static final String AppName = "ClimbTheWorld";

	private List<Fragment> fragments = new Vector<Fragment>(); // list of fragments to be loaded
	private PagerAdapter mPagerAdapter; // page adapter for ViewPager

	private ViewPager mPager;
	private static Context sContext;

	private WebDialog dialog = null;
	private String dialogAction = null;
	private Bundle dialogParams = null;
	
	ProgressDialog PD;
	//finestra di dialogo che propone all'utente di configurare l'algoritmo
	private static Dialog alertDialog;

	private String requestId;
	SharedPreferences pref;

	private UiLifecycleHelper uiHelper;
	private Session mSession;
	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};
	
	public static RelativeLayout waitLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		pref = getSharedPreferences("UserSession", 0);
		
	//	Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_mainLayout);
	//	setSupportActionBar(toolbar);

		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);

		// DEBUG: used to obtain the key hash to use to enable Facebook login in
		// sandbox mode

		// try {
		// PackageInfo info =
		// getPackageManager().getPackageInfo("org.unipd.nbeghin.climbtheworld",
		// PackageManager.GET_SIGNATURES);
		// for (Signature signature : info.signatures) {
		// MessageDigest md = MessageDigest.getInstance("SHA");
		// md.update(signature.toByteArray());
		// Log.d("KeyHash:", Base64.encodeToString(md.digest(),
		// Base64.DEFAULT));
		// }
		// } catch (NameNotFoundException e) {
		//
		// } catch (NoSuchAlgorithmException e) {
		//
		// }
		
	
		
		waitLayout = (RelativeLayout) findViewById(R.id.waitLayout);
		
		
		sContext = getApplicationContext();

		// loading fragments
		fragments.add(Fragment.instantiate(this, BuildingsFragment.class.getName())); // instance building fragments
		fragments.add(Fragment.instantiate(this, ToursFragment.class.getName())); // instance tours fragments
		fragments.add(Fragment.instantiate(this, NotificationFragment.class.getName()));
		fragments.add(Fragment.instantiate(this, BadgesFragment.class.getName()));

		mPagerAdapter = new PagerAdapter(super.getSupportFragmentManager(), fragments);
		mPager = (ViewPager) super.findViewById(R.id.pager);
		mPager.setAdapter(this.mPagerAdapter);
		
		
		final ActionBar action_bar = getSupportActionBar();
		action_bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		String names[] = {getString(R.string.buildings), getString(R.string.tours), getString(R.string.notifications), getString(R.string.trophies)};
		//add tabs
		for(String tab_name : names)
			action_bar.addTab(action_bar.newTab().setText(tab_name).setTabListener(this));
		
		//on swiping
		mPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				action_bar.setSelectedNavigationItem(position);
				
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		mPager.setOffscreenPageLimit(0);
		
		
	}
	
	
	
	public static void showWaitLayout(){
		waitLayout.setVisibility(ViewPager.VISIBLE);
	}
	
	public static void hideWaitLayout(){
		waitLayout.setVisibility(View.GONE);
	}

	@Override
	protected void onStart() {
		super.onStart();
		// if not logged in, login default owner user

		if (pref.getInt("local_id", -1) == -1) {
			setUserOwner();
		} else {
			ClimbApplication.refreshClimbings();
			ClimbApplication.refreshCollaborations();
			ClimbApplication.refreshCompetitions();
			ClimbApplication.refreshMicrogoals();
			ClimbApplication.refreshTeamDuels();
			ClimbApplication.refreshUserBadge();
		}

        Intent i = getIntent();
        boolean isFromSplashScreen = false;

        if(i != null && i.getExtras() != null)
            isFromSplashScreen = getIntent().getExtras().getBoolean("FirstOpen");//i.getBooleanExtra("FirstOpen", false);

		if (!FacebookUtils.isOnline(sContext) && isFromSplashScreen) {
			Toast t = Toast.makeText(getContext(), getString(R.string.offline), Toast.LENGTH_LONG);
			t.setGravity(Gravity.TOP, 0, 300);
			t.show();
		}
		
		
        if(!PreferenceManager.getDefaultSharedPreferences(sContext).getBoolean("algorithm_configured", false) && isFromSplashScreen){
			
			//si crea una finestra di dialogo che propone all'utente di configurare l'algoritmo
			alertDialog = new Dialog(this);			
	        LayoutInflater inflater = getLayoutInflater();
	        View dialoglayout = inflater.inflate(R.layout.dialog_algorithm_config, null);
	        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
	        alertDialog.setContentView(dialoglayout);
	        alertDialog.setCancelable(false);
	        alertDialog.show();
		}



        boolean is_alg_config = false;
        if(i != null && i.getExtras() != null)
            is_alg_config = getIntent().getExtras().getBoolean("end_config_alg");

        if(is_alg_config) {
            startOnboarding();
            getIntent().removeExtra("end_config_alg");
        }

        if(isFromSplashScreen)
            getIntent().removeExtra("FirstOpen");
		
        
        	
        
	}
	
	public void startOnboarding(){
		
		// onboarding message 1
				if (pref.getBoolean("first_open_1", true)) {
					if(!pref.getBoolean("done_tutorial", false)){
						Intent intent = new Intent(getContext(), OnBoardingActivity.class);
						intent.putExtra("source", "MainActivity");
						startActivity(intent);
						pref.edit().putBoolean("first_open_1", false).commit();
					}else{
						pref.edit().putBoolean("first_open_1", false).commit();
					}
				}
	}
	
public void onWindowFocusChanged(boolean hasFocus) {
		
		
		if (pref.getBoolean("first_open_4", true) && !pref.getBoolean("first_open_3", true)) {
			//if(!pref.getBoolean("done_tutorial", false)){
			View locButton = findViewById(R.id.itemProfile);

			TooltipManager manager = TooltipManager.getInstance(this);
			manager.create(1)
		       .anchor(locButton, TooltipManager.Gravity.BOTTOM)
		       .actionBarSize(GraphicsUtils.getActionBarSize(getBaseContext()))
		       .closePolicy(TooltipManager.ClosePolicy.TouchOutside, 0)
		       .text(R.string.tip_profile)
		       .toggleArrow(true)
		       .maxWidth(400)
		       .showDelay(300)
		       .withCallback(this)
		       .withCustomView(R.layout.activity_splashscreen, false)
		       .withStyleId(R.style.ToolTipLayoutCustomStyle)
		       .show();
				ClimbApplication.setTextTipStyle(((TextView) findViewById(android.R.id.text1)));

				
				pref.edit().putBoolean("first_open_4", false).commit();
//			}else{
//				pref.edit().putBoolean("first_open_4", false).commit();
//			}
		}
		
	

		super.onWindowFocusChanged(hasFocus);

		
	}

	private void onSessionStateChange(final Session session, SessionState state, Exception exception) {
		updateFacebookSession(session, state);
	}

	private void setUserOwner() {
		Map<String, Object> conditions = new HashMap<String, Object>();
		conditions.put("owner", new Integer(1));
		List<User> users = ClimbApplication.userDao.queryForFieldValuesArgs(conditions);

		if (users.isEmpty()) { // is there user owner in the db????
			// no, create local user owner
			User user = new User();
			user.setFBid("empty");
			user.setLevel(0);
			user.setXP(0);
			user.setOwner(true);
			user.setName("User");
			ClimbApplication.userDao.create(user);
			ClimbApplication.setCurrentUser(user);
			SharedPreferences pref = getApplicationContext().getSharedPreferences("UserSession", 0);
			Editor editor = pref.edit();
			editor.putInt("local_id", user.get_id());
			editor.commit();
		} else {
			// owner found
			User user = users.get(0);
			ClimbApplication.setCurrentUser(user);
			Log.d("MainActivity", "current User set");
			SharedPreferences pref = getApplicationContext().getSharedPreferences("UserSession", 0);
			Editor editor = pref.edit();
			editor.putInt("local_id", user.get_id());
			editor.commit();

		}
		
		//LOG-------------------------------------------------
		//LINE
				String line = "OWNER LOGGED";
				LogUtils.writeGameUpdate(sContext, line);
		//
		//---------------------------------------------------
		ClimbApplication.refreshClimbings();
		ClimbApplication.refreshCollaborations();
		ClimbApplication.refreshCompetitions();
		ClimbApplication.refreshTeamDuels();
		ClimbApplication.refreshUserBadge();
		ClimbApplication.refreshMicrogoals();
		
		

		//user account connected to FB?
		if (FacebookUtils.isOnline(this)) {
			Session session = Session.getActiveSession();
			if (session != null && session.isOpened()) {
				updateFacebookSession(session, session.getState());
			} else {
				Toast.makeText(sContext, getString(R.string.not_logged), Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(sContext, getString(R.string.connection_needed), Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Method necessary not to repeat makeMeRequest twice
	 * 
	 * @param session
	 * @return
	 */
	private boolean isSessionChanged(Session session) {

		// Check if session state changed
		if (mSession.getState() != session.getState())
			return true;

		// Check if accessToken changed
		if (mSession.getAccessToken() != null) {
			if (!mSession.getAccessToken().equals(session.getAccessToken()))
				return true;
		} else if (session.getAccessToken() != null) {
			return true;
		}

		// Nothing changed
		return false;
	}

	
	private void updateFacebookSession(final Session session, SessionState state) {
		Log.d("MainActivity", "updateFBSession");
		if (FacebookUtils.isOnline(this)) {
			if (state.isOpened()) {
				// this check is necessary not to repeat the newMeRequest twice
				if (mSession == null || isSessionChanged(session)) {
					mSession = session;
					if (!pref.getString("FBid", "none").equalsIgnoreCase("none") && pref.getBoolean("openedFirst", false)) {
						new NetworkRequestAsyncTask(session, this).execute();
						Editor edit = pref.edit();
						edit.putBoolean("openedFirst", false);
						edit.commit();
					}
				}
			} else if (state.isClosed()) {
				Log.i(MainActivity.AppName, "Logged out...");
			}
		}
	}

	/**
	 * Asynctask with ProgressDialog to download FB notification in app
	 * @author silviasegato
	 *
	 */
	private class NotificationAsyncTask extends AsyncTask<Void, Void, Void> {

		final SharedPreferences pref = ClimbApplication.getContext().getSharedPreferences("UserSession", 0);
		private ProgressDialog PD;

		NotificationAsyncTask() {

		}

		@Override
		protected void onPreExecute() {

			super.onPreExecute();
			PD = new ProgressDialog(MainActivity.this);
			PD.setTitle(MainActivity.this.getString(R.string.wait));
			PD.setMessage(MainActivity.this.getString(R.string.download_notifications));
			PD.setCancelable(false);
			PD.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			Session session = Session.getActiveSession();
			Bundle params_ = new Bundle();
			params_.putString("access_token", session.getAccessToken());
			Request request = new Request(session, "me/apprequests", params_, HttpMethod.GET, new Request.Callback() {

				@Override
				public void onCompleted(Response response) {
					try {
						List<Request> deleteRequests = new ArrayList<Request>();
						GraphObject res = response.getGraphObject();
						JSONArray array = (JSONArray) res.getProperty("data");
						Log.d("MainActivity", "Notifications: " + array.length());
						for (int i = 0; i < array.length(); i++) {
							if(!ModelsUtil.containsId(ClimbApplication.notifications, ((JSONObject) array.get(i)).getString("id")))
									createNotification((JSONObject) array.get(i), deleteRequests);
						}
						Request.executeBatchAndWait(deleteRequests);
					} catch (Exception e) {
					}
				}
			});
			Request.executeAndWait(request);

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			PD.dismiss();
			if (ClimbApplication.notifications.isEmpty())
				Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.no_notification), Toast.LENGTH_SHORT).show();
			else {
				Toast.makeText(MainActivity.this, MainActivity.this.getResources().getQuantityString(R.plurals.n_notification, ClimbApplication.notifications.size(), ClimbApplication.notifications.size()), Toast.LENGTH_SHORT).show();
				mPager.getAdapter().notifyDataSetChanged(); //update UI with new data
			}
		}
	}

	/**
	 * Starts to download facebook notification for current user e create the corresponding Notification objects.
	 * 
	 * @param v
	 */
	public void downloadFBNotification(MenuItem v) {
		new NotificationAsyncTask().execute();
	}

	/**
	 * Check for an incoming notifications. If there's any and if they're valid, then create the corresponding Notification object and add it to the Nofitication list.
	 */
	public void onUpdateNotifications() {
		System.out.println("UPDATE NOTIFICATION");
		new NotificationAsyncTask().execute();
		// Check for an incoming notification. Save the info if it is valid
//		Uri intentUri = getIntent().getData();
//		if (intentUri != null) {
//			String requestIdParam = intentUri.getQueryParameter("request_ids");
//			if (requestIdParam != null) {
//				String array[] = requestIdParam.split(","); System.out.println("array not size: " + array.length);
//				if(array.length > 0)
//					new NotificationAsyncTask().execute();
//				//for (int i = 0; i < array.length; i++) {
//					//requestId = array[i];
//					//Log.i("onActivityCreated", "Request id: " + requestId);
//					//getRequestData(requestId);
//				//}
////				if (ClimbApplication.notifications.isEmpty())
////					Toast.makeText(this, getString(R.string.no_notification), Toast.LENGTH_SHORT).show();
////				else
////					Toast.makeText(this, getResources().getQuantityString(R.plurals.n_notification, ClimbApplication.notifications.size()), Toast.LENGTH_SHORT).show();
//
//			}
//
//		}
	}

	/**
	 * Creates the notification objects, given the FB notification for the currently logged user, and delete the exipred ones.
	 * @param notification FB notification data in JSON format
	 * @param deleted list of the expired notification Request
	 */
	private void createNotification(JSONObject notification, List<Request> deleted) {
		// Get the data, parse info to get the key/value info
		JSONObject dataObject;
		JSONObject fromObject;
		JSONObject toObject;
		String groupName = "";
		String sender = "";
		String toId = "noId";
		int type = -1;
		String id = "";

		int building_id = -1;
		String building_name = "";
		String collaboration_id = "";
		boolean isReceiverChallenged = false;
		boolean isSenderCreator = false;
		String duel_id = "";

		try {
			dataObject = new JSONObject(notification.getString("data"));
			fromObject = new JSONObject(notification.getString("from"));
			toObject = new JSONObject(notification.getString("to"));
			type = dataObject.getInt("type");
			sender = fromObject.getString("name");
			toId = toObject.getString("id");
			id = notification.getString("id");

			if (type == 1 || type == 2) {
				building_id = dataObject.getInt("idBuilding");
				building_name = dataObject.getString("nameBuilding");
				collaboration_id = dataObject.getString("idCollab");
			} else if (type == 3) {
				isReceiverChallenged = dataObject.getBoolean("challenger");
				isSenderCreator = dataObject.getBoolean("isSenderCreator");
				duel_id = dataObject.getString("idCollab");
				building_id = dataObject.getInt("idBuilding");
				building_name = dataObject.getString("nameBuilding");
			}

			String time = notification.getString("created_time");
			boolean isNotfValid = isValid(time);
			if (isNotfValid && toId.equalsIgnoreCase(pref.getString("FBid", ""))) {
				Log.d("qui", "request valida");
				switch (type) {
				case 0:
					Notification notf = new InviteNotification(id, sender, groupName, type);
					ClimbApplication.notifications.add(notf);
					break;

				case 1:
					Notification notfA = new AskCollaborationNotification(id, sender, groupName, type);
					((AskCollaborationNotification) notfA).setBuilding_id(building_id);
					((AskCollaborationNotification) notfA).setBuilding_name(building_name);
					((AskCollaborationNotification) notfA).setCollaborationId(collaboration_id);
					ClimbApplication.notifications.add(notfA);
					break;

				case 2:
					Notification notfB = new AskCompetitionNotification(id, sender, groupName, type);
					((AskCompetitionNotification) notfB).setBuilding_id(building_id);
					((AskCompetitionNotification) notfB).setBuilding_name(building_name);
					((AskCompetitionNotification) notfB).setCompetitionId(collaboration_id);
					ClimbApplication.notifications.add(notfB);
					break;
				case 3:
					Notification notfC = new AskTeamDuelNotification(id, sender, duel_id, type, isReceiverChallenged, isSenderCreator);
					((AskTeamDuelNotification) notfC).setBuilding_id(building_id);
					((AskTeamDuelNotification) notfC).setBuilding_name(building_name);
					if (isReceiverChallenged)
						((AskTeamDuelNotification) notfC).setType(NotificationType.ASK_TEAM_COMPETITION_CHALLENGER);
					else
						((AskTeamDuelNotification) notfC).setType(NotificationType.ASK_TEAM_COMPETITION_TEAM);

					ClimbApplication.notifications.add(notfC);
					break;
				}

			} else if (!isNotfValid && toId.equalsIgnoreCase(pref.getString("FBid", ""))) {
				deleted.add(deleteRequest(id));
				// Request.executeBatchAndWait(deleteRequest(id));
			}
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();

		}

	}

	/**
	 * Examines the request with given id and create the corrispondent Notification object
	 * 
	 * @param inRequestId
	 *            the id of the request to examine
	 */
	private void getRequestData(final String inRequestId) {
		// Create a new request for an HTTP GET with the request ID as the Graph path.
		Request request = new Request(Session.getActiveSession(), inRequestId, null, HttpMethod.GET, new Request.Callback() {

			@Override
			public void onCompleted(Response response) {
				// Process the returned response
				GraphObject graphObject = response.getGraphObject();
				FacebookRequestError error = response.getError();
				String message = "";
				if (graphObject != null) {
					Log.d("graph obj not null", graphObject.toString());

					// Get the data, parse info to get the key/value info
					JSONObject dataObject;
					JSONObject fromObject;
					JSONObject toObject;
					String groupName = "";
					String sender = "";
					String toId = "noId";
					int type = -1;
					String id = "";
					int building_id = -1;
					String building_name = "";
					String collaboration_id = "";
					boolean isReceiverChallenged = false;
					boolean isSenderCreator = false;
					String duel_id = "";

					try {
						dataObject = new JSONObject((String) graphObject.getProperty("data"));
						fromObject = (JSONObject) graphObject.getProperty("from");
						toObject = (JSONObject) graphObject.getProperty("to");
						type = dataObject.getInt("type");
						sender = fromObject.getString("name");
						toId = toObject.getString("id");
						id = ((String) graphObject.getProperty("id"));

						if (type == 1 || type == 2) {
							building_id = dataObject.getInt("idBuilding");
							building_name = dataObject.getString("nameBuilding");
							collaboration_id = dataObject.getString("idCollab");
						} else if (type == 3) {
							isReceiverChallenged = dataObject.getBoolean("challenger");
							isSenderCreator = dataObject.getBoolean("isSenderCreator");
							duel_id = dataObject.getString("idCollab");
							building_id = dataObject.getInt("idBuilding");
							building_name = dataObject.getString("nameBuilding");
						}

					} catch (JSONException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();

					}

					message += " " + groupName;

					String time = (String) graphObject.getProperty("created_time");
					message += " " + time;
					boolean isNotfValid = isValid(time);
					if (isNotfValid && toId.equalsIgnoreCase(pref.getString("FBid", ""))) {
						Log.d("qui", "request valida");
						switch (type) {
						case 0:
							Notification notf = new InviteNotification(id, sender, groupName, type);
							ClimbApplication.notifications.add(notf);
							break;

						case 1:
							Notification notfA = new AskCollaborationNotification(id, sender, groupName, type);
							((AskCollaborationNotification) notfA).setBuilding_id(building_id);
							((AskCollaborationNotification) notfA).setBuilding_name(building_name);
							((AskCollaborationNotification) notfA).setCollaborationId(collaboration_id);
							ClimbApplication.notifications.add(notfA);
							break;

						case 2:
							Notification notfB = new AskCompetitionNotification(id, sender, groupName, type);
							((AskCompetitionNotification) notfB).setBuilding_id(building_id);
							((AskCompetitionNotification) notfB).setBuilding_name(building_name);
							((AskCompetitionNotification) notfB).setCompetitionId(collaboration_id);
							ClimbApplication.notifications.add(notfB);
							break;
						case 3:
							Notification notfC = new AskTeamDuelNotification(id, sender, duel_id, type, isReceiverChallenged, isSenderCreator);
							((AskTeamDuelNotification) notfC).setBuilding_id(building_id);
							((AskTeamDuelNotification) notfC).setBuilding_name(building_name);
							if (isReceiverChallenged)
								((AskTeamDuelNotification) notfC).setType(NotificationType.ASK_TEAM_COMPETITION_CHALLENGER);
							else
								((AskTeamDuelNotification) notfC).setType(NotificationType.ASK_TEAM_COMPETITION_TEAM);

							ClimbApplication.notifications.add(notfC);
							break;
						}

					} else if (!isNotfValid && toId.equalsIgnoreCase(pref.getString("FBid", ""))) {
						Request.executeBatchAsync(deleteRequest(inRequestId));
					}

					String title = "";
					message = title;

				} else {
					System.out.println("graph obj null");
				}
				Toast.makeText(getApplicationContext(), getString(R.string.request_arrived), Toast.LENGTH_SHORT).show();
				// deleteRequest(inRequestId);//da chiamare solo se non ci sono
				// errori
			}
		});
		// Execute the request asynchronously.
		Request.executeBatchAsync(request);
	}

	/**
	 * Checks if the notification is valid (that is if the given date is older than 24h).
	 * 
	 * @param creation_time
	 *            creation time of the notification to check
	 * @return true if (current time - creation_time) < 24h, false otherwise
	 */
	private boolean isValid(String creation_time) {
		SimpleDateFormat ISO8601DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ITALY);
		Date now = new Date();
		Date olderDate = null;

		try {
			olderDate = ISO8601DATEFORMAT.parse(creation_time);
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		double diffInMin = (double) ((now.getTime() - olderDate.getTime()) / (1000 * 60));

		// if 24h older
		if (diffInMin >= 1440)
			return false;
		else
			return true;
	}
	
	public void selectAction(View v){
		
		switch(v.getId()) {
		case R.id.dialog_config_btt_ok:
			alertDialog.dismiss();
			startActivityForResult(new Intent(this, StartConfigActivity.class), 1);
			break;
		case R.id.dialog_config_btt_cancel:
			alertDialog.dismiss();
			startOnboarding();
			break;
		}
	}
	
	public void onShowLog(MenuItem v) {
		Intent intent = new Intent(sContext, ShowLogActivity.class);
		startActivity(intent);
	}

	/**
	 * Helper method to access application context
	 */
	public static Context getContext() {
		return sContext;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.actionbar, menu);
		return true;
	}

	public void onShowActionProfile(MenuItem v) {
		Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
		startActivity(intent);
	}

	public void onShowSettings(MenuItem v) {
		Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
		startActivity(intent);
	}

	public void onShowGroups(MenuItem v) {
		Intent intent = new Intent(getApplicationContext(), GroupsActivity.class);
		startActivity(intent);
	}

	@Override
	protected void onResume() {
		Log.i(MainActivity.AppName, "MainActivity onResume");
		super.onResume();
		ClimbApplication.refresh();
		mPager.getAdapter().notifyDataSetChanged();
		Session session = Session.getActiveSession();
		if (session != null && (session.isOpened() || session.isClosed())) {
			onSessionStateChange(session, session.getState(), null);
		}

		uiHelper.onResume();
		ClimbApplication.activityResumed();
	}

	@Override
	protected void onPause() {
		Log.i(MainActivity.AppName, "MainActivity onPause");
		super.onPause();
		uiHelper.onPause();
		ClimbApplication.activityPaused();
	}

	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
	}

	public void onShareDb(MenuItem v) {
		shareDb();
	}

	public void onShareAnr(MenuItem v) {
		shareAnr();
	}
	
	public void onShareLog(MenuItem v){
		shareLog();
	}


	public static void emptyNotificationList() {
		ClimbApplication.notifications.clear();
	}

	/**
	 * Create a Request object to delete a request in Facebook
	 * 
	 * @param inRequestId
	 *            the id of the request to be deleted
	 * @return the Request object to delete the request with the given id from Facebook
	 */
	private Request deleteRequest(final String inRequestId) {
		// Create a new request for an HTTP delete with the
		// request ID as the Graph path.
		Request request = new Request(Session.getActiveSession(), inRequestId, null, HttpMethod.DELETE, new Request.Callback() {

			@Override
			public void onCompleted(Response response) {
				// Show a confirmation of the deletion
				// when the API call completes successfully.
				Log.d("deleteRequest", "Request " + inRequestId + " succesfully deleted");
				Toast.makeText(MainActivity.getContext(), getString(R.string.request_deleted), Toast.LENGTH_SHORT).show();
			}
		});
		// Execute the request asynchronously.
		// Request.executeBatchAsync(request);
		return request;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data);
		Log.d("TEST", "onActivityResult");
		startOnboarding();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
	}

	/**
	 * Request to be executed asynchronously: it checks if user has already connected his account with Facebook. If this is the case, it downloads all his previous data.
	 */
	@Override
	public void makeRequest(final Session session, final ProgressDialog PD) {
		Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
			@Override
			public void onCompleted(GraphUser user, Response response) {
				if (session == Session.getActiveSession()) {
					ClimbApplication.user = user;
					if (user != null && pref.getString("FBid", "none").equalsIgnoreCase("none")) {
						// look for my FBid
						Map<String, Object> conditions = new HashMap<String, Object>();
						conditions.put("FBid", user.getId());
						User newUser = null;

						List<User> users = ClimbApplication.userDao.queryForFieldValuesArgs(conditions);
						if (users.size() > 0) {
							// this FBid is already linked to a user
							newUser = users.get(0);
							ClimbApplication.setCurrentUser(newUser);
							// save data locally
							Editor editor = pref.edit();
							editor.putString("FBid", newUser.getFBid());
							editor.putString("username", newUser.getName());
							editor.putInt("local_id", newUser.get_id());
							editor.commit();
							String own = newUser.isOwner() ? "\n" + getString(R.string.owner) : "";
							Toast.makeText(MainActivity.this, getString(R.string.logged_as, newUser.getName()) + own, Toast.LENGTH_SHORT).show();
							//check if user exists in parse
							ClimbApplication.userExists(user, session, PD, MainActivity.this);
						} else {
							Toast.makeText(MainActivity.this, getString(R.string.not_logged), Toast.LENGTH_SHORT).show();
						}
					} else {
						// System.err.println("no user");
						//FBid gia settato, scarico le mie informazioni
						new MyAsync(MainActivity.this, PD, false).execute();
						//LOG-------------------------------------------------
						//LINE
						String line = "USERNAME (FB) " + pref.getString("username", "");
						System.out.println("USERNAME (FB) " + pref.getString("username", ""));
						System.out.println("USERNAME (ID) " + pref.getInt("local_id", -1));
						LogUtils.writeGameUpdate(sContext, line);
						//
						//---------------------------------------------------
						Editor edit = pref.edit();
						edit.putBoolean("openedFirst", false);
						edit.commit();
						
						//onUpdateNotifications();

					}
				}
				if (response.getError() != null) {
					Log.e(MainActivity.AppName, "FB exception: " + response.getError());
				}
			}
		});
		request.executeAndWait();
	}

	public void showDemo(MenuItem v) {
		Intent i = new Intent(this, WalkthroughActivity.class);
		startActivity(i);
	}
	
	
	

	/**
	 * UNUSED
	 * Send invite to user's facebook friends.
	 
	private void sendInvites() {
		Bundle params = new Bundle();

		// Uncomment following link once uploaded on Google Play for deep
		// linking
		// params.putString("link",
		// "https://play.google.com/store/apps/details?id=com.facebook.android.friendsmash");

		// 1. No additional parameters provided - enables generic Multi-friend
		// selector
		params.putString("message", "Join me in Climb the world");

		// 2. Optionally provide a 'to' param to direct the request at a
		// specific user
		// params.putString("to", "515768651");

		// 3. Suggest friends the user may want to request - could be game
		// specific
		// e.g. players you are in a match with, or players who recently played
		// the game etc.
		// Normally this won't be hardcoded as follows but will be context
		// specific
		// String [] suggestedFriends = {
		// "695755709",
		// "685145706",
		// "569496010",
		// "286400088",
		// "627802916",
		// };
		// params.putString("suggestions", TextUtils.join(",",
		// suggestedFriends));
		//
		// Show FBDialog without a notification bar
		showDialogWithoutNotificationBar("apprequests", params);
	}*/

	/**
	 * UNUSED
	 *
	
	private void showDialogWithoutNotificationBar(String action, Bundle params) {
		// Create the dialog
		dialog = new WebDialog.Builder(this, Session.getActiveSession(), action, params).setOnCompleteListener(new WebDialog.OnCompleteListener() {

			@Override
			public void onComplete(Bundle values, FacebookException error) {
				if (error != null) {
					if (error instanceof FacebookOperationCanceledException) {
						Toast.makeText(getApplicationContext(), getString(R.string.request_cancelled), Toast.LENGTH_SHORT).show();
					} 
				} else {
					final String requestId = values.getString("request");
					if (requestId != null) {
						Toast.makeText(getApplicationContext(), getString(R.string.request_sent), Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(getApplicationContext(), getString(R.string.request_cancelled), Toast.LENGTH_SHORT).show();
					}
				}
			}

		}).build();

		// Hide the notification bar and resize to full screen
		Window dialog_window = dialog.getWindow();
		dialog_window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// Store the dialog information in attributes
		dialogAction = action;
		dialogParams = params;

		// Show the dialog
		dialog.show();
	} */

	// DEBUG ONLY
	private void shareDb() {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		String output_name = "ClimbTheWorld_" + df.format(new Date()) + ".db";
		try {
			File file = new File(ClimbApplication.dbHelper.getDbPath()); // get private db reference
			if (file.exists() == false || file.length() == 0)
				throw new Exception("Empty DB");
			this.copyFile(new FileInputStream(file), this.openFileOutput(output_name, MODE_WORLD_READABLE));
			file = this.getFileStreamPath(output_name);
			Intent i = new Intent(Intent.ACTION_SEND);
			i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
			i.setType("*/*");
			startActivity(Intent.createChooser(i, "Share to"));
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), getString(R.string.db_error, e.getMessage()), Toast.LENGTH_SHORT).show();
			Log.e(AppName, e.getMessage());
		}
	}

	// DEBUG ONLY
	private void shareAnr() {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		String output_name = "ClimbTheWorld_" + df.format(new Date()) + ".txt";
		try {
			File file = new File("/data/anr/traces.txt");
			if (file.exists() == false || file.length() == 0)
				throw new Exception("Empty DB");
			this.copyFile(new FileInputStream(file), this.openFileOutput(output_name, MODE_WORLD_READABLE));
			file = this.getFileStreamPath(output_name);
			Intent i = new Intent(Intent.ACTION_SEND);
			i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
			i.setType("*/*");
			startActivity(Intent.createChooser(i, "Share to"));
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), getString(R.string.db_error, e.getMessage()), Toast.LENGTH_SHORT).show();
			Log.e(AppName, e.getMessage());
		}
	}
	
	//DEBUG ONLY
	private void shareLog(){
		try {
			appendLog(getLogCat());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static final int BUFFER_SIZE = 1024;

	/*public String getLogCat() {
	    String[] logcatArgs = new String[] {"logcat", "-v", "time"};

	    Process logcatProc = null;
	    try {
	        logcatProc = Runtime.getRuntime().exec(logcatArgs);
	    }
	    catch (IOException e) {
	        return null;
	    }

	    BufferedReader reader = null;
	    String response = null;
	    try {
	        String separator = System.getProperty("line.separator");
	        StringBuilder sb = new StringBuilder();
	        reader = new BufferedReader(new InputStreamReader(logcatProc.getInputStream()), BUFFER_SIZE);
	        String line;
	        while ((line = reader.readLine()) != null) {
	            sb.append(line);
	            sb.append(separator);
	        }
	        response = sb.toString();
	    }
	    catch (IOException e) {
	    }
	    finally {
	        if (reader != null) {
	            try {
	                reader.close();
	            }
	            catch (IOException e) {}
	        }
	    }

	    return response;
	}*/
	
	public String getLogCat() throws IOException{
		Process process = Runtime.getRuntime().exec("logcat -d");
	      BufferedReader bufferedReader = new BufferedReader(
	      new InputStreamReader(process.getInputStream()));

	      StringBuilder log=new StringBuilder();
	      String line;
	      while ((line = bufferedReader.readLine()) != null) {
	        log.append(line);
	        log.append("\n");
	      }
	      return log.toString();

	}
	
	
	public void appendLog(String text)
	{    
		System.out.println(text);
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		String output_name = "ClimbTheWorld_Log_" + df.format(new Date()) + ".txt";   
	   File logFile = new File("/data/data/org.unipd.nbeghin.climbtheworld/" + output_name);
	   if (!logFile.exists())
	   {
	      try
	      {
	         logFile.createNewFile();
	      } 
	      catch (IOException e)
	      {
	         // TODO Auto-generated catch block
	         e.printStackTrace();
	      }
	   }
	   try
	   {
	      //BufferedWriter for performance, true to set append to file flag
	      BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
	      buf.append(text);
	      buf.newLine();
	      buf.close();
	      this.copyFile(new FileInputStream(logFile), this.openFileOutput(output_name, MODE_WORLD_READABLE));
	      logFile = this.getFileStreamPath(output_name);
	  	Intent i = new Intent(Intent.ACTION_SEND);
		i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(logFile));
		i.setType("*/*");
		startActivity(Intent.createChooser(i, "Share to"));
	   }
	   catch (IOException e)
	   {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	   }
	}

	@Override
	public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction arg1) {
		mPager.setCurrentItem(tab.getPosition());
		
	}

	@Override
	public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void onClosing(int id, boolean fromUser, boolean containsTouch) {
		// TODO Auto-generated method stub
		
	}
	
	public void uploadGameLog(MenuItem v){
		if(FacebookUtils.isOnline(getApplicationContext()))
			new UploadGameLogTask(getApplicationContext()).execute();
		else
			Toast.makeText(getApplicationContext(), "Ops! No intenet connection", Toast.LENGTH_SHORT).show();
	}

}
