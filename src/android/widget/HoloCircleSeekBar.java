package android.widget;

import org.unipd.nbeghin.climbtheworld.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Class representing a circular progress bar. 
 * 
 */
public class HoloCircleSeekBar extends View implements Parcelable{

	//vari campi utili a costruire la barra di progresso
	private Paint mColorWheelPaint;
	private Paint mPointerHaloPaint;
	private Paint mPointerColor;
	private int mColorWheelStrokeWidth;
	private int mPointerRadius;
	private RectF mColorWheelRectangle = new RectF();
	private boolean mUserIsMovingPointer = false;
	private int mColor;
	private float mTranslationOffset;
	private float mColorWheelRadius;

	private float mAngle;
	private Paint textPaint;
	private String text=new String();
	private int conversion = 0;
	private int max = 100;
	private String color_attr;
	private int color;
	private SweepGradient s;
	private Paint mArcColor;
	private String wheel_color_attr, wheel_unactive_color_attr,
			pointer_color_attr, pointer_halo_color_attr, text_color_attr;
	private int wheel_color, unactive_wheel_color, pointer_color,
			pointer_halo_color, text_size, text_color, init_position;
	private boolean block_end = false;
	private float lastX;
	private int last_radians = 0;
	private boolean block_start = false;

	private int arc_finish_radians = 360;
	private int start_arc = 270;

	private float[] pointerPosition;
	private Paint mColorCenterHalo;
	private RectF mColorCenterHaloRectangle = new RectF();
	private Paint mCircleTextColor;
	private int end_wheel;

	private boolean show_text=true;
	
	private int beginDone;
	private int sweepDone;
	
	private Context context;
	private AttributeSet attset;
	private int defStyle;
	boolean play = false;

	private boolean show_percentage_symbol;
	
	
	/**
	 * Costruttore della progress bar.
	 * @param context contesto dell'activity che invoca il costruttore
	 */
	public HoloCircleSeekBar(Context context) {
		super(context);
		this.context = context;
		attset = null;
		this.defStyle = 0;		
		init(null, 0);		
	}

	
	/**
	 * Costruttore della progress bar che viene definita con un certo insieme di attributi.
	 * @param context contesto dell'activity che invoca il costruttore
	 * @param attrs set di attributi
	 */
	public HoloCircleSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.defStyle = 0;
		this.context = context;
		attset = attrs;
		init(attrs, 0);
	}

	
	/**
	 * Costruttore della progress bar che viene definita con un certo insieme di attributi e 
	 * un determinato stile.
	 * @param context contesto dell'activity che invoca il costruttore
	 * @param attrs set di attributi
	 * @param defStyle intero che fa riferimento ad uno stile
	 */
	public HoloCircleSeekBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.defStyle = defStyle;
		this.context = context;
		attset = attrs;
		init(attrs, defStyle);
	}

	
	/**
	 * Metodo che permette di inizializzare gli attributi per colorare la ruota e relativi stili
	 * della progress bar.
	 * @param attrs insieme degli attributi
	 * @param defStyle intero che fa riferimento ad uno stile
	 */
	private void init(AttributeSet attrs, int defStyle) { 
		final TypedArray a = getContext().obtainStyledAttributes(attrs,
				R.styleable.HoloCircleSeekBar, defStyle, 0);
		
		initAttributes(a);

		a.recycle();

		mColorWheelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mColorWheelPaint.setShader(s);
		mColorWheelPaint.setColor(unactive_wheel_color);
		mColorWheelPaint.setStyle(Paint.Style.STROKE);
		mColorWheelPaint.setStrokeWidth(5);

		mColorCenterHalo = new Paint(Paint.ANTI_ALIAS_FLAG);
		mColorCenterHalo.setColor(Color.WHITE);
		mColorCenterHalo.setAlpha(0xCC);

		mPointerHaloPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPointerHaloPaint.setColor(pointer_halo_color);
		mPointerHaloPaint.setStrokeWidth(mPointerRadius + 10);

		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
		textPaint.setColor(text_color); //Color.rgb(51,181,229) Color.BLACK
		textPaint.setStyle(Style.FILL_AND_STROKE);
		textPaint.setTextAlign(Align.LEFT);
		textPaint.setTextSize(text_size);
		//textPaint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
		//textPaint.setTypeface(Typeface.SERIF);

		mPointerColor = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPointerColor.setStrokeWidth(mPointerRadius);

		mPointerColor.setColor(pointer_color);

		mArcColor = new Paint(Paint.ANTI_ALIAS_FLAG);
		mArcColor.setColor(wheel_color);
		mArcColor.setStyle(Paint.Style.STROKE);
		mArcColor.setStrokeWidth(5);

		mCircleTextColor = new Paint(Paint.ANTI_ALIAS_FLAG);
		mCircleTextColor.setColor(Color.WHITE);
		mCircleTextColor.setStyle(Paint.Style.FILL);

		invalidate();
	}

	
	/**
	 * Metodo che permette di inizializzare gli attributi necessari per colorare la ruota.
	 * @param a array contenente i valori da assegnare agli attributi
	 */
	private void initAttributes(TypedArray a) {
		mColorWheelStrokeWidth = a.getInteger(
				R.styleable.HoloCircleSeekBar_wheel_size, 16);
		mPointerRadius = a.getInteger(
				R.styleable.HoloCircleSeekBar_pointer_size, 48);
		max = a.getInteger(R.styleable.HoloCircleSeekBar_max, 100);

		color_attr = a.getString(R.styleable.HoloCircleSeekBar_color_seekbar);
		wheel_color_attr = a
				.getString(R.styleable.HoloCircleSeekBar_wheel_active_color);
		wheel_unactive_color_attr = a
				.getString(R.styleable.HoloCircleSeekBar_wheel_unactive_color);
		pointer_color_attr = a
				.getString(R.styleable.HoloCircleSeekBar_pointer_color);
		pointer_halo_color_attr = a
				.getString(R.styleable.HoloCircleSeekBar_pointer_halo_color);

		text_color_attr = a.getString(R.styleable.HoloCircleSeekBar_text_color);

		text_size = a.getInteger(R.styleable.HoloCircleSeekBar_text_size, 50); //90 100

		init_position = a.getInteger(
				R.styleable.HoloCircleSeekBar_init_position, 0);

		start_arc = a.getInteger(R.styleable.HoloCircleSeekBar_start_angle, 0);
		end_wheel = a.getInteger(R.styleable.HoloCircleSeekBar_end_angle, 360);

		show_text = a.getBoolean(R.styleable.HoloCircleSeekBar_show_text, true);

		show_percentage_symbol=a.getBoolean(R.styleable.HoloCircleSeekBar_show_percent_symbol, false);
		
		
		last_radians = end_wheel;

		if (init_position < start_arc)
			init_position = calculateTextFromStartAngle(start_arc);

		if (color_attr != null) {
			try {
				color = Color.parseColor(color_attr);
			} catch (IllegalArgumentException e) {
				color = Color.WHITE;
			}
			color = Color.parseColor(color_attr);
		} else {
			color = Color.WHITE;
		}

		if (wheel_color_attr != null) {
			try {
				wheel_color = Color.parseColor(wheel_color_attr);
			} catch (IllegalArgumentException e) {
				wheel_color = Color.DKGRAY;
			}

		} else {
			wheel_color = Color.DKGRAY;
		}
		if (wheel_unactive_color_attr != null) {
			try {
				unactive_wheel_color = Color
						.parseColor(wheel_unactive_color_attr);
			} catch (IllegalArgumentException e) {
				unactive_wheel_color = Color.WHITE;//Color.rgb(51,181,229);
			}

		} else {
			unactive_wheel_color =  Color.WHITE;//Color.rgb(51,181,229);
		}

		if (pointer_color_attr != null) {
			try {
				pointer_color = Color.parseColor(pointer_color_attr);
			} catch (IllegalArgumentException e) {
				pointer_color =  Color.rgb(51,181,229);
			}

		} else {
			pointer_color =  Color.rgb(51,181,229);
		}

		if (pointer_halo_color_attr != null) {
			try {
				pointer_halo_color = Color.parseColor(pointer_halo_color_attr);
			} catch (IllegalArgumentException e) {
				pointer_halo_color = Color.WHITE;
			}

		} else {
			pointer_halo_color = Color.DKGRAY;
		}

		if (text_color_attr != null) {
			try {
				text_color = Color.parseColor(text_color_attr);
			} catch (IllegalArgumentException e) {
				text_color =  Color.rgb(51,181,229);
			}
		} else {
			text_color =  Color.rgb(51,181,229);
		}
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) { 
		setWillNotDraw(false);

		canvas.translate(mTranslationOffset, mTranslationOffset);

		//colora la ruota
		canvas.drawArc(mColorWheelRectangle, beginDone, sweepDone, false, mColorWheelPaint);
		canvas.drawArc(mColorWheelRectangle, beginDone + sweepDone,360 - sweepDone, false, mArcColor);

		//puntino sulla circonferenza
		if(beginDone+sweepDone <360){
			pointerPosition = calculatePointerPosition(calculateRadiansFromAngle(beginDone+sweepDone));
		}else{
			pointerPosition = calculatePointerPosition(calculateRadiansFromAngle(0+(sweepDone - (360 - beginDone))));
		}
		
		Rect bounds = new Rect();
		textPaint.getTextBounds(text, 0, text.length(), bounds);
		
		
		if (show_text){
			canvas.drawText(
					text,
					(mColorWheelRectangle.centerX())
					- (textPaint.measureText(text) / 2),
					mColorWheelRectangle.centerY() + bounds.height() / 2,
					textPaint);
		}
	}

	
	
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
		int height = getDefaultSize(getSuggestedMinimumHeight(),
				heightMeasureSpec);
		int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
		int min = Math.min(width, height);
		setMeasuredDimension(min, min);

		mTranslationOffset = min * 0.5f;
		mColorWheelRadius = mTranslationOffset - 2.1f;

		mColorWheelRectangle.set(-mColorWheelRadius, -mColorWheelRadius,
				mColorWheelRadius, mColorWheelRadius);

		mColorCenterHaloRectangle.set(-mColorWheelRadius / 2,
				-mColorWheelRadius / 2, mColorWheelRadius / 2,
				mColorWheelRadius / 2);

		pointerPosition = calculatePointerPosition(mAngle);
	}

	
	/**
	 * Calcola la posizione dato un angolo.
	 * @param angle l'angolo dato
	 * @return la posizione finale
	 */
	private int calculateTextFromAngle(float angle) {
		float m = angle - start_arc;

		float f = (float) ((end_wheel - start_arc) / m);

		return (int) (max / f);
	}

	
	/**
	 * Calcola la posizione dato l'angolo di partenza.
	 * @param angle l'angolo di partenza
	 * @return la posizione finale
	 */
	@SuppressLint("DrawAllocation")
	private int calculateTextFromStartAngle(float angle) {
		float m = angle;

		float f = (float) ((end_wheel - start_arc) / m);

		return (int) (max / f);
	}
	

	/**
	 * Converte un angolo da sessaggesimale a radiante.
	 * @param angle l'angolo in sessaggesimali da convertire
	 * @return l'angolo convertito in radianti
	 */
	private int calculateRadiansFromAngle(float angle) {
		float unit = (float) (angle / (2 * Math.PI));
		if (unit < 0) {
			unit += 1;
		}
		int radians = (int) ((unit * 360) - ((360 / 4) * 3));
		if (radians < 0)
			radians += 360;
		return radians;
	}

	
	/**
	 * Converte un angolo da radiante a sessaggesimale.
	 * @param radians l'angolo in radianti da convertire
	 * @return l'angolo convertito in sessagesimali
	 */
	private float calculateAngleFromRadians(int radians) {
		return (float) (((radians + 270) * (2 * Math.PI)) / 360);
	}

	
	/**
	 * Setta la progress bar circolare in base al dato valore di avanzamento.
	 * @param value il valore a cui è arrivato countdown
	 * @param max il valore da cui il countdown è partito
	 */
	public void setValue(int value, int max){
		mAngle = (360/max)*(max-value);

		if(mAngle == 360/max) block_start = true;
		else block_start = false;
		if(mAngle == (360/max)*(max-1)) block_end = true;
		else block_end = false;
		
		int radians = calculateRadiansFromAngle(mAngle);
		
		text = String.valueOf(value);
		
		if(show_percentage_symbol){
			text=text+"%";
		}		
		
		beginDone = 270;
		sweepDone = (int) mAngle;
		
		invalidate();

		last_radians = radians;
	}

	
	/*@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX() - mTranslationOffset;
		float y = event.getY() - mTranslationOffset;

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mAngle = (float) java.lang.Math.atan2(y, x);

			block_end = false;
			block_start = false;
			mUserIsMovingPointer = true;

			arc_finish_radians = calculateRadiansFromAngle(mAngle);

			if (arc_finish_radians > end_wheel) {
				arc_finish_radians = end_wheel;
				block_end = true;
			}

			if (!block_end && !block_start) {
				text = String
						.valueOf(calculateTextFromAngle(arc_finish_radians));
				pointerPosition = calculatePointerPosition(mAngle);
				invalidate();
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (mUserIsMovingPointer) {
				mAngle = (float) java.lang.Math.atan2(y, x);

				int radians = calculateRadiansFromAngle(mAngle);

				if (last_radians > radians && radians < (360 / 6) && x > lastX
						&& last_radians > (360 / 6)) {

					if (!block_end && !block_start)
						block_end = true;
				} else if (last_radians >= start_arc
						&& last_radians <= (360 / 4) && radians <= (360 - 1)
						&& radians >= ((360 / 4) * 3) && x < lastX) {
					if (!block_start && !block_end)
						block_start = true;

				} else if (radians >= end_wheel && !block_start
						&& last_radians < radians) {
					block_end = true;
				} else if (radians < end_wheel && block_end
						&& last_radians > end_wheel) {
					block_end = false;
				} else if (radians < start_arc && last_radians > radians
						&& !block_end) {
					block_start = true;
				} else if (block_start && last_radians < radians
						&& radians > start_arc && radians < end_wheel) {
					block_start = false;
				}

				if (block_end) {

					arc_finish_radians = end_wheel - 1;
					text = String.valueOf(max);
					mAngle = calculateAngleFromRadians(arc_finish_radians);
					pointerPosition = calculatePointerPosition(mAngle);
				} else if (block_start) {

					arc_finish_radians = start_arc;
					mAngle = calculateAngleFromRadians(arc_finish_radians);
					text = String.valueOf(0);
					pointerPosition = calculatePointerPosition(mAngle);
				} else {
					arc_finish_radians = calculateRadiansFromAngle(mAngle);
					text = String
							.valueOf(calculateTextFromAngle(arc_finish_radians));
					pointerPosition = calculatePointerPosition(mAngle);
				}
				invalidate();
				last_radians = radians;

			}
			break;
		case MotionEvent.ACTION_UP:
			mUserIsMovingPointer = false;
			break;
		}
		//fix scrolling
		if (event.getAction() == MotionEvent.ACTION_MOVE && getParent() != null) {
			getParent().requestDisallowInterceptTouchEvent(true);
		}
		lastX = x;

		return true;
	}*/

	
	/**
	 * Calcola le coordinate del punto nella ruota usando l'angolo fornito.
	 * @param angle
	 *            la posizione del punto espressa come angolo (in rad)
	 * 
	 * @return le coordinate del centro del punto espresse nel sistema interno di coordinate
	 */
	private float[] calculatePointerPosition(float angle) {
		float x = (float) (mColorWheelRadius * Math.cos(angle));
		float y = (float) (mColorWheelRadius * Math.sin(angle));
		return new float[] { x, y };
	}

	
	@Override
	public int describeContents() {
		return 0;
	}

	
	@Override
	public void writeToParcel(Parcel dest, int flags) {	

		dest.writeValue(context);
		dest.writeValue(attset);
		dest.writeInt(defStyle);
		dest.writeInt(arc_finish_radians);
		dest.writeInt(beginDone);
		dest.writeString(String.valueOf(block_end));
		dest.writeString(String.valueOf(block_start));
		dest.writeInt(color);
		dest.writeString(color_attr);
		dest.writeInt(conversion);
		dest.writeInt(end_wheel);
		dest.writeInt(init_position);
		dest.writeInt(last_radians);
		dest.writeFloat(lastX);
		dest.writeFloat(mAngle);
		dest.writeValue(mArcColor);
		dest.writeInt(max);
		dest.writeValue(mCircleTextColor);
		dest.writeInt(mColor);
		dest.writeValue(mColorCenterHalo);
		dest.writeValue(mColorCenterHaloRectangle);
		dest.writeValue(mColorWheelPaint);
		dest.writeFloat(mColorWheelRadius);
		dest.writeValue(mColorWheelRectangle);
		dest.writeInt(mColorWheelStrokeWidth);
		dest.writeValue(mPointerColor);
		dest.writeValue(mPointerHaloPaint);
		dest.writeInt(mPointerRadius);
		dest.writeFloat(mTranslationOffset);
		dest.writeString(String.valueOf(mUserIsMovingPointer));
		dest.writeInt(pointer_color);
		dest.writeString(pointer_color_attr);
		dest.writeInt(pointer_halo_color);
		dest.writeString(pointer_halo_color_attr);
		dest.writeFloatArray(pointerPosition);
		dest.writeValue(s);
		dest.writeString(String.valueOf(show_text));
		dest.writeString(String.valueOf(show_percentage_symbol));
		dest.writeInt(start_arc);
		dest.writeInt(sweepDone);
		dest.writeString(text);
		dest.writeInt(text_color);
		dest.writeString(text_color_attr);
		dest.writeInt(text_size);
		dest.writeValue(textPaint);
		dest.writeInt(unactive_wheel_color);
		dest.writeInt(wheel_color);
		dest.writeString(wheel_color_attr);
		dest.writeString(wheel_unactive_color_attr);		
	}
	
	
	
	public HoloCircleSeekBar(Parcel in){
		super((Context) in.readValue(Context.class.getClassLoader()), (AttributeSet) in.readValue(AttributeSet.class.getClassLoader()), in.readInt());
		
		arc_finish_radians = in.readInt();
		beginDone = in.readInt();
		block_end = Boolean.getBoolean(in.readString());
		block_start = Boolean.getBoolean(in.readString());
		color = in.readInt();
		color_attr = in.readString();
		conversion = in.readInt();
		end_wheel = in.readInt();
		init_position = in.readInt();
		last_radians = in.readInt();
		lastX = in.readFloat();
		mAngle = in.readFloat();
		mArcColor = (Paint) in.readValue(Paint.class.getClassLoader());
		max = in.readInt();
		mCircleTextColor = (Paint) in.readValue(Paint.class.getClassLoader());
		mColor = in.readInt();
		mColorCenterHalo = (Paint) in.readValue(Paint.class.getClassLoader());
		mColorCenterHaloRectangle = (RectF) in.readValue(RectF.class.getClassLoader());
		mColorWheelPaint = (Paint) in.readValue(Paint.class.getClassLoader());
		mColorWheelRadius = in.readFloat();
		mColorCenterHaloRectangle = (RectF) in.readValue(RectF.class.getClassLoader());
		mColorWheelStrokeWidth = in.readInt();
		mPointerColor = (Paint) in.readValue(Paint.class.getClassLoader());
		mPointerHaloPaint = (Paint) in.readValue(Paint.class.getClassLoader());
		mPointerRadius = in.readInt();
		mTranslationOffset = in.readFloat();
		mUserIsMovingPointer = Boolean.getBoolean(in.readString());
		pointer_color = in.readInt();
		pointer_color_attr = in.readString();
		pointer_halo_color = in.readInt();
		pointer_halo_color_attr = in.readString();
		in.readFloatArray(pointerPosition);
		s = (SweepGradient) in.readValue(SweepGradient.class.getClassLoader());
		show_text = Boolean.getBoolean(in.readString());
		show_percentage_symbol = Boolean.getBoolean(in.readString());
		start_arc = in.readInt();
		sweepDone = in.readInt();
		text = in.readString();
		text_color = in.readInt();
		text_color_attr = in.readString();
		text_size = in.readInt();
		textPaint = (Paint) in.readValue(Paint.class.getClassLoader());
		unactive_wheel_color = in.readInt();
		wheel_color = in.readInt();
		wheel_color_attr = in.readString();
		wheel_unactive_color_attr = in.readString();		
	}
	
	
	public static final Parcelable.Creator<HoloCircleSeekBar> CREATOR = new Parcelable.Creator<HoloCircleSeekBar>() {
        public HoloCircleSeekBar createFromParcel(Parcel in) {
            return new HoloCircleSeekBar(in);
        }

        public HoloCircleSeekBar[] newArray(int size) {
            return new HoloCircleSeekBar[size];
        }
    };
    
    

    protected Parcelable onSaveInstanceState() {
    	
    	Bundle bundle = new Bundle();
    	bundle.putParcelable("instanceState", super.onSaveInstanceState());    	

    	bundle.putParcelable("mColorCenterHaloRectangle", mColorCenterHaloRectangle);
    	bundle.putParcelable("mColorWheelRectangle", mColorWheelRectangle);    	
    	bundle.putInt("defStyle", defStyle);
    	bundle.putInt("arc_finish_radians", arc_finish_radians);
    	bundle.putInt("beginDone", beginDone);
    	bundle.putInt("color", color);
    	bundle.putInt("conversion", conversion);
    	bundle.putInt("end_wheel", end_wheel);
    	bundle.putInt("init_position", init_position);
    	bundle.putInt("last_radians", last_radians);
    	bundle.putInt("max", max);
    	bundle.putInt("mColor", mColor);
    	bundle.putInt("mColorWheelStrokeWidth", mColorWheelStrokeWidth);
    	bundle.putInt("mPointerRadius", mPointerRadius);
    	bundle.putInt("pointer_color", pointer_color);
    	bundle.putInt("pointer_halo_color", pointer_halo_color);
    	bundle.putInt("start_arc", start_arc);
    	bundle.putInt("sweepDone", sweepDone);
    	bundle.putInt("text_color", text_color);
    	bundle.putInt("text_size", text_size);
    	bundle.putInt("unactive_wheel_color", unactive_wheel_color);
    	bundle.putInt("wheel_color", wheel_color);    	
    	bundle.putBoolean("block_end", block_end);
    	bundle.putBoolean("block_start",block_start);
    	bundle.putString("color_attr", color_attr);    	
    	bundle.putBoolean("mUserIsMovingPointer",mUserIsMovingPointer);
    	bundle.putString("pointer_color_attr",pointer_color_attr);
    	bundle.putString("pointer_halo_color_attr",pointer_halo_color_attr);
    	bundle.putBoolean("show_text",show_text);
    	bundle.putBoolean("show_percentage_symbol",show_percentage_symbol);
    	bundle.putString("text",text);
    	bundle.putString("text_color_attr",text_color_attr);
    	bundle.putString("wheel_color_attr",wheel_color_attr);
    	bundle.putString("wheel_unactive_color_attr",wheel_unactive_color_attr);
    	bundle.putFloat("lastX",lastX);
    	bundle.putFloat("mAngle",mAngle);
    	bundle.putFloat("mColorWheelRadius",mColorWheelRadius);
    	bundle.putFloat("mTranslationOffset",mTranslationOffset);
    	bundle.putFloatArray("pointerPosition",pointerPosition);
    	
    	return bundle;
    };
    
    
    @Override
    protected void onRestoreInstanceState(Parcelable state) {

    	if (state instanceof Bundle) {
    		Bundle bundle = (Bundle) state;
            
            mColorCenterHaloRectangle=bundle.getParcelable("mColorCenterHaloRectangle");  
            mColorWheelRectangle=bundle.getParcelable("mColorWheelRectangle");
            
            mColorWheelStrokeWidth=bundle.getInt("mColorWheelStrokeWidth");
            mPointerRadius=bundle.getInt("mPointerRadius");
            mColorWheelRectangle=bundle.getParcelable("mColorWheelRectangle");
            mUserIsMovingPointer=bundle.getBoolean("mUserIsMovingPointer");
            mColor=bundle.getInt("mColor");
            mTranslationOffset=bundle.getFloat("mTranslationOffset");
            mColorWheelRadius=bundle.getFloat("mColorWheelRadius");
            mAngle=bundle.getFloat("mAngle");            
            text=bundle.getString("text");
            conversion=bundle.getInt("conversion");
            max=bundle.getInt("max");
            color_attr=bundle.getString("color_attr");
            color=bundle.getInt("color");
            wheel_color_attr=bundle.getString("wheel_color_attr");
            wheel_unactive_color_attr=bundle.getString("wheel_unactive_color_attr");
            pointer_color_attr=bundle.getString("pointer_color_attr");
            pointer_halo_color_attr=bundle.getString("pointer_halo_color_attr");
            text_color_attr=bundle.getString("text_color_attr");
            wheel_color=bundle.getInt("wheel_color");
            unactive_wheel_color=bundle.getInt("unactive_wheel_color");
            pointer_color=bundle.getInt("pointer_color");
            pointer_halo_color=bundle.getInt("pointer_halo_color");
            text_size=bundle.getInt("text_size");
            text_color=bundle.getInt("text_color");
            init_position=bundle.getInt("init_position");
            block_end=bundle.getBoolean("block_end");
            lastX=bundle.getFloat("lastX");
            last_radians=bundle.getInt("last_radians");
            block_start=bundle.getBoolean("block_start");
            arc_finish_radians=bundle.getInt("arc_finish_radians");
            start_arc=bundle.getInt("start_arc");
            pointerPosition=bundle.getFloatArray("pointerPosition");
            end_wheel=bundle.getInt("end_wheel");
            show_text=bundle.getBoolean("show_text");
            beginDone=bundle.getInt("beginDone");
            sweepDone=bundle.getInt("sweepDone");            
            defStyle=bundle.getInt("defStyle");
            show_percentage_symbol=bundle.getBoolean("show_percentage_symbol");
    		
            state = bundle.getParcelable("instanceState");
    	}
    	super.onRestoreInstanceState(state);
    }
    
    
}