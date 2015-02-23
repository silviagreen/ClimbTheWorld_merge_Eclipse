package org.unipd.nbeghin.climbtheworld.services;

import org.unipd.nbeghin.climbtheworld.ClimbApplication;
import org.unipd.nbeghin.climbtheworld.R;
import org.unipd.nbeghin.climbtheworld.util.FacebookUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.Gravity;
import android.widget.Toast;

public class NetworkBroadcasterReceiver extends BroadcastReceiver{

	SharedPreferences pref;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		pref = context.getSharedPreferences("UserSession", 0);
		
		if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
		    NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
		    if(networkInfo != null && networkInfo.isConnected()) {
		//if(FacebookUtils.isOnline(context)){
			System.out.println("START");
			Intent filter = new Intent(context, UpdateService.class);
        	context.startService(filter);
		}
		}
        
        if(!FacebookUtils.isOnline(context) && ClimbApplication.isActivityVisible() && !pref.getString("FBid", "none").equals("none") && !pref.getString("FBid", "none").equals("empty") && !pref.getString("FBid", "none").isEmpty()){
			Toast t = Toast.makeText(context, context.getString(R.string.offline), Toast.LENGTH_SHORT);
			t.setGravity(Gravity.TOP, 0, 0);
			t.show();
		}
	}

}
