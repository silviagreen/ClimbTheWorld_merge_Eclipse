package org.unipd.nbeghin.climbtheworld.services;

import org.unipd.nbeghin.climbtheworld.ClimbApplication;
import org.unipd.nbeghin.climbtheworld.MainActivity;
import org.unipd.nbeghin.climbtheworld.models.Alarm;
import org.unipd.nbeghin.climbtheworld.receivers.StairsClassifierReceiver;
import org.unipd.nbeghin.climbtheworld.util.AlarmUtils;
import org.unipd.nbeghin.climbtheworld.util.LogUtils;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

public class SetNextAlarmTriggersIntentService extends IntentService {

	public SetNextAlarmTriggersIntentService() {
		super("SetNextAlarmTriggersIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		//id dell'alarm corrente
		int current_alarm_id = intent.getIntExtra("current_alarm_id", -1);	
		//booleano che indica se viene richiesto il set dei trigger
		boolean set_triggers = intent.getBooleanExtra("set_triggers", false);
		//booleano che indica se il set dei trigger viene richiesto all'inizio del giorno
		boolean on_midnight = intent.getBooleanExtra("on_midnight", false);
		//booleano che indica se viene richiesto il set del prossimo alarm
		boolean set_next_alarm = intent.getBooleanExtra("set_next_alarm", true);
		//booleano che indica se tale intent service è chiamato dopo il completamento
		//del boot con l'alarm settato in precedenza ancora valido
		boolean valid_on_boot = intent.getBooleanExtra("valid_on_boot", false);
		//booleano che indica se la batteria ha raggiunto il livello critico
		boolean low_battery_status = intent.getBooleanExtra("low_battery", false);		
			
		//riferimento alle SharedPreferences
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
		//////////////////////////////////////////////
		//per test algoritmo
		boolean write_log_off_intervals = intent.getBooleanExtra("write_log_off_intervals", false);
		if(write_log_off_intervals){
			//SI TRACCIANO GLI INTERVALLI NON VALUTATI A CAUSA DELL'ALGORIMO NON ATTIVO (device
			//spento o algoritmo fermato per livello di batteria critica)
			LogUtils.offIntervalsTracking(getApplicationContext(), current_alarm_id);
		}
		//////////////////////////////////////////////

		
		//viene richiesto il set dei trigger
		if(set_triggers){
			AlarmUtils.setTriggers(getApplicationContext());
			
			if(on_midnight){				
				//se il primo intervallo di attività è quello 00:00-00:05 e se il precedente metodo ha
		    	//impostato un primo trigger proprio in corrispondenza di questo primo intervallo, può
		    	//darsi che l'action di start sia partita prima del set trigger e, quindi, non abbia 
		    	//mostrato la notifica; in ogni caso la si visualizza
		    	Alarm first_alarm = AlarmUtils.getAlarm(getApplicationContext(), 1);		    	
		    	int first_trigger_index = pref.getInt("first_trigger", -1);		    	
		    	if(first_alarm.get_hour()==0 && first_alarm.get_minute()==0 && first_trigger_index==1){
		    		//si visualizza la notifica (non causa problemi se la notifica è già on-screen)
		    	}
			}
		}
			
		//viene richiesto il set del prossimo alarm
		if(set_next_alarm){
						
			//si cancella l'alarm che era stato settato in precedenza
			AlarmUtils.cancelAlarm(getApplicationContext(), AlarmUtils.getAlarm(getApplicationContext(), current_alarm_id));
							
			//se non si è arrivati ad livello di batteria critico
			if(!low_battery_status){
					
				//se si è nell'evento on-boot e l'alarm precedentemente impostato non è più valido
				//oppure si è in una qualsiasi altra situazione che richiede di settare l'alarm
				//successivo, si invoca il metodo 'setNextAlarm' con gli opportuni parametri
				if(!valid_on_boot){
					
					//si recuperano i parametri del metodo passati attraverso l'intent
					boolean takeAllAlarms = intent.getBooleanExtra("takeAllAlarms", false);		
					boolean prevAlarmNotAvailable = intent.getBooleanExtra("prevAlarmNotAvailable", false);
						
					//si imposta e si lancia il prossimo alarm
					AlarmUtils.setNextAlarm(getApplicationContext(),takeAllAlarms,prevAlarmNotAvailable,current_alarm_id);
				}
				else{ //se si è nell'evento on-boot e l'alarm precedentemente impostato è ancora
					  //valido, allora lo si reimposta attraverso l'alarm manager
										
					//parametri che servono a reimpostare l'alarm
					int curr_day_index = intent.getIntExtra("current_day_index", 0);
					int alarm_date = intent.getIntExtra("alarm_time_date", 0);
					int alarm_month = intent.getIntExtra("alarm_time_month", 0);
					int alarm_year = intent.getIntExtra("alarm_time_year", 0);
					long alarm_millis = intent.getLongExtra("alarm_time_millis", 0L);
					
					//riferimento all'alarm manager
					final AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
					//si ottiene l'alarm corrente attraverso il suo identificativo
					Alarm current_next_alarm = AlarmUtils.getAlarm(getApplicationContext(), current_alarm_id);
					
					//se il next alarm settato è un evento di stop significa che si è
					//all'interno di un intervallo attivo iniziato in precedenza (infatti
					//gli alarm sono preordinati per fare in modo che dopo un evento di
					//start ci sia un evento di stop): se tale intervallo è un "intervallo
					//di esplorazione", allora significa che il service di activity
					//recognition dovrebbe essere già in esecuzione e, quindi, lo si
					//attiva; invece, se è un "intervallo con scalini", si attiva il
					//classificatore scalini/non_scalini (il cl. Google/scalini viene
					//fatto ripartire solo nel caso in cui i valori già ottenuti in questo
					//intervallo (se ce ne sono) non bastano per definire/confermare un
					//'intervallo con scalini')						
					if(!current_next_alarm.get_actionType()){
						
						//è un "intervallo di esplorazione"
							if(!current_next_alarm.isStepsInterval(curr_day_index)){ 
							
							//si fa ripartire il service di activity recognition solo se
							//l'intervallo non è stato interessato da un periodo di gioco
							//con scalini
							if(StairsClassifierReceiver.getStepsNumber(pref)<1){
								getApplicationContext().startService(new Intent(getApplicationContext(), ActivityRecognitionRecordService.class));
								//si registra anche il receiver per la registrazione dell'attività utente
								//context.getApplicationContext().registerReceiver(userMotionReceiver, userMotionFilter);
								//context.getPackageManager().setComponentEnabledSetting(new ComponentName(context, UserMotionReceiver.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
							}
						}				
						else{ //è un "intervallo con scalini"
							
							//si fa ripartire il classificatore scalini solo se il numero
							//di scalini contati fino ad ora nell'intervallo è inferiore
							//alla soglia (soglia=1 per ora)
							if(StairsClassifierReceiver.getStepsNumber(pref)<1){
								getApplicationContext().startService(new Intent(getApplicationContext(), SamplingClassifyService.class));
								//si registra anche il receiver
								getApplicationContext().getPackageManager().setComponentEnabledSetting(new ComponentName(getApplicationContext(), StairsClassifierReceiver.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);					
							}
						}
					}						
			    		
					//si re-imposta l'alarm precedente
					//si crea il pending intent creando dapprima un intent con tutti i
					//dati dell'alarm per identificarlo in modo univoco
					PendingIntent pi = AlarmUtils.createPendingIntent(getApplicationContext(), current_next_alarm, new int[]{alarm_date, alarm_month, alarm_year});
					
					if(Build.VERSION.SDK_INT < 19){
						alarmManager.set(AlarmManager.RTC_WAKEUP, alarm_millis, pi);			
						System.out.println("API "+ Build.VERSION.SDK_INT +", SET valid next alarm on-boot");
					}
					else{
						//se nel sistema sta eseguendo una versione di Android con API >=19
						//allora è necessario invocare il metodo setExact
						alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarm_millis, pi);
						System.out.println("API "+ Build.VERSION.SDK_INT +", SET EXACT valid next alarm on-boot");
					}
			    		
					if(ClimbApplication.logEnabled){
						Log.d(MainActivity.AppName,"On boot - the previous alarm is valid; we set the same alarm");		
					}
				}
			}
		}
				
	}
}