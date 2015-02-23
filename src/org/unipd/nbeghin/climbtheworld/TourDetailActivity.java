package org.unipd.nbeghin.climbtheworld;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unipd.nbeghin.climbtheworld.comparator.BuildingTourComparator;
import org.unipd.nbeghin.climbtheworld.fragments.BuildingsForTourFragment;
import org.unipd.nbeghin.climbtheworld.fragments.ToursFragment;
import org.unipd.nbeghin.climbtheworld.models.Building;
import org.unipd.nbeghin.climbtheworld.models.BuildingText;
import org.unipd.nbeghin.climbtheworld.models.BuildingTour;
import org.unipd.nbeghin.climbtheworld.models.Tour;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

/**
 * List all buildings associated to a given tour
 *
 */
public class TourDetailActivity extends FragmentActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int tour_id=getIntent().getIntExtra(ToursFragment.tour_intent_object, 0); // get tour id from received intent
		setContentView(R.layout.activity_tour_detail);
		
		LinearLayout root = (LinearLayout)findViewById(R.id.mainLayoutTour);
		Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
        root.addView(bar, 0); // insert at top
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

		if (tour_id>0) {
			Log.i(MainActivity.AppName, "Loading buildings for tour "+tour_id);
			List<BuildingTour> buildingTours=ClimbApplication.getBuildingsForTour(tour_id); // get all buildings associated to a tour
			Log.i(MainActivity.AppName, "Detected "+buildingTours.size()+" building for tour #"+tour_id);
			Collections.sort(buildingTours, new BuildingTourComparator()); // sort by building order
			BuildingsForTourFragment fragment=(BuildingsForTourFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentBuildingsForTour); // load fragment
			List<BuildingText> buildings=new ArrayList<BuildingText>();
			for(BuildingTour buildingTour: buildingTours) {
				Building current_building = buildingTour.getBuilding();
				buildings.add(ClimbApplication.getBuildingTextByBuilding(current_building.get_id()));
			}
			fragment.loadBuildings(buildings); // set buildings for fragment
		} else {
			Log.e(MainActivity.AppName, "No tour ID detected");
		}
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
		// getMenuInflater().inflate(R.menu.tour_detail, menu);
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
