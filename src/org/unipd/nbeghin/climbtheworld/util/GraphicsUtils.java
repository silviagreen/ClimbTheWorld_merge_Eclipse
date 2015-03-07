package org.unipd.nbeghin.climbtheworld.util;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.support.v7.appcompat.R;


/**
 * 
 * @author SAM BHADANI, Segato Silvia
 *
 */
public class GraphicsUtils
{ 
 public static Bitmap getCircleBitmap(Bitmap bitmap, int pixels) {
  Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
    bitmap.getHeight(), Bitmap.Config.ARGB_8888);
  Canvas canvas = new Canvas(output);

  final int color = 0xffff0000;
  final Paint paint = new Paint();
  final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
  final RectF rectF = new RectF(rect);

  paint.setAntiAlias(true);
  paint.setDither(true);
  paint.setFilterBitmap(true);
  canvas.drawARGB(0, 0, 0, 0);
  paint.setColor(color);
  canvas.drawOval(rectF, paint);

  paint.setColor(Color.BLUE);
  paint.setStyle(Paint.Style.STROKE);
  paint.setStrokeWidth((float) 4);
  paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
  canvas.drawBitmap(bitmap, rect, rect, paint);

  return output;
 }

 public static Bitmap getRoundedShape(Bitmap scaleBitmapImage) {
  int targetWidth = 125;
  int targetHeight = 125;
  Bitmap targetBitmap = Bitmap.createBitmap(targetWidth, 
    targetHeight,Bitmap.Config.ARGB_8888);

  Canvas canvas = new Canvas(targetBitmap);
  Path path = new Path();
  path.addCircle(((float) targetWidth - 1) / 2,
    ((float) targetHeight - 1) / 2,
    (Math.min(((float) targetWidth), 
      ((float) targetHeight)) / 2),
      Path.Direction.CCW);

  canvas.clipPath(path);
  Bitmap sourceBitmap = scaleBitmapImage;
  canvas.drawBitmap(sourceBitmap, 
    new Rect(0, 0, sourceBitmap.getWidth(),
      sourceBitmap.getHeight()), 
      new Rect(0, 0, targetWidth,
        targetHeight), null);
  return targetBitmap;
 }
 
 /**
  * Created by alessandro on 04/09/14.
  */
 public static int getActionBarSize(final Context context) {

		final int[] attrs;

		if (Build.VERSION.SDK_INT >= 14) {
			attrs = new int[]{android.R.attr.actionBarSize};
		}
		else {
			attrs = new int[]{R.attr.actionBarSize};
		}

		TypedArray values = context.getTheme().obtainStyledAttributes(attrs);
		try {
			return values.getDimensionPixelSize(0, 0);
		} finally {
			values.recycle();
		}
	}

	public static int getStatusBarHeight(Context context) {
		int result = 0;
		int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = context.getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}
	
	public static int calculateInSampleSize(BitmapFactory.Options options,
	        int reqWidth, int reqHeight) {
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;

	    if (height > reqHeight || width > reqWidth) {

	        // Calculate ratios of height and width to requested height and
	        // width
	        final int heightRatio = Math.round((float) height
	                / (float) reqHeight);
	        final int widthRatio = Math.round((float) width / (float) reqWidth);

	        // Choose the smallest ratio as inSampleSize value, this will
	        // guarantee
	        // a final image with both dimensions larger than or equal to the
	        // requested height and width.
	        inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
	    }

	    return inSampleSize;
	}

	public static Bitmap decodeSampledBitmapFromResource(Resources res,
	        int resId, int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeResource(res, resId, options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth,
	            reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeResource(res, resId, options);
	}

}