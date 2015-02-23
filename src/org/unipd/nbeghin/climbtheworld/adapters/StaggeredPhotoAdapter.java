package org.unipd.nbeghin.climbtheworld.adapters;

import java.util.List;

import org.unipd.nbeghin.climbtheworld.MainActivity;
import org.unipd.nbeghin.climbtheworld.R;
import org.unipd.nbeghin.climbtheworld.models.Photo;
import org.unipd.nbeghin.climbtheworld.util.ScaleImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;

public class StaggeredPhotoAdapter extends ArrayAdapter<Photo> {
	private final List<Photo> photos;

	public StaggeredPhotoAdapter(Context context, int textViewResourceId, List<Photo> objects) {
		super(context, textViewResourceId, objects);
		this.photos=objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		try {
			if (photos.get(position)==null) throw new NullPointerException();
			if (convertView == null) {
				LayoutInflater layoutInflator = LayoutInflater.from(getContext());
				convertView = layoutInflator.inflate(R.layout.row_staggered_demo, null);
				holder = new ViewHolder(); // view-holder pattern
				holder.imageView = (ScaleImageView) convertView.findViewById(R.id.imgRocket);
				holder.url=photos.get(position).getUrl();
				convertView.setTag(holder);
			}
			holder = (ViewHolder) convertView.getTag();
			
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
			 
			final ProgressBar pb = (ProgressBar) convertView.findViewById(R.id.progressBarItem);
			
			ImageLoader.getInstance().displayImage(holder.url, holder.imageView, options, new ImageLoadingListener() {
				
				@Override
				public void onLoadingStarted(String arg0, View arg1) {
					pb.setVisibility(View.VISIBLE);
					pb.setIndeterminate(true);
				}
				
				@Override
				public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
					pb.setVisibility(View.GONE);
				}
				
				@Override
				public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
					pb.setVisibility(View.GONE);					
				}
				
				@Override
				public void onLoadingCancelled(String arg0, View arg1) {
					pb.setVisibility(View.GONE);					
				}
			});
		} catch(NullPointerException e) {
			Log.w(MainActivity.AppName, "No photo at position @"+position);
		} catch(Exception e) {
			Log.e(MainActivity.AppName, "GalleryActivity: unable to show image");
		}
		return convertView;
	}

	static class ViewHolder {
		ScaleImageView	imageView;
		String url;
	}
}
