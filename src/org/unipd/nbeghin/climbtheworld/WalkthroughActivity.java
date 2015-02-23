package org.unipd.nbeghin.climbtheworld;

import org.unipd.nbeghin.climbtheworld.models.GameModeType;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CirclePageIndicator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class WalkthroughActivity extends Activity {

    private static final int MAX_VIEWS = 4;

    ViewPager mViewPager;
    //TextView nav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walkthrough_ex);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mViewPager.setAdapter(new WalkthroughPagerAdapter());
        WalkthroughPageChangeListener listener = new WalkthroughPageChangeListener();
        mViewPager.setOnPageChangeListener(listener);
        mViewPager.setOnPageChangeListener(new WalkthroughPageChangeListener());
        
        CirclePageIndicator cri= (CirclePageIndicator)findViewById(R.id.indicator);
        cri.setViewPager(mViewPager);
        cri.setOnPageChangeListener(listener);
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


    class WalkthroughPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return MAX_VIEWS;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == (View) object;
        }

        @Override
        public Object instantiateItem(View container, int position) {
        	LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        	View imageViewContainer = inflater.inflate(R.layout.walkthrough_page_ex, null);
        	Typeface tf = Typeface.createFromAsset(ClimbApplication.getContext().getAssets(),"fonts/cake.ttf");
            
        	LinearLayout single_step = (LinearLayout) imageViewContainer.findViewById(R.id.layout_steps);
        	LinearLayout how_to = (LinearLayout) imageViewContainer.findViewById(R.id.layout_how_to);

            ImageView image = (ImageView) imageViewContainer.findViewById(R.id.imageDemo);
            TextView textDown = (TextView) imageViewContainer.findViewById(R.id.textDown);
            TextView title = (TextView) imageViewContainer.findViewById(R.id.textTitle);
            
            TextView one = (TextView) imageViewContainer.findViewById(R.id.textView1);
            TextView two = (TextView) imageViewContainer.findViewById(R.id.textView2);
            TextView three = (TextView) imageViewContainer.findViewById(R.id.textView3);

            TextView text_one = (TextView) imageViewContainer.findViewById(R.id.textView1text);
            TextView text_two = (TextView) imageViewContainer.findViewById(R.id.textView2text);
            TextView text_three = (TextView) imageViewContainer.findViewById(R.id.textView3text);

            
            textDown.setTypeface(tf);
            title.setTypeface(tf);
            one.setTypeface(tf);
            two.setTypeface(tf);
            three.setTypeface(tf);
            text_one.setTypeface(tf);
            text_two.setTypeface(tf);
            text_three.setTypeface(tf);
            
            switch (position) {
			case 0:
				title.setText(getString(R.string.demo1_title) + "\n(Offline ok)");
				how_to.setVisibility(View.VISIBLE);
				single_step.setVisibility(View.GONE);
				textDown.setVisibility(View.GONE);
				
				String s = "s -> " + getString(R.string.settings) + " -> " + getString(R.string.demo1_slide01);
				SpannableString ss = new SpannableString(s); 
		        Drawable d = getResources().getDrawable(R.drawable.overflow_white); 
		        d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight()); 
		        ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE); 
		        ss.setSpan(span, 0, "s".length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE); 
				
				text_one.setText(ss);
				text_two.setText(getString(R.string.demo1_slide02));
				text_three.setText(getString(R.string.demo1_slide03));
				break;
			case 1:
				title.setText(getString(R.string.demo1_title));
				how_to.setVisibility(View.GONE);
				single_step.setVisibility(View.VISIBLE);
				textDown.setVisibility(View.VISIBLE);
				image.setImageResource(R.drawable.normal_card);
				textDown.setText(getString(R.string.demo1_slide1));
				TableRow tr = null;
				
				for(int i = 0; i < 4; i++){
					Button myButton = new Button(getApplicationContext());
					myButton.setTypeface(tf);
					switch (i) {
					case 0:
						myButton.setText("Solo Climb");
						myButton.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								Intent intent = new Intent(WalkthroughActivity.this, InnerWalkthroughActivity.class);
								intent.putExtra("game_mode", GameModeType.SOLO_CLIMB);
								startActivity(intent);
															
							}
						});
						break;	
					case 1:
						myButton.setText("Social Climb");

						myButton.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								Intent intent = new Intent(WalkthroughActivity.this, InnerWalkthroughActivity.class);
								intent.putExtra("game_mode", GameModeType.SOCIAL_CLIMB);
								startActivity(intent);
															
							}
						});
						break;
					case 2:
						myButton.setText("Social Challenge");
						myButton.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								Intent intent = new Intent(WalkthroughActivity.this, InnerWalkthroughActivity.class);
								intent.putExtra("game_mode", GameModeType.SOCIAL_CHALLENGE);
								startActivity(intent);
															
							}
						});
	
						break;
					case 3:
						myButton.setText("Team vs Team");
						myButton.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								Intent intent = new Intent(WalkthroughActivity.this, InnerWalkthroughActivity.class);
								intent.putExtra("game_mode", GameModeType.TEAM_VS_TEAM);
								startActivity(intent);
															
							}
						});
						break;

					}
					if(i < 2){
						tr = (TableRow) imageViewContainer.findViewById(R.id.tableRowBtn1);
					}else{
						tr = (TableRow) imageViewContainer.findViewById(R.id.tableRowBtn2);
					}
					tr.addView(myButton);
				}

				
				break;
			case 2:
				title.setText(getString(R.string.demo1_title));
				how_to.setVisibility(View.GONE);
				single_step.setVisibility(View.VISIBLE);
				textDown.setVisibility(View.VISIBLE);				
				image.setImageResource(R.drawable.back_to_solo_card);
				textDown.setText(getString(R.string.demo1_slide2));
				break;
			case 3:
				title.setText(getString(R.string.demo1_slide3_title));
				how_to.setVisibility(View.GONE);
				single_step.setVisibility(View.VISIBLE);
				textDown.setVisibility(View.VISIBLE);
				image.setImageResource(R.drawable.step_counter);
				
				s = getString(R.string.profile) + "p1 -> " + getString(R.string.demo1_slide3);
				int i = s.indexOf("p1");
				ss = new SpannableString(s); 
		        d = getResources().getDrawable(R.drawable.profile_white); 
		        d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight()); 
		        span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE); 
		        ss.setSpan(span, i, i+"p1".length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE); 
				textDown.setText(ss);
				break;
			
			}




        	((ViewPager) container).addView(imageViewContainer, 0);     	
        	return imageViewContainer;
        }
        

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager)container).removeView((View)object);
        }
    }


    class WalkthroughPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageSelected(int position) {
            // Here is where you should show change the view of page indicator
            switch(position) {

            case MAX_VIEWS - 1:
                break;

            default:

            }
            
          // nav.setText("Page " + (position + 1) + " of " + MAX_VIEWS);


        }

    }
}
