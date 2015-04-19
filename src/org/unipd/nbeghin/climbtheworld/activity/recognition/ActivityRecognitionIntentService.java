package org.unipd.nbeghin.climbtheworld.activity.recognition;

import java.util.List;

import org.unipd.nbeghin.climbtheworld.ClimbApplication;
import org.unipd.nbeghin.climbtheworld.MainActivity;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

public class ActivityRecognitionIntentService extends IntentService {

	//variable to hold the total number of values returned by the service 
	//private static int values_number = 0;	
	//variable to hold the number of activities
	//private static int activities_number = 0;
	//sum of the activities' weights
	//private static int weights_sum = 0;
	//sum of the confidences-weights products
	//private static float confidences_weights_sum = 0f;
	
	
	private SharedPreferences prefs;
	
	public ActivityRecognitionIntentService() {
		 super("ActivityRecognitionIntentService");
	}

		
	@Override
	public void onCreate() {
		super.onCreate();
		
		prefs=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
		Log.d(MainActivity.AppName,"OnCreate activityRec service - n. values " + getValuesNumber(prefs));	
	}
	
	
	@Override
	protected void onHandleIntent(Intent intent) {
		
		// If the incoming intent contains an update
        if (ActivityRecognitionResult.hasResult(intent)) {
        	
            // Get the update
            ActivityRecognitionResult result =
                    ActivityRecognitionResult.extractResult(intent);
            // Get the most probable activity from the list of activities in the update
            DetectedActivity mostProbableActivity =
                    result.getMostProbableActivity();
           
             //Get the probability that this activity is the
             //the user's actual activity
            // Get the confidence percentage for the most probable activity
             int confidence = mostProbableActivity.getConfidence();
            
            
            //Get an integer describing the type of activity           
            int activityType = mostProbableActivity.getType();
            // Get the type of activity
            String activityName = getNameFromType(activityType);
            
            //At this point, you have retrieved all the information
             //for the current update. You can display this
             //information to the user in a notification, or
              //send it to an Activity or Service in a broadcast
             //Intent.
            
            
            if(ClimbApplication.logEnabled){
            	Log.d(MainActivity.AppName,"ActivityRecognitionIntentService - the intent contains an update");
	    		
            	Log.d(MainActivity.AppName,"ActivityRecognitionIntentService - activity: "+activityName+" confidence: " + (float)confidence/100);
	    	}
                        
            
            prefs.edit().putInt("ar_values_number", prefs.getInt("ar_values_number", 0)+1).commit();
            //values_number++;
            
            handleActivity(prefs,activityType,confidence);
           
        } else {
           //This implementation ignores intents that don't contain
            //an activity update. If you wish, you can report them as
             //errors.
            
        }

	}
	
	
	/*
	@Override
	protected void onHandleIntent(Intent intent) {
				
		// If the incoming intent contains an update
        if (ActivityRecognitionResult.hasResult(intent)) {
        	
            // Get the update
            ActivityRecognitionResult result =
                    ActivityRecognitionResult.extractResult(intent);
            //Returns the list of activities that where detected with the confidence value 
            //associated with each activity. The activities are sorted by most probable 
            //activity first
            List<DetectedActivity> probableActivities =
                    result.getProbableActivities();         
            
            
            DetectedActivity mostProbablePhysicalActivity = getMostProbablePhysicalActivity(probableActivities);
            
            
            //if the list contains a physical activity, it can be properly handled 
            if(mostProbablePhysicalActivity!=null){  
            	
            	int activityType = mostProbablePhysicalActivity.getType();
            	
            	int confidence = mostProbablePhysicalActivity.getConfidence();
            	
            	if(MainActivity.logEnabled){
                	Log.d(MainActivity.AppName,"ActivityRecognitionIntentService - activity: "+ getNameFromType(activityType)+" confidence: " + (float)confidence/100);
    	    	}            	
            	
            	handleActivity(prefs, activityType, confidence);            	
            }
            else{            	
            	if(MainActivity.logEnabled){
                	Log.d(MainActivity.AppName,"ActivityRecognitionIntentService - NO activity");
    	    	}            	
            }               
            
            prefs.edit().putInt("ar_values_number", prefs.getInt("ar_values_number", 0)+1).commit();
            //values_number++;
           
        } else {
           //This implementation ignores intents that don't contain
            //an activity update. If you wish, you can report them as
             //errors.            
        }
	}
	*/
	
	
	
	private void handleActivity(SharedPreferences prefs, int activityType, int confidence){
		/*
		switch(activityType) { 
		  case DetectedActivity.ON_FOOT:
			  weights_sum += 30;
			  confidences_weights_sum += 30*((float)confidence/100);
              activities_number++;
              break;
		  case DetectedActivity.WALKING:    
			  weights_sum += 60;
			  confidences_weights_sum += 60*((float)confidence/100);
              activities_number++;
              break;
		  case DetectedActivity.RUNNING:    
			  weights_sum += 100;
			  confidences_weights_sum += 100*((float)confidence/100);
              activities_number++;
              break;
		  case DetectedActivity.ON_BICYCLE: 
			  weights_sum += 80;
			  confidences_weights_sum += 80*((float)confidence/100);
              activities_number++;
              break;
		}*/
		
		
		switch(activityType) { 
		  case DetectedActivity.ON_FOOT:
			  prefs.edit().putInt("ar_weights_sum", prefs.getInt("ar_weights_sum", 0)+30)
			  	.putFloat("ar_confidences_weights_sum", prefs.getFloat("ar_confidences_weights_sum", 0f)+30*((float)confidence/100))
			  	.putInt("ar_activities_number", prefs.getInt("ar_activities_number", 0)+1)
			  	.commit();
            break;
		  case DetectedActivity.WALKING:  
			  prefs.edit().putInt("ar_weights_sum", prefs.getInt("ar_weights_sum", 0)+60)
			  	.putFloat("ar_confidences_weights_sum", prefs.getFloat("ar_confidences_weights_sum", 0f)+60*((float)confidence/100))
			  	.putInt("ar_activities_number", prefs.getInt("ar_activities_number", 0)+1)
			  	.commit();
            break;
		  case DetectedActivity.RUNNING:  
			  prefs.edit().putInt("ar_weights_sum", prefs.getInt("ar_weights_sum", 0)+100)
			  	.putFloat("ar_confidences_weights_sum", prefs.getFloat("ar_confidences_weights_sum", 0f)+100*((float)confidence/100))
			  	.putInt("ar_activities_number", prefs.getInt("ar_activities_number", 0)+1)
			  	.commit();
            break;
		  case DetectedActivity.ON_BICYCLE: 
			  prefs.edit().putInt("ar_weights_sum", prefs.getInt("ar_weights_sum", 0)+80)
			  	.putFloat("ar_confidences_weights_sum", prefs.getFloat("ar_confidences_weights_sum", 0f)+80*((float)confidence/100))
			  	.putInt("ar_activities_number", prefs.getInt("ar_activities_number", 0)+1)
			  	.commit();
            break;
		}
		
	}
	
	
	
	private String getNameFromType(int activityType) {
        switch(activityType) {
            case DetectedActivity.IN_VEHICLE:
                return "in_vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "on_bicycle";
            case DetectedActivity.ON_FOOT:
                return "on_foot";
            case DetectedActivity.RUNNING:
                return "running";
            case DetectedActivity.WALKING:
                return "walking";
            case DetectedActivity.STILL:
                return "still";
            case DetectedActivity.UNKNOWN:
                return "unknown";
            case DetectedActivity.TILTING:
                return "tilting";
             
        }
        return "unknown";
    }
	
	
	
	/*
	private DetectedActivity getMostProbablePhysicalActivity(List<DetectedActivity> activities){
		
		for (DetectedActivity activity : activities) {			
			if(isPhysicalActivity(activity.getType())){
				return activity;
			}
		}
		
		return null;		
	}

    private boolean isPhysicalActivity(int type) {
        switch (type) {
            // These types mean that the user is probably not moving
            case DetectedActivity.STILL:
            case DetectedActivity.TILTING:
            case DetectedActivity.UNKNOWN:
            case DetectedActivity.IN_VEHICLE:
                return false;
            default:
                return true;
        }
    }
	*/
	
	
	public static void clearValuesCount(SharedPreferences prefs){
		/*values_number=0;
		activities_number=0;
		weights_sum=0;
		confidences_weights_sum=0f;*/
		
		prefs.edit().putInt("ar_values_number", 0).putInt("ar_activities_number", 0).
			putInt("ar_weights_sum", 0).putFloat("ar_confidences_weights_sum", 0f).commit();
	}
	
	
	public static int getValuesNumber(SharedPreferences prefs){
		//return values_number;
		
		return prefs.getInt("ar_values_number", 0);
	}
	
	
	public static int getActivitiesNumber(SharedPreferences prefs){
		//return activities_number;
		
		return prefs.getInt("ar_activities_number", 0);
	}
	
	public static float getConfidencesWeightsSum(SharedPreferences prefs){				
		//return confidences_weights_sum;
		
		return prefs.getFloat("ar_confidences_weights_sum", 0f);
	}
	
	
	public static int getWeightsSum(SharedPreferences prefs){	
		//return weights_sum;
		
		return prefs.getInt("ar_weights_sum", 0);
	}
	
}
