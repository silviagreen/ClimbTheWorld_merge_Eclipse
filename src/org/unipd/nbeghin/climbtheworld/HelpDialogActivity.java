package org.unipd.nbeghin.climbtheworld;

import org.unipd.nbeghin.climbtheworld.models.GameModeType;
import org.unipd.nbeghin.climbtheworld.models.TeamDuel;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class HelpDialogActivity extends Dialog {
	
	SharedPreferences pref;
	
	ImageView imageShare;
	ImageView imageGallery;
	ImageView imageUpdate;
	ImageView imageMicrogoal;
	ImageView imageRow1;
	ImageView imageRow2;
	
	TextView textShare;
	TextView textGallery;
	TextView textUpdate;
	TextView textMicrogoal;
	TextView textRow1;
	TextView textRow2;
	
	private int i; // 0 -> ClimbActivity
					// 1 -> TeamPreparationActivity

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("HelpDialog", "onCreate");
        // creating the fullscreen dialog
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_climb_help);
		RelativeLayout root = (RelativeLayout) findViewById(R.id.main_layout);
		//root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

		//getWindow().setBackgroundDrawable(new ColorDrawable(Color.YELLOW));
		//getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		root.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
					dismiss();
					return true;
				
			}
		});
		
		 imageShare =(ImageView) findViewById(R.id.imageShareHelp);
		 imageGallery = (ImageView) findViewById(R.id.imageGallery);
		 imageUpdate = (ImageView) findViewById(R.id.imageUpdate);
		 imageMicrogoal = (ImageView) findViewById(R.id.imageMicrogoal);
		 imageRow1 = (ImageView) findViewById(R.id.imageRow1);
		 imageRow2 = (ImageView) findViewById(R.id.imageRow2);
		
		 textShare = ((TextView) findViewById(R.id.textShare));
		 textGallery = ((TextView) findViewById(R.id.textGallery));
		 textUpdate = ((TextView) findViewById(R.id.textUpdate));
		 textMicrogoal = ((TextView) findViewById(R.id.textMicrogoal));
		 textRow1 = ((TextView) findViewById(R.id.textRow1));
		 textRow2 = ((TextView) findViewById(R.id.textRow2));
		
		Typeface tf = Typeface.createFromAsset(ClimbApplication.getContext().getAssets(),"fonts/cake.ttf");  //sketch-me
		textShare.setTypeface(tf);
		textGallery.setTypeface(tf);
		textUpdate.setTypeface(tf);
		textMicrogoal.setTypeface(tf);
		textRow1.setTypeface(tf);
		textRow2.setTypeface(tf);
		((TextView) findViewById(R.id.TextDismiss)).setTypeface(tf);
		
		switch(i){
		case 0:
			setClimbActivityHelp();
			break;
		case 1:
			setTeamPreparationActivityHelp();
			break;
		case 2:
			setProfileHelp();
			break;
		default:
			break;
		}

    }
	
	GameModeType mode;
	double percentage;
	boolean new_steps;
	int n_icons;
	boolean samplingEnabled;
	boolean isCounterMode;
	boolean fbConnected;
	
	public HelpDialogActivity(Context context, int theme, GameModeType mode, double percentage, boolean new_steps, int n_icons, boolean samplingEnabled, boolean isCounterMode) {
		super(context, theme);
		i = 0;
		this.mode = mode;
		this.percentage = percentage;
		this.new_steps = new_steps;
		this.n_icons = n_icons;
		this.samplingEnabled = samplingEnabled;
		this.isCounterMode = isCounterMode;
		pref = context.getSharedPreferences("UserSession", 0);
		fbConnected = (!pref.getString("FBid", "none").equalsIgnoreCase("none") && !pref.getString("FBid", "none").equalsIgnoreCase("empty")) ? true : false;
	}
	
	private void setClimbActivityHelp(){
		if(n_icons == 2){
			setMargins(imageMicrogoal, 30);
		}else{
			setMargins(imageMicrogoal, 150);
		}
				if(isCounterMode){
					imageRow1.setVisibility(View.GONE);
					imageRow2.setVisibility(View.GONE);
					textRow1.setVisibility(View.GONE);
					textRow2.setVisibility(View.GONE);
					
					imageUpdate.setVisibility(View.INVISIBLE);
					imageMicrogoal.setVisibility(View.INVISIBLE);
					textUpdate.setVisibility(View.INVISIBLE);
					textMicrogoal.setVisibility(View.INVISIBLE);
					
					imageGallery.setVisibility(View.INVISIBLE);
					textGallery.setVisibility(View.INVISIBLE);
					
					imageShare.setVisibility(View.VISIBLE);
					textShare.setVisibility(View.VISIBLE);
					if(!samplingEnabled)
						textShare.setText(ClimbApplication.getContext().getString(R.string.play_help));
					else
						textShare.setText(ClimbApplication.getContext().getString(R.string.pause_help));
					
					
				}else{

				if(percentage >= 1.00){
					//end climb
					if(fbConnected){
						imageShare.setVisibility(View.VISIBLE);
						textShare.setText(ClimbApplication.getContext().getString(R.string.share_victory_help));
						}else{
							imageShare.setVisibility(View.INVISIBLE);
							textShare.setVisibility(View.INVISIBLE);
						}
					imageGallery.setVisibility(View.VISIBLE);
					textGallery.setText(ClimbApplication.getContext().getString(R.string.gallery_help));
					
					imageMicrogoal.setVisibility(View.INVISIBLE);
					textMicrogoal.setVisibility(View.INVISIBLE);
					
					imageUpdate.setVisibility(View.INVISIBLE);
					textUpdate.setVisibility(View.INVISIBLE);
					
					imageRow1.setVisibility(View.GONE);
					imageRow2.setVisibility(View.GONE);
					textRow1.setVisibility(View.GONE);
					textRow2.setVisibility(View.GONE);
				}else{
					imageShare.setVisibility(View.VISIBLE);
					imageMicrogoal.setVisibility(View.VISIBLE);
					textShare.setVisibility(View.VISIBLE);
					textMicrogoal.setVisibility(View.VISIBLE);
					textMicrogoal.setText(ClimbApplication.getContext().getString(R.string.microgoal_help));
					if(!samplingEnabled)
						textShare.setText(ClimbApplication.getContext().getString(R.string.play_help));
					else
						textShare.setText(ClimbApplication.getContext().getString(R.string.pause_help));

					if(!new_steps){ //play then pause and no steps done
						imageGallery.setVisibility(View.INVISIBLE);
						textGallery.setVisibility(View.INVISIBLE);
					}else{
						if(fbConnected){
							textGallery.setText(ClimbApplication.getContext().getString(R.string.share_progress_help));
							textGallery.setVisibility(View.VISIBLE);
							imageGallery.setVisibility(View.VISIBLE);
							}
						else{
							textGallery.setVisibility(View.INVISIBLE);
							imageGallery.setVisibility(View.INVISIBLE);
						}
					}
					
						
					if(mode != GameModeType.SOLO_CLIMB){
						textUpdate.setText(ClimbApplication.getContext().getString(R.string.update_help));
						imageUpdate.setVisibility(View.VISIBLE);
						textUpdate.setVisibility(View.VISIBLE);
						if(mode != GameModeType.TEAM_VS_TEAM){
							imageRow1.setVisibility(View.VISIBLE);
							imageRow2.setVisibility(View.VISIBLE);
							textRow1.setVisibility(View.VISIBLE);
							textRow2.setVisibility(View.VISIBLE);
							imageRow1.setImageResource(R.drawable.minus);
							textRow1.setText(ClimbApplication.getContext().getString(R.string.delete_player));
							imageRow2.setImageResource(R.drawable.plus);
							textRow2.setText(ClimbApplication.getContext().getString(R.string.add_other_help));}
						else{
							imageRow1.setVisibility(View.GONE);
							imageRow2.setVisibility(View.GONE);
							textRow1.setVisibility(View.GONE);
							textRow2.setVisibility(View.GONE);
						}
					}else{
						textUpdate.setVisibility(View.INVISIBLE);
						imageUpdate.setVisibility(View.INVISIBLE);
						imageRow1.setVisibility(View.GONE);
						imageRow2.setVisibility(View.GONE);
						textRow1.setVisibility(View.GONE);
						textRow2.setVisibility(View.GONE);
					}
				}
				}
	}
	
	TeamDuel duel;
	
	
	public HelpDialogActivity(Context context, int theme, TeamDuel duel) {
		super(context, theme);
		i = 1;
		this.duel = duel;
	}
	
	private void setTeamPreparationActivityHelp(){
		imageShare.setVisibility(View.VISIBLE);
		imageGallery.setVisibility(View.INVISIBLE);
		imageMicrogoal.setVisibility(View.VISIBLE);
		imageUpdate.setVisibility(View.INVISIBLE);
		textGallery.setVisibility(View.INVISIBLE);
		textUpdate.setVisibility(View.INVISIBLE);
		textMicrogoal.setVisibility(View.VISIBLE);
		textMicrogoal.setText(ClimbApplication.getContext().getString(R.string.update_group_help));
		if(duel.isReadyToPlay()){
			textShare.setText(ClimbApplication.getContext().getString(R.string.play_climb_help));
		}else{
			textShare.setText(ClimbApplication.getContext().getString(R.string.exit_game_help));
		}
		if(duel.isChallenger()){
			imageRow1.setVisibility(View.VISIBLE);
			textRow1.setVisibility(View.VISIBLE);
			imageRow1.setImageResource(R.drawable.add_group);
			textRow1.setText(ClimbApplication.getContext().getString(R.string.add_group_help));
			imageRow2.setVisibility(View.GONE);
			textRow2.setVisibility(View.GONE);
		}else if(duel.isCreator()){
			imageRow1.setVisibility(View.VISIBLE);
			textRow1.setVisibility(View.VISIBLE);
			imageRow1.setImageResource(R.drawable.add_person);
			textRow1.setText(ClimbApplication.getContext().getString(R.string.add_challenger));
			imageRow2.setVisibility(View.VISIBLE);
			textRow2.setVisibility(View.VISIBLE);
			imageRow2.setImageResource(R.drawable.add_group);
			textRow2.setText(ClimbApplication.getContext().getString(R.string.add_group_help));
		}else{
			imageRow1.setVisibility(View.GONE);
			textRow1.setVisibility(View.GONE);
			imageRow2.setVisibility(View.GONE);
			textRow2.setVisibility(View.GONE);
		}
	}
	
	
	public HelpDialogActivity(Context context, int theme, boolean fb) {
		super(context, theme);
		i = 2;
	}
	
	private void setProfileHelp(){
		imageUpdate.setVisibility(View.INVISIBLE);
		imageMicrogoal.setVisibility(View.INVISIBLE);
		textUpdate.setVisibility(View.INVISIBLE);
		textMicrogoal.setVisibility(View.INVISIBLE);
		
		textRow1.setVisibility(View.INVISIBLE);
		textRow2.setVisibility(View.INVISIBLE);
		imageRow1.setVisibility(View.INVISIBLE);
		imageRow2.setVisibility(View.INVISIBLE);
		
		textGallery.setVisibility(View.INVISIBLE);
		imageGallery.setVisibility(View.INVISIBLE);
		
		textShare.setVisibility(View.VISIBLE);
		imageShare.setVisibility(View.VISIBLE);
		textShare.setText(ClimbApplication.getContext().getString(R.string.profile_fb_help));
	}
	

	@Override
	public boolean onTouchEvent(MotionEvent event) {
			return true;
	
	}
	
	public void setMargins (View v, int r) {
	    if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
	        ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
	        p.setMargins(p.leftMargin, p.topMargin, r, p.bottomMargin);
	        v.requestLayout();
	    }
	}
	
	

}



class MyGestureDetector extends SimpleOnGestureListener {
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

		//System.out.println("Help im being touched!");
		return false;
	}
}

// public class ClimbHelpActivity extends Activity{
//
// @Override
// protected void onCreate(Bundle savedInstanceState) {
// super.onCreate(savedInstanceState);
// setContentView(R.layout.activity_climb_help);
// RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.main_layout);
// mainLayout.setOnTouchListener(new OnTouchListener() {
//
// public boolean onTouch(View v, MotionEvent event) {
//
// finish();
// return true;
// }
// });
// }
//
//
// }
