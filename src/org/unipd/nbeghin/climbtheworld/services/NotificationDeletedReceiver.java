package org.unipd.nbeghin.climbtheworld.services;

import org.unipd.nbeghin.climbtheworld.ClimbApplication;
import org.unipd.nbeghin.climbtheworld.util.FacebookUtils;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

/*public class NotificationDeletedReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("delete receiver", "resettttt");
		ClimbApplication.bonus_notification = 0;
	}

}*/


public class NotificationDeletedReceiver extends IntentService{

	public NotificationDeletedReceiver() {
		super("Service");
		Log.d("Service", "crea");
	}
	
	@Override
    protected void onHandleIntent(Intent workIntent) {
		Log.d("Service", "azzeramento");
		if(Build.VERSION.SDK_INT < 16)
			ClimbApplication.bonus_notification = 0;
		else
			ClimbApplication.emptyMultimapNotification();
    }
	
}