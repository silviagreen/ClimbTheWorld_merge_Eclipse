package org.unipd.nbeghin.climbtheworld.activity.recognition;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.ActivityRecognition;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.util.Log;

/**
 * Class for connecting to Location Services and removing activity recognition updates.
 * <b>
 * Note: Clients must ensure that Google Play services is available before removing activity 
 * recognition updates.
 * </b> Use GooglePlayServicesUtil.isGooglePlayServicesAvailable() to check.
 *
 *
 * To use a DetectionRemover, instantiate it, then call removeUpdates().
 *
 */
public class ActivityDetectionRemover
        implements ConnectionCallbacks, OnConnectionFailedListener {

	//campo per il contesto che arriva dal client chiamante
    private Context context;

  //campo per memorizzare l'istanza corrente del client di activity recognition
    private GoogleApiClient mActivityRecognitionClient;

  //pending intent usato per mandare indietro all'app gli eventi di activity recognition
    private PendingIntent callbackIntent;


    /**
     * Construct a DetectionRemover for the current Context
     *
     * @param context A valid Context
     */
    public ActivityDetectionRemover(Context context) {
        // Save the context
        this.context = context;

        // Initialize the globals to null
        mActivityRecognitionClient = null;

    }

    /**
     * Remove the activity recognition updates associated with a PendIntent. The PendingIntent is 
     * the one used in the request to add activity recognition updates.
     *
     * @param requestIntent The PendingIntent used to request activity recognition updates
     */
    public void removeUpdates(PendingIntent requestIntent) {

        /*
         * Set the request type, store the List, and request a activity recognition client
         * connection.
         */
        callbackIntent = requestIntent;

        // Continue the removal by requesting a connection
        requestConnection();
    }

    /**
     * Request a connection to Location Services. This call returns immediately,
     * but the request is not complete until onConnected() or onConnectionFailure() is called.
     */
    private void requestConnection() {
        getActivityRecognitionClient().connect();
    }

    /**
     * Get the current activity recognition client, or create a new one if necessary.
     *
     * @return An ActivityRecognitionClient object
     */
    public GoogleApiClient getActivityRecognitionClient() {
        /*
         * If a client doesn't already exist, create a new one, otherwise
         * return the existing one. This allows multiple attempts to send
         * a request without causing memory leaks by constantly creating
         * new clients.
         *
         */
        if (mActivityRecognitionClient == null) {
            // Create a new one
            setActivityRecognitionClient(new GoogleApiClient.Builder(context)
            .addApi(ActivityRecognition.API)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build());
        }
        return mActivityRecognitionClient;
    }

    /**
     * Get a activity recognition client and disconnect from Location Services
     */
    private void requestDisconnection() {

        // Disconnect the client
        getActivityRecognitionClient().disconnect();

        // Set the client to null
        setActivityRecognitionClient(null);
    }

    /**
     * Set the global activity recognition client
     * @param client An ActivityRecognitionClient object
     */
    public void setActivityRecognitionClient(GoogleApiClient client) {
        mActivityRecognitionClient = client;

    }

    /*
     * Called by Location Services once the activity recognition client is connected.
     *
     * Continue by removing activity recognition updates.
     */
    @Override
    public void onConnected(Bundle connectionData) {
        // If debugging, log the connection
        Log.d(ActivityRecognitionUtils.TAG, "Remover - on connected"); //context.getString(R.string.connected)
        // Send a request to Location Services to remove activity recognition updates
        continueRemoveUpdates();
    }

    /**
     * Once the connection is available, send a request to remove activity recognition updates. 
     */
    private void continueRemoveUpdates() {
        
        // Remove the updates
    	ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mActivityRecognitionClient,callbackIntent);
        
        /*
         * Cancel the PendingIntent. This stops Intents from arriving at the IntentService, even if
         * request fails. 
         */
        callbackIntent.cancel();
        
        // Disconnect the client
        requestDisconnection();
    }

    /*
     * Called by Location Services once the activity recognition client is disconnected.
     */
    /*
    @Override
    public void onDisconnected() {

        // In debug mode, log the disconnection
        Log.d(ActivityUtils.TAG, "Remover - on disconnected"); //context.getString(R.string.disconnected)

        // Destroy the current activity recognition client
        mActivityRecognitionClient = null;
    }
*/


    /*
     * Implementation of OnConnectionFailedListener.onConnectionFailed
     * If a connection or disconnection request fails, report the error
     * connectionResult is passed in from Location Services
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {

            try {
                connectionResult.startResolutionForResult((Activity) context,
                    ActivityRecognitionUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

            /*
             * Thrown if Google Play services canceled the original
             * PendingIntent
             */
            } catch (SendIntentException e) {
               // display an error or log it here.
            }

        /*
         * If no resolution is available, display Google
         * Play service error dialog. This may direct the
         * user to Google Play Store if Google Play services
         * is out of date.
         */
        } else {
        	
        	GooglePlayServicesUtil.showErrorNotification(connectionResult.getErrorCode(), context);
        	
        	/*
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                            connectionResult.getErrorCode(),
                            (Activity) context,
                            ActivityUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);
            if (dialog != null) {
                dialog.show();
            }*/
        }
    }

	@Override
	public void onConnectionSuspended(int arg0) {
		
		 // In debug mode, log the disconnection
        Log.d(ActivityRecognitionUtils.TAG, "Remover - On connection suspended"); //context.getString(R.string.disconnected)

        // Destroy the current activity recognition client
        mActivityRecognitionClient = null;
		
	}
}
