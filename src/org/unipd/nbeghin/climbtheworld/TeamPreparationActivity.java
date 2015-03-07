package org.unipd.nbeghin.climbtheworld;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;
import org.unipd.nbeghin.climbtheworld.models.Building;
import org.unipd.nbeghin.climbtheworld.models.Climbing;
import org.unipd.nbeghin.climbtheworld.models.Group;
import org.unipd.nbeghin.climbtheworld.models.TeamDuel;
import org.unipd.nbeghin.climbtheworld.models.User;
import org.unipd.nbeghin.climbtheworld.util.FacebookUtils;
import org.unipd.nbeghin.climbtheworld.util.ParseUtils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class TeamPreparationActivity extends ActionBarActivity {

	// Our created menu to use
	private Menu mymenu;

	Button addMyMembersBtn;
	Button addChallengerBtn;
	ImageButton startPlay;
	Button addChallengerTeamBtn;
	Button exitTeam;
	ProgressBar progressbar;
	List<TextView> myMembers = new ArrayList<TextView>();
	List<TextView> theirMembers = new ArrayList<TextView>();
	TextView challengerName;
	TextView creatorName;
	TextView offline;
	TeamDuel duel;

	int building_id = 0;
	int team_online_id = 0;
	Building building;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preparation_teams);
		SharedPreferences pref = getSharedPreferences("UserSession", 0);
		building_id = getIntent().getIntExtra(ClimbApplication.building_text_intent_object, 0);
		building = ClimbApplication.getBuildingById(building_id);
		addMyMembersBtn = (Button) findViewById(R.id.addMyTeamBtn);
		addChallengerBtn = (Button) findViewById(R.id.addChallengerBtn);
		startPlay = (ImageButton) findViewById(R.id.btnStartClimbing);
		exitTeam = (Button) findViewById(R.id.btnExitClimbing);
		addChallengerTeamBtn = (Button) findViewById(R.id.addChallengerTeam);
		challengerName = (TextView) findViewById(R.id.textChallenger);
		creatorName = (TextView) findViewById(R.id.textCreator);
		offline = (TextView) findViewById(R.id.textOffline);
		progressbar = (ProgressBar) findViewById(R.id.progressBarTeams);
		progressbar.setIndeterminate(true);
		progressbar.setVisibility(View.VISIBLE);
		startPlay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				onStartClimbingBtn();
			}
		});

		addChallengerBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onAddChallengerBtn();
			}
		});

		addMyMembersBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onAddMyMembersBtn();
			}
		});

		addChallengerTeamBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				onAddChallengerMemberBtn();
			}
		});

		addMyMembersBtn.setEnabled(false);
		addChallengerBtn.setEnabled(false);
		startPlay.setEnabled(false);
		exitTeam.setEnabled(false);
		
//		if(!FacebookUtils.isOnline(getApplicationContext()))
//			exitTeam.setEnabled(false);
//		else
//			exitTeam.setEnabled(true);

		for (int i = 0; i < ClimbApplication.N_MEMBERS_PER_GROUP - 1; i++) {
			int id = getResources().getIdentifier("textMyMember" + (i + 1), "id", getPackageName());
			myMembers.add((TextView) findViewById(id));
		}
		for (int i = 0; i < ClimbApplication.N_MEMBERS_PER_GROUP - 1; i++) {
			int id = getResources().getIdentifier("textOtherMember" + (i + 1), "id", getPackageName());
			theirMembers.add((TextView) findViewById(id));
		}

		// int team_online_id =
		// getIntent().getIntExtra(ClimbApplication.duel_intent_object, 0);
		// System.out.println(building_id + " " + pref.getInt("local_id", -1));
		duel = ClimbApplication.getTeamDuelByBuildingAndUser(building_id, pref.getInt("local_id", -1));
		// quando arrivo qui, id online di duel deve essere settato

		if (duel.isCreator()) {
			addChallengerBtn.setVisibility(View.VISIBLE);
			addChallengerTeamBtn.setVisibility(View.GONE);
			addMyMembersBtn.setVisibility(View.VISIBLE);
		} else if (duel.isChallenger()) {
			addChallengerBtn.setVisibility(View.GONE);
			addChallengerTeamBtn.setVisibility(View.VISIBLE);
			addMyMembersBtn.setVisibility(View.GONE);
		} else if (!duel.isCreator() && !duel.isChallenger()) {
			addChallengerBtn.setVisibility(View.GONE);
			addChallengerTeamBtn.setVisibility(View.GONE);
			addMyMembersBtn.setVisibility(View.GONE);
		}

		
		//getTeams(false);
		timer.schedule(updates, 1500, 3000);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.team_preparation, menu);

		// We should save our menu so we can use it to reset our updater.
		mymenu = menu;

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
//		case R.id.itemUpdate:
//			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//			ImageView iv = (ImageView) inflater.inflate(R.layout.refresh_dark, null);
//			Animation rotation = AnimationUtils.loadAnimation(this, R.anim.rotate_refresh);
//			rotation.setRepeatCount(Animation.INFINITE);
//			iv.startAnimation(rotation);
//			MenuItemCompat.setActionView(item, iv);
//			onUpdate();
//			return true;
		case R.id.itemHelp:
			new HelpDialogActivity(this, R.style.Transparent, duel).show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

//	public void resetUpdating() {
//		// Get our refresh item from the menu
//		MenuItem m = mymenu.findItem(R.id.itemUpdate);
//		if (MenuItemCompat.getActionView(m) != null) {
//			// Remove the animation.
//			MenuItemCompat.getActionView(m).clearAnimation();
//			MenuItemCompat.setActionView(m, null);
//		}
//	}

	/**
	 * Get the team from Parse and shows its data in current activity
	 * 
	 * @param isUpdate
	 */
	private void getTeams(final boolean isUpdate) {
		if(FacebookUtils.isOnline(getApplicationContext())){
			
		ParseQuery<ParseObject> query = ParseQuery.getQuery("TeamDuel");
		query.getInBackground(duel.getId_online(), new GetCallback<ParseObject>() {

			@Override
			public void done(ParseObject obj, ParseException e) {
				final SharedPreferences pref = getSharedPreferences("UserSession", 0);

				if (e == null) {
					try {
						JSONObject challenger = obj.getJSONObject("challenger");
						JSONObject creator = obj.getJSONObject("creator");
						Iterator<String> it_challenger = challenger.keys();
						if (it_challenger.hasNext()) {
							String challenger_name = challenger.getString(it_challenger.next());
							if (duel.getChallenger_name() == null || duel.getChallenger_name().equals("") || !duel.getChallenger_name().equalsIgnoreCase(challenger_name)) {
								duel.setChallenger_name(challenger_name);
								if(duel.getChallenger_name().equalsIgnoreCase(pref.getString("username", ""))){
									duel.setChallenger(true);
									duel.setMygroup(Group.CHALLENGER);}
								ClimbApplication.teamDuelDao.update(duel);
								addChallengerBtn.setEnabled(false);
							} else if (duel.getChallenger_name() == null || duel.getChallenger_name().equals("")) {
								addChallengerBtn.setEnabled(true);
							}
						} else {System.out.println(duel.getChallenger_name() != null );
							if(duel.getChallenger_name() == null || !duel.getChallenger_name().equals("")){
								duel.setChallenger_name("");
								ClimbApplication.teamDuelDao.update(duel);
							}
							addChallengerBtn.setEnabled(true);

						}
						Iterator<String> it = creator.keys();
						
						if (it.hasNext()) {
							Iterator<String> it_creator = creator.keys();
							if(it_creator.hasNext()){
								String creator_name = creator.getString(it_creator.next());
								if (duel.getCreator_name() == null || duel.getCreator_name().equals("") || !duel.getCreator_name().equalsIgnoreCase(creator_name)) {
									duel.setCreator_name(creator_name);
									if(duel.getCreator_name().equalsIgnoreCase(pref.getString("username", ""))){
										duel.setCreator(true);
										duel.setMygroup(Group.CREATOR);}
									ClimbApplication.teamDuelDao.update(duel);
								} else if(duel.getCreator_name() == null || duel.getCreator_name().equals("")){
								
								}
							}else{
								if(duel.getCreator_name() == null || !duel.getCreator_name().equals("")){
									duel.setCreator_name("");
									ClimbApplication.teamDuelDao.update(duel);
								}
							}
													
								
							

						}

						showTeams(obj, isUpdate);
					} catch (JSONException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

				} else {
					if(e.getCode() == ParseException.OBJECT_NOT_FOUND){
						ClimbApplication.teamDuelDao.delete(duel);
						// final String id_to_delete = duel.getId_online();
						User me = ClimbApplication.getUserByFBId(pref.getString("FBid", ""));
						final List<Climbing> climbings = ClimbApplication.getClimbingListForBuildingAndUser(duel.getBuilding().get_id(), me.get_id());
						
						ParseQuery<ParseObject> query = ParseQuery.getQuery("Climbing");
						query.whereEqualTo("id_mode", duel.getId_online());
						query.whereEqualTo("users_id", me.getFBid());
						query.getFirstInBackground(new GetCallback<ParseObject>() {

							@Override
							public void done(ParseObject climbing, ParseException ex) {
								if (ex == null) {
									if (climbing != null) {

										Climbing l_climbing;
										if (climbings.size() == 2) {
											if (climbings.get(0).getGame_mode() == 3)
												ParseUtils.deleteClimbing(climbing, climbings.get(0));
											else
												ParseUtils.deleteClimbing(climbing, climbings.get(1));
										} else if (climbings.size() == 1) { 
											climbing.put("id_mode", "");
											climbing.put("game_mode", 0);
											l_climbing = climbings.get(0);
											l_climbing.setId_mode("");
											l_climbing.setGame_mode(0);
											ParseUtils.saveClimbing(climbing, l_climbing);
										}
									}
								} else {
									Climbing l_climbing;
									if (climbings.size() == 1) {
										l_climbing = climbings.get(0);
										l_climbing.setId_mode("");
										l_climbing.setGame_mode(0);
										l_climbing.setSaved(false);
										ClimbApplication.climbingDao.update(l_climbing);
									} else if (climbings.size() == 2) {
										l_climbing = climbings.get(0).getGame_mode() == 3 ? climbings.get(0) : climbings.get(1);
										l_climbing.setDeleted(true);
										l_climbing.setSaved(false);
										ClimbApplication.climbingDao.update(l_climbing);

									}
									

									// Toast.makeText(ClimbApplication.getContext(),
									// ClimbApplication.getContext().getString(R.string.connection_problem2),
									// Toast.LENGTH_SHORT).show();
									offline.setVisibility(View.VISIBLE);
									exitTeam.setEnabled(false);
									Log.e(getClass().getName(), ex.getMessage());
								}

							}

						});
					Toast.makeText(getApplicationContext(), getString(R.string.team_duel_no_available), Toast.LENGTH_SHORT).show();
					
					finish();
					}else{
						offline.setVisibility(View.VISIBLE);
						exitTeam.setEnabled(false);
						Toast.makeText(getApplicationContext(), getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
					}
					Log.e("onUpdate", e.getCode() + e.getMessage());
					//if (isUpdate)
						// Change the menu back
						//resetUpdating();
				}
				progressbar.setVisibility(View.GONE);
				offline.setVisibility(View.VISIBLE);
				exitTeam.setEnabled(true);
			}
		});
	}else{
		offline.setVisibility(View.VISIBLE);
		exitTeam.setEnabled(false);
		//if (isUpdate)
			// Change the menu back
			//resetUpdating();
	}
	}
	/**
	 * Shows teams to user
	 * 
	 * @param duelParse
	 * @param isUpdate
	 * @throws JSONException
	 */
	private void showTeams(final ParseObject duelParse, boolean isUpdate) throws JSONException {
		if (duel.getCreator_name() != null && !duel.getCreator_name().equals(""))
			creatorName.setText(getString(R.string.creator) + " " + duel.getCreator_name());
		else
			creatorName.setText(getString(R.string.creator) + " - ");
		if ((duel.getChallenger_name() != null && !duel.getChallenger_name().equals("")))
			challengerName.setText(getString(R.string.creator) + " " + duel.getChallenger_name());
		else
			challengerName.setText(getString(R.string.creator) + " - ");
		if (duel.isCreator()) {
			if (duel.getChallenger_name() != null && !duel.getChallenger_name().equals(""))
				addChallengerBtn.setEnabled(false);
			else
				addChallengerBtn.setEnabled(true);
		}
		JSONObject myTeam;
		JSONObject challengerTeam;
		// if(duel.getMygroup() == Group.CREATOR){
		myTeam = duelParse.getJSONObject("creator_team");
		challengerTeam = duelParse.getJSONObject("challenger_team");
		// }else{
		// challengerTeam = duelParse.getJSONObject("creator_team");
		// myTeam = duelParse.getJSONObject("challenger_team");
		// }

		showTeam(myTeam, myMembers);
		showTeam(challengerTeam, theirMembers);
		exitTeam.setEnabled(true);
		exitTeam.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(FacebookUtils.isOnline(getApplicationContext())){
					offline.setVisibility(View.VISIBLE);
					backToSoloClimb(duelParse);
				}else{
					offline.setVisibility(View.VISIBLE);
					Toast.makeText(getApplicationContext(), getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
				}
			}
		});

		if (duel.isCreator()) {
			if (myTeam.length() < (ClimbApplication.N_MEMBERS_PER_GROUP - 1))
				addMyMembersBtn.setEnabled(true);
			else
				addMyMembersBtn.setEnabled(false);
		} else if (duel.isChallenger()) {
			if (challengerTeam.length() < (ClimbApplication.N_MEMBERS_PER_GROUP - 1))
				addChallengerTeamBtn.setEnabled(true);
			else
				addChallengerTeamBtn.setEnabled(false);
		} else {
			addChallengerTeamBtn.setEnabled(false);
			addMyMembersBtn.setEnabled(false);
			addChallengerBtn.setEnabled(false);
		}
		if (myTeam.length() == (ClimbApplication.N_MEMBERS_PER_GROUP - 1) && challengerTeam.length() == (ClimbApplication.N_MEMBERS_PER_GROUP - 1)) {
			startPlay.setEnabled(true);
			startPlay.setVisibility(View.VISIBLE);
			exitTeam.setEnabled(false);
			exitTeam.setVisibility(View.GONE);
			duel.setReadyToPlay(true);
			offline.setText(getString(R.string.completed_teams));
			offline.setVisibility(View.VISIBLE);
			ClimbApplication.teamDuelDao.update(duel);
			timer.cancel();
		} else {
			startPlay.setEnabled(false);
			startPlay.setVisibility(View.GONE);
			duel.setReadyToPlay(false);
			exitTeam.setEnabled(true);
			exitTeam.setVisibility(View.VISIBLE);
			offline.setText(getString(R.string.wait_team));
			offline.setVisibility(View.VISIBLE);
			ClimbApplication.teamDuelDao.update(duel);
			
		}

//		if (isUpdate)
//			// Change the menu back
//			resetUpdating();

	}

	private void showTeam(JSONObject team, List<TextView> textTeam) throws JSONException {
		Iterator<String> it1 = team.keys();
		int i = 0;
		while (it1.hasNext()) {
			textTeam.get(i).setText(team.getString(it1.next()));
			i++;
		}
		for (; i < textTeam.size(); i++) {
			textTeam.get(i).setText("   -");
		}
	}

	public void onUpdate() {
		getTeams(true);
	}
	
	final Handler handler = new Handler();
    Timer timer = new Timer();
    TimerTask updates = new TimerTask() {       
        @Override
        public void run() {
            handler.post(new Runnable() {
                public void run() {       
                	getTeams(true);
                }
            });
        }
    };

	/**
	 * Opens ClimbActivity to start the game
	 */
	public void onStartClimbingBtn() { // passo anche team id????
		Log.i("Team Preparation", "Building id clicked: " + String.valueOf(building_id));
		Intent intent = new Intent(getApplicationContext(), ClimbActivity.class);
		intent.putExtra(ClimbApplication.building_text_intent_object, building_id);
		finish();
		startActivity(intent);
	}

	/**
	 * Open a dialog to let user choose the friend to challenge
	 */
	public void onAddChallengerBtn() {
		Bundle params = new Bundle();
		params.putString("data", "{\"idCollab\":\"" + duel.getId_online() + "\"," + "" + "\"idBuilding\":\"" + building_id + "\"," + "\"nameBuilding\":\"" + building.getName() + "\"," + " \"type\": \"3\"," + "\"challenger\": \"" + true + "\"," // you
																																																													// will
																																																													// be
																																																													// my
																																																													// challenger
				+ "\"isSenderCreator\": \"" + duel.isCreator() + "\"" + "}");
		params.putString("message", "team challenge");

		sendRequests(params);
	}

	/**
	 * Open a dialog to let user choose the friends to invite as member of its
	 * team.
	 */
	public void onAddMyMembersBtn() {

		Bundle params = new Bundle();
		params.putString("data", "{\"idCollab\":\"" + duel.getId_online() + "\"," + "" + "\"idBuilding\":\"" + building_id + "\"," + "\"nameBuilding\":\"" + building.getName() + "\"," + " \"type\": \"3\"," + "\"challenger\": \"" + false + "\"," // you
																																																														// will
																																																														// not
																																																														// be
																																																														// my
																																																														// challenger
				+ "\"isSenderCreator\": \"" + duel.isCreator() + "\"" // enter
																		// in my
																		// team
				+ "}");
		params.putString("message", "team challenge");

		sendRequests(params);

	}

	public void onAddChallengerMemberBtn() {
		Bundle params = new Bundle();
		params.putString("data", "{\"idCollab\":\"" + duel.getId_online() + "\"," + "" + "\"idBuilding\":\"" + building_id + "\"," + "\"nameBuilding\":\"" + building.getName() + "\"," + " \"type\": \"3\"," + "\"challenger\": \"" + false + "\"," // you
																																																														// will
																																																														// not
																																																														// be
																																																														// my
																																																														// challenger
				+ "\"isSenderCreator\": \"" + duel.isCreator() + "\"" // enter
																		// in my
																		// team
				+ "}");
		params.putString("message", "team challenge");

		sendRequests(params);
	}

	/**
	 * Opens a dialog to let user send facebook requests to his friends, given
	 * parameters
	 * 
	 * @param params
	 *            the given parameters
	 */
	private void sendRequests(Bundle params) {
		if (Session.getActiveSession() != null && Session.getActiveSession().isOpened()) {

			WebDialog requestsDialog = (new WebDialog.RequestsDialogBuilder(TeamPreparationActivity.this, Session.getActiveSession(), params)).setOnCompleteListener(new OnCompleteListener() {

				@Override
				public void onComplete(Bundle values, FacebookException error) {
					if (error != null) {
						if (error instanceof FacebookOperationCanceledException) {
							Toast.makeText(getApplicationContext(), getString(R.string.request_cancelled), Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(getApplicationContext(), getString(R.string.network_error), Toast.LENGTH_SHORT).show();
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
			requestsDialog.show();
		} else {
			Toast.makeText(getApplicationContext(), getString(R.string.not_logged), Toast.LENGTH_SHORT).show();
		}
	}

	private void backToSoloClimb(ParseObject team) {
		final SharedPreferences pref = getSharedPreferences("UserSession", 0);
		// final String id_to_delete = duel.getId_online();
		User me = ClimbApplication.getUserByFBId(pref.getString("FBid", ""));
		final List<Climbing> climbings = ClimbApplication.getClimbingListForBuildingAndUser(duel.getBuilding().get_id(), me.get_id());

		ParseQuery<ParseObject> query = ParseQuery.getQuery("Climbing");
		query.whereEqualTo("id_mode", duel.getId_online());
		query.whereEqualTo("users_id", me.getFBid());
		query.getFirstInBackground(new GetCallback<ParseObject>() {

			@Override
			public void done(ParseObject climbing, ParseException ex) {
				if (ex == null) {
					if (climbing != null) {

						Climbing l_climbing;
						if (climbings.size() == 2) {
							if (climbings.get(0).getGame_mode() == 3)
								ParseUtils.deleteClimbing(climbing, climbings.get(0));
							else
								ParseUtils.deleteClimbing(climbing, climbings.get(1));
						} else if (climbings.size() == 1) { 
							climbing.put("id_mode", "");
							climbing.put("game_mode", 0);
							l_climbing = climbings.get(0);
							l_climbing.setId_mode("");
							l_climbing.setGame_mode(0);
							ParseUtils.saveClimbing(climbing, l_climbing);
						}
					}
				} else {
					Climbing l_climbing;
					if (climbings.size() == 1) {
						l_climbing = climbings.get(0);
						l_climbing.setId_mode("");
						l_climbing.setGame_mode(0);
						l_climbing.setSaved(false);
						ClimbApplication.climbingDao.update(l_climbing);
					} else if (climbings.size() == 2) {
						l_climbing = climbings.get(0).getGame_mode() == 3 ? climbings.get(0) : climbings.get(1);
						l_climbing.setDeleted(true);
						l_climbing.setSaved(false);
						ClimbApplication.climbingDao.update(l_climbing);

					}

					// Toast.makeText(ClimbApplication.getContext(),
					// ClimbApplication.getContext().getString(R.string.connection_problem2),
					// Toast.LENGTH_SHORT).show();
					Log.e(getClass().getName(), ex.getMessage());
				}

			}

		});

		if (!duel.isReadyToPlay()) {
			System.out.println("ready to play");
			JSONObject team_challenger = team.getJSONObject("challenger_stairs");
			JSONObject creator_team = team.getJSONObject("creator_stairs");
			JSONObject creators = team.getJSONObject("creator_team");
			JSONObject challengers = team.getJSONObject("challenger_team");

			if (duel.isChallenger()) {
				JSONObject challenger = team.getJSONObject("challenger");
				challenger.remove(pref.getString("FBid", ""));
				team_challenger.remove(pref.getString("FBid", ""));
				team.put("challenger", challenger);
				team.put("challenger_stairs", team_challenger);
				if (challengers.length() > 0) {
					// il primo amico ad aver accettato diventa il team leader
					Iterator<String> it = challengers.keys();
					String next_challenger_key = it.next();
					String next_challenger_name;
					try {
						next_challenger_name = challengers.getString(next_challenger_key);
						challenger.put(next_challenger_key, next_challenger_name);
						challengers.remove(next_challenger_key);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			} else if (duel.isCreator()) {
				System.out.println("is creatore");
				JSONObject creator = team.getJSONObject("creator");
				creator.remove(pref.getString("FBid", ""));
				creator_team.remove(pref.getString("FBid", ""));
				team.put("creator", creator);
				team.put("creator_stairs", creator_team);
				if (creators.length() > 0) {
					// il primo amico ad aver accettato diventa il team leader
					Iterator<String> it = creators.keys();
					String next_creator_key = it.next();
					String next_creator_name;
					try {
						next_creator_name = creators.getString(next_creator_key);
						creator.put(next_creator_key, next_creator_name);
						creators.remove(next_creator_key);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else {

				if (creators.has(pref.getString("FBid", ""))) {
					creators.remove(pref.getString("FBid", ""));
					creator_team.remove(pref.getString("FBid", ""));
					team.put("creator_stairs", creator_team);
					team.put("creator_team", creators);

				} else {
					team_challenger.remove(pref.getString("FBid", ""));
					challengers.remove(pref.getString("FBid", ""));
					team.put("challenger_stairs", team_challenger);
					team.put("challenger_team", challengers);
				}

			}
			System.out.println(creator_team.length());
			if (creator_team.length() == 0 /*&& team_challenger.length() == 0*/)
				ParseUtils.deleteTeamDuel(team, duel);
			else{
				ParseUtils.saveTeamDuel(team, duel);
				ClimbApplication.teamDuelDao.delete(duel);
			}

		}

		finish();
	
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
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		timer.cancel();
	}
}
