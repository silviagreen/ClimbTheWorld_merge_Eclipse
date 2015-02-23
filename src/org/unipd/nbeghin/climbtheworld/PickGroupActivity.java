package org.unipd.nbeghin.climbtheworld;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.unipd.nbeghin.climbtheworld.models.Building;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;
/**
 * UNUSED 
 *
 */
public class PickGroupActivity extends Activity{

	private ExpandListAdapter ExpAdapter;
	private ArrayList<ExpandListGroup> ExpListItems;
	private ExpandableListView ExpandList;
	private TextView nogroupsText;
	private Building building;
	
	@Override
    public void onCreate(Bundle savedInstanceState) { 
		//TODO building da intent
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);
        ExpandList = (ExpandableListView) findViewById(R.id.groupsList);
        nogroupsText = (TextView) findViewById(R.id.noGroupsText);
        nogroupsText.setVisibility(View.GONE);
        ExpandList.setVisibility(View.VISIBLE);
        pickMyGroups();
        ExpAdapter = new ExpandListAdapter(PickGroupActivity.this, ExpListItems);
        ExpandList.setAdapter(ExpAdapter);
        
        ExpandList.setOnChildClickListener(new OnChildClickListener() {
			
			@Override
			public boolean onChildClick(ExpandableListView arg0, View arg1, int groupPosition,
					int childPosition, long arg4) {
					ExpandableListAdapter adapter = ExpandList.getExpandableListAdapter();
					ExpandListChild elc = (ExpandListChild) adapter.getChild(groupPosition, childPosition);
					ExpandListGroup elg = (ExpandListGroup) adapter.getGroup(groupPosition);
					
					//invia le richieste per entrare nel gruppo
	           		Bundle params = new Bundle();
	           	    params.putString("message", "Hey, let's make a team!!!!");
	           	    params.putString("data", "{\"team_name\":\"" + elg.getName() + "\", \"type\": \"1\", \"team_id\":\"" + elc.getTag() + "\" , \"building_id\": \""+ building.get_id() +"\" , \"building_name\": \"" + building.getName() + "\"}");
	           	    WebDialog requestsDialog = (
	           		        new WebDialog.RequestsDialogBuilder(PickGroupActivity.this, Session.getActiveSession(), params)).setOnCompleteListener(new OnCompleteListener() {
	           	                @Override
	           	                public void onComplete(Bundle values, FacebookException error) {
	           	                    if (error != null) {
	           	                        if (error instanceof FacebookOperationCanceledException) {
	           	                            Toast.makeText(getApplicationContext(), 
	           	                                getString(R.string.request_cancelled), 
	           	                                Toast.LENGTH_SHORT).show();
	           	                        } else {
	           	                            Toast.makeText(getApplicationContext(), 
	           	                            		getString(R.string.network_error), 
	           	                                Toast.LENGTH_SHORT).show();

	           	                        }
	           	                    } else {
	           	                        final String requestId = values.getString("request");
	           	                        if (requestId != null) {
	           	                            Toast.makeText(getApplicationContext(), 
	           	                            		getString(R.string.request_sent),  
	           	                                Toast.LENGTH_SHORT).show();

	           	                        } else {
	           	                            Toast.makeText(getApplicationContext(), 
	           	                            		getString(R.string.request_cancelled), 
	           	                                Toast.LENGTH_SHORT).show();

	           	                        }
	           	                    } 
	           	                    
	           	                 Intent intent = new Intent(getApplicationContext(),
	           	      				MainActivity.class);
	           	      		startActivity(intent);
	           	                }
	           	            }).build();
	           	    requestsDialog.show();
					
				return false;
			}
		});
        
        
	}
	
	void pickMyGroups(){
		SharedPreferences pref = getSharedPreferences("UserSession", 0);
		String myFBid = pref.getString("FBid", "");
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Group");
		ArrayList<String> me = new ArrayList<String>();
		me.add(myFBid);
		query.whereContainsAll("members", me);
		query.findInBackground(new FindCallback<ParseObject>() {
			
			@Override
			public void done(List<ParseObject> mygroups, ParseException e) {
				if(e == null){
					// nessun errore
					setList(mygroups);
				}else{
					Toast.makeText(getApplicationContext(), getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
					Log.e("findInBackground PickGroups", e.getMessage());
				}
			}
		});
	}
	
	void setList(List<ParseObject> mygroups){
		ArrayList<ExpandListGroup> list = new ArrayList<ExpandListGroup>();
	 	ArrayList<ExpandListChild> list2 = new ArrayList<ExpandListChild>();
	   	
	 	if(mygroups.size() == 0){ 
	 		ExpandList.setVisibility(View.INVISIBLE);
	 		nogroupsText.setVisibility(View.VISIBLE);
	 	}else{
	 		for(final ParseObject group : mygroups){
	 	        ExpandListGroup gru1 = new ExpandListGroup();
	 	        gru1.setName(group.getString("name"));
	 	        if(group.getList("members").size() < 5){
	 	        		ExpandListChild ch1_1 = new ExpandListChild();
	 	        		ch1_1.setName(getString(R.string.pick_group));
	 	        		ch1_1.setTag(group.getObjectId());
	 	        		list2.add(ch1_1);

	 	        }
	 			gru1.setItems(list2);
	 	        list.add(gru1);
	 	    	}
	 	}
	}
}
