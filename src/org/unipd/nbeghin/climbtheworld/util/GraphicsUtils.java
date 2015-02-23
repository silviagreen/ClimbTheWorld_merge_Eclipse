package org.unipd.nbeghin.climbtheworld.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
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

}