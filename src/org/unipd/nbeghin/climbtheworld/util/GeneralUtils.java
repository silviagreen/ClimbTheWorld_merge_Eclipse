package org.unipd.nbeghin.climbtheworld.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.unipd.nbeghin.climbtheworld.ClimbActivity;
import org.unipd.nbeghin.climbtheworld.ClimbApplication;
import org.unipd.nbeghin.climbtheworld.MainActivity;
import org.unipd.nbeghin.climbtheworld.models.Alarm;
import org.unipd.nbeghin.climbtheworld.receivers.StairsClassifierReceiver;
import org.unipd.nbeghin.climbtheworld.receivers.TimeBatteryWatcher;
import org.unipd.nbeghin.climbtheworld.services.ActivityRecognitionRecordService;
import org.unipd.nbeghin.climbtheworld.services.SamplingClassifyService;
import org.unipd.nbeghin.climbtheworld.services.SetNextAlarmTriggersIntentService;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * 
 * Classe che contiene alcuni metodi di utilità.
 *
 */
public final class GeneralUtils {
	
	//numero di giorni di cui è composta una settimana
	public static int daysOfWeek = 2;
	private static AlarmManager alarmMgr;

	
	/**
	 * Costruttore della classe.
	 */
	private GeneralUtils(){
		
	}
	
	
	/**
     * Il metodo <code>isInternetConnectionUp</code> permette di controllare se 
     * è disponibile o meno una connessione dati.
     * 
     * @param context
     *            contesto dell'activity che chiama il metodo
     * @return 'true' se è disponibile una connessione dati, 'false' altrimenti
     */
    public static boolean isInternetConnectionUp(Context context) {
    	
    	ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    	NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
       
    	if (netInfo!=null && netInfo.isConnected()) {
    		return true;
    	}
    	return false;
    }
	
    
    public static boolean isActivityRecognitionServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("org.unipd.nbeghin.climbtheworld.services.ActivityRecognitionRecordService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    
    
    public static boolean isSamplingClassifyServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("org.unipd.nbeghin.climbtheworld.services.SamplingClassifyRecordService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
        
    /*
    public static int isServiceInRestartPhase(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("org.unipd.nbeghin.climbtheworld.services.SamplingClassifyService".equals(service.service.getClassName())) {                
            	if(service.restarting!=0){
            		return 1;
            	}
            	else{
            		return 0;	
            	}
            }
        }
        return -1;
    }
    */
    
       
    /**
     * Initializes the algorithm by setting up the first alarm.
     * @param context context of the application.
     * @param prefs reference to android shared preferences. 
     */
    public static void initializeAlgorithm(final Context context, final SharedPreferences prefs) {
    	    	    	
    	//si fa il setup del db per gli alarm
    	//AlarmUtils.setupAlarmsDB(context); 
    	//si creano gli alarm
		//AlarmUtils.createAlarms(context);  
    	//readIntervalsFromFile(context);
		
    	Thread thread = new Thread(){
    		@SuppressLint("NewApi")
			@Override
    		public void run() {
    			
    			//si recupera il numero di alarm che sono presenti nel database (le API del 
    			//database presentano un metodo che ritorna il numero in 'long'; si fa il cast a
    			//'int' per andare meglio ad utilizzare questo numero; tale cast è safe in quanto
    			//non si perde informazione, infatti il numero ritornato è tra min=0 e max=576 con
    			//min > Integer.MIN_VALUE e max < Integer.MAX_VALUE)
    			int alarms_number = (int) AlarmUtils.countAlarms(context);
    			   			    			
    			//se sono stati creati intervalli
    			if(alarms_number>0){
    				Editor editor = prefs.edit();    
    				//nelle SharedPreferences si salva il numero totale di alarm creati
    				editor.putInt("alarms_number", alarms_number).commit();
    				        	    	
        	    	/////////		
        	    	//PER TEST ALGORITMO
        	    	editor.putInt("artificialDayIndex", 0);    	
        	    	Calendar cal = Calendar.getInstance();
        	    	SimpleDateFormat calFormat = new SimpleDateFormat("yyyy-MM-dd");
        	    	String dateFormatted = calFormat.format(cal.getTime());
        	    	editor.putString("dateOfIndex", dateFormatted);
        	    	editor.commit();    	
        	    	
        	    	//si imposta l'alarm per aggiornare l'indice artificiale che rappresenta il giorno
        	    	//all'interno della settimana corta
        	    	alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        	    	Intent intent = new Intent(context, TimeBatteryWatcher.class);
        	    	intent.setAction("org.unipd.nbeghin.climbtheworld.UPDATE_DAY_INDEX_TESTING");    	
        	    	Calendar calendar = Calendar.getInstance();
        	    	//si imposta a partire dalla mezzanotte del giorno successivo
        	    	calendar.add(Calendar.DATE, 1); 
        	    	calendar.set(Calendar.HOUR_OF_DAY, 0);
        	    	calendar.set(Calendar.MINUTE, 0);
        	    	calendar.set(Calendar.SECOND, 0); 
        	    	        	    	
        	    	//si imposta l'alarm per la mezzanotte del giorno successivo (poi per ripeterlo
        	    	//ogni giorno alla stessa ora si reimposta una volta consumato/al boot del device);
        	    	//non si usa il metodo setRepeating perché a partire dalle API 19 esso imposta 
        	    	//alarm inesatti
        	    	if(Build.VERSION.SDK_INT < 19){
        	    		alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), PendingIntent.getBroadcast(context, 0, intent, 0));
        	    		System.out.println("API "+ Build.VERSION.SDK_INT +", SET first update intent");
        	    	}
        	    	else{ 
        	    		//se nel sistema sta eseguendo una versione di Android con API >=19
        	    		//allora è necessario invocare il metodo setExact
        	    		alarmMgr.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), PendingIntent.getBroadcast(context, 0, intent, 0));
        	    		System.out.println("API "+ Build.VERSION.SDK_INT +", SET EXACT first update intent");
        	    	}
        	    	
        	    	/*
        	    	alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
        	    			AlarmManager.INTERVAL_DAY, PendingIntent.getBroadcast(context, 0, intent, 0));
        	       	*/
        	    	if(ClimbApplication.logEnabled){
        	    		Log.d(MainActivity.AppName + " - TEST","GeneralUtils - init index: 0, init date: " + dateFormatted);	
        	    		Log.d(MainActivity.AppName + " - TEST","GeneralUtils - set update day index alarm");
        	    		int month =calendar.get(Calendar.MONTH)+1;    	
        	        	Log.d(MainActivity.AppName + " - TEST", "GeneralUtils - UPDATE DAY INDEX ALARM: h:m:s=" 
        	    				+ calendar.get(Calendar.HOUR_OF_DAY)+":"+ calendar.get(Calendar.MINUTE)+":"+ calendar.get(Calendar.SECOND) +
        	    				"  "+calendar.get(Calendar.DATE)+"/"+month+"/"+calendar.get(Calendar.YEAR));        	
        	        	Log.d(MainActivity.AppName + " - TEST", "GeneralUtils - milliseconds of the update day index alarm: " + calendar.getTimeInMillis());
        	    	}
        	    	/////////        	    	
        	    	        	    	
        	    	////////////////////////////
        	    	//utile per scrivere il LOG
        	    	editor.putBoolean("next_alarm_mutated", false).commit();
        	    	////////////////////////////
        	    	    	
        	    	//LogUtils.writeLogFile(context, "ALGORITMO\n");
        	    	LogUtils.initLogFile(context, AlarmUtils.getAllAlarms(context));
        	    	
        	    	LogUtils.offIntervalsTracking(context, -1);    			
        	    	//si imposta e si lancia il prossimo alarm
        	    	AlarmUtils.setNextAlarm(context,true,false,-1);
        	    	
        	    	prefs.edit().putInt("steps_with_game_yesterday",100).commit();
        	    	
        	    	//si imposta l'alarm che serve per recuperare il livello di carica della batteria; è utile per
        	    	//attuare il bilanciamento energetico        	    	
        	    	Intent battery_intent = new Intent(context, TimeBatteryWatcher.class);
        	    	battery_intent.setAction("org.unipd.nbeghin.climbtheworld.BATTERY_ENERGY_BALANCING");    	
        	    	//si ripete l'alarm circa ogni ora (il primo lancio avviene entro 5 secondi)
        	    	alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 5000,
        	    			3600000, PendingIntent.getBroadcast(context, 0, battery_intent, 0));
    			}
    			else{
    				//LogUtils.writeLogFile(context, "NON FAI MAI ATTIVITA' FISICA/SCALINI: NON E' STATO CREATO ALCUN INTERVALLO");
    				LogUtils.initLogFile(context, null);
    			}
    		}
    	};
    	thread.start();
    }   
    
    
    
    public static void stopAlgorithm(Context context, int alarm_id, SharedPreferences prefs){
    	
    	//si recupera il prossimo alarm impostato in precedenza
		Alarm current_next_alarm = AlarmUtils.getAlarm(context, alarm_id);
		//se è di stop significa che si è all'interno di un intervallo attivo e, quindi,
		//si ferma il classificatore eventualmente in esecuzione
		if(!current_next_alarm.get_actionType()){
				
			if(!current_next_alarm.isStepsInterval(prefs.getInt("artificialDayIndex", 0))){
				if(isActivityRecognitionServiceRunning(context)){
					Log.d(MainActivity.AppName,"BATTERY LOW - Stop activity recognition");
					context.stopService(new Intent(context, ActivityRecognitionRecordService.class));
				}
			}
			else{
				//se l'intervallo è un "intervallo con scalini" e il gioco non è in esecuzione, allora
				//si ferma il classificatore scalini/non_scalini
				if(!ClimbActivity.isGameActive()){
					Log.d(MainActivity.AppName,"BATTERY LOW - Gioco non attivo, si ferma il classificatore scalini");
					context.stopService(new Intent(context, SamplingClassifyService.class));
					//si disabilita anche il receiver
					//context.getApplicationContext().unregisterReceiver(stairsReceiver);
					context.getPackageManager().setComponentEnabledSetting(new ComponentName(context, StairsClassifierReceiver.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
				}	
			}
		}
				
		//si fa partire l'intent service del setNextAlarm che, in tal caso, cancella
		//solamente il prossimo alarm, precedentemente impostato (in tal modo si
		//ferma l'algoritmo)
		context.startService(new Intent(context, SetNextAlarmTriggersIntentService.class)	
			.putExtra("current_alarm_id", prefs.getInt("alarm_id",-1))
			.putExtra("low_battery", true)); 
    }
    
    
    
    /*
    private static void readIntervalsFromFile(Context context) {

    	DbHelper helper = DbHelper.getInstance(context);
    	
    	RuntimeExceptionDao<Alarm, Integer> alarmDao = helper.getAlarmDao();
    	
	    AssetManager assetManager = context.getResources().getAssets();
	    	    
	    try {
	    	
	        InputStream inputStream = assetManager.open("intervals.txt");

	        if (inputStream!=null) {
	        	
	        	InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
	            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
	            String receiveString = "";
	            
	            Calendar cal_start = Calendar.getInstance();
	            Calendar cal_stop = (Calendar) cal_start.clone();	            
	        	SimpleDateFormat calFormat = new SimpleDateFormat("HH:mm:ss");
	        	  
	        	int current_day = cal_start.get(Calendar.DATE);
	        	int current_month = cal_start.get(Calendar.MONTH);
	        	int current_year = cal_start.get(Calendar.YEAR);
	        	
	        	Calendar cal_previous_stop = Calendar.getInstance();
	        	//si inizializza questo oggetto Calendar con una data precedente a quella corrente;
	        	//serve per inserire un intervallo solo se il suo inizio viene dopo la fine del 
	        	//precedente intervallo
	        	cal_previous_stop.add(Calendar.YEAR,-1);
	        	
	        	//serve perché si saltano le prime due linee
	        	int line_number=0;
	        	
	            while ((receiveString = bufferedReader.readLine())!=null) {
	            	            	
	            	Log.d(MainActivity.AppName, "Read intervals file: line number=" + line_number);
	            	
	            	if(line_number>1){
	            	
		            	int substring_end = receiveString.indexOf(";",0);
		            		            	
		            	String str_startTime = receiveString.substring(0,substring_end).trim();
		            	Log.d(MainActivity.AppName, "Read intervals file: start time string " + str_startTime);
		            	
		            	String str_stopTime = receiveString.substring(substring_end+1,receiveString.indexOf(";",substring_end+1)).trim();
		            	Log.d(MainActivity.AppName, "Read intervals file: stop time string " + str_stopTime);
		            	
		            	substring_end = receiveString.indexOf(";",substring_end+1);
		            	
		            	cal_start.setTime(calFormat.parse(str_startTime));
		            	cal_start.set(Calendar.DATE, current_day);
		            	cal_start.set(Calendar.MONTH, current_month);
		            	cal_start.set(Calendar.YEAR, current_year);
		            	cal_stop.setTime(calFormat.parse(str_stopTime));
		            	cal_stop.set(Calendar.DATE, current_day);
		            	cal_stop.set(Calendar.MONTH, current_month);
		            	cal_stop.set(Calendar.YEAR, current_year);
					
		            	Log.d(MainActivity.AppName, "Read intervals file: HH:mm:ss START:" + cal_start.get(Calendar.HOUR_OF_DAY)+ ":"
	        					+ cal_start.get(Calendar.MINUTE)+":"+cal_start.get(Calendar.SECOND) + " STOP: " + cal_stop.get(Calendar.HOUR_OF_DAY)+ ":"
	        					+ cal_stop.get(Calendar.MINUTE)+":"+cal_stop.get(Calendar.SECOND));
	        					            	
		            	   
		            	if(cal_start.after(cal_previous_stop)){ 
		            		//l'intervallo ha un orario di inizio successivo alla fine
		            		//dell'intervallo precedente
		            		
		            		Log.d(MainActivity.AppName, "Read intervals file: start time of this interval > end time of the previous one");
		            		
		            		if(cal_start.before(cal_stop)){
		            			//l'intervallo è valido
		            			Log.d(MainActivity.AppName, "Read intervals file: start time < end time");
		            			
		            			
		            			boolean activationState[] = new boolean[daysOfWeek];
		            			
		            			
		            			String str_initialState = receiveString.substring(substring_end+1,receiveString.indexOf(";",substring_end+1)).trim();
		            			//System.out.println("str state: " + str_initialState);
		            			
		            			String[] parts = str_initialState.split("\\:");
		            			//System.out.println("str state lenght: " + parts.length + " 0:" + parts[0] + " 1:" + parts[1]);
		            			
		            			
		            			for(int i=0; i<parts.length; i++){
		            				
		            				if(parts[i].equals("1")){
		            					activationState[i]=true;
		            				}
		            				else{ //parts[i].equals("0")
		            					activationState[i]=false;
		            				}
		            			}		            			
		            			
		            			Alarm start = new Alarm(cal_start.get(Calendar.HOUR_OF_DAY), cal_start.get(Calendar.MINUTE), cal_start.get(Calendar.SECOND), true, activationState, new float[] {0.25f,0.25f});
		            			Alarm stop = new Alarm(cal_stop.get(Calendar.HOUR_OF_DAY), cal_stop.get(Calendar.MINUTE), cal_stop.get(Calendar.SECOND), false, activationState, new float[] {0.25f,0.25f});
		            			
		            			alarmDao.createIfNotExists(start);
		            			alarmDao.createIfNotExists(stop);
		            			
		            			cal_stop=(Calendar) cal_previous_stop.clone();
		            		}
		            	}
	            	}
	            	line_number++;
	            }
	            
	            Log.d(MainActivity.AppName, "Read intervals file: Total number of lines: " + line_number);
	            
	            inputStream.close();
	        }
	    } 
	    catch (ParseException e) {
			e.printStackTrace();
		}
	    catch (FileNotFoundException e) {
	        Log.e(MainActivity.AppName, " - File not found: " + e.toString());
	    } catch (IOException e) {
	        Log.e(MainActivity.AppName, " - Can not read file: " + e.toString());
	    }
	}
    */
    
    
    @SuppressWarnings("deprecation")
	public static String uploadLogFile(Context context) throws IOException{
    	
    	String log_file_name="";
    	int log_file_id = PreferenceManager.getDefaultSharedPreferences(context).getInt("log_file_id", -1);
    	    	
    	if(log_file_id==-1){
    		log_file_name="algorithm_log";
    	}
    	else{
    		log_file_name="algorithm_log_"+log_file_id;
    	}    	
    	
    	final File logFile = new File(context.getDir("climbTheWorld_dir", Context.MODE_PRIVATE), log_file_name);
    	
    	
    	if(logFile.exists()){
    		
    		//Php script path
        	String uploadServerUri = "http://www.learningquiz.altervista.org/quiz_game_api/uploadLogFile.php";
        	    		
        	HttpClient client = new DefaultHttpClient();
        	
        	HttpPost post = new HttpPost(uploadServerUri);
        	MultipartEntityBuilder builder = MultipartEntityBuilder.create();        
        	builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            FileBody fb = new FileBody(logFile);

            builder.addPart("file", fb);  
            builder.addTextBody("log_file_id", String.valueOf(log_file_id), ContentType.TEXT_PLAIN);
        	
            final HttpEntity entity = builder.build();
        	 
            post.setEntity(entity);
            
            HttpResponse response = null;
            
            try {
    			response = client.execute(post);
    		} catch (ClientProtocolException e) {
    			e.printStackTrace();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}        
            
            
            //response code from the server
            int server_response_code = response.getStatusLine().getStatusCode();
                    
            if(server_response_code==200){ //ok        	
            	return getContent(response);
            }
            else{        	
            	return "server_error";
            }
    	}
    	else{
    		return "logfile_not_exists";
    	}
    } 
    	
    
    
    private static String getContent(HttpResponse response) throws IOException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String body = "";
        String content = "";

        while ((body = rd.readLine()) != null){
            content += body + "\n";
        }
        return content.trim();
    }
    
    
    
    public static void renameLogFile(Context context, int log_file_id){
    	    	
    	//si rinomina il file di log aggiungendo l'id ritornato dal server
    	File from = new File(context.getDir("climbTheWorld_dir", Context.MODE_PRIVATE), "algorithm_log");
    	File to = new File(context.getDir("climbTheWorld_dir", Context.MODE_PRIVATE), "algorithm_log_"+log_file_id);
       	from.renameTo(to);
       	
    	//si salva l'id nelle shared preferences
    	PreferenceManager.getDefaultSharedPreferences(context).edit().putInt("log_file_id", log_file_id).commit();
    }
    
    
    @SuppressWarnings("deprecation")
	public static String uploadGameLogFile(Context context) throws IOException{
    	
    	String log_file_name="";
    	int log_file_id = PreferenceManager.getDefaultSharedPreferences(context).getInt("log_file_id", -1);
    	    	
    	if(log_file_id==-1){
    		log_file_name="game_log";
    	}
    	else{
    		log_file_name="game_log"+log_file_id;
    	}    	
    	
    	final File logFile = new File(context.getDir("climbTheWorld_dir", Context.MODE_PRIVATE), log_file_name);
    	
    	
    	if(logFile.exists()){
    		
    		//Php script path
        	String uploadServerUri = "http://www.learningquiz.altervista.org/quiz_game_api/uploadGameLogFile.php";
        	    		
        	HttpClient client = new DefaultHttpClient();
        	
        	HttpPost post = new HttpPost(uploadServerUri);
        	MultipartEntityBuilder builder = MultipartEntityBuilder.create();        
        	builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            FileBody fb = new FileBody(logFile);

            builder.addPart("file", fb);  
            builder.addTextBody("log_file_id", String.valueOf(log_file_id), ContentType.TEXT_PLAIN);
        	
            final HttpEntity entity = builder.build();
        	 
            post.setEntity(entity);
            
            HttpResponse response = null;
            
            try {
    			response = client.execute(post);
    		} catch (ClientProtocolException e) {
    			e.printStackTrace();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}        
            
            
            //response code from the server
            int server_response_code = response.getStatusLine().getStatusCode();
                    
            if(server_response_code==200){ //ok        	
            	return getContent(response);
            }
            else{        	
            	return "server_error";
            }
    	}
    	else{
    		return "logfile_not_exists";
    	}
    }
    
   
}