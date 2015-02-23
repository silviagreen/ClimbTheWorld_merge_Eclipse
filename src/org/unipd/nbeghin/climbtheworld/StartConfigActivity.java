package org.unipd.nbeghin.climbtheworld;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import org.unipd.nbeghin.climbtheworld.R;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class StartConfigActivity extends Activity {
	
	//campi per i vari componenti grafici utilizzati
	private TextView config_text;
	private Button btt_next_config;
	private Button btt_google_play_services;
	
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_start_config);	
		
		config_text = (TextView) findViewById(R.id.config_text); 
    	btt_next_config = (Button) findViewById(R.id.btt_next_config);  
    	btt_google_play_services = (Button) findViewById(R.id.btt_google_play_action);  
		    	
    	
    	//check status of Google Play Services
    	final int gms_status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
    	    
    	
    	btt_google_play_services.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if(gms_status==ConnectionResult.SERVICE_DISABLED){
					//si apre la finestra delle impostazioni del device
					startActivity(new Intent(Settings.ACTION_SETTINGS));					
				}
				else{ //nota: se Google Play Services è attivo e aggiornato, questo bottone non viene visualizzato
					try {
					    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + GooglePlayServicesUtil.GOOGLE_PLAY_SERVICES_PACKAGE)));
					} catch (android.content.ActivityNotFoundException anfe) {
					    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + GooglePlayServicesUtil.GOOGLE_PLAY_SERVICES_PACKAGE)));
					}
				}
			}
		});
    	    	
    	//si visualizzano o meno la label e il bottone relativi al componente Google Play Services
    	checkGooglePlayServicesStatus(gms_status);    
	}
	
	
	
	
	
	public void selectAction(View v){
    	
    	switch(v.getId()) {
    		case R.id.btt_next_config:
    			startActivityForResult((new Intent(this,AlgorithmConfigActivity.class)), 1);
    			break;
    		case R.id.btt_cancel_config:
                Intent returnIntent = new Intent();
                returnIntent.putExtra("end_config_alg",true);
                setResult(RESULT_OK,returnIntent);
    			finish();
    			break;
    	}
    }
	
	
	
	

    private void checkGooglePlayServicesStatus(int status){
    	
    	if(status!=ConnectionResult.SUCCESS){
    		
    		if(status==ConnectionResult.SERVICE_MISSING || status==ConnectionResult.SERVICE_INVALID){
        		config_text.setText(R.string.lab_google_play_services_missing_invalid);        	
        		btt_google_play_services.setText(R.string.btt_google_play_services_install); 
    		}
        	else if(status==ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED){
        		config_text.setText(R.string.lab_google_play_services_update_required);
        		btt_google_play_services.setText(R.string.btt_google_play_services_update);
        	}
        	else if(status==ConnectionResult.SERVICE_DISABLED){
        		config_text.setText(R.string.lab_google_play_services_disabled);
        		btt_google_play_services.setText(R.string.btt_google_play_services_enable);
        	}    		
    		
        	btt_next_config.setVisibility(View.INVISIBLE);
        	btt_google_play_services.setVisibility(View.VISIBLE);
    	}
    	else{
    		config_text.setText(R.string.lab_first_config);
    		btt_google_play_services.setVisibility(View.INVISIBLE);
    		btt_next_config.setVisibility(View.VISIBLE);
    	}
    }
	
}