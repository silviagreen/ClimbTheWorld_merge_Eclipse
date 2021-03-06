package org.unipd.nbeghin.climbtheworld;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unipd.nbeghin.climbtheworld.adapters.StaggeredPhotoAdapter;
import org.unipd.nbeghin.climbtheworld.models.Photo;
import org.unipd.nbeghin.climbtheworld.util.FacebookUtils;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.etsy.android.grid.StaggeredGridView;

//import com.origamilabs.library.views.StaggeredGridView;
//import com.origamilabs.library.views.StaggeredGridView.OnItemClickListener;

public class GalleryActivity extends BaseImageLoaderActivity implements AbsListView.OnItemClickListener {
	public static List<Photo>	photos;
	private int building_id=0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gallery);
		if (building_id == 0) { // get the building id only if not coming back from 
			building_id = getIntent().getIntExtra("gallery_building_id", 0);
		}
		if (building_id != 0) {
			Log.i(MainActivity.AppName, "Loading gallery for building #" + building_id);
			Map<String, Object> conditions = new HashMap<String, Object>();
			conditions.put("building_id", building_id); // filter for building ID
			photos = ClimbApplication.photoDao.queryForFieldValuesArgs(conditions);
			if (photos.isEmpty() == false) { // at least one photo exists for the given building
				if(FacebookUtils.isOnline(this)){
					((TextView) findViewById(R.id.textNoConnection)).setVisibility(View.GONE);
					Log.i(MainActivity.AppName, "Photos found: " + photos.size());
					StaggeredGridView gridView = (StaggeredGridView) this.findViewById(R.id.photoGallery);
					int margin = getResources().getDimensionPixelSize(R.dimen.margin);
					//gridView.setItemMargin(margin); // set the GridView margin
					gridView.setPadding(margin, 0, margin, 0); // have the margin on the sides as well
					StaggeredPhotoAdapter adapter = new StaggeredPhotoAdapter(this, R.id.imgRocket, photos);
					gridView.setAdapter(adapter);
					gridView.setOnItemClickListener(this);
//					gridView.setOnItemClickListener(new OnItemClickListener() {
//						@Override
//						public void onItemClick(StaggeredGridView parent, View view, int position, long id) {
//							startImagePagerActivity(position);
//						};
//					});
					adapter.notifyDataSetChanged();
				}else{
					((TextView) findViewById(R.id.textNoConnection)).setVisibility(View.VISIBLE);
					((TextView) findViewById(R.id.textNoConnection)).setText(getString(R.string.gallery_no_connection));
				}
			} else {
				Log.w(MainActivity.AppName, "No photo for building id " + building_id);					
				((TextView) findViewById(R.id.textNoConnection)).setVisibility(View.VISIBLE);
				((TextView) findViewById(R.id.textNoConnection)).setText(getString(R.string.gallery_empty));
			}
		} else {
			Log.w(MainActivity.AppName, "No building id in received intent");
		}
	}

	private void startImagePagerActivity(int position) {
		Intent intent = new Intent(this, PhotoPagerActivity.class);
		intent.putExtra("imagePagerPosition", position);
		startActivity(intent);
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
		// getMenuInflater().inflate(R.menu.gallery, menu);
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

				onBackPressed();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	

	@Override
	protected void onResume() {
		Log.i(MainActivity.AppName, "GalleryActivity onResume");
		super.onResume();
		ClimbApplication.activityResumed();
	}

	@Override
	protected void onPause() {
		Log.i(MainActivity.AppName, "GalleryActivity onPause");
		super.onPause();
		ClimbApplication.activityPaused();
	}

	@Override
	protected void onDestroy() {
		Log.i(MainActivity.AppName, "GalleryActivity onDestroy");
		super.onDestroy();
	}
	
	 @Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
	        //Toast.makeText(this, "Item Clicked: " + position, Toast.LENGTH_SHORT).show();
		 startImagePagerActivity(position);
	  }
}
