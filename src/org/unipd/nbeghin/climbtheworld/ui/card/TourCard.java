package org.unipd.nbeghin.climbtheworld.ui.card;

import java.util.List;

import org.unipd.nbeghin.climbtheworld.ClimbApplication;
import org.unipd.nbeghin.climbtheworld.R;
import org.unipd.nbeghin.climbtheworld.TourDetailActivity;
import org.unipd.nbeghin.climbtheworld.fragments.ToursFragment;
import org.unipd.nbeghin.climbtheworld.models.Tour;
import org.unipd.nbeghin.climbtheworld.models.TourText;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fima.cardsui.objects.Card;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

/**
 * CardsUI card for a single tour
 *
 */
public class TourCard extends Card {
	final Tour	tour;
	TourText tourText;
	Activity parentActivity;
	HorizontalScrollView scrollview;

	public TourCard(TourText tour, Activity activity) {
		super(tour.getTour().getTitle());
		this.tourText = tour;
		this.tour = tour.getTour();
		parentActivity = activity;
	}

	@Override
	public View getCardContent(Context context) {
		View view = LayoutInflater.from(context).inflate(R.layout.card_tour_ex, null);
		((TextView) view.findViewById(R.id.title)).setText(tourText.getTitle());
		((TextView) view.findViewById(R.id.numBuildingForTour)).setText(ClimbApplication.getContext().getString(R.string.num_buildings,tour.getNum_buildings()));
		((TextView) view.findViewById(R.id.description)).setText(tourText.getDescription());
		LinearLayout layout=(LinearLayout) view.findViewById(R.id.buildingsForTourPhotoList);
		List<Integer> images=ClimbApplication.getBuildingPhotosForTour(tour.get_id());
		for(int image: images) {
			ImageView imageView=new ImageView(context);
			
			
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
			
			ImageLoader.getInstance().displayImage("drawable://"+image, imageView, options, new ImageLoadingListener() {
				
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
			
			//imageView.setImageResource(image);
			imageView.setAdjustViewBounds(true);
			imageView.setPadding(0, 0, 7, 0);
			imageView.setScaleType(ScaleType.FIT_START);
			layout.addView(imageView);
		}
		layout.refreshDrawableState();
		
		scrollview = (HorizontalScrollView) view.findViewById(R.id.horizontalScrollViewTours);
		layout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(parentActivity.getApplicationContext(), TourDetailActivity.class);
				intent.putExtra(ToursFragment.tour_intent_object, tour.get_id());
				parentActivity.startActivity(intent);
				
			}
		});
		return view;
	}


	@Override
	public boolean convert(View convertCardView) {
		// TODO Auto-generated method stub
		return false;
	}
}
