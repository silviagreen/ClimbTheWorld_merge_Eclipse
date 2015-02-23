package org.unipd.nbeghin.climbtheworld;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import org.unipd.nbeghin.climbtheworld.R;

public class AlgorithmConfigActivity extends FragmentActivity implements AlgorithmConfigFragment.Callbacks {

	//riferimento al fragment per la configurazione dell'algoritmo 
	private AlgorithmConfigFragment configFrg;		

	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		 setContentView(R.layout.activity_algorithm_config);
	        
		 configFrg = (AlgorithmConfigFragment) getSupportFragmentManager().findFragmentById(R.id.algorithmConfigFragment);	
	}



	@Override
	public void onTaskFinished(Context context, Intent intent) {
		
		startActivity(intent);
	}
	
	
	public void selectAction(View v){
		configFrg.selectAction(v);
	}
}
