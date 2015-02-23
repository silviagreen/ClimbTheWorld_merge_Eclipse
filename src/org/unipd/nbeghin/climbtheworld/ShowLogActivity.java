package org.unipd.nbeghin.climbtheworld;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Toast;
import org.unipd.nbeghin.climbtheworld.R;

public class ShowLogActivity  extends FragmentActivity implements ShowLogFragment.Callbacks {


	//riferimento al fragment per visualizzazione del logfile 
	private ShowLogFragment logFrg;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		 setContentView(R.layout.activity_show_log);
	        
		 logFrg = (ShowLogFragment) getSupportFragmentManager().findFragmentById(R.id.showLogFragment);	
	}
	
	
	@Override
	public void onTaskFinished(Context context, Intent intent) {
		
		//si mostra un Toast che indica se l'operazione di upload è andata a buon fine o meno	
		Toast.makeText(this, intent.getStringExtra("result_message"), Toast.LENGTH_SHORT).show();
	}		
	
	public void selectAction(View v){
		logFrg.selectAction(v);
	}
	
}
