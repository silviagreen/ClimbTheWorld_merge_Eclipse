package org.unipd.nbeghin.climbtheworld.services;

import org.unipd.nbeghin.climbtheworld.ClimbApplication;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class NotificationClickedService extends IntentService{
	public NotificationClickedService() {
		super("Service");
		Log.d("Service", "crea");
	}
	
	@Override
    protected void onHandleIntent(Intent workIntent) {
		Log.d("Service2", "azzeramento2");
		if(Build.VERSION.SDK_INT < 16)
			ClimbApplication.bonus_notification = 0;
		else
			ClimbApplication.emptyMultimapNotification();
		
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		for(int i = 1; i <= 4; i++)
			mNotificationManager.cancel(i);
		
		Intent i = getPackageManager().getLaunchIntentForPackage("org.unipd.nbeghin.climbtheworld");
		i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT|Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setPackage(null);
		startActivity(i);
    }

}
