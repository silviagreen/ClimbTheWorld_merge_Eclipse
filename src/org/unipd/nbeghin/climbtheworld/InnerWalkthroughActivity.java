package org.unipd.nbeghin.climbtheworld;



import org.unipd.nbeghin.climbtheworld.models.GameModeType;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CirclePageIndicator;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

public class InnerWalkthroughActivity extends Activity{
	private static final int MAX_VIEWS = 4;

    ViewPager mViewPager;
    //TextView nav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walkthrough_ex);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mViewPager.setAdapter(new WalkthroughPagerAdapter((GameModeType) getIntent().getSerializableExtra("game_mode")));
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

    	private ImageView image;
    	private TextView textDown;
    	private TextView title;
    	private GameModeType mode;
    	
    	WalkthroughPagerAdapter(GameModeType mode){
    		this.mode = mode;
    	}
    	
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
        	
            image = (ImageView) imageViewContainer.findViewById(R.id.imageDemo);
            textDown = (TextView) imageViewContainer.findViewById(R.id.textDown);
            title = (TextView) imageViewContainer.findViewById(R.id.textTitle);
            textDown.setTypeface(tf);
            title.setTypeface(tf);
            
            switch (mode) {
			case SOLO_CLIMB:
				soloClimbWalkthrough(position);
				break;
			case SOCIAL_CLIMB:
				socialClimbWalkthrough(position);
				break;
			case SOCIAL_CHALLENGE:
				socialChallengeWalkthrough(position);
				break;
			case TEAM_VS_TEAM:
				teamVsTeamWalkthrough(position);
				break;

			}
           


        	((ViewPager) container).addView(imageViewContainer, 0);     	
        	return imageViewContainer;
        }
        

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager)container).removeView((View)object);
        }
        
        private void soloClimbWalkthrough(int position){
       	 switch (position) {
   			case 0:
   				title.setText(getString(R.string.demo2_title, "Solo Climb"));
   				image.setImageResource(R.drawable.solo_play);
   				textDown.setText(getString(R.string.demo2_slide00));
   				break;
   			case 1:
   				title.setText(getString(R.string.demo2_title, "Solo Climb"));
   				image.setImageResource(R.drawable.solo_pause);	
   				textDown.setText(getString(R.string.demo2_slide01));
   				break;
   			case 2:
   				title.setText(getString(R.string.demo2_title, "Solo Climb"));
   				image.setImageResource(R.drawable.solo_star);
   				textDown.setText(getString(R.string.demo2_slide02));
   				break;
   			case 3:
   				title.setText(getString(R.string.demo2_title, "Solo Climb"));
   				image.setImageResource(R.drawable.solo_win);
   				textDown.setText(getString(R.string.demo2_slide03));
   				break;
   			
   			}
       }
       
       private void socialClimbWalkthrough(int position){
       	 switch (position) {
   			case 0:
   				title.setText(getString(R.string.demo2_title, "Social Climb"));
   				image.setImageResource(R.drawable.solo_play);
   				textDown.setText(getString(R.string.demo2_slide10));
   				break;
   			case 1:
   				title.setText(getString(R.string.demo2_title, "Social Climb"));
   				image.setImageResource(R.drawable.solo_play);	
   				textDown.setText(getString(R.string.demo2_slide11));
   				break;
   			case 2:
   				title.setText(getString(R.string.demo2_title, "Social Climb"));
   				image.setImageResource(R.drawable.solo_play);
   				textDown.setText(getString(R.string.demo2_slide12));
   				break;
   			case 3:
   				title.setText(getString(R.string.demo2_title, "Social Climb"));
   				image.setImageResource(R.drawable.solo_play);
   				textDown.setText(getString(R.string.demo2_slide13));
   				break;
   			
   			}
       }
       
       private void socialChallengeWalkthrough(int position){
       	 switch (position) {
   			case 0:
   				title.setText(getString(R.string.demo2_title, "Social Challenge"));
   				image.setImageResource(R.drawable.solo_play);
   				textDown.setText(getString(R.string.demo2_slide20));
   				break;
   			case 1:
   				title.setText(getString(R.string.demo2_title, "Social Challenge"));
   				image.setImageResource(R.drawable.solo_play);	
   				textDown.setText(getString(R.string.demo2_slide21));
   				break;
   			case 2:
   				title.setText(getString(R.string.demo2_title, "Social Challenge"));
   				image.setImageResource(R.drawable.solo_play);
   				textDown.setText(getString(R.string.demo2_slide22));
   				break;
   			case 3:
   				title.setText(getString(R.string.demo2_title, "Social Challenge"));
   				image.setImageResource(R.drawable.solo_play);
   				textDown.setText(getString(R.string.demo2_slide23));
   				break;
   			
   			}
       }
       
       private void teamVsTeamWalkthrough(int position){
       	 switch (position) {
   			case 0:
   				title.setText(getString(R.string.demo2_title, "Team vs Team"));
   				image.setImageResource(R.drawable.solo_play);
   				textDown.setText(getString(R.string.demo2_slide30));
   				break;
   			case 1:
   				title.setText(getString(R.string.demo2_title, "Team vs Team"));
   				image.setImageResource(R.drawable.solo_play);
   				textDown.setText(getString(R.string.demo2_slide31));
   				break;
   			case 2:
   				title.setText(getString(R.string.demo2_title, "Team vs Team"));
   				image.setImageResource(R.drawable.solo_play);
   				textDown.setText(getString(R.string.demo2_slide32));
   				break;
   			case 3:
   				title.setText(getString(R.string.demo2_title, "Team vs Team"));
   				image.setImageResource(R.drawable.solo_play);
   				textDown.setText(getString(R.string.demo2_slide33));
   				break;
   			
   			}
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
