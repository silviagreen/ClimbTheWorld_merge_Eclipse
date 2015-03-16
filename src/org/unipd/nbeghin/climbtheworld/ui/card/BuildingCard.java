package org.unipd.nbeghin.climbtheworld.ui.card;



import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.SimpleTimeZone;

import org.json.JSONException;
import org.json.JSONObject;
import org.unipd.nbeghin.climbtheworld.ClimbApplication;
import org.unipd.nbeghin.climbtheworld.R;
import org.unipd.nbeghin.climbtheworld.TeamPreparationActivity;
import org.unipd.nbeghin.climbtheworld.models.Building;
import org.unipd.nbeghin.climbtheworld.models.BuildingText;
import org.unipd.nbeghin.climbtheworld.models.Climbing;
import org.unipd.nbeghin.climbtheworld.models.Collaboration;
import org.unipd.nbeghin.climbtheworld.models.Competition;
import org.unipd.nbeghin.climbtheworld.models.GameModeType;
import org.unipd.nbeghin.climbtheworld.models.Group;
import org.unipd.nbeghin.climbtheworld.models.Microgoal;
import org.unipd.nbeghin.climbtheworld.models.MicrogoalText;
import org.unipd.nbeghin.climbtheworld.models.TeamDuel;
import org.unipd.nbeghin.climbtheworld.models.User;
import org.unipd.nbeghin.climbtheworld.util.FacebookUtils;
import org.unipd.nbeghin.climbtheworld.util.GraphicsUtils;
import org.unipd.nbeghin.climbtheworld.util.ModelsUtil;
import org.unipd.nbeghin.climbtheworld.util.ParseUtils;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.fima.cardsui.objects.Card;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

/**
 * CardsUI card for a single building
 * 
 */
public class BuildingCard extends Card {
	final Building building;
	final private int		order;
	BuildingText buildingText;
	Climbing climbing;
	Climbing soloClimbing;
	final SharedPreferences pref = ClimbApplication.getContext().getSharedPreferences("UserSession", 0);
	GameModeType mode;
	Activity activity;
	ParseObject collabParse = new ParseObject("Collaboration");
	ParseObject competParse = new ParseObject("Competition");
	ParseObject teamDuelParse = new ParseObject("TeamDuel");
	

	final Collaboration collab = new Collaboration();
	final Competition compet = new Competition();
	final TeamDuel duel = new TeamDuel();

	boolean isUnlocked = false;

	TextView gameMode;
	TextView climbingStatus;
	TextView tourOrderText;
	Button socialClimbButton;
	Button socialChallengeButton;
	Button teamVsTeamButton;
	ImageButton microGoalBtn;
	ProgressBar progressBar;
	ImageView photo;
	
	ProgressDialog PD;

	public BuildingCard(BuildingText building, Activity activity, int order) {
		super(building.getBuilding().getName());
		this.buildingText = building;
		this.building = building.getBuilding();
		this.activity = activity;
		this.order = order;
	}

	/**
	 * Set modality text
	 * 
	 * @return the string to be shown
	 */
	private String setModeText() {
		if (climbing != null) { // a climb has began
			switch (GameModeType.values()[climbing.getGame_mode()]) {
			case SOCIAL_CHALLENGE:
				mode = GameModeType.SOCIAL_CHALLENGE;
				return "Social Challenge";
			case SOCIAL_CLIMB:
				mode = GameModeType.SOCIAL_CLIMB;
				return "Social Climb";
			case SOLO_CLIMB:
				mode = GameModeType.SOLO_CLIMB;
				return "Solo Climb";
			case TEAM_VS_TEAM:
				mode = GameModeType.TEAM_VS_TEAM;
				return "Team vs Team";
			default:
				return "Solo Climb";

			}
		} else {
			mode = GameModeType.SOLO_CLIMB;
			return "Solo Climb";
		}
	}

	
	private void showDialog(){
		PD = new ProgressDialog(activity);
		PD.setTitle(activity.getString(R.string.wait));
		PD.setMessage(activity.getString(R.string.creating_mode));
		PD.setCancelable(false);
		PD.show();
	}
	
	private void hideDialog(){
		PD.dismiss();
	}
	
	@Override
	public View getCardContent(Context context) {
		View view = LayoutInflater.from(context).inflate(R.layout.card_building_ex, null);
		gameMode = ((TextView) view.findViewById(R.id.textModalita));
		gameMode.setText(ClimbApplication.getContext().getString(R.string.mode));
		socialClimbButton = ((Button) view.findViewById(R.id.socialClimbButton));
		socialChallengeButton = ((Button) view.findViewById(R.id.socialChallengeButton));
		teamVsTeamButton = ((Button) view.findViewById(R.id.teamVsTeamButton));
		microGoalBtn = ((ImageButton) view.findViewById(R.id.microGoalBtn));
		progressBar = ((ProgressBar) view.findViewById(R.id.progressBarClimb));
		photo = ((ImageView) view.findViewById(R.id.photo));
		tourOrderText = ((TextView) view.findViewById(R.id.tourOrder));
		((TextView) view.findViewById(R.id.title)).setText(buildingText.getName());
		int imageId = ClimbApplication.getBuildingImageResource(building);
		if (imageId > 0){
//			Bitmap bitmap_image = ClimbApplication.getBitmap(building.get_id());
//			if(bitmap_image == null){
//			final int maxSize = 960;
//			int outWidth;
//			int outHeight;
//			Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),imageId);
//			int inWidth = bitmap.getWidth();
//			int inHeight = bitmap.getHeight();
//			if(inWidth > inHeight){
//			    outWidth = maxSize;
//			    outHeight = (inHeight * maxSize) / inWidth; 
//			} else {
//			    outHeight = maxSize;
//			    outWidth = (inWidth * maxSize) / inHeight; 
//			}
//			//photo.setImageResource(imageId);
//			photo.setImageBitmap(GraphicsUtils.decodeSampledBitmapFromResource(ClimbApplication.getContext().getResources(), imageId, outWidth, outHeight));
//			}else{
//				photo.setImageBitmap(bitmap_image);
//			}
			//DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().cacheInMemory(true).bitmapConfig(Bitmap.Config.RGB_565).cacheOnDisc(true).build(); // enable image caching
			
			ImageLoaderConfiguration config_image = new ImageLoaderConfiguration.Builder(context)
	        //.memoryCacheSize(41943040)
	        //.discCacheSize(104857600)
	        .threadPoolSize(10)
	        .build();

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
			
			
			
			//ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(activity.getApplicationContext()).threadPoolSize(3).defaultDisplayImageOptions(options).build();
			ImageLoader.getInstance().init(config_image);
			
			ImageLoader.getInstance().displayImage("drawable://"+imageId, photo, options, new ImageLoadingListener() {
				
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
		LayoutParams paramsPhoto = (LayoutParams) photo.getLayoutParams();
		((TextView) view.findViewById(R.id.buildingStat)).setMinLines(2);
		boolean tutorial = false;
		if(building.get_id() == 6) tutorial = true;
		((TextView) view.findViewById(R.id.buildingStat)).setText(building.getSteps() + " " + ClimbApplication.getContext().getString(R.string.steps) + building.getHeight() + "m)" + "\n" + ClimbApplication.getContext().getString(R.string.reward, ClimbApplication.XPforStep(building.getSteps(), tutorial)));
		((TextView) view.findViewById(R.id.location)).setText(buildingText.getLocation());
		
		if(order <= 0)
			tourOrderText.setVisibility(View.GONE);
		else{
			tourOrderText.setVisibility(View.VISIBLE);
			tourOrderText.setText(Integer.toString(order));
		}
			

		if (building.getBase_level() > ClimbApplication.getUserById(pref.getInt("local_id", -1)).getLevel()) {
			// locked building
			((TextView) view.findViewById(R.id.description)).setText(ClimbApplication.getContext().getString(R.string.unlock_at_level, building.getBase_level()));
			isUnlocked = false;
			ImageView photoLock = ((ImageView) view.findViewById(R.id.photoLock));
			photoLock.setVisibility(View.VISIBLE);
			photoLock.setLayoutParams(paramsPhoto);
			socialClimbButton.setVisibility(View.INVISIBLE);
			socialChallengeButton.setVisibility(View.INVISIBLE);
			teamVsTeamButton.setVisibility(View.INVISIBLE);
			microGoalBtn.setVisibility(View.INVISIBLE);
		} else {
			// unlocked building
			
			((TextView) view.findViewById(R.id.description)).setText(buildingText.getDescription());
			((ImageView) view.findViewById(R.id.photoLock)).setVisibility(View.INVISIBLE);
			isUnlocked = true;
			climbingStatus = (TextView) view.findViewById(R.id.climbingStatus);

			List<Climbing> climbs = ClimbApplication.getClimbingListForBuildingAndUser(building.get_id(), pref.getInt("local_id", -1));
			if (climbs.size() == 0)
				climbing = null;
			else if (climbs.size() == 1)
				climbing = climbs.get(0);
			else if (climbs.size() == 2 || climbs.size() == 3) {
				// if there are both a solo climbed 'paused' Climbing and
				// a social-mode Climbing, show the Social-mode Climbing
				if (climbs.get(0).getGame_mode() == 2 || climbs.get(0).getGame_mode() == 3) {
					climbing = climbs.get(0);
					soloClimbing = climbs.get(1);
				} else {
					climbing = climbs.get(1);
					soloClimbing = climbs.get(0);
				}
			}

			gameMode.setText(ClimbApplication.getContext().getString(R.string.mode) + setModeText());
			if (climbing != null) {
				if (climbing.getPercentage() >= 1.00 || climbing.getGame_mode() != 0)
					microGoalBtn.setVisibility(View.GONE);
				else
					microGoalBtn.setVisibility(View.VISIBLE);
				switch (climbing.getGame_mode()) {
				case 1:
					setSocialClimb();
					break;
				case 2:
					setSocialChallenge();
					break;
				case 3:
					setTeamChallenge();
					break;
				default:
					break;
				}
			} else {
				microGoalBtn.setVisibility(View.GONE);
			}

			updateStatus();

			if (FacebookUtils.isLoggedIn()) {

				if (climbing != null && climbing.getGame_mode() == 0) {
					if (climbing.getPercentage() >= 1.00) {
						// socialClimbButton.setEnabled(false);
						socialClimbButton.setVisibility(View.INVISIBLE);
						socialChallengeButton.setVisibility(View.VISIBLE);
						socialChallengeButton.setEnabled(true);
						teamVsTeamButton.setVisibility(View.VISIBLE);
						teamVsTeamButton.setEnabled(true);
					} else {
						socialChallengeButton.setVisibility(View.VISIBLE);
						socialClimbButton.setVisibility(View.VISIBLE);
						teamVsTeamButton.setVisibility(View.VISIBLE);
						socialClimbButton.setEnabled(true);
						socialChallengeButton.setEnabled(true);
						teamVsTeamButton.setEnabled(true);
					}
				} else if (climbing == null) {
					climbingStatus.setText(ClimbApplication.getContext().getString(R.string.notClimbedYet));
					socialChallengeButton.setVisibility(View.VISIBLE);
					socialClimbButton.setVisibility(View.VISIBLE);
					teamVsTeamButton.setVisibility(View.VISIBLE);
					socialClimbButton.setEnabled(true);
					socialChallengeButton.setEnabled(true);
					teamVsTeamButton.setEnabled(true);
				}
			} else {
				socialChallengeButton.setVisibility(View.INVISIBLE);
				socialClimbButton.setVisibility(View.INVISIBLE);
				teamVsTeamButton.setVisibility(View.INVISIBLE);
			}
			
			if(building.get_id() == 6){ //useless for tutorial
				socialChallengeButton.setVisibility(View.INVISIBLE);
				socialClimbButton.setVisibility(View.INVISIBLE);
				teamVsTeamButton.setVisibility(View.INVISIBLE);
			}
			
			microGoalBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {// show dialog with the current
												// microgoal

					try {
					
						Microgoal microgoal = ClimbApplication.getMicrogoalByUserAndBuilding(pref.getInt("local_id", -1), building.get_id());
						if (microgoal != null) {
							MicrogoalText texts = ModelsUtil.getMicrogoalTextByStory(microgoal.getStory_id());// ClimbApplication.getMicrogoalTextByStory(microgoal.getStory_id());

							final Dialog dialog = new Dialog(activity, R.style.FullHeightDialog);
							dialog.setContentView(R.layout.dialog_micro_goal);
							dialog.setCancelable(true);
							// adapt dialog to screen
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
								checked[k] = microgoal.getDone_steps() >= check_steps ? true : false; System.out.println("checked steps " + check_steps);
								climbs[k] = currents_steps;
							}

							TableLayout layout = (TableLayout) dialog.findViewById(R.id.checkBoxesLayout);

							String intro = "";
							// Random rand = new Random();
							// int randomNum1 = rand.nextInt((10 - 1) + 1) + 1;
							// int randomNum2 = rand.nextInt((20 - randomNum1) +
							// 1) + randomNum1;

							int randomNum1 = Integer.valueOf(climbs[0]) / 5;

							if (checked_size == 1)
								intro = String.format(texts.getIntro(), randomNum1) + ClimbApplication.getContext().getString(R.string.bonus_excluded);
							else if (checked_size == 2) {
								int randomNum2 = Integer.valueOf(climbs[0] + climbs[1]) / 5;
								intro = String.format(texts.getIntro(), randomNum1, randomNum2) + ClimbApplication.getContext().getString(R.string.bonus_excluded);
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
								 * TableRow row =new TableRow(activity);
								 * row.setId(i); row.setLayoutParams(new
								 * LayoutParams
								 * (LayoutParams.MATCH_PARENT,LayoutParams
								 * .WRAP_CONTENT)); CheckBox checkBox = new
								 * CheckBox(activity);
								 * checkBox.setEnabled(false);
								 * checkBox.setId(i);
								 * checkBox.setText(steps[i]);
								 * checkBox.setChecked(checked[i]);
								 * row.addView(checkBox); layout.addView(row);
								 * checkBox.setWidth(LayoutParams.WRAP_CONTENT);
								 * checkBox
								 * .setHeight(LayoutParams.WRAP_CONTENT);
								 */
								CheckBox cb = (CheckBox) dialog.findViewById(ClimbApplication.getContext().getResources().getIdentifier("checkBox" + (i + 1), "id", activity.getPackageName()));
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
							// DisplayMetrics metrics =
							// activity.getResources().getDisplayMetrics();
							// int width = metrics.widthPixels;
							// int height = metrics.heightPixels;
							// dialog.getWindow().setLayout((6 * width)/7, (4 *
							// height)/5);

							ProgressBar pb = (ProgressBar) dialog.findViewById(R.id.progressBarDialog);
							TextView perc = (TextView) dialog.findViewById(R.id.textPercentageDialog);
							double percentage = Math.round(((double) microgoal.getDone_steps() / (double) microgoal.getTot_steps()) * 100);
							pb.setIndeterminate(false);
							if(percentage >= 100) percentage = 100;
							pb.setProgress((int) percentage);
							perc.setText(String.valueOf(percentage) + "%");

							dialog.show();
						} else {
							Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.not_yet_microgoal), Toast.LENGTH_SHORT).show();
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

			});

			socialClimbButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (isUnlocked) {
						if (!pref.getString("FBid", "none").equalsIgnoreCase("none")) {
							if (FacebookUtils.isOnline(activity)) {

								switch (mode) {
								case SOLO_CLIMB: // from Solo Climb to Social Climb
									showDialog();
									if (climbing == null) {
										// create new collaboration if none
										// exists for
										// current building
										climbing = new Climbing();
										climbing.setBuilding(building);
										climbing.setCompleted(0);
										climbing.setCompleted_steps(0);
										climbing.setRemaining_steps(building.getSteps());
										climbing.setCreated(new Date().getTime());
										climbing.setModified(new Date().getTime());
										climbing.setGame_mode(1);
										climbing.setPercentage(0);
										climbing.setUser(ClimbApplication.getUserById(pref.getInt("local_id", -1)));
										ClimbApplication.climbingDao.create(climbing);
										saveClimbingInParse();
									} else {
										// update existing Climbing object
										climbing.setGame_mode(1);
										if (climbing.getPercentage() >= 1.00) {
											// if I have already climbed this
											// building,
											// keep in memory that I have
											// completed it once
											// then reuse it for current
											// collaboration
											climbing.setPercentage(0);
											climbing.setCompleted_steps(0);
											climbing.setRemaining_steps(building.getSteps());
										}
										ClimbApplication.climbingDao.update(climbing);
										updateClimbingInParse(climbing, false);
									}

									break;

								case SOCIAL_CLIMB: // back to solo climb
									Log.d("onClick", "solo");
									climbing.setGame_mode(0);
									climbing.setId_mode("");
									ClimbApplication.climbingDao.update(climbing);
									updateClimbingInParse(climbing, true);
									leaveCollaboration();
									break;
								}

							} else {
								Toast.makeText(activity.getApplicationContext(), ClimbApplication.getContext().getString(R.string.connection_needed), Toast.LENGTH_SHORT).show();
							}
						} else {
							Toast.makeText(activity.getApplicationContext(), ClimbApplication.getContext().getString(R.string.no_fb_connection), Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(activity.getApplicationContext(), ClimbApplication.getContext().getString(R.string.lock_message, building.getBase_level()), Toast.LENGTH_SHORT).show();
					}
				}
			});

			socialChallengeButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					System.out.println(isUnlocked);
					if (isUnlocked) {
						if (!pref.getString("FBid", "none").equalsIgnoreCase("none")) {
							if (FacebookUtils.isOnline(activity)) {

								switch (mode) {
								case SOLO_CLIMB:
									showDialog();
									if (climbing != null) {
										// if there is an existing Climbing object with game mode as 0 then 'pause' it and create a new Climbing
										// object with game mode as 2 when the social challenge has
										// finished, delete the last Climbing object and 'resume' the first one in solo climb
							
										soloClimbing = climbing;
									}
									// otherwise create a new Climbing object with game mode as 2
									climbing = new Climbing();
									climbing.setBuilding(building);
									climbing.setCompleted(0);
									climbing.setCompleted_steps(0);
									climbing.setRemaining_steps(building.getSteps());
									climbing.setCreated(new Date().getTime());
									climbing.setModified(new Date().getTime());
									climbing.setGame_mode(2);
									climbing.setPercentage(0);
									climbing.setId_mode("");
									climbing.setUser(ClimbApplication.getUserById(pref.getInt("local_id", -1)));
									ClimbApplication.climbingDao.create(climbing);
									saveClimbingInParse();
									break;

								case SOCIAL_CHALLENGE: // back to Solo Climb
									Log.d("onClick", "solo");
									/*
									 * climbing.setGame_mode(0);
									 * climbing.setId_mode("");
									 * ClimbApplication.
									 * climbingDao.update(climbing);
									 */
									if (soloClimbing == null) {
										climbing.setGame_mode(0);
										climbing.setId_mode("");
										ClimbApplication.climbingDao.update(climbing);
										updateClimbingInParse(climbing, true);

									} else {
										Climbing del = climbing;
										deleteClimbingInParse(del);
										climbing = soloClimbing;
									}
									leaveCompetition();

									break;
								}

							} else {
								Toast.makeText(activity.getApplicationContext(), ClimbApplication.getContext().getString(R.string.connection_needed), Toast.LENGTH_SHORT).show();

							}
						} else {
							Toast.makeText(activity.getApplicationContext(), ClimbApplication.getContext().getString(R.string.no_fb_connection), Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(activity.getApplicationContext(), ClimbApplication.getContext().getString(R.string.lock_message, building.getBase_level()), Toast.LENGTH_SHORT).show();
					}
				}
			});

			teamVsTeamButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if (isUnlocked) {
						if (!pref.getString("FBid", "none").equalsIgnoreCase("none")) {

							switch (mode) {
							case SOLO_CLIMB:
								if (FacebookUtils.isOnline(activity)) {
									showDialog();
									if (climbing != null) {
										// if there is an existing Climbing
										// object with
										// game mode as 0
										// then 'pause' it and create a new
										// Climbing
										// object with game mode as 3
										// when the social challenge has
										// finished,
										// delete the last Climbing object and
										// 'resume'
										// the first one in solo climb
										soloClimbing = climbing;
									}
									// otherwise create a new Climbing object
									// with game
									// mode as 2
									climbing = new Climbing();
									climbing.setBuilding(building);
									climbing.setCompleted(0);
									climbing.setCompleted_steps(0);
									climbing.setRemaining_steps(building.getSteps());
									climbing.setCreated(new Date().getTime());
									climbing.setModified(new Date().getTime());
									climbing.setGame_mode(3);
									climbing.setPercentage(0);
									climbing.setId_mode("");
									climbing.setUser(ClimbApplication.getUserById(pref.getInt("local_id", -1)));
									ClimbApplication.climbingDao.create(climbing);
									saveClimbingInParse();

								} else {
									Toast.makeText(activity.getApplicationContext(), ClimbApplication.getContext().getString(R.string.connection_needed), Toast.LENGTH_SHORT).show();

								}
								break;
							case TEAM_VS_TEAM:
								TeamDuel currentDuel = ClimbApplication.getTeamDuelByBuildingAndUser(building.get_id(), pref.getInt("local_id", -1));
								if (currentDuel.getId_online() == null || currentDuel.getId_online().equals(""))
									Toast.makeText(activity.getApplicationContext(), ClimbApplication.getContext().getString(R.string.connection_needed), Toast.LENGTH_SHORT).show();
								else {
									Log.i("BuildingCard", "Building id clicked: " + building.get_id());
									Intent intent = new Intent(activity.getApplicationContext(), TeamPreparationActivity.class);
									intent.putExtra(ClimbApplication.building_text_intent_object, buildingText.get_id());
									intent.putExtra(ClimbApplication.duel_intent_object, currentDuel.get_id());
									activity.startActivity(intent);
								}
								break;
							}
						} else {
							Toast.makeText(activity.getApplicationContext(), ClimbApplication.getContext().getString(R.string.no_fb_connection), Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(activity.getApplicationContext(), ClimbApplication.getContext().getString(R.string.lock_message, building.getBase_level()), Toast.LENGTH_SHORT).show();
					}
				}
			});

		}
		
		

		
		
		return view;
	}

	/**
	 * Delete the given Climbing object from Parse. If the elimination is not
	 * successfull, remember locally to delete it later.
	 * 
	 * @param climb
	 *            the Climbing object to be removed
	 */
	private void deleteClimbingInParse(final Climbing climb) {
		System.out.println("delete " + climb.getId_online());
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Climbing");
		query.whereEqualTo("objectId", climb.getId_online());
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> climbs, ParseException e) {
				if (e == null) {
					if (climbs.size() != 0) {
						ParseUtils.deleteClimbing(climbs.get(0), climb);
						//climbs.get(0).deleteEventually();
						//ClimbApplication.climbingDao.delete(climb);

					}
				} else {
					climb.setDeleted(true);
					climb.setSaved(false);
					ClimbApplication.climbingDao.update(climb);
					Toast.makeText(activity.getApplicationContext(), ClimbApplication.getContext().getString(R.string.connection_needed), Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	/**
	 * Save the update of climbing object in Parse. Here, only game mode and
	 * id_mode fields can change
	 */
	private void updateClimbingInParse(final Climbing climbing, final boolean rollback) {
		Log.d("update climbing", String.valueOf(climbing.getGame_mode()));
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Climbing");
		/*
		 * query.whereEqualTo("building", climbing.getBuilding().get_id());
		 * query.whereEqualTo("users_id", climbing.getUser().getFBid()); if(used
		 * && mode == GameModeType.SOCIAL_CHALLENGE){ if(paused){ if(equal)
		 * query.whereEqualTo("id_mode", "paused"); else
		 * query.whereNotEqualTo("id_mode", "paused"); } else
		 * query.whereEqualTo("id_mode", climbing.getId_mode()); }
		 */
		query.whereEqualTo("objectId", climbing.getId_online());
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> climbings, ParseException e) {
				if (e == null) {
					System.out.println("update " + climbings.get(0).getObjectId());
					ParseObject c = climbings.get(0);
					c.put("game_mode", climbing.getGame_mode());
					if (climbing.getId_mode() != null)
						c.put("id_mode", climbing.getId_mode());
					else
						c.put("id_mode", "");
					System.out.println("climbing id mode: " + climbing.getId_mode());
					c.saveInBackground(new SaveCallback() {

						@Override
						public void done(ParseException e) {
							if (e == null) {
								if (!rollback && climbing.getId_mode() == null || climbing.getId_mode().equalsIgnoreCase("")) {
									switch (climbing.getGame_mode()) {
									case 1:
										saveCollaboration();
										break;
									case 2:

										break;
									case 3:
										break;
									default:
										break;
									}
								}
								if (climbing.getGame_mode() == 3 && !rollback) {
									hideDialog();
									mode = GameModeType.TEAM_VS_TEAM;
									setTeamChallenge();

									Log.i("Building Card", "Building id clicked: " + building.get_id());
									Intent intent = new Intent(activity.getApplicationContext(), TeamPreparationActivity.class);
									intent.putExtra(ClimbApplication.building_text_intent_object, buildingText.get_id());
									activity.startActivity(intent);
								}
							} else {
								Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.connection_needed), Toast.LENGTH_SHORT).show();
								Log.e("updateClimbingInParse", e.getMessage());
								if (climbing.getGame_mode() == 1 && collab.getBuilding() == null) {
									climbing.setGame_mode(0);
									climbing.setId_mode("");
								}
								// if(climbing.getId_mode().equals("paused")){
								// climbing.setId_mode("");
								// rollback(2);
								// }
								hideDialog();
								climbing.setSaved(false);
								ClimbApplication.climbingDao.update(climbing);
							}
						}
					});
					climbing.setSaved(true);
					ClimbApplication.climbingDao.update(climbing);
				} else {
					Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
					Log.e("updateClimbingInParse", e.getMessage());
					if (climbing.getGame_mode() == 1 && collab.getBuilding() == null) {
						climbing.setGame_mode(0);
						climbing.setId_mode("");
					}
					hideDialog();
					climbing.setSaved(false);
					ClimbApplication.climbingDao.update(climbing);
				}
			}
		});
	}

	/**
	 * Save current Collaboration object in Parse
	 */
	private void saveClimbingInParse() {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		df.setTimeZone(new SimpleTimeZone(0, "GMT"));
		final ParseObject climb = new ParseObject("Climbing");
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
		climb.put("checked", climbing.isChecked());
		if (climbing.getId_mode() != null)
			climb.put("id_mode", climbing.getId_mode());
		else
			climb.put("id_mode", "");
		climb.saveInBackground(new SaveCallback() {

			@Override
			public void done(ParseException e) {
				if (e == null) {
					climbing.setId_online(climb.getObjectId());
					climbing.setSaved(true);
					ClimbApplication.climbingDao.update(climbing);
					switch (climbing.getGame_mode()) {
					case 1:
						saveCollaboration();
						break;
					case 2:
						saveCompetition();
						break;
					case 3:
						saveTeamDuel();
					}

				} else {
					if (climbing.getGame_mode() == 1) {
						// unable to save the collaboration, save only the
						// climbing
						climbing.setGame_mode(0);
						climbing.setId_mode("");
					}
					if (climbing.getGame_mode() == 2 || climbing.getGame_mode() == 3) {
						if (soloClimbing == null) {
							climbing.setGame_mode(0);
							climbing.setId_mode("");
						} else {
							Climbing del = climbing;
							climbing = soloClimbing;
							climbing.setGame_mode(0);
							climbing.setId_mode("");
							del.setDeleted(true);
							del.setSaved(false);
							ClimbApplication.climbingDao.update(del);
						}
					}
					climbing.setSaved(false);
					ClimbApplication.climbingDao.update(climbing);
					hideDialog();
					//Toast.makeText(activity, ClimbApplication.getContext().getString(R.string.connection_problem2), Toast.LENGTH_SHORT).show();
				}

			}
		});

	}

	/**
	 * Update button graphics after changing game mode
	 */
	void setSocialClimb() {
		gameMode.setText(ClimbApplication.getContext().getString(R.string.mode) + setModeText());
		socialClimbButton.setText(ClimbApplication.getContext().getString(R.string.back_solo_climb));
		socialChallengeButton.setVisibility(View.INVISIBLE);
		teamVsTeamButton.setVisibility(View.INVISIBLE);
		// socialChallengeButton.setEnabled(false);
		// teamVsTeamButton.setEnabled(false);
	}

	void setSocialChallenge() {
		gameMode.setText(ClimbApplication.getContext().getString(R.string.mode) + setModeText());
		socialChallengeButton.setText(ClimbApplication.getContext().getString(R.string.back_solo_climb));
		teamVsTeamButton.setVisibility(View.INVISIBLE);
		socialClimbButton.setVisibility(View.INVISIBLE);
		// socialClimbButton.setEnabled(false);
		// teamVsTeamButton.setEnabled(false);
	}

	void setTeamChallenge() {
		gameMode.setText(ClimbApplication.getContext().getString(R.string.mode) + setModeText());
		teamVsTeamButton.setVisibility(View.INVISIBLE);
		socialClimbButton.setVisibility(View.INVISIBLE);
		socialChallengeButton.setVisibility(View.INVISIBLE);
		// teamVsTeamButton.setEnabled(false);
		// socialClimbButton.setEnabled(false);
		// socialChallengeButton.setEnabled(false);
	}

	/**
	 * Create and save a new Collaboration object both online and offline. Then,
	 * open dialog to let user choose the friends to invite to his
	 * collaboration.
	 */
	private void saveCollaboration() {
		Log.d("saveCollaboration", "into");
		JSONObject stairs = new JSONObject();
		JSONObject collaborators = new JSONObject();

		try {
			collaborators.put(pref.getString("FBid", ""), pref.getString("username", ""));
			stairs.put(pref.getString("FBid", ""), climbing.getCompleted_steps());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		User me = ClimbApplication.getUserById(pref.getInt("local_id", -1));
		collab.setBuilding(building);
		collab.setMy_stairs(0);
		collab.setOthers_stairs(0);
		collab.setLeaved(false);
		collab.setUser(me);
		collab.setCompleted(false);
		collab.setAmICreator(true);
		ClimbApplication.collaborationDao.create(collab);

		JSONObject creator = new JSONObject();
		try {
			creator.put(me.getFBid(), me.getName());
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		collabParse = new ParseObject("Collaboration");
		collabParse.put("building", building.get_id());
		collabParse.put("stairs", stairs);
		collabParse.put("collaborators", collaborators);
		collabParse.put("completed", false);
		collabParse.put("creator", creator);
		collabParse.saveInBackground(new SaveCallback() {

			@Override
			public void done(ParseException e) {
				if (e == null) {
					collab.setId(collabParse.getObjectId());
					collab.setSaved(true);
					ClimbApplication.collaborationDao.update(collab);
					climbing.setId_mode(collab.getId());
					ClimbApplication.climbingDao.update(climbing);
					updateClimbingInParse(climbing, false);

					mode = GameModeType.SOCIAL_CLIMB;
					hideDialog();
					setSocialClimb();
					sendRequest(GameModeType.SOCIAL_CLIMB, collab.getId());
				} else {
					collab.setSaved(false);
					ClimbApplication.collaborationDao.update(collab);
					/*
					 * climbing.setId_mode("set"); climbing.setGame_mode(1);
					 */
					climbing.setSaved(false);
					ClimbApplication.climbingDao.update(climbing);
					hideDialog();
					Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
					Log.e("saveCollaboration", e.getMessage());
				}
			}
		});

	}
	
	

	/**
	 * Saves the Competition object both locally and in Parse. If the operation
	 * is not successfull, remember locally to retry it later.
	 */
	private void saveCompetition() {
		System.out.println("save competition");
		JSONObject stairs = new JSONObject();
		JSONObject collaborators = new JSONObject();

		try {
			collaborators.put(pref.getString("FBid", ""), pref.getString("username", ""));
			stairs.put(pref.getString("FBid", ""), climbing.getCompleted_steps());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		User me = ClimbApplication.getUserById(pref.getInt("local_id", -1));
		JSONObject creator = new JSONObject();
		try {
			creator.put(me.getFBid(), me.getName());
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
		compet.setBuilding(building);
		compet.setMy_stairs(0);
		compet.setCurrent_position(0);
		compet.setLeaved(false);
		compet.setUser(me);
		compet.setCompleted(false);
		compet.setAmICreator(true);
		compet.setChecks(0);
		compet.setWinner_id("0");
		compet.setVictory_time(0);
		compet.setDifficulty(Integer.parseInt(settings.getString("difficulty", "10")));
		ClimbApplication.competitionDao.create(compet);
		
		competParse = new ParseObject("Competition");
		competParse.put("building", building.get_id());
		competParse.put("stairs", stairs);
		competParse.put("competitors", collaborators);
		competParse.put("completed", false);
		competParse.put("creator", creator);
		competParse.put("checks", compet.getChecks());
		competParse.put("winner_id", compet.getWinner_id());
		competParse.put("difficulty", compet.getDifficulty());
		try {
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			competParse.put("victory_time", df.parse(df.format(compet.getVictory_time())));
		} catch (java.text.ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		competParse.saveInBackground(new SaveCallback() {

			@Override
			public void done(ParseException e) {
				if (e == null) {
					System.out.println("assegnato id a competiz" + competParse.getObjectId());
					compet.setId_online(competParse.getObjectId());
					compet.setSaved(true);
					ClimbApplication.competitionDao.update(compet);

					climbing.setId_mode(compet.getId_online());
					ClimbApplication.climbingDao.update(climbing);
					updateClimbingInParse(climbing, false);
					hideDialog();
					mode = GameModeType.SOCIAL_CHALLENGE;
					setSocialChallenge();
					sendRequest(GameModeType.SOCIAL_CHALLENGE, compet.getId_online());
				} else {
					//System.out.println("assegnato id a competiz????" + competParse.getObjectId());
					compet.setSaved(false);
					climbing.setSaved(false);
					ClimbApplication.competitionDao.update(compet);
					ClimbApplication.climbingDao.update(climbing);
					hideDialog();
					Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
					Log.e("saveCompetition", e.getMessage());
				}
			}
		});
	}

	/**
	 * Saves the TeamDuel object both locally and in Parse. If the operation is
	 * not successful, remember locally to retry it later.
	 */
	private void saveTeamDuel() {
		System.out.println("save team duel");
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
		User me = ClimbApplication.getUserById(pref.getInt("local_id", -1));
		duel.setBuilding(building);
		duel.setUser(me);
		duel.setSteps_my_group(0);
		duel.setSteps_other_group(0);
		duel.setMy_steps(0);
		duel.setChallenger_name("");
		duel.setCreator_name(me.getName());
		duel.setMygroup(Group.CREATOR);
		duel.setCompleted(false);
		duel.setCreator(true);
		duel.setDeleted(false);
		duel.setCreator_name(me.getName());
		duel.setReadyToPlay(false);
		duel.setVictory_time(0);
		duel.setChecks(0);
		duel.setWinner_id("0");
		duel.setDifficulty(Integer.parseInt(settings.getString("difficulty", "10")));
		ClimbApplication.teamDuelDao.create(duel);

		JSONObject creator_stairs = new JSONObject();
		JSONObject creator_team = new JSONObject();
		JSONObject challenger_stairs = new JSONObject();
		JSONObject challenger_team = new JSONObject();
		JSONObject creator = new JSONObject();
		JSONObject challenger = new JSONObject();

		try {
			// creator_team.put(pref.getString("FBid", ""),
			// pref.getString("username", ""));
			creator_stairs.put(pref.getString("FBid", ""), 0);
			creator.put(pref.getString("FBid", ""), pref.getString("username", ""));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		teamDuelParse = new ParseObject("TeamDuel");
		teamDuelParse.put("creator", creator);
		teamDuelParse.put("challenger", challenger);
		teamDuelParse.put("building", building.get_id());
		teamDuelParse.put("completed", false);
		teamDuelParse.put("creator_stairs", creator_stairs);
		teamDuelParse.put("creator_team", creator_team);
		teamDuelParse.put("challenger_team", challenger_team);
		teamDuelParse.put("challenger_stairs", challenger_stairs);
		teamDuelParse.put("winner_id", duel.getWinner_id());
		teamDuelParse.put("checks", duel.getChecks());
		teamDuelParse.put("difficulty", duel.getDifficulty());
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

		try {
			teamDuelParse.put("victory_time", df.parse(df.format(compet.getVictory_time())));
		} catch (java.text.ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		teamDuelParse.saveInBackground(new SaveCallback() {

			@Override
			public void done(ParseException e) {
				if (e == null) {

					duel.setId_online(teamDuelParse.getObjectId());
					//System.out.println("id duel: " + teamDuelParse.getObjectId());

					duel.setSaved(true);
					ClimbApplication.teamDuelDao.update(duel);

					climbing.setId_mode(duel.getId_online());
					ClimbApplication.climbingDao.update(climbing);
					//System.out.println("id duel: " + duel.getId_online());

					updateClimbingInParse(climbing, false);

				} else {
					duel.setSaved(false);
					climbing.setSaved(false);
					ClimbApplication.teamDuelDao.update(duel);
					ClimbApplication.climbingDao.update(climbing);
					hideDialog();
					Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
					Log.e("saveTeamCompetition", e.getMessage());
				}
			}
		});
	}

	/**
	 * Makes the current logged user leaving the current Collaboration. It saved
	 * the update in Parse if it is possible, otherwise, it saved the update
	 * locally e remembers to retry it later.
	 */
	private void leaveCollaboration() {
		final Collaboration collab = ClimbApplication.getCollaborationByBuildingAndUser(building.get_id(), pref.getInt("local_id", -1));
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Collaboration");
		query.whereEqualTo("objectId", collab.getId());
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> collabs, ParseException e) {
				if (e == null) {
					if (collabs.size() == 0) {
						Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.collaboration_not_exists), Toast.LENGTH_SHORT).show();
						ClimbApplication.collaborationDao.delete(collab);
					} else {
						ParseObject c = collabs.get(0);
						JSONObject collaborators = c.getJSONObject("collaborators");
						JSONObject stairs = c.getJSONObject("stairs");
						collaborators.remove(pref.getString("FBid", ""));
						stairs.remove(pref.getString("FBid", ""));
						c.put("collaborators", collaborators);
						c.put("stairs", stairs);
						collab.setLeaved(true);
						if(collab.getAmICreator() && collaborators.length() > 0){
							Iterator<String> it = collaborators.keys();
							try {
								JSONObject creator = c.getJSONObject("creator");
								creator.remove(pref.getString("FBid", ""));
								String new_creator_key = it.next();
								creator.put(new_creator_key, collaborators.get(new_creator_key));
							} catch (JSONException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							
						}
						if (collaborators.length() == 0)
							ParseUtils.deleteCollaboration(c, collab);
						// c.deleteEventually();
						else {
							ParseUtils.saveCollaboration(c, collab);

						}
						// c.saveEventually();

						// ClimbApplication.collaborationDao.delete(collab);
						climbing.setGame_mode(0);
						climbing.setId_mode("");
						ClimbApplication.climbingDao.update(climbing);
						updateClimbingInParse(climbing, true);
					}
				} else {
					collab.setLeaved(true);
					collab.setSaved(false);
					ClimbApplication.collaborationDao.update(collab);
					Toast.makeText(ClimbApplication.getContext(), ClimbApplication.getContext().getString(R.string.connection_problem), Toast.LENGTH_SHORT).show();
					Log.e("updateClimbingInParse", e.getMessage());
				}
				socialChallengeButton.setVisibility(View.VISIBLE);
				socialChallengeButton.setEnabled(true);
				socialClimbButton.setVisibility(View.VISIBLE);
				socialClimbButton.setEnabled(true);
				teamVsTeamButton.setVisibility(View.VISIBLE);
				teamVsTeamButton.setEnabled(true);
				socialClimbButton.setText("Social Climb");
				mode = GameModeType.SOLO_CLIMB;
				gameMode.setText(ClimbApplication.getContext().getString(R.string.mode) + setModeText());
			}
		});
	}

	/**
	 * Makes the current logged user leaving the current Competition. It saved
	 * the update in Parse if it is possible, otherwise, it saved the update
	 * locally e remembers to retry it later.
	 */
	private void leaveCompetition() {
		final Competition competit = ClimbApplication.getCompetitionByBuildingAndUser(building.get_id(), pref.getInt("local_id", -1));
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Competition");
		query.whereEqualTo("objectId", competit.getId_online());
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> competits, ParseException e) {
				if (e == null) {
					if (competits.size() == 0) {
						Toast.makeText(ClimbApplication.getContext(), "This competition does not exists anymore", Toast.LENGTH_SHORT).show();
						ClimbApplication.competitionDao.delete(competit);
					} else {
						ParseObject c = competits.get(0);
						JSONObject collaborators = c.getJSONObject("competitors");
						JSONObject stairs = c.getJSONObject("stairs");
						collaborators.remove(pref.getString("FBid", ""));
						stairs.remove(pref.getString("FBid", ""));
						c.put("competitors", collaborators);
						c.put("stairs", stairs);
						competit.setLeaved(true);
						if(competit.getAmICreator() && collaborators.length() > 0){
							Iterator<String> it = collaborators.keys();
							try {
								JSONObject creator = c.getJSONObject("creator");
								creator.remove(pref.getString("FBid", ""));
								String new_creator_key = it.next();
								creator.put(new_creator_key, collaborators.get(new_creator_key));
							} catch (JSONException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							
						}
						if (collaborators.length() == 0)
							// c.deleteEventually();
							ParseUtils.deleteCompetition(c, competit);
						else
							// c.saveEventually();
							ParseUtils.saveCompetition(c, competit);

						// ClimbApplication.competitionDao.delete(competit);

					}
				} else {
					competit.setLeaved(true);
					competit.setSaved(false);
					ClimbApplication.competitionDao.update(competit);
					Toast.makeText(ClimbApplication.getContext(), "7 Connection Problems", Toast.LENGTH_SHORT).show();
					Log.e("leaveCompetition", e.getMessage());
				}
				socialChallengeButton.setVisibility(View.VISIBLE);
				socialChallengeButton.setEnabled(true);
				socialClimbButton.setVisibility(View.VISIBLE);
				socialClimbButton.setEnabled(true);
				teamVsTeamButton.setVisibility(View.VISIBLE);
				teamVsTeamButton.setEnabled(true);
				socialChallengeButton.setText("Social Challenge");
				mode = GameModeType.SOLO_CLIMB;
				gameMode.setText(ClimbApplication.getContext().getString(R.string.mode) + setModeText());
			}
		});
	}

	/**
	 * Open a dialog to let user choose the friends to invite.
	 * 
	 * @param mode
	 *            current game mode
	 * @param idCollab
	 *            id of the current Collaboration/Competition/TeamDuel object
	 */
	private void sendRequest(GameModeType mode, String idCollab) {
		Bundle params = new Bundle();
		final GameModeType currentMode = mode;
		switch (mode) {
		case SOCIAL_CLIMB:
			params.putString("data", "{\"idCollab\":\"" + idCollab + "\"," + "\"idBuilding\":\"" + building.get_id() + "\"," + "\"nameBuilding\":\"" + building.getName() + "\", \"type\": \"1\"}");
			params.putString("message", "Please, help me!!!!");
			break;
		case SOCIAL_CHALLENGE:
			params.putString("data", "{\"idCollab\":\"" + idCollab + "\"," + "\"idBuilding\":\"" + building.get_id() + "\"," + "\"nameBuilding\":\"" + building.getName() + "\", \"type\": \"2\"}");
			params.putString("message", "challenge");

			break;
		case TEAM_VS_TEAM:
			params.putString("data", "{\"idCollab\":\"" + idCollab + "\"," + "\"idBuilding\":\"" + building.get_id() + "\"," + "\"nameBuilding\":\"" + building.getName() + "\", \"type\": \"3\"}");
			params.putString("message", "team challenge");

			break;
		}

		if (Session.getActiveSession() != null && Session.getActiveSession().isOpened()) {
			WebDialog requestsDialog = (new WebDialog.RequestsDialogBuilder(activity, Session.getActiveSession(), params)).setOnCompleteListener(new OnCompleteListener() {

				@Override
				public void onComplete(Bundle values, FacebookException error) {
					if (error != null) {
						if (error instanceof FacebookOperationCanceledException) {
							Toast.makeText(activity, "Request cancelled", Toast.LENGTH_SHORT).show();
							switch (currentMode) {
							case SOCIAL_CLIMB:
								rollback(1);
								break;

							case SOCIAL_CHALLENGE:
								rollback(2);
								break;
							}
						} else {
							Toast.makeText(activity, "Network Error", Toast.LENGTH_SHORT).show();
							switch (currentMode) {
							case SOCIAL_CLIMB:
								rollback(1);
								break;

							case SOCIAL_CHALLENGE:
								rollback(2);
								break;
							}
						}
					} else {
						final String requestId = values.getString("request");
						if (requestId != null) {
							Toast.makeText(activity, "Request sent", Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(activity, "Request cancelled", Toast.LENGTH_SHORT).show();
							switch (currentMode) {
							case SOCIAL_CLIMB:
								rollback(1);
								break;

							case SOCIAL_CHALLENGE:
								rollback(2);
								break;
							}

						}
					}
				}

			}).build();
			requestsDialog.show();
		} else {
			Toast.makeText(activity, "Currently not logged to FB", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Rollback in case of deleted operation
	 */
	private void rollback(int type) {
		switch (type) {
		case 1:
			ParseUtils.deleteCollaboration(collabParse, collab);
			//collabParse.deleteEventually();
			//ClimbApplication.collaborationDao.delete(collab);

			climbing.setGame_mode(0);
			climbing.setId_mode("");
			ClimbApplication.climbingDao.update(climbing);
			updateClimbingInParse(climbing, true);
			graphicsRollBack(type);

			mode = GameModeType.SOLO_CLIMB;
			gameMode.setText(ClimbApplication.getContext().getString(R.string.mode) + setModeText());
			break;

		case 2:
			if (competParse.getJSONObject("competitors") != null) {
				//competParse.deleteEventually();
				ParseUtils.deleteCompetition(competParse, compet);
			}
			if (compet.getBuilding() != null) {
				System.out.println("delete local");
				ClimbApplication.competitionDao.delete(compet);
			}

			if (soloClimbing != null) {
				System.out.println("sistema due climb");
				Climbing del = climbing;
				climbing = soloClimbing;
				climbing.setId_mode("");
				ClimbApplication.climbingDao.update(climbing);
				updateClimbingInParse(climbing, true);

				deleteClimbingInParse(del);
			} else {
				System.out.println("sistema unico climb");
				climbing.setGame_mode(0);
				climbing.setId_mode("");
				ClimbApplication.climbingDao.update(climbing);
				updateClimbingInParse(climbing, true);
			}
			graphicsRollBack(type);

			mode = GameModeType.SOLO_CLIMB;
			gameMode.setText(ClimbApplication.getContext().getString(R.string.mode) + setModeText());
			break;

		case 3:
			/*
			 * teamDuelParse.deleteEventually();
			 * ClimbApplication.teamDuelDao.delete(duel);
			 * 
			 * soloClimbing.setId_mode("");
			 * ClimbApplication.climbingDao.update(soloClimbing);
			 * updateClimbingInParse(climbing, true);
			 * 
			 * deleteClimbingInParse(climbing); graphicsRollBack(type);
			 * 
			 * mode = GameModeType.SOLO_CLIMB; gameMode.setText("Modalit�: " +
			 * setModeText());
			 */
			break;
		}
		updateStatus();
	}

	/**
	 * Updates Status text
	 */
	private void updateStatus() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
		if (climbing != null) {
			if (climbing.getPercentage() >= 1.00) {
				climbingStatus.setText(ClimbApplication.getContext().getString(R.string.climb_complete, sdf.format(new Date(climbing.getModified()))));
			} else {
				climbingStatus.setText(ClimbApplication.getContext().getString(R.string.climb_status, new DecimalFormat("#").format(climbing.getPercentage() * 100), sdf.format(new Date(climbing.getModified()))));
			}
			progressBar.setIndeterminate(false);
			progressBar.setProgress((int) (climbing.getPercentage() * 100));
		}
	}

	/**
	 * Update graphics in case of deleted operation
	 * 
	 * @param type
	 */
	private void graphicsRollBack(int type) {
		if (climbing.getPercentage() >= 1.00) {
			socialChallengeButton.setVisibility(View.VISIBLE);
			socialChallengeButton.setEnabled(true);
			socialClimbButton.setVisibility(View.INVISIBLE);
			socialClimbButton.setEnabled(false);
			teamVsTeamButton.setVisibility(View.VISIBLE);
			teamVsTeamButton.setEnabled(true);
		} else {
			socialChallengeButton.setVisibility(View.VISIBLE);
			socialChallengeButton.setEnabled(true);
			socialClimbButton.setVisibility(View.VISIBLE);
			socialClimbButton.setEnabled(true);
			teamVsTeamButton.setVisibility(View.VISIBLE);
			teamVsTeamButton.setEnabled(true);
		}
		switch (type) {
		case 1:
			socialClimbButton.setText("Social Climb");
			break;
		case 2:
			socialChallengeButton.setText("Social Challenge");
			break;
		case 3:
			teamVsTeamButton.setText("Team vs Team");
			break;

		}
	}

	@Override
	public boolean convert(View convertCardView) {
		// TODO Auto-generated method stub
		return false;
	}
}
