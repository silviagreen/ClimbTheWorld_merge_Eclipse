package org.unipd.nbeghin.climbtheworld.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View.MeasureSpec;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;

/**
 * 
 * This view will auto determine the width or height by determining if the 
 * height or width is set and scale the other dimension depending on the images dimension
 * 
 * This view also contains an ImageChangeListener which calls changed(boolean isEmpty) once a 
 * change has been made to the ImageView
 * 
 * @author Maurycy Wojtowicz
 *
 */
public class ScaleImageView extends ImageView {
	private ImageChangeListener imageChangeListener;
	private boolean scaleToWidth = false; // this flag determines if should measure height manually dependent of width

	public ScaleImageView(Context context) {
		super(context);
		init();
	}

	public ScaleImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public ScaleImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init(){
		this.setScaleType(ScaleType.CENTER_INSIDE);
	}

	@Override
	public void setImageBitmap(Bitmap bm) {
		super.setImageBitmap(bm);
		if (imageChangeListener != null)
			imageChangeListener.changed((bm == null));
	}

	@Override
	public void setImageDrawable(Drawable d) {
		super.setImageDrawable(d);
		if (imageChangeListener != null)
			imageChangeListener.changed((d == null));
	}

	@Override
	public void setImageResource(int id){
		super.setImageResource(id);
	}

	public interface ImageChangeListener {
		// a callback for when a change has been made to this imageView
		void changed(boolean isEmpty); 
	}

	public ImageChangeListener getImageChangeListener() {
		return imageChangeListener;
	}

	public void setImageChangeListener(ImageChangeListener imageChangeListener) {
		this.imageChangeListener = imageChangeListener;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
		
		/**
		 * if both width and height are set scale width first. modify in future if necessary
		 */
		
		if(widthMode == MeasureSpec.EXACTLY || widthMode == MeasureSpec.AT_MOST){
			scaleToWidth = true;
		}else if(heightMode == MeasureSpec.EXACTLY || heightMode == MeasureSpec.AT_MOST){
			scaleToWidth = false;
		}else throw new IllegalStateException("width or height needs to be set to match_parent or a specific dimension");
		
		if(getDrawable()==null || getDrawable().getIntrinsicWidth()==0 ){
			// nothing to measure
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			return;
		}else{
			if(scaleToWidth){
				int iw = this.getDrawable().getIntrinsicWidth();
				int ih = this.getDrawable().getIntrinsicHeight();
				int heightC = width*ih/iw;
				if(height > 0)
				if(heightC>height){
					// dont let hegiht be greater then set max
					heightC = height;
					width = heightC*iw/ih;
				}
				
				this.setScaleType(ScaleType.CENTER_CROP);
				setMeasuredDimension(width, heightC);
				
			}else{
				// need to scale to height instead
				int marg = 0;
				if(getParent()!=null){
					if(getParent().getParent()!=null){
						marg+= ((RelativeLayout) getParent().getParent()).getPaddingTop();
						marg+= ((RelativeLayout) getParent().getParent()).getPaddingBottom();
					}
				}
				
				int iw = this.getDrawable().getIntrinsicWidth();
				int ih = this.getDrawable().getIntrinsicHeight();

				width = height*iw/ih;
				height-=marg;
				setMeasuredDimension(width, height);
			}

		}
	}
	
	public static void scaleImage(ImageView view, int boundBoxInDp, boolean rounded)
	{
	    // Get the ImageView and its bitmap
	    Drawable drawing = view.getDrawable();
	    Bitmap bitmap = ((BitmapDrawable)drawing).getBitmap();

	    // Get current dimensions
	    int width = bitmap.getWidth();
	    int height = bitmap.getHeight();

	    // Determine how much to scale: the dimension requiring less scaling is
	    // closer to the its side. This way the image always stays inside your
	    // bounding box AND either x/y axis touches it.
	    float xScale = ((float) boundBoxInDp) / width;
	    float yScale = ((float) boundBoxInDp) / height;
	    float scale = (xScale <= yScale) ? xScale : yScale;

	    // Create a matrix for the scaling and add the scaling data
	    Matrix matrix = new Matrix();
	    matrix.postScale(scale, scale);

	    // Create a new bitmap and convert it to a format understood by the ImageView
	    Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
	    BitmapDrawable result;
	    if(rounded)
	    	result= new BitmapDrawable(GraphicsUtils.getRoundedShape(scaledBitmap));
	    else 
	    	result= new BitmapDrawable(scaledBitmap);
	    width = scaledBitmap.getWidth();
	    height = scaledBitmap.getHeight();
	    
	    

	    // Apply the scaled bitmap
	    view.setImageDrawable(result);

	    // Now change ImageView's dimensions to match the scaled image
	    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
	    params.width = width;
	    params.height = height;
	    view.setLayoutParams(params);
	}

}
