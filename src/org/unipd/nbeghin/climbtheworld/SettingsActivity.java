package org.unipd.nbeghin.climbtheworld;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.json.JSONException;
import org.json.JSONObject;
import org.unipd.nbeghin.climbtheworld.models.User;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
//import com.facebook.widget.ProfilePictureView;
import android.widget.ProfilePictureView;
import android.widget.TextView;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {
	/**
	 * Determines whether to always show the simplified settings UI, where
	 * settings are presented in a single list. When false, settings are shown
	 * as a master/detail two-pane view on tablets. When true, a single pane is
	 * shown on tablets.
	 */
	private static final boolean ALWAYS_SIMPLE_PREFS = true;
	private static SettingsActivity current_activity;
	private UiLifecycleHelper uiHelper;
	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};
	private Bitmap userImage;
	protected static Activity getThisActivity(){
		return current_activity;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("SettingsActivity", "onCreate");
		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);
		getUserImage();
		current_activity = SettingsActivity.this;
		
		
		
		Session session = Session.getActiveSession();
		if (session != null && session.isOpened()) {
			//updateFacebookSession(session, session.getState());
		}

	}
	
	private void getUserImage(){
		Bundle params = new Bundle();
		params.putBoolean("redirect", false);
		params.putString("height", "200");
		params.putString("type", "small");
		params.putString("width", "200");
		/* make the API call */
		new Request(
		    Session.getActiveSession(),
		    "/me/picture",
		    params,
		    HttpMethod.GET,
		    new Request.Callback() {
		        public void onCompleted(Response response) {
		        	GraphObject graphObject = response.getGraphObject();
		            FacebookRequestError error = response.getError();
		            if (graphObject != null) { System.out.println(graphObject.toString());
		            GraphObject res = response.getGraphObject(); 
		            JSONObject array = (JSONObject) res.getProperty("data");
		            try {
						if (array.getString("url") != null) {
							   try {
								   String imageURL = ( array.getString("url")).replace("\\", "");
							        new RetrieveImageTask(imageURL).execute();
							        
							    } catch (Exception e) {
							        // Log exception
							        userImage = null;
							        e.printStackTrace();
							    }
						   }
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		           }
		        }
		    }
		).executeAsync();
	}
	
	class RetrieveImageTask extends AsyncTask<String, Void, Bitmap> {

	    private Exception exception;
	    String url_image;
	    
	    public RetrieveImageTask(String url){
	    	this.url_image = url;
	    }

	    protected Bitmap doInBackground(String... urls) {
	        try {
	        	URL url = new URL(url_image);
		        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		        connection.setDoInput(true);
		        connection.connect();
		        InputStream input = connection.getInputStream();
		        Bitmap myBitmap = BitmapFactory.decodeStream(input);
		        return ProfilePictureView.getRoundedBitmap(myBitmap);
	        } catch (Exception e) {
	            this.exception = e;
	            return null;
	        }
	    }

	    protected void onPostExecute(Bitmap myBitmap) {
	    	userImage = myBitmap;
			final Preference profile_name = findPreference("profile_name");
	        profile_name.setIcon(new BitmapDrawable(getResources(), userImage));
	    }
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// Show the Up button in the action bar.
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}



	private void updateFacebookSession(final Session session, SessionState state) {
		final TextView lblFacebookUser = (TextView) findViewById(R.id.lblFacebookUser);
		final ProfilePictureView profilePictureView = (ProfilePictureView) findViewById(R.id.fb_profile_picture);
		final Preference profile_name = findPreference("profile_name");
		if (state.isOpened()) {
			Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
				@Override
				public void onCompleted(GraphUser user, Response response) {
					if (session == Session.getActiveSession()) {
						if (user != null && profilePictureView != null) {
							profilePictureView.setCropped(true);
							profilePictureView.setProfileId(user.getId());
							profilePictureView.setPresetSize(-2); //SMALL
							lblFacebookUser.setText(user.getName());
							profile_name.setSummary(user.getName());
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
			profilePictureView.setPresetSize(-2);
			lblFacebookUser.setText("Not logged in");
			profile_name.setSummary("No user defined");
		}
	}

	private void onSessionStateChange(final Session session, SessionState state, Exception exception) {
		//updateFacebookSession(session, state);
	}
	
//	public Bitmap getUserPic(String userID) {
//	    String imageURL;
//	    Bitmap bitmap = null;
//	    Log.d("PreferenceActivity", "Loading Picture");
//	    imageURL = "http://graph.facebook.com/"+userID+"/picture?type=small";
//	    try {
//	        bitmap = BitmapFactory.decodeStream((InputStream)new URL(imageURL).getContent());
//	    } catch (Exception e) {
//	        Log.d("TAG", "Loading Picture FAILED");
//	        e.printStackTrace();
//	    }
//	    return bitmap;
//	}
//	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		((ProfilePictureView) findViewById(R.id.fb_profile_picture)).setPresetSize(-2);
		final Preference profile_name = findPreference("profile_name");
		ProfilePictureView image = ((ProfilePictureView) findViewById(R.id.fb_profile_picture));
		SharedPreferences pref = getSharedPreferences("UserSession", 0);
		//profile_name.setIcon(new BitmapDrawable(getResources(), getUserPic(pref.getString("FBid", "none"))));
		((LinearLayout) findViewById(R.id.lblFacebook)).setVisibility(View.GONE);
		
		super.onWindowFocusChanged(hasFocus);
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
			// TODO: If Settings has multiple levels, Up should navigate up
			// that hierarchy.
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		setupSimplePreferencesScreen();
	}

	/**
	 * Shows the simplified settings UI if the device configuration if the
	 * device configuration dictates that a simplified, single-pane UI should be
	 * shown.
	 */
	@SuppressWarnings("deprecation")
	private void setupSimplePreferencesScreen() {
		if (!isSimplePreferences(this)) {
			return;
		}
		// In the simplified UI, fragments are not used at all and we instead
		// use the older PreferenceActivity APIs.
		setContentView(R.layout.facebook_settings);
		addPreferencesFromResource(R.xml.pref_general);
		
		FrameLayout root = (FrameLayout)findViewById(R.id.top_control_bar);
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
        root.addView(bar, 0); // insert at top
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
		// Bind the summaries of EditText/List/Dialog/Ringtone preferences to
		// their values. When their values change, their summaries are updated
		// to reflect the new value, per the Android Design guidelines.
		bindPreferenceSummaryToValue(findPreference("profile_name"));
		// bindPreferenceSummaryToValue(findPreference("vstep_for_rstep"));
		// bindPreferenceSummaryToValue(findPreference("userWeight"));
		// bindPreferenceSummaryToValue(findPreference("userHeight"));
		// bindPreferenceSummaryToValue(findPreference("stepHeight"));
		bindPreferenceSummaryToValue(findPreference("difficulty"));
		// float
		// detectedSamplingRate=PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getFloat("detectedSamplingRate",
		// 0.0f);
		// findPreference("detectedSamplingRate").setSummary(new
		// DecimalFormat("#.##").format(detectedSamplingRate)+"Hz");
		
		
		SharedPreferences pref = getApplicationContext().getSharedPreferences("UserSession", 0);

		User me = ClimbApplication.getUserById(pref.getInt("local_id", -1));
		if(me != null){
			((TextView) findViewById(R.id.lblFacebookUser)).setText(me.getName());
			final Preference profile_name = findPreference("profile_name");
			profile_name.setSummary(me.getName());
			//((ProfilePictureView) findViewById(R.id.fb_profile_picture2)).setPresetSize(-2);
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean onIsMultiPane() {
		return isXLargeTablet(this) && !isSimplePreferences(this);
	}

	/**
	 * Helper method to determine if the device has an extra-large screen. For
	 * example, 10" tablets are extra-large.
	 */
	private static boolean isXLargeTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	/**
	 * Determines whether the simplified settings UI should be shown. This is
	 * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
	 * doesn't have newer APIs like {@link PreferenceFragment}, or the device
	 * doesn't have an extra-large screen. In these cases, a single-pane
	 * "simplified" settings UI should be shown.
	 */
	private static boolean isSimplePreferences(Context context) {
		return ALWAYS_SIMPLE_PREFS || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB || !isXLargeTablet(context);
	}

	/** {@inheritDoc} */
	// @Override
	// @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	// public void onBuildHeaders(List<Header> target) {
	// if (!isSimplePreferences(this)) {
	// loadHeadersFromResource(R.xml.pref_headers, target);
	// }
	// }
	/**
	 * A preference value change listener that updates the preference's summary
	 * to reflect its new value.
	 */
	public static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			String stringValue = value.toString();
			if (preference instanceof ListPreference) {
				// For list preferences, look up the correct display value in
				// the preference's 'entries' list.
				ListPreference listPreference = (ListPreference) preference;
				int index = listPreference.findIndexOfValue(stringValue);
				// Set the summary to reflect the new value.
				preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
			} else {
				// For all other preferences, set the summary to the value's
				// simple string representation.
				if(stringValue.isEmpty()){
					 final AlertDialog.Builder builder = new AlertDialog.Builder(current_activity);
	                    builder.setTitle(R.string.invalid_input_title);
	                    builder.setMessage(R.string.invalid_input_msg);
	                    builder.setPositiveButton(android.R.string.ok, null);
	                    builder.show();
	                    return false;
				}else
					preference.setSummary(stringValue);
			}
			return true;
		}
	};

	/**
	 * Binds a preference's summary to its value. More specifically, when the
	 * preference's value is changed, its summary (line of text below the
	 * preference title) is updated to reflect the value. The summary is also
	 * immediately updated upon calling this method. The exact display format is
	 * dependent on the type of preference.
	 * 
	 * @see #sBindPreferenceSummaryToValueListener
	 */
	private static void bindPreferenceSummaryToValue(Preference preference) {
		// Set the listener to watch for value changes.
		preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
		// Trigger the listener immediately with the preference's
		// current value.
		sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
	}

	/**
	 * This fragment shows general preferences only. It is used when the
	 * activity is showing a two-pane settings UI.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class GeneralPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_general);
			// Bind the summaries of EditText/List/Dialog/Ringtone preferences
			// to their values. When their values change, their summaries are
			// updated to reflect the new value, per the Android Design
			// guidelines.
			bindPreferenceSummaryToValue(findPreference("profile_name"));
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		Session session = Session.getActiveSession();
		if (session != null && (session.isOpened() || session.isClosed())) {
			onSessionStateChange(session, session.getState(), null);
		}

		uiHelper.onResume();
		ClimbApplication.activityResumed();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data);
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
}
