package org.unipd.nbeghin.climbtheworld.activity.recognition;

import android.content.Context;
import android.preference.PreferenceManager;


/**
 * Utility class for the activity recognition service.
 */
public final class ActivityRecognitionUtils {

    public static final String TAG = "ActivityRecognition";

    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    
    /**
     * Sets the detection interval for the activity recognition updates.
     * @param context context of the application.
     * @param milliseconds number of milliseconds that defines the activity update interval.
     */
    public static void setDetectionIntervalMilliseconds(Context context, int milliseconds){    	
    	PreferenceManager.getDefaultSharedPreferences(context).edit().putInt("ar_detection_interval_milliseconds", milliseconds).commit();    	
    }
    
    /**
     * Gets the number of milliseconds that defines the activity update interval.
     * @param context context of the application.
     * @return the number of milliseconds that defines the activity update interval.
     */
    public static int getDetectionIntervalMilliseconds(Context context){
    	return PreferenceManager.getDefaultSharedPreferences(context).getInt("ar_detection_interval_milliseconds", 5000);
    }
    
}