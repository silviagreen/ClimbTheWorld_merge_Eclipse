package org.unipd.nbeghin.climbtheworld;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import org.unipd.nbeghin.climbtheworld.R;

public class EndConfigActivity extends Activity {

	private Button backToMainBtt;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_end_config);		
	    backToMainBtt = (Button) findViewById(R.id.btt_endConfig);	    
	    
	    backToMainBtt.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				//nell'intent per chiamare l'activity principale si setta il flag 
				//'FLAG_ACTIVITY_CLEAR_TOP' per fare in modo che essa sia al top dello stack,
				//facendo il pop delle precedenti activity
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.putExtra("end_config_alg", true);
				startActivity(i);
			}
		});
	}
	
	
	//si fa l'override del metodo che gestisce il back button per fare in modo di
	//ritornare all'activity principale

	@Override
	public void onBackPressed() {
		startActivity(new Intent(getApplicationContext(), MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));		
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		startActivity(new Intent(getApplicationContext(), MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
		return super.onKeyDown(keyCode, event);
	}
	
}
