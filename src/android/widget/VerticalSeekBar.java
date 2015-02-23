package android.widget;

import java.util.ArrayList;
import java.util.List;

import org.unipd.nbeghin.climbtheworld.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout.LayoutParams;

/**
 * Custom android widget: vertical seekbar customized by Silvia Segato
 * 
 * @url https://github.com/AndroSelva/Vertical-SeekBar-Android/
 * 
 */
public class VerticalSeekBar extends SeekBar {
	
	 private Bitmap thumb1 = BitmapFactory.decodeResource(getResources(), R.drawable.star);
	 private Bitmap thumb2 = BitmapFactory.decodeResource(getResources(), R.drawable.star);
	 private Bitmap thumb3 = BitmapFactory.decodeResource(getResources(), R.drawable.star);
	 private Bitmap thumb4 = BitmapFactory.decodeResource(getResources(), R.drawable.star);

	 private static double starHeight = 0;
	 private static double perc_unit = 0;
	 private static int totalHeight = 0;
	 private int height = 0;
	 private int width = 0;
	 private List<View> lines = new ArrayList<View>();
	 private List<Bitmap> thumbs = new ArrayList<Bitmap>();
	 
	 private void setLists(){
			RelativeLayout parent = (RelativeLayout) this.getParent();
			lines.add(parent.findViewById(R.id.redLine1));
			lines.add(parent.findViewById(R.id.redLine2));
			lines.add(parent.findViewById(R.id.redLine3));
			lines.add(parent.findViewById(R.id.redLine4));
			thumbs.add(thumb1);
			thumbs.add(thumb2);
			thumbs.add(thumb3);
			thumbs.add(thumb4);
	 }
	 
	public VerticalSeekBar(Context context) {
		super(context);
		requestLayout();
		setWillNotDraw(false);
	}

	public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		requestLayout();
		setWillNotDraw(false);
	}

	public VerticalSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		requestLayout();
		setWillNotDraw(false);
	}

	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(h, w, oldh, oldw);
	}

	@Override
	protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) { 
		super.onMeasure(heightMeasureSpec, widthMeasureSpec);
		setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
			height = MeasureSpec.getSize(heightMeasureSpec);
			width = MeasureSpec.getSize(widthMeasureSpec);	
	}
	
	
	
	protected void onDraw(Canvas c) {
		c.rotate(-90);
		c.translate(-getHeight(), 0);
		if(lines.isEmpty() && thumbs.isEmpty()) setLists();

		for(int i = 0; i < lines.size(); i++){
			View v = lines.get(i);
			v.setVisibility(View.GONE);
			v.setLayoutParams(new LayoutParams(width/2, 2));
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)v.getLayoutParams();
			
			starHeight = ((double) height) /  ((double) 100) * (perc_unit * (i + 1)) - 70 + 5;
			if(i == lines.size() - 1){
				float toolbar_dimens = getResources().getDimension(R.dimen.abc_action_bar_default_height_material);
				starHeight = height- toolbar_dimens + 50;//100 + 5;
			}
			
			c.drawBitmap(thumbs.get(i), (float)starHeight , 40,null); System.out.println(starHeight);
//			params.setMargins(0,  totalHeight - ((int) starHeight) - 30, 0, 0); //substitute parameters for left, top, right, bottom
//			v.setLayoutParams(params);
//			v.setBackgroundColor(getResources().getColor(R.color.red));
		}
//		line1 = parent.findViewById(R.id.redLine1);		
//		line1.setLayoutParams(new LayoutParams(width/2, 2));
//		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)line1.getLayoutParams();
//		params.setMargins(0,  totalHeight - ((int) starHeight) - 30, 0, 0); //substitute parameters for left, top, right, bottom
//		line1.setLayoutParams(params);
//		line1.setBackgroundColor(getResources().getColor(R.color.red));
		
//		c.drawBitmap(thumb1, (float)starHeight , 40,null);
        
        
		super.onDraw(c);
	}
	
	public void nextStar(int progress){
		thumb1 = BitmapFactory.decodeResource(getResources(), R.drawable.star);
		//System.out.println("height " + height  + " progress " + progress);
		starHeight = (((double) progress * ((double) height)) /(double) 100) + 5;
		if(starHeight >= height){
			float toolbar_dimens = getResources().getDimension(R.dimen.abc_action_bar_default_height_material);
			starHeight = height - toolbar_dimens;//100 + 5;
		}
		//System.out.println(starHeight);
		invalidate();
		
	}
	
	public void setTotalHeight(){
		totalHeight = height;
	}
	
	public void setGoldStar(int position){
		Bitmap goldenStar = BitmapFactory.decodeResource(getResources(), R.drawable.gold_star);
		thumbs.set(position - 1, goldenStar);
		invalidate();
	}
	
	public void setInitialGoldenStars(int finalPosition, double unit){
		perc_unit = unit;
		Bitmap goldenStar = BitmapFactory.decodeResource(getResources(), R.drawable.gold_star);
		for(int i = 0; i < finalPosition; i++)
			thumbs.set(i, goldenStar);
		invalidate();
	}

	/*
	 * nbeghin: added onSizeChanged to solve thumb image not updated
	 * @see android.widget.ProgressBar#setProgress(int)
	 */
	@Override
	public synchronized void setProgress(int progress) {
		super.setProgress(progress);
		onSizeChanged(getWidth(), getHeight(), 0, 0);
	}
	

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return false;
//		if (!isEnabled()) {
//			return false;
//		}
//		switch (event.getAction()) {
//			case MotionEvent.ACTION_DOWN:
//			case MotionEvent.ACTION_MOVE:
//			case MotionEvent.ACTION_UP:
//				int i = 0;
//				i = getMax() - (int) (getMax() * event.getY() / getHeight());
//				setProgress(i);
//				onSizeChanged(getWidth(), getHeight(), 0, 0);
//				break;
//			case MotionEvent.ACTION_CANCEL:
//				break;
//		}
//		return true;
	}
	
	
	
}