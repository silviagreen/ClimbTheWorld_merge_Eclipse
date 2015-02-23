package org.unipd.nbeghin.climbtheworld;

import android.app.Activity;
import android.app.ActionBar.LayoutParams;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class OnBoardingActivity extends Activity{

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_begin);
		TextView text = (TextView) findViewById(R.id.textOnborading);
		TextView title = (TextView) findViewById(R.id.textTitleOnborading);
		Button okBtn = (Button) findViewById(R.id.buttonOk);
		Button skipBtn = (Button) findViewById(R.id.buttonSkip);
		skipBtn.setText(getString(R.string.onboarding_skip_btn));
		//Typeface tf = Typeface.createFromAsset(ClimbApplication.getContext().getAssets(),"fonts/cake.ttf");  //sketch-me
		//text.setTypeface(tf);
		//okBtn.setTypeface(tf);
	
		
		okBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		skipBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SharedPreferences pref = getSharedPreferences("UserSession", 0);
				pref.edit().putBoolean("first_open_2", false).commit();
				pref.edit().putBoolean("first_open_1", false).commit();
				pref.edit().putBoolean("first_open_3", false).commit();
				pref.edit().putBoolean("first_open_4", false).commit();
				pref.edit().putBoolean("first_open_5", false).commit();
				finish();
			}
		});
		
		Intent i = getIntent();
		String source = i.getExtras().getString("source");
		if(source.equalsIgnoreCase("MainActivity")){
			title.setText(getString(R.string.onboarding_title_1));
			text.setText(getString(R.string.onboarding_1));
			okBtn.setText(getString(R.string.onboarding_btn_1));
			skipBtn.setVisibility(View.VISIBLE);
		}else if(source.equalsIgnoreCase("ClimbActivity")){
			title.setText(getString(R.string.onboarding_title_2));
			text.setText(getString(R.string.onboarding_2));
			okBtn.setText(getString(R.string.onboarding_btn_2));
			skipBtn.setVisibility(View.GONE);
		}else if(source.equalsIgnoreCase("ClimbActivityVictory")){
			title.setText(getString(R.string.onboarding_title_3));
			text.setText(getString(R.string.onboarding_3));
			okBtn.setText(getString(R.string.onboarding_btn_3));
			skipBtn.setVisibility(View.GONE);
		}

	}
}
