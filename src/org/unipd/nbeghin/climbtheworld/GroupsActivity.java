package org.unipd.nbeghin.climbtheworld;

//TODO stabilire se nome gruppo e valido
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.unipd.nbeghin.climbtheworld.util.FacebookUtils;

import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.facebook.FacebookException;
import com.parse.FindCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.ParseException;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity.Header;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

/**
 * UNUSED
 *
 */
public class GroupsActivity extends ActionBarActivity {
	/** Called when the activity is first created. */
	private ExpandListAdapter ExpAdapter;
	private ArrayList<ExpandListGroup> ExpListItems;
	private ExpandableListView ExpandList;
	private EditText newName;
	private ImageButton sendRequests;
	boolean usable;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_groups);
		ExpandList = (ExpandableListView) findViewById(R.id.groupsList);
		// ExpListItems =
		setMyGroups();
		
		newName = (EditText) findViewById(R.id.insertName);

		ExpandList.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				ExpandableListAdapter adapter = ExpandList
						.getExpandableListAdapter();
				ExpandListChild elc = (ExpandListChild) adapter.getChild(
						groupPosition, childPosition);
				String selected = elc.getTag();

				Log.d("child cick", "child of " + selected);

				// Nothing here ever fires
				if (selected != null) {
					// invia le richieste per entrare nel gruppo
					Bundle params = new Bundle();
					params.putString("message", "Hey, let's make a team!!!!");
					params.putString("data", "{\"team_name\":\"" + selected
							+ "\", \"type\": \"0\"}");
					WebDialog requestsDialog = (new WebDialog.RequestsDialogBuilder(
							GroupsActivity.this, Session.getActiveSession(),
							params)).setOnCompleteListener(
							new OnCompleteListener() {
								@Override
								public void onComplete(Bundle values,
										FacebookException error) {
									if (error != null) {
										if (error instanceof FacebookOperationCanceledException) {
											Toast.makeText(
													getApplicationContext(),
													getString(R.string.request_cancelled),
													Toast.LENGTH_SHORT).show();
										} else {
											Toast.makeText(
													getApplicationContext(),
													getString(R.string.network_error),
													Toast.LENGTH_SHORT).show();

										}
									} else {
										final String requestId = values
												.getString("request");
										if (requestId != null) {
											Toast.makeText(
													getApplicationContext(),
													getString(R.string.request_sent),
													Toast.LENGTH_SHORT).show();

										} else {
											Toast.makeText(
													getApplicationContext(),
													getString(R.string.request_cancelled),
													Toast.LENGTH_SHORT).show();

										}
									}
								}
							}).build();
					requestsDialog.show();
				}
				return true;
			}
		});

		sendRequests = (ImageButton) findViewById(R.id.addMembers);

		sendRequests.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (FacebookUtils.isOnline(GroupsActivity.this)) {
					onCreateNewGroup();
				} else {
					Toast.makeText(getApplicationContext(),
							getString(R.string.check_connection), Toast.LENGTH_SHORT)
							.show();
				}

			}
		});
	}

	public void setMyGroups() {
		List<String> myGroups = new ArrayList<String>();
		SharedPreferences pref = getSharedPreferences("UserSession", 0);

		ParseQuery<ParseObject> groups = ParseQuery.getQuery("Group");

		/*
		 * ArrayList<String> me = new ArrayList<String>();
		 * me.add(pref.getString("FBid", "")); System.out.println("cerco " +
		 * pref.getString("FBid", "")); groups.whereContainsAll("members",
		 * me);//whereEqualTo("members", pref.getString("FBid", ""));
		 */
		groups.whereEqualTo("members." + pref.getString("FBid", ""),
				pref.getString("username", ""));

		groups.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> mygroups, ParseException e) {
				if (e == null)
					SetGroups(mygroups);
				else {
					e.printStackTrace();
					Toast.makeText(getApplicationContext(),
							getString(R.string.check_connection), Toast.LENGTH_SHORT)
							.show();
				}

			}
		});
		// List<ParseObject> mygroups = groups.find();
		// System.out.println("gruppi: " + mygroups.size());

		/*
		 * for(ParseObject group : mygroups){
		 * myGroups.add(group.getString("name")); }
		 */

		/*
		 * ParseQuery<ParseUser> user = ParseUser.getQuery();
		 * user.whereEqualTo("FBid", pref.getString("FBid", ""));
		 * Log.d("FBid da cercare", "cerco " + pref.getString("FBid", "")); try
		 * { List<ParseUser> users = user.find(); List<String> array =
		 * (ArrayList<String>) users.get(0).get("Groups"); SetGroups((array)); }
		 * catch (ParseException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */

		/*
		 * user.findInBackground(new FindCallback<ParseUser>() { public void
		 * done(List<ParseUser> users, ParseException e) { if (e == null) {
		 * String[] array = (String[]) users.get(0).get("groups");
		 * SetGroups(Arrays.asList(array)); } else {
		 * 
		 * } } });
		 */
	}

	public void SetGroups(List<ParseObject> groups) {

		ArrayList<ExpandListGroup> list = new ArrayList<ExpandListGroup>();
		ArrayList<ExpandListChild> list2 = new ArrayList<ExpandListChild>();

		if (groups.size() == 0) {
			System.out.println("nessun gruppo");
			ExpandListGroup gru1 = new ExpandListGroup();
			gru1.setName("No Groups :(");

			list.add(gru1);
		} else {

			for (final ParseObject group : groups) {
				ExpandListGroup gru1 = new ExpandListGroup();
				gru1.setName(group.getString("name"));
				JSONObject members = group.getJSONObject("members");

				Iterator ids = members.keys();
				while (ids.hasNext()) {
					String name = "";
					try {
						name = members.getString((String) ids.next());
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					ExpandListChild ch1_m = new ExpandListChild();
					ch1_m.setName("    " + name);
					list2.add(ch1_m);
				}

				if (members.length() < 5) {
					ExpandListChild ch1_1 = new ExpandListChild();
					ch1_1.setName(getString(R.string.add_member));
					ch1_1.setTag(gru1.getName());

					list2.add(ch1_1);
					// System.out.println("ch1.1 tag " + ch1_1.getTag());

				}
				gru1.setItems(list2);
				list.add(gru1);
			}
		}


		ExpListItems = list;
		ExpAdapter = new ExpandListAdapter(GroupsActivity.this, ExpListItems);
		ExpandList.setAdapter(ExpAdapter);
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
		getMenuInflater().inflate(R.menu.groups, menu);
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

	/*
	 * boolean usableGroupName(String name){
	 * 
	 * ParseQuery<ParseObject> query = ParseQuery.getQuery("Group");
	 * query.whereEqualTo("name", name); try { int groups = query.count();
	 * if(groups == 0) return true; else return false; } catch (ParseException
	 * e) { // TODO Auto-generated catch block e.printStackTrace();
	 * Toast.makeText(getApplicationContext(), "No connection available",
	 * Toast.LENGTH_SHORT).show(); return false; } }
	 */

	// controllo su id
	private void onCreateNewGroup() {
		final String groupName = newName.getText().toString();
		if (groupName.isEmpty() /* || !usableGroupName(groupName) */)
			Toast.makeText(getApplicationContext(),
					"The name must be non empty", Toast.LENGTH_SHORT).show();
		else {

			// salva gruppo su db con data di creazione (fatta in automatico)
			SharedPreferences pref = getSharedPreferences("UserSession", 0);

			final ParseObject group = new ParseObject("Group");
			group.put("name", groupName);
			JSONObject me = new JSONObject();
			try {
				me.put(pref.getString("FBid", ""), pref.getString("username", ""));
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			group.put("creator", me);
			JSONObject members = new JSONObject();
			try {
				members.put(pref.getString("FBid", ""),
						pref.getString("username", ""));
				// member.put("name", pref.getString("username", ""));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// members.put(member);
			group.put("members", members);

			group.saveEventually();

			/*
			 * List<String> members = new ArrayList<String>();
			 * members.add(pref.getString("FBid", "")); group.put("members",
			 * members); group.saveEventually();
			 */

			/*
			 * ParseObject group = new ParseObject("Group"); group.put("name",
			 * groupName); JSONArray members = new JSONArray(); JSONObject
			 * member = new JSONObject(); try { member.put("FBid",
			 * pref.getString("FBid", "")); member.put("name",
			 * pref.getString("username", "")); } catch (JSONException e) { //
			 * TODO Auto-generated catch block e.printStackTrace(); }
			 * members.put(member); group.put("members", members);
			 * 
			 * group.saveEventually(); String groupId = group.getObjectId();
			 * 
			 * ParseQuery<ParseUser> user = ParseUser.getQuery();
			 * user.whereEqualTo("FBid", pref.getString("FBid", ""));
			 * Log.d("FBid da cercare", "cerco " + pref.getString("FBid", ""));
			 * user.findInBackground(new FindCallback<ParseUser>() { public void
			 * done(List<ParseUser> users, ParseException e) { if (e == null) {
			 * System.out.println(users.size()); ParseUser me = users.get(0);
			 * me.addAllUnique("Groups", Arrays.asList(groupName));
			 * me.saveEventually(); } else {
			 * 
			 * } } });
			 */

			// invia le richieste per entrare nel gruppo
			Bundle params = new Bundle();
			params.putString("message", "Hey, let's make a team!!!!");
			params.putString("data", "{\"team_name\":\"" + groupName
					+ "\", \"type\": \"0\"}");

			WebDialog requestsDialog = (new WebDialog.RequestsDialogBuilder(
					this, Session.getActiveSession(), params))
					.setOnCompleteListener(new OnCompleteListener() {
						@Override
						public void onComplete(Bundle values,
								FacebookException error) {
							if (error != null) {
								if (error instanceof FacebookOperationCanceledException) {
									Toast.makeText(getApplicationContext(),
											getString(R.string.request_cancelled),
											Toast.LENGTH_SHORT).show();
									deleteGroup(group);
								} else {
									Toast.makeText(getApplicationContext(),
											getString(R.string.network_error), Toast.LENGTH_SHORT)
											.show();
									deleteGroup(group);

								}
							} else {
								final String requestId = values
										.getString("request");
								if (requestId != null) {
									Toast.makeText(getApplicationContext(),
											getString(R.string.request_sent), Toast.LENGTH_SHORT)
											.show();

								} else {
									Toast.makeText(getApplicationContext(),
											getString(R.string.request_cancelled),
											Toast.LENGTH_SHORT).show();
									deleteGroup(group);

								}
							}
						}
					}).build();
			requestsDialog.show();
		}

	}

	public void deleteGroup(ParseObject gr) {
		gr.deleteEventually();
	}

}
