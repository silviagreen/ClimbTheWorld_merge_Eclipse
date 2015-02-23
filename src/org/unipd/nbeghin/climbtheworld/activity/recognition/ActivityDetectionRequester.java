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
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.util.Log;

/**
 * Class for connecting to Location Services and activity recognition updates.
 * Note: Clients must ensure that Google Play services is available before requesting updates.
 * Use GooglePlayServicesUtil.isGooglePlayServicesAvailable() to check.
 * To use a ActivityDetectionRequester, instantiate it and call requestUpdates(). Everything else
 * is done automatically.
 */
public class ActivityDetectionRequester 
		implements ConnectionCallbacks, OnConnectionFailedListener {

	//campo per il contesto che arriva dal client chiamante
	private Context context;
	
	//campo per memorizzare l'istanza corrente del client di activity recognition
	private static GoogleApiClient mActivityRecognitionClient;	
	
	//pending intent usato per mandare indietro all'app gli eventi di activity recognition
	private static PendingIntent callbackIntent;

	/**
	 * Costruttore della classe che permette di richiedere al sistema gli update di 
	 * activity recognition attraverso la connessione ai Location Services.
	 */
	/**
	 * Constructor of the ActivityDetectionRequester, a class for connecting to activity recognition updates.
	 * @param context context given by the caller client.
	 */
	public ActivityDetectionRequester(Context context) {
		this.context=context;

        //si inizializzano a null le variabili relative al client di activity
		//recognition e all'intent per ritornare il risultato
		mActivityRecognitionClient = null;
        callbackIntent = null;        
	}
	
	/**
     * Start the activity recognition update request process by
     * getting a connection.
     */
    public void requestUpdates() {
        requestConnection();
    }

    /**
     * Make the actual update request. This is called from onConnected().
     */
    private void continueRequestActivityUpdates() {
        /*
         * Request updates, using the default detection interval.
         * The PendingIntent sends updates to ActivityRecognitionIntentService
         */
    	ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(getActivityRecognitionClient(),
                ActivityRecognitionUtils.getDetectionIntervalMilliseconds(context),
                createRequestPendingIntent());

        //disconnect the client
        requestDisconnection();
    }

    /**
     * Request a connection to Location Services. This call returns immediately,
     * but the request is not complete until onConnected() or onConnectionFailure() is called.
     */
    private void requestConnection() {
        getActivityRecognitionClient().connect();
    }
	
	
    /**
     * Get the current activity recognition client and disconnect from Location Services
     */
    private void requestDisconnection() {
        getActivityRecognitionClient().disconnect();
    }
    
    
    /**
     * Get a PendingIntent to send with the request to get activity recognition updates. Location
     * Services issues the Intent inside this PendingIntent whenever a activity recognition update
     * occurs.
     *
     * @return A PendingIntent for the IntentService that handles activity recognition updates.
     */
    private PendingIntent createRequestPendingIntent() {

        //se il PendingIntent esiste già
        if (null != getRequestPendingIntent()) {

            //si ritorna l'intent esistente
            return callbackIntent;
        } 
        else {
        	//se non esiste alcun PendingIntent
            //si crea un intent che punta ad un IntentService
            Intent intent = new Intent(context, ActivityRecognitionIntentService.class);

            /*
             * Return a PendingIntent to start the IntentService.
             * Always create a PendingIntent sent to Location Services
             * with FLAG_UPDATE_CURRENT, so that sending the PendingIntent
             * again updates the original. Otherwise, Location Services
             * can't match the PendingIntent to requests made with it.
             */
            PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            setRequestPendingIntent(pendingIntent);
            return pendingIntent;
        }

    }
	
	

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

	
	/*
     * Called by Location Services once the activity recognition client is connected.
     *
     * Continue by requesting activity updates.
     */
	@Override
	public void onConnected(Bundle arg0) {
		
		 // If debugging, log the connection
        Log.d(ActivityRecognitionUtils.TAG, "Detection - On connected"); //context.getString(R.string.connected)

        // Continue the process of requesting activity recognition updates
        continueRequestActivityUpdates();
	}

	/**
     * Returns the current PendingIntent to the caller.
     *
     * @return The PendingIntent used to request activity recognition updates
     */
    public PendingIntent getRequestPendingIntent() {
        return callbackIntent;
    }

    /**
     * Sets the PendingIntent used to make activity recognition update requests
     * @param intent The PendingIntent
     */
    public void setRequestPendingIntent(PendingIntent intent) {
        callbackIntent = intent;
    }
	
	
	/**
     * Get the current activity recognition client, or create a new one if necessary.
     * This method facilitates multiple requests for a client, even if a previous
     * request wasn't finished. Since only one client object exists while a connection
     * is underway, no memory leaks occur.
     *
     * @return An ActivityRecognitionClient object
     */
    private GoogleApiClient getActivityRecognitionClient() {
        if (mActivityRecognitionClient == null) {

            mActivityRecognitionClient =
            		
            		new GoogleApiClient.Builder(context)
            .addApi(ActivityRecognition.API)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build();

            		
                   // new ActivityRecognitionClient(context, this, this);
        }
        return mActivityRecognitionClient;
    }

    
    /*
     * Called by Location Services once the activity recognition client is disconnected.
     */
	@Override
	public void onConnectionSuspended(int arg0) {
		
		// In debug mode, log the disconnection
        Log.d(ActivityRecognitionUtils.TAG, "Detection - On connection suspended"); //context.getString(R.string.disconnected)

        // Destroy the current activity recognition client
        mActivityRecognitionClient = null;		
	}
	
}