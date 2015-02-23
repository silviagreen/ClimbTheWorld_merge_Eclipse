package org.unipd.nbeghin.climbtheworld;


import it.sephiroth.android.library.tooltip.TooltipManager;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unipd.nbeghin.climbtheworld.adapters.StatAdapter;
import org.unipd.nbeghin.climbtheworld.models.Building;
import org.unipd.nbeghin.climbtheworld.models.Stat;
import org.unipd.nbeghin.climbtheworld.models.User;
import org.unipd.nbeghin.climbtheworld.models.UserBadge;
import org.unipd.nbeghin.climbtheworld.util.FacebookUtils;
import org.unipd.nbeghin.climbtheworld.util.GraphicsUtils;
import org.unipd.nbeghin.climbtheworld.util.StatUtils;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProfilePictureView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
//import com.facebook.widget.ProfilePictureView;
import com.parse.ParseUser;

public class ProfileActivity extends ActionBarActivity implements NetworkRequests,  TooltipManager.onTooltipClosingCallback  {

	Button improveBtn;
	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};
	private UiLifecycleHelper uiHelper;
	private ProgressDialog PD;
	private TextView lblFacebookUser;
	private Session mSession;
	private User me;
	SharedPreferences pref;
	public ProfilePictureView profilePictureView;
	
	List<String> permissions;
	
	public void setProfileData(GraphUser user, boolean login) {
		me = ClimbApplication.getUserById(pref.getInt("local_id", -1));
		profilePictureView.setCropped(true);
		profilePictureView.setProfileId(user.getId());
		lblFacebookUser.setText(user.getName());
		if(login){
			String own = me.isOwner() ? "\n" + getString(R.string.owner) : "";
			Toast.makeText(this, getString(R.string.logged_as, me.getName()) + own, Toast.LENGTH_SHORT).show();
		}
		updateUserData();
	}

	public ProfilePictureView getProfilePictureView() {
		return profilePictureView;
	}
	


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);

		lblFacebookUser = ((TextView) findViewById(R.id.textUserName));
		profilePictureView = (ProfilePictureView) findViewById(R.id.fb_profile_picture2);
		profilePictureView.setPresetSize(-2); //SMALL
		pref = getApplicationContext().getSharedPreferences("UserSession", 0);
		me = ClimbApplication.getUserById(pref.getInt("local_id", -1));
		
		LoginButton authButton = (LoginButton) findViewById(R.id.login_button);
	    authButton.setPublishPermissions("public_profile,user_friends");
//	    Session.NewPermissionsRequest newPermissionsRequest = new 
//	    Session.NewPermissionsRequest(this, Arrays.asList("user_friends,public_profile"));
//	    Session.getActiveSession().requestNewReadPermissions(newPermissionsRequest);
		
		updateUserData();

		Session session = Session.getActiveSession();
		
		 
		    
		    
//		if(session != null && session.isOpened()){
//		final List<String> PERMISSIONS = Arrays.asList("publish_actions, user_friends");
//		List<String> permissions = Session.getActiveSession().getPermissions();
//		
//		for(String p : permissions)
//			System.out.println(p);
//
//		if (!new HashSet<String>(permissions).containsAll(PERMISSIONS)) {
//			Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(this, PERMISSIONS);
//			Session.getActiveSession().requestNewPublishPermissions(newPermissionsRequest);
//			Log.w("FBShare", "has permission");
//		}
//		
//		permissions = Session.getActiveSession().getPermissions();
//		System.out.println("dopo");
//		for(String p : permissions)
//			System.out.println(p);
//		}
		
		if (session != null && session.isOpened()) {
			setProfilePicture(session, session.getState());
		}
		
		boolean needHelp = getIntent().getBooleanExtra("need_help", false);
		if(needHelp){
			new HelpDialogActivity(this, R.style.Transparent, true).show();
		}

	}
	
	@Override
	protected void onStart(){
		super.onStart();
	}
	


	/**
	 * Downloads Facebook profile picture asynchronously
	 * @param session
	 * @param state
	 */
	private void setProfilePicture(final Session session, SessionState state) {
		if (state.isOpened()) {

			
//			try
//            {
//                Session.OpenRequest request = new Session.OpenRequest(this);
//                request.setPermissions(Arrays.asList("publish_actions, user_friends"));
//            }
//        catch (Exception e)
//            {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
			
			
             
			Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
				@Override
				public void onCompleted(GraphUser user, Response response) {
					if (session == Session.getActiveSession()) {
						if (user != null && profilePictureView != null) {
							profilePictureView.setCropped(true);
							profilePictureView.setProfileId(user.getId());
						} else
							System.err.println("no user");
					}
					if (response.getError() != null) {
						Log.e(MainActivity.AppName, "FB exception: " + response.getError());
					}
				}
			});
			request.executeAsync();
		} else if (state.isClosed()) {
			Log.i(MainActivity.AppName, "Logged out...");
			profilePictureView.setProfileId(null);
		}
	}

	private void updateUserData() {
		me = ClimbApplication.getUserById(pref.getInt("local_id", -1));
		List<Stat> stats = StatUtils.calculateStats();
		((ListView) findViewById(R.id.listStatistics)).setAdapter(new StatAdapter(this, R.layout.stat_item, stats));
		lblFacebookUser.setText(me.getName());
		((TextView) findViewById(R.id.textLevel)).setText(getString(R.string.level, String.valueOf((me.getLevel()))));
		((TextView) findViewById(R.id.textXP)).setText(String.valueOf(me.getXP()) + " XP");
		((TextView) findViewById(R.id.MyStepsText)).setText(getString(R.string.mean_text, String.valueOf(me.getMean())));
		((TextView) findViewById(R.id.textCurrentValue)).setText(getString(R.string.today_step, String.valueOf(me.getCurrent_steps_value())));
		DecimalFormat df = new DecimalFormat("####0.00");
		((TextView) findViewById(R.id.textHeight)).setText(getString(R.string.height_text) + ": \t " + df.format(me.getHeight()) /*String.valueOf(me.getHeight())*/ + "mt");
		int total = ClimbApplication.levelToXP(me.getLevel() + 1);
		int percentage = 0;
		if (total != 0)
			percentage = ((100 * me.getXP()) / total);
		ProgressBar levelPB = (ProgressBar) findViewById(R.id.progressBarLevel);
		levelPB.setIndeterminate(false);
		levelPB.setProgress(percentage);

		improveBtn = (Button) findViewById(R.id.buttonCounter);
		improveBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(getApplicationContext(), ClimbActivity.class);
				intent.putExtra(ClimbApplication.counter_mode, true);
				startActivity(intent);
			}
		});
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.profile, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void onSessionStateChange(final Session session, SessionState state, Exception exception) {
		System.out.println("onSessionStateChange");
		updateFacebookSession(session, state);
	}

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
	
	public void onWindowFocusChanged(boolean hasFocus) {
		
		
		if (pref.getBoolean("first_open_5", true)) {
			if(!pref.getBoolean("done_tutorial", false)){
				View locButton = findViewById(R.id.itemInviteFacebookFriends);


				TooltipManager manager = TooltipManager.getInstance(this);
				manager.create(0)
			       .anchor(locButton, TooltipManager.Gravity.BOTTOM)
			       .actionBarSize(GraphicsUtils.getActionBarSize(getBaseContext()))
			       .closePolicy(TooltipManager.ClosePolicy.TouchOutside, 0)
			       .text(R.string.tip_invite)
			       .toggleArrow(true)
			       .maxWidth(400)
			       .showDelay(300)
			       .withCallback(this)
			       .withCustomView(R.layout.activity_splashscreen, false)
			       .withStyleId(R.style.ToolTipLayoutCustomStyle)			       
			       .show();
				
				ClimbApplication.setTextTipStyle(((TextView) findViewById(android.R.id.text1)));

				
				

				pref.edit().putBoolean("first_open_5", false).commit();
			}else{
				pref.edit().putBoolean("first_open_5", false).commit();
			}
		}
		
		super.onWindowFocusChanged(hasFocus);

		
	}

	private void updateFacebookSession(final Session session, SessionState state) {

		if (FacebookUtils.isOnline(this)) {

			if (state.isOpened()) {
				SharedPreferences pref = getSharedPreferences("UserSession", 0);
				Editor edit = pref.edit();
				edit.putBoolean("openedFirst", false);
				edit.commit();
				new NetworkRequestAsyncTask(session, this).execute();

			} else if (state.isClosed()) {
				Log.i("Climb The World", "Logged out...");
				ParseUser.getCurrentUser().logOut();
				ClimbApplication.setCurrentUser(null);
				if (!pref.getString("FBid", "none").equalsIgnoreCase("none")) {
					Editor editor = pref.edit();
					// the owner returns to be the current user
					Map<String, Object> conditions2 = new HashMap<String, Object>();
					conditions2.put("owner", 1);
					List<User> users2 = ClimbApplication.userDao.queryForFieldValuesArgs(conditions2);
					if (users2.size() > 0) {
						Log.d("logout", "carico user owner");
						editor.putInt("local_id", users2.get(0).get_id());
						editor.putString("username", "owner");
						editor.commit();
						ClimbApplication.setCurrentUser(users2.get(0));
					} else {
						Log.d("vuoto", "creo owner");
						// create new local user owner
						User userOwner = new User();
						userOwner.setFBid("empty");
						userOwner.setLevel(0);
						userOwner.setXP(0);
						userOwner.setOwner(true);
						ClimbApplication.userDao.create(userOwner);
						editor.putInt("local_id", userOwner.get_id());
						editor.putString("username", "owner");

						editor.commit();
						Log.d("local id", String.valueOf(userOwner.get_id()));
						ClimbApplication.setCurrentUser(userOwner);

					}

					editor.putString("FBid", "none");
					editor.commit();
					updateUserData();
					profilePictureView.setProfileId(null);
					lblFacebookUser.setText(getString(R.string.not_logged_in));
					updateGameData();
				}

			}
		} else
			Toast.makeText(getApplicationContext(), getString(R.string.check_connection), Toast.LENGTH_LONG).show();
	}

	public void updateGameData() {
		updateUserData();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d("ProfileActivity", "onResume");
		Session session = Session.getActiveSession();

		if (session != null && (session.isOpened() || session.isClosed())) {
			//onSessionStateChange(session, session.getState(), null);
		}
		me = ClimbApplication.getUserById(pref.getInt("local_id", -1));
		((TextView) findViewById(R.id.MyStepsText)).setText(getString(R.string.mean_text, String.valueOf(me.getMean())));
		((TextView) findViewById(R.id.textCurrentValue)).setText(getString(R.string.today_step, String.valueOf(me.getCurrent_steps_value())));
		DecimalFormat df = new DecimalFormat("####0.00");
		((TextView) findViewById(R.id.textHeight)).setText(getString(R.string.height_text) + ": " /*String.valueOf(me.getHeight()) */ + "\t " + df.format(me.getHeight())+ "mt");

		
		if(session != null && session.isOpened()){
			

//			if (!new HashSet<String>(permissions).containsAll(PERMISSIONS)) {
//				Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(this, PERMISSIONS);
//				Session.getActiveSession().requestNewPublishPermissions(newPermissionsRequest);
//				Log.w("FBShare", "has permission");
//			}
			
			
			}

		uiHelper.onResume();
		ClimbApplication.activityResumed();
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
		ClimbApplication.activityPaused();
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

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	public void onInviteFacebookFriends(MenuItem v) {
		/*
		 * Intent intent = new Intent(sContext, FBPickFriendActivity.class);
		 * startActivity(intent);
		 */
		if (me != null && !me.getFBid().equalsIgnoreCase("empty")) {
			if (FacebookUtils.isOnline(this)) {
				sendInviteToFriends();
			} else
				Toast.makeText(getApplicationContext(), getString(R.string.check_connection), Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(getApplicationContext(), getString(R.string.fb_connect_invite), Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Starts activity for inviting Facebook friends if user is currently logged in
	 */
	private void sendInviteToFriends() {
		Session session = Session.getActiveSession();
		if (session == null || !session.isOpened()) {
			Toast.makeText(getApplicationContext(), getString(R.string.not_logged_in), Toast.LENGTH_LONG).show();
			return;
		}
		Intent intent = new Intent(getApplicationContext(), FBPickFriendActivity.class);
		startActivity(intent);

	}

	/**
	 * Request to be executed asynchronously
	 */
	@Override
	public void makeRequest(final Session session, final ProgressDialog PD) {
		Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
			@Override
			public void onCompleted(GraphUser user, Response response) {

				if (session == Session.getActiveSession()) {
					if (user != null && profilePictureView != null) {
						ClimbApplication.user = user;
						// if current user is not logged with facebook
						if (pref.getString("FBid", "none").equalsIgnoreCase("none")) {
							// look for my FBid
							Map<String, Object> conditions = new HashMap<String, Object>();
							conditions.put("FBid", user.getId());
							User newUser = null;
							List<User> users = ClimbApplication.userDao.queryForFieldValuesArgs(conditions);

							Map<String, Object> conditions2 = new HashMap<String, Object>();
							conditions2.put("owner", 1);
							List<User> users2 = ClimbApplication.userDao.queryForFieldValuesArgs(conditions2);

							// already connected FB account????
							if (users.isEmpty()) {
								// no, am I the owner????
								if (!users2.isEmpty()) { // there is an owner locally
									if (users2.get(0).getFBid().equalsIgnoreCase("empty")) {
										// this is FBid of the owner
										newUser = users2.get(0);
										newUser.setFBid(user.getId());
										newUser.setName(user.getName());
										ClimbApplication.userDao.update(newUser);
									} else {
										// new host user
										newUser = new User();
										newUser.setFBid(user.getId());
										newUser.setName(user.getName());
										newUser.setLevel(0);
										newUser.setXP(0);
										newUser.setOwner(false);
										ClimbApplication.userDao.create(newUser);
										createBadges();
									}
									// no, connect it now
								} else {
									// create new local user owner
									User userOwner = new User();
									userOwner.setFBid("empty");
									userOwner.setLevel(0);
									userOwner.setXP(0);
									userOwner.setOwner(true);

									user.setName("user owner");
									ClimbApplication.userDao.create(userOwner);
									createBadges();

									Editor editor = pref.edit();
									editor.putInt("local_id", userOwner.get_id());
									editor.commit();
									Log.d("local id", String.valueOf(userOwner.get_id()));
								}
							} else {// yes, get my account
								newUser = users.get(0);
							}
							// save data locally
							SharedPreferences pref = getSharedPreferences("UserSession", 0);
							Editor editor = pref.edit();
							editor.putString("FBid", newUser.getFBid());
							editor.putString("username", newUser.getName());
							editor.putInt("local_id", newUser.get_id());
							editor.commit();

							ClimbApplication.setCurrentUser(newUser);
							// ClimbApplication.loadFriendsFromFacebook();
							ClimbApplication.userExists(user, session, PD, ProfileActivity.this);

						}
					} else {
						System.err.println("no user");
						synchronized (ClimbApplication.lock) {
							ClimbApplication.lock.notify();
							ClimbApplication.BUSY = false;
						}
					}
				} else {
					synchronized (ClimbApplication.lock) {
						ClimbApplication.lock.notify();
						ClimbApplication.BUSY = false;
					}
				}

				if (response.getError() != null) {
					Log.e("Settings Activity", "FB exception: " + response.getError());
					synchronized (ClimbApplication.lock) {
						ClimbApplication.lock.notify();
						ClimbApplication.BUSY = false;
					}
				}
			}
		});
		request.executeAndWait();
	}

	/**
	 * Creates UserBadges for the current logged in user
	 */
	private void createBadges() {
		boolean emptybadges = ClimbApplication.areThereUserBadges(pref.getInt("local_id", -1)) == 0 ? false : true;
		if (emptybadges) {
			for (Building building : ClimbApplication.buildings) {
				UserBadge ub = new UserBadge();
				ub.setBadge(ClimbApplication.getBadgeByCategory(0));
				ub.setObj_id(building.get_id());
				ub.setPercentage(0.0);
				ub.setSaved(true);
				ub.setUser(ClimbApplication.getUserById(pref.getInt("local_id", -1)));
				ClimbApplication.userBadgeDao.create(ub);
			}
		}
		ClimbApplication.refreshUserBadge();
	}

	@Override
	public void onClosing(int id, boolean fromUser, boolean containsTouch) {
		// TODO Auto-generated method stub
		
	}
}
