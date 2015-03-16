package org.unipd.nbeghin.climbtheworld.services;

import java.util.ArrayList;

import org.unipd.nbeghin.climbtheworld.ClimbApplication;
import org.unipd.nbeghin.climbtheworld.MainActivity;
import org.unipd.nbeghin.climbtheworld.models.GameNotification;

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
		

		
//		ArrayList<String> notif_texts = null;
//        if(workIntent != null && workIntent.getExtras() != null)
//        	notif_texts = workIntent.getExtras().getStringArrayList("events");
//        
//        if(notif_texts != null){
//        	GameNotification game_not = new GameNotification(notif_texts);
//        	ClimbApplication.notifications.add(game_not);
//        	workIntent.removeExtra("notificationText");
//        }
        
		Intent i = getPackageManager().getLaunchIntentForPackage("org.unipd.nbeghin.climbtheworld");
		i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT|Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setPackage(null);
		
//		Intent i = new Intent(this, MainActivity.class);
//		i.putStringArrayListExtra("notificationText", workIntent.getStringArrayListExtra("events"));
//		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
    }

}
