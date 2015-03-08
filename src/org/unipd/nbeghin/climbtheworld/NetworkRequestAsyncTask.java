package org.unipd.nbeghin.climbtheworld;

import org.unipd.nbeghin.climbtheworld.models.User;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;

/**
 * Asynctask for requests to be executed asynchronously.
 * @author silviasegato
 *
 */
public class NetworkRequestAsyncTask extends AsyncTask<Void, Void, Void>{

	Session session;
	Activity activity;
	SharedPreferences pref;
	static private ProgressDialog PD;
	private User me;


	public NetworkRequestAsyncTask(Session session, Activity activity) {
		this.session = session;
		this.activity = activity;
		pref = activity.getSharedPreferences("UserSession", 0);
	}
	
	@Override
	protected void onPreExecute() {

		super.onPreExecute();
		me = ClimbApplication.getUserById(activity.getSharedPreferences("UserSession", 0).getInt("local_id", -1));
		PD = new ProgressDialog(activity);
		PD.setTitle(activity.getString(R.string.wait));
		PD.setMessage(activity.getString(R.string.loading_progress));
		PD.setCancelable(false);
		PD.show();
	}
	
	@Override
	protected Void doInBackground(Void... arg0) {
		ClimbApplication.BUSY = true;
		if(activity instanceof NetworkRequests)
			((NetworkRequests) activity).makeRequest(session, PD);
		synchronized (ClimbApplication.lock) {
			while (ClimbApplication.BUSY) {
				try {
					ClimbApplication.lock.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	public static void setMessage(String msg){
		PD.setMessage(msg);
	}
	
	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		PD.dismiss();
		if(activity instanceof MainActivity)
			((MainActivity)activity).onUpdateNotifications();
		else if(activity instanceof ProfileActivity){
			((ProfileActivity)activity).setProfileData(ClimbApplication.user, true);	
		}

	}

	
}
