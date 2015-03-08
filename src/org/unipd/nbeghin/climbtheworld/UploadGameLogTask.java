package org.unipd.nbeghin.climbtheworld;

import java.io.IOException;

import org.unipd.nbeghin.climbtheworld.ShowLogFragment.TaskFragment;
import org.unipd.nbeghin.climbtheworld.util.GeneralUtils;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;


public class UploadGameLogTask extends AsyncTask<Void, Void, String> {



	Context mContext;
	
	
	public UploadGameLogTask(Context context) {
		mContext=context;
	}
	
	
	

	
	
	
	@Override
	protected String doInBackground(Void... params) {
		
		String response = "";			
		
		try {
			response=GeneralUtils.uploadGameLogFile(mContext);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return response;
	}
	
	
	
	
	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);

					
		if(result.equals("logfile_not_exists")){
			result=mContext.getResources().getString(R.string.lab_upload_filenotexists);
		}			
		else if(result.equals("server_error") || result.equals("query_fail")){
			result=mContext.getResources().getString(R.string.lab_upload_error);
		}
		else{
		
			int returned_id= Integer.valueOf(result);
			
			if(returned_id!=-1){					
				GeneralUtils.renameLogFile(mContext, returned_id);
			}
			
			result=mContext.getResources().getString(R.string.lab_upload_success);
		}
		Toast.makeText(mContext, result, Toast.LENGTH_SHORT).show();
		
	}
	
	
}