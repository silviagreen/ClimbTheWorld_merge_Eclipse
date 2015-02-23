package org.unipd.nbeghin.climbtheworld;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.json.JSONObject;
import org.unipd.nbeghin.climbtheworld.adapters.CheckboxListViewAdapter;
import org.unipd.nbeghin.climbtheworld.util.FacebookUtils;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.widget.WebDialog;

public class FBPickFriendActivity extends ActionBarActivity {

	
	public static List<String> idsToInvite = new ArrayList<String>();
	private ListView invitableList;
	private Button doneButton;
	private TextView noInvitable;
	private CheckboxListViewAdapter adapter;
	// Parameters of a WebDialog that should be displayed
    private WebDialog dialog = null;
    private String dialogAction = null;
    private Bundle dialogParams = null;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fb_pick_friend);
		noInvitable = (TextView) findViewById(R.id.textNoInvitable);
		doneButton = (Button) findViewById(R.id.buttonInvite);
		invitableList = (ListView) findViewById(R.id.listInvitableView);
		
		if(ClimbApplication.invitableFriends.isEmpty()){
			noInvitable.setVisibility(View.VISIBLE);
			invitableList.setVisibility(View.GONE);
		}else{
			noInvitable.setVisibility(View.GONE);
			invitableList.setVisibility(View.VISIBLE);
		}

		adapter = new CheckboxListViewAdapter(this, ClimbApplication.invitableFriends);
		adapter.sort(new Comparator<JSONObject>() {//alphabetical order
		    @Override
		    public int compare(JSONObject lhs, JSONObject rhs) {
		    	String name_lhs = lhs.optString("first_name") + " " + lhs.optString("last_name");
		    	String name_rhs = rhs.optString("first_name") + " " + rhs.optString("last_name");
		        return name_lhs.compareTo(name_rhs);    
		    }
		});
		invitableList.setAdapter(adapter);

		invitableList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {

				CheckBox cb = (CheckBox) view.findViewById(R.id.checkBoxItem);
				JSONObject clickedUser = ClimbApplication.invitableFriends.get(position);
				String invitableToken = clickedUser.optString("id");

				// items act as toggles. so check to see if item exists. if it
				// does
				// then remove. otherwise, add it.
				if (idsToInvite.contains(invitableToken)) {
					cb.setChecked(false);
					idsToInvite.remove(invitableToken);
				} else {
					cb.setChecked(true);
					idsToInvite.add(invitableToken);
				}
			}
		});
		
		doneButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(FacebookUtils.isOnline(FBPickFriendActivity.this)){
					sendDirectedInvite(idsToInvite);
				}else
					Toast.makeText(FBPickFriendActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
			}
		});
	}
	

	private void sendDirectedInvite(List<String> invitableTokens) {
		Bundle params = new Bundle();
		params.putString("message", "Come join me in Climb the world!");
		params.putString("to", TextUtils.join(",", invitableTokens));
		showDialogWithoutNotificationBar("apprequests", params);
	}

	// Show a dialog (feed or request) without a notification bar (i.e. full
	// screen)
	private void showDialogWithoutNotificationBar(String action, Bundle params) {
		// Create the dialog
		dialog = new WebDialog.Builder(this, Session.getActiveSession(), action, params).setOnCompleteListener(new WebDialog.OnCompleteListener() {

			@Override
			public void onComplete(Bundle values, FacebookException error) {
				if (error != null && !(error instanceof FacebookOperationCanceledException)) {
					showError(getResources().getString(R.string.network_error));
				}
				if(error == null)
					Toast.makeText(FBPickFriendActivity.this, getString(R.string.invite_sent), Toast.LENGTH_SHORT).show();

				dialog = null;
				dialogAction = null;
				dialogParams = null;
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
	}
	
	// Show user error message as a toast
		void showError(String error) {
			Toast.makeText(this, error, Toast.LENGTH_LONG).show();
		}
		
		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
//			// Inflate the menu; this adds items to the action bar if it is present.

			 getMenuInflater().inflate(R.menu.fb_invite, menu);

		        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		        MenuItem menuitem = (MenuItem) menu.findItem(R.id.action_search_friend);
		        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuitem);
		        
		        if (null != searchView )
		        {
		            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		            searchView.setIconifiedByDefault(false);   
		        }

		        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() 
		        {
		            public boolean onQueryTextChange(String newText) 
		            {
		                // this is your adapter that will be filtered
		                adapter.getFilter().filter(newText);
		                return true;
		            }

		            public boolean onQueryTextSubmit(String query) 
		            {
		                // this is your adapter that will be filtered
		                adapter.getFilter().filter(query);
		                return true;
		            }
		        };
		        searchView.setOnQueryTextListener(queryTextListener);

		        return super.onCreateOptionsMenu(menu);
		}
		
		 @Override
		    protected void onResume() {
		      super.onResume();
		      ClimbApplication.activityResumed();
		    }

		    @Override
		    protected void onPause() {
		      super.onPause();
		      ClimbApplication.activityPaused();
		    }
}
