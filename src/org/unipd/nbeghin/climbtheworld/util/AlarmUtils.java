package org.unipd.nbeghin.climbtheworld.util;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.unipd.nbeghin.climbtheworld.AlgorithmConfigFragment;
import org.unipd.nbeghin.climbtheworld.ClimbApplication;
import org.unipd.nbeghin.climbtheworld.MainActivity;
import org.unipd.nbeghin.climbtheworld.activity.recognition.ActivityRecognitionIntentService;
import org.unipd.nbeghin.climbtheworld.comparator.AlarmComparator;
import org.unipd.nbeghin.climbtheworld.db.DbHelper;
import org.unipd.nbeghin.climbtheworld.models.Alarm;
import org.unipd.nbeghin.climbtheworld.receivers.StairsClassifierReceiver;
import org.unipd.nbeghin.climbtheworld.services.ActivityRecognitionRecordService;
import org.unipd.nbeghin.climbtheworld.services.SamplingClassifyService;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseIntArray;

import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public final class AlarmUtils {
	
	//private static PreparedQuery<Alarm> alarmsForTemplateQuery = null;
	private static Random rand = new Random();
	
	/**
	 * Costruttore della classe.
	 */
	private AlarmUtils(){	
	}

	
    public static void setupAlarmsDB(Context context){
    	
    	ConnectionSource connectionSource = new AndroidConnectionSource(DbHelper.getInstance(context));
    	    	
    	try{    		
    		DaoManager.createDao(connectionSource, Alarm.class);
			//DaoManager.createDao(connectionSource, TimeTemplate.class);
			//DaoManager.createDao(connectionSource, AlarmTimeTemplate.class);
			
			TableUtils.createTableIfNotExists(connectionSource, Alarm.class);
			//TableUtils.createTableIfNotExists(connectionSource, TimeTemplate.class);
			//TableUtils.createTableIfNotExists(connectionSource, AlarmTimeTemplate.class);
			
    	} catch (SQLException e) {
			e.printStackTrace();
		}
    }
    
    
    public static void createAlarms(Context context){
    	
    	DbHelper helper = DbHelper.getInstance(context);
    	
    	RuntimeExceptionDao<Alarm, Integer> alarmDao = helper.getAlarmDao();
    	
    	//esempi    	
    	
    	//creo alarm
    	//boolean bb[] = new boolean[] {true,true,true,true,true,true,true};
    	//boolean bb1[] = new boolean[] {false,false,true,true,true,true,true};
    	//boolean noweekend[] = new boolean[] {false,true,true,true,true,true,false}; 
    	boolean bb[] = new boolean[] {true,true};
    	//float pf[] = new float[] {0.25f,0.25f,0.25f,0.25f,0.25f,0.25f,0.25f};
    	float pf[] = new float[] {0.25f,0.25f};
    	Alarm alm1 = new Alarm(0,0,00,true,new boolean[]{false,true},pf);
		Alarm alm2 = new Alarm(0,01,50,false,new boolean[]{false,true},pf);
		Alarm alm3 = new Alarm(9,45,51,true,new boolean[]{true,true},pf);
		Alarm alm4 = new Alarm(9,49,50,false,new boolean[]{true,true},pf);
		Alarm alm5 = new Alarm(11,13,51,true,new boolean[]{true,false},pf); 
		Alarm alm6 = new Alarm(11,14,50,false,new boolean[]{true,false},pf);
		Alarm alm7 = new Alarm(15,49,15,true,bb,pf); //boolean[]{false,true}
		Alarm alm8 = new Alarm(15,50,00,false,bb,pf);
		Alarm alm9 = new Alarm(15,50,01,true,new boolean[]{false,true},pf);
		Alarm alm10 = new Alarm(15,50,50,false,new boolean[]{false,true},pf);
		Alarm alm11 = new Alarm(15,50,51,true,new boolean[]{false,true},pf);
		Alarm alm12 = new Alarm(15,55,50,false,new boolean[]{false,true},pf);
		
		alm7.setStepsInterval(PreferenceManager.getDefaultSharedPreferences(context).getInt("artificialDayIndex", 0), true);
		alm8.setStepsInterval(PreferenceManager.getDefaultSharedPreferences(context).getInt("artificialDayIndex", 0), true);
		
		
		//alm11.setStepsInterval(PreferenceManager.getDefaultSharedPreferences(context).getInt("artificialDayIndex", 0), true);
		//alm12.setStepsInterval(PreferenceManager.getDefaultSharedPreferences(context).getInt("artificialDayIndex", 0), true);
		
		/*
		//creo template
		
		//template usato nella prima settimana
		TimeTemplate tt1 = new TimeTemplate(1,true);		
		//template che viene popolato di alarm durante il processo di learning (prima settimana)
		TimeTemplate tt2 = new TimeTemplate(2,true); 
		//template che viene popolato di alarm durante la seconda settimana
		TimeTemplate tt3 = new TimeTemplate(3,true); 
		
		//metto insieme alarm e relativo template facendo il join delle tabelle
		
		//esempio: il template 1 contiene 2 alarm
		AlarmTimeTemplate att1 = new AlarmTimeTemplate(alm1,tt1);
		AlarmTimeTemplate att2 = new AlarmTimeTemplate(alm2,tt1);
		AlarmTimeTemplate att3 = new AlarmTimeTemplate(alm3,tt1);
		AlarmTimeTemplate att4 = new AlarmTimeTemplate(alm4,tt1);
		AlarmTimeTemplate att5 = new AlarmTimeTemplate(alm5,tt1);
		AlarmTimeTemplate att6 = new AlarmTimeTemplate(alm6,tt1);
				
	    */
				
		// persist the alarm object to the database
		alarmDao.createIfNotExists(alm1);
		alarmDao.createIfNotExists(alm2);
		alarmDao.createIfNotExists(alm3);
		alarmDao.createIfNotExists(alm4);
		alarmDao.createIfNotExists(alm5);
		alarmDao.createIfNotExists(alm6);
		alarmDao.createIfNotExists(alm7);
		alarmDao.createIfNotExists(alm8);
		alarmDao.createIfNotExists(alm9);
		alarmDao.createIfNotExists(alm10);
		alarmDao.createIfNotExists(alm11);
		alarmDao.createIfNotExists(alm12);
		
		/*
		helper.getTimeTemplateDao().createIfNotExists(tt1);
		helper.getTimeTemplateDao().createIfNotExists(tt2);
		helper.getTimeTemplateDao().createIfNotExists(tt3);
		
		helper.getAlarmTimeTemplateDao().createIfNotExists(att1);
		helper.getAlarmTimeTemplateDao().createIfNotExists(att2);
		helper.getAlarmTimeTemplateDao().createIfNotExists(att3);
		helper.getAlarmTimeTemplateDao().createIfNotExists(att4);
		helper.getAlarmTimeTemplateDao().createIfNotExists(att5);
		helper.getAlarmTimeTemplateDao().createIfNotExists(att6);
		*/
    }
    
    
    /**
     * Creates the 5-minute intervals from the time slots specified by the user.
     * @param context context of the application.
     * @param time_slots array that holds the time slots.
     */
    public static void createIntervals(Context context, SparseIntArray time_slots, AlgorithmConfigFragment.CreateIntervalsTask task){
    	
    	//per prima cosa viene creata la tabella per gli alarm
    	setupAlarmsDB(context);
    	
    	DbHelper helper = DbHelper.getInstance(context);    	
    	RuntimeExceptionDao<Alarm, Integer> alarmDao = helper.getAlarmDao();
    	
    	int count=0;    	    	
    	
    	int progress=4; //100/n. fasce orarie -> 100/24   	
    	
    	task.doProgress(progress);
    	
    	for(int i=0; i<time_slots.size(); i++){
    		
    		//se la fascia oraria deve essere considerata
    		if(time_slots.get(i, -1)!=Color.RED){
    			
    			//la valutazione iniziale di un intervallo viene posta a 0,25
    			//(utile perché indica la probabilità di ripescare un intervallo non attivo)
    			float evaluation[] = new float[GeneralUtils.daysOfWeek];
    			
    			for(int j=0; j<GeneralUtils.daysOfWeek; j++){
    				evaluation[j]=0.25f;
    			}
    			
    			//stato di attivazione di un intervallo: se si è in una fascia oraria verde,
    			//tutti gli intervalli in essa sono attivi; se, invece, si è in una fascia
    			//gialla, tutti gli intervalli in essa non sono attivi con la possibilità
    			//di essere mutati (all'inizio la probabilità di mutazione è pari a 0,25)
    			boolean activationState[] = new boolean[GeneralUtils.daysOfWeek];
    			
    			if(time_slots.get(i,-1)==Color.GREEN){    				
    				for(int j=0; j<GeneralUtils.daysOfWeek; j++){
    					activationState[j]=true;
    				}
    			}
    			else{ //Color.YELLOW
    				for(int j=0; j<GeneralUtils.daysOfWeek; j++){
    					activationState[j]=false;
    				}
    			}
    			
    			//si creano gli intervalli da 5 minuti all'interno della fascia oraria
    			for(int j=0; j<=50; j=j+5){
    				
    				Alarm start = new Alarm(i, j, 1, true, activationState, evaluation);
        			Alarm stop = new Alarm(i, j+5, 0, false, activationState, evaluation);
        			
        			alarmDao.createIfNotExists(start);
        			alarmDao.createIfNotExists(stop);
    			
        			System.out.println("Create intervals: HH:mm:ss START:" + start.get_hour()+ ":"
        					+ start.get_minute()+":"+start.get_second() + " STOP: " + stop.get_hour()+ ":"
        					+ stop.get_minute()+":"+stop.get_second());
        			
        			count++;
    			}
    			
    			alarmDao.createIfNotExists(new Alarm(i, 55, 1, true, activationState, evaluation));
    			
    			Alarm last_stop_time;
    			
    			//se si è in corripondenza dell'ultima fascia oraria della giornata, l'ultimo
    			//intervallo lo si fa durare un po' meno per non farlo coincidere con l'inizio
    			//del giorno successivo (perché per aggiornare la valutazione dell'intervallo
    			//c'è bisogno dell'indice del giorno corrente)
    			if(i==23){
    				last_stop_time=new Alarm(i, 59, 55, false, activationState, evaluation);
    			}
    			else{
    				last_stop_time=new Alarm(i+1, 0, 0, false, activationState, evaluation);
    			}
    			
    			alarmDao.createIfNotExists(last_stop_time);
    		
    			System.out.println("Create intervals: HH:mm:ss START:" + i+ ":"
    					+ 55+":"+1 + " STOP: " + last_stop_time.get_hour()+ ":"
    					+ last_stop_time.get_minute()+":"+last_stop_time.get_second());
    			count++;
    		}    		
    		
    		progress=progress+4;
    		task.doProgress(progress);    		
    	}
    	
    	System.out.println("Number of intervals: "+count);
    }
    
    
   /* 
    //forse non serve
    private static void saveAlarmInDB(Context context, Alarm alarm, int template_id){
    	    	
    	DbHelper helper = DbHelper.getInstance(context);
    	    	
    	//persists the alarm object to the database
    	helper.getAlarmDao().createIfNotExists(alarm);
    		
    	//the AlarmTimeTemplate object is created (by associating the alarm passed as a parameter and the template)
    	//and is saved into the database
    	helper.getAlarmTimeTemplateDao().createIfNotExists(new AlarmTimeTemplate(alarm, getTemplate(context, template_id)));
        	
    }
    */
    
    public static Alarm getAlarm(Context context, int alarm_id){  //filtrare per id utente
    	
    	return DbHelper.getInstance(context).getAlarmDao().queryForId(alarm_id);
    }
    
    
    public static List<Alarm> getAlarmsByActionType(Context context, boolean type){
    	
    	List<Alarm> alarms = null;
      	
    	RuntimeExceptionDao<Alarm, Integer> alarmDao = DbHelper.getInstance(context).getAlarmDao();    	
    	
    	
    	QueryBuilder<Alarm, Integer> queryBuilder = alarmDao.queryBuilder();

    	// get the WHERE object to build our query; the type field must be equal to 'type' parameter
    	try {
			alarms = queryBuilder.where().eq(Alarm.ACTION_TYPE_FIELD_NAME, type).query();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	
    	return alarms;
    	
    }
    
    /*
    public static TimeTemplate getTemplate(Context context, int template_id){ 
    	
    	return DbHelper.getInstance(context).getTimeTemplateDao().queryForId(template_id);
    }


    public static List<Alarm> lookupAlarmsForTemplate(Context context, TimeTemplate template) {
    	
    	List<Alarm> alarms = null;
    	
    	DbHelper helper = DbHelper.getInstance(context);
    	    	
    	try{
    		if (alarmsForTemplateQuery == null) {
    			alarmsForTemplateQuery = makeAlarmsForTemplateQuery(helper);
    		}
    		alarmsForTemplateQuery.setArgumentHolderValue(0, template);
    		alarms=helper.getAlarmDao().query(alarmsForTemplateQuery);    		
    	} catch (SQLException e) {
			e.printStackTrace();
		}  
    	
    	return alarms;
    }
    
    
    
	private static PreparedQuery<Alarm> makeAlarmsForTemplateQuery(DbHelper dbh) throws SQLException {
		// build our inner query for AlarmTimeTemplate objects
		QueryBuilder<AlarmTimeTemplate, Integer> alarmTemplateQb = dbh.getAlarmTimeTemplateDao().queryBuilder();
		// just select the alarm-id field
		alarmTemplateQb.selectColumns(AlarmTimeTemplate.ALARM_ID_FIELD_NAME);
		SelectArg timetemplateSelectArg = new SelectArg();

		alarmTemplateQb.where().eq(AlarmTimeTemplate.TIMETEMPLATE_ID_FIELD_NAME, timetemplateSelectArg);

		// build our outer query for Alarm objects
		QueryBuilder<Alarm, Integer> alarmQb = dbh.getAlarmDao().queryBuilder();
		// where the id matches in the alarm-id from the inner query
		alarmQb.where().in(Alarm.ID_FIELD_NAME, alarmTemplateQb);
		return alarmQb.prepare();
	}
	
	*/
	
	
	
    public static List<Alarm> getAllAlarms(Context context) { //filtrare per id utente
    	
    	DbHelper helper = DbHelper.getInstance(context);
    	    	
    	return helper.getAlarmDao().queryForAll();  
    }
	

    /**
     * Sets up the next alarm; it is also called to initialize the first alarm.
     * @param context context of the application.
     * @param alarms list of all alarms saved in the database.
     * @param takeAllAlarms boolean indicating if the algorithm have to take all the alarms saved in the database.
     * @param prevAlarmNotAvailable boolean indicating if the previous alarm is no longer available.
     * @param current_alarm_id id of the current alarm, previously set.
     */
	@SuppressLint("NewApi") public static void setNextAlarm(Context context, List<Alarm> alarms, boolean takeAllAlarms, boolean prevAlarmNotAvailable, int current_alarm_id){
		//questo metodo serve per settare il prossimo alarm dopo averne consumato uno nell'apposito
		//receiver; è chiamato inizialmente per	inizializzare il primo alarm
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		RuntimeExceptionDao<Alarm, Integer> alarmDao = DbHelper.getInstance(context).getAlarmDao();    	
		
		//si usa l'indice artificiale per il test dell'algoritmo (altrimenti l'indice del
		//giorno si può ricavare dalla data corrente) 
		int artificialIndex = prefs.getInt("artificialDayIndex", 0);//context.getSharedPreferences("appPrefs", 0).getInt("artificialDayIndex", 0);
		
		
		int alarm_artificial_day_index=0;
		
		
		if(ClimbApplication.logEnabled){
			Log.d(MainActivity.AppName, "AlarmUtils - SetNextAlarm: list size " + alarms.size());
			Log.d(MainActivity.AppName, "AlarmUtils - SetNextAlarm: list elements");
			for (Alarm e : alarms) {		    
				Log.d(MainActivity.AppName,"Alarm id: " + e.get_id() + " - hms: " + e.get_hour() + "," + e.get_minute() + "," + e.get_second());			
			}
		}
		
		
		Alarm nextAlarm=null;
		
		/*
		//prima si ordina la lista di alarm per orario (perché magari all'inizio 
		//gli alarm non sono stati inseriti in ordine di orario; utile anche per il 
		//fatto di poterne aggiungere/togliere in futuro senza doversi preoccupare
		//del loro ordinamento)
		//FORSE DA TOGLIERE SE SI INSERISCONO GLI ALARM GIA' PRONTI
		Collections.sort(alarms,new AlarmComparator());
		
		
		if(MainActivity.logEnabled){
			Log.d(MainActivity.AppName, "AlarmUtils - SetNextAlarm: lista dopo collection sort");
			for (Alarm e : alarms) {		    
				Log.d(MainActivity.AppName,"Alarm id: " + e.get_id() + " - hms: " + e.get_hour() + "," + e.get_minute() + "," + e.get_second());			
			}
		}*/
		
		
		
		Calendar now = Calendar.getInstance();
		Calendar alarmTime = (Calendar) now.clone();//Calendar.getInstance();	
		//DA RIPRISTINARE CON SETTIMANA DI 7 GIORNI
		//int today = alarmTime.get(Calendar.DAY_OF_WEEK)-1;
		
		
		boolean stop=false;		
				
		//se si è al primo avvio dell'applicazione o all'evento di boot del device, allora
		//per cercare il prossimo alarm si considera tutta la lista di alarm e si sceglie
		//il primo che risulta valido per essere lanciato nel giorno corrente (o in uno
		//successivo); in tal caso si esegue una ricerca binaria
		if(takeAllAlarms){ 
			
			int begin = 0;
			int end = alarms.size()-1;
			int center = 0;			
			//indice per l'id dell'alarm restituito dalla ricerca binaria (final_index>0 se
			//restituisce un alarm, in quanto gli id partono da 1)
			int final_index=0; 
			
			while(begin <= end){
				
				//indice dell'elemento della lista da recuperare
				center = (begin+end)/2;
				
				//si ottiene l'alarm
				Alarm e = alarms.get(center);	
				
				//si impostano ora, minuti e secondi prendendo tali parametri dall'alarm salvato
				alarmTime.set(Calendar.HOUR_OF_DAY, e.get_hour());
				alarmTime.set(Calendar.MINUTE, e.get_minute());
				alarmTime.set(Calendar.SECOND, e.get_second());
				
				if(alarmTime.before(now)){ 
					begin=center+1;
				}
				else{
					nextAlarm=e;
					end=center-1;
					final_index=nextAlarm.get_id();
					
					Log.d(MainActivity.AppName,"Binary search - next alarm id: " +nextAlarm.get_id()+" end index: "+end);
				}
			}
			
			
			if(nextAlarm!=null){
				//se si arriva qui significa che la ricerca binaria precedente ha trovato
				//un alarm con tempo di inizio valido per il giorno corrente
				
				//ora si controlla se questo alarm è attivo nel giorno corrente;
				//se questo è il caso significa che è attivato anche l'altro alarm a lui
				//associato (perchè una coppia di alarm (start,stop) definisce un intervallo:
				//quando un alarm di start viene attivato (disattivato) viene attivato
				//(disattivato) anche l'alarm di stop successivo)
				
				
				if(isLastListenedIntervalFarEnough(context, nextAlarm, prevAlarmNotAvailable,0)){
					
					//se l'alarm non è attivato per questo giorno e se esso è di start, vuol dire
					//che il relativo intervallo non è stato attivato per questo giorno					
					if(!nextAlarm.getRepeatingDay(artificialIndex)){
						
						////////////////////////////
						//utile per scrivere il LOG					
						String status="";							
						if(nextAlarm.isStepsInterval(artificialIndex)){
							//status="Intervallo con scalini non attivo";
							status="S,0";
						}
						else{
							//status="Intervallo di esplorazione non attivo";
							status="E,0";
						}			
												
						////////////////////////////
						int id_this_alarm=nextAlarm.get_id();				
						
						//se è un alarm di start
						if(nextAlarm.get_actionType()){
							
							System.out.println("next alarm non attivo e di start");
													
							//si prova ad effettuare la mutazione, attivando l'intervallo
							if(!intervalMutated(nextAlarm, alarms, artificialIndex, alarmDao,context)){
								
								//si scrive nel file di log che questo intervallo non è stato
								//mutato e, quindi, non viene valutato
								
								////////////////////////////
								//utile per scrivere il LOG	
								/*if(id_this_alarm==1){
									int month = alarmTime.get(Calendar.MONTH)+1;
									LogUtils.writeLogFile(context,"Indice giorno: "+artificialIndex+" - "+alarmTime.get(Calendar.DATE)+"/"+month+"/"+alarmTime.get(Calendar.YEAR));
								}*/
								
								//si ottiene il relativo alarm di stop (esiste sicuramente)
								Alarm next_stop= getAlarm(context, id_this_alarm+1); 							
								
								/*
								LogUtils.writeLogFile(context,status+": " + nextAlarm.get_hour()+":"+nextAlarm.get_minute()+
										":"+nextAlarm.get_second()+" - "+next_stop.get_hour()+":"+next_stop.get_minute()+
										":"+next_stop.get_second()+ " | Non valutato perche' non mutato | "+
										status+" la prossima settimana");
								*/
								LogUtils.writeIntervalStatus(context, artificialIndex, nextAlarm, next_stop, "|"+status+";NM;-;"+status);
								
								////////////////////////////
								
								nextAlarm=null;							
							}
						}
						else{ //se l'alarm non è attivo ed è di stop, si deve cercare un
							  //altro intervallo
							
							//si scrive nel file di log che questo intervallo non è stato mutato
							//(in quanto l'inizio dell'intervallo è avvenuto a device spento o
							//prima della configurazione dell'algoritmo) e, quindi, non viene
							//valutato
							
							////////////////////////////
							//utile per scrivere il LOG	
							/*if(id_this_alarm==2){
								int month = alarmTime.get(Calendar.MONTH)+1;
								LogUtils.writeLogFile(context,"Indice giorno: "+artificialIndex+" - "+alarmTime.get(Calendar.DATE)+"/"+month+"/"+alarmTime.get(Calendar.YEAR));
							}*/
							
							//si ottiene il relativo alarm di start (esiste sicuramente)
							Alarm prev_start= getAlarm(context, id_this_alarm-1); 
							
							//String extra_str="a device spento)"; //ad algoritmo non attivo
							String extra_str="NA";
							if(!prevAlarmNotAvailable){
								//extra_str="ad algoritmo non ancora configurato)";
								extra_str="NC";
							}
							/*
							LogUtils.writeLogFile(context,status+": " + prev_start.get_hour()+":"+prev_start.get_minute()+
									":"+prev_start.get_second()+" - "+nextAlarm.get_hour()+":"+nextAlarm.get_minute()+
									":"+nextAlarm.get_second()+ " | Non valutato (mutazione non tentata a causa di inizio intervallo "+
									extra_str+ " | "+ status+" la prossima settimana");
							*/
							LogUtils.writeIntervalStatus(context, artificialIndex, prev_start, nextAlarm, "|"+status+";-("+extra_str+");-;"+status);
							
							////////////////////////////
							
							nextAlarm=null;
						}
					}
				}
				else{					
					////////////////////////////
					//utile per scrivere il LOG					
					writeSkippedInterval(context, nextAlarm, artificialIndex, 0);
					////////////////////////////
					
					nextAlarm=null;
				}
			}
			//se l'alarm trovato non è attivo per il giorno corrente, allora si prova a
			//cercarne un altro partendo dall'alarm successivo; gli alarm che nella lista
			//vengono dopo a quello trovato hanno tutti tempo di inizio valido per il
			//giorno corrente
			if(nextAlarm==null && final_index!=0){
				
				//si prende l'alarm successivo a quello trovato
				for(int i=final_index; i<alarms.size() && !stop; i++){
					
					Alarm e = alarms.get(i);					
					
					/////////
					//PER TEST ALGORITMO: l'indice del giorno corrente è impostato "artificialmente",
					//in quanto lo deve rappresentare all'interno della settimana "corta";
					//normalmente l'indice è dato dalla data corrente: e.getRepeatingDay(today)
					/////////
					
					
					if(isLastListenedIntervalFarEnough(context, e, prevAlarmNotAvailable,0)){
						
						if(e.getRepeatingDay(artificialIndex)){
							//l'alarm è attivato per questo giorno
							//cio' significa che è attivato anche l'altro alarm a lui associato (perchè una
							//coppia di alarm (start,stop) definisce un intervallo: quando un alarm di start
							//viene attivato (disattivato) viene attivato (disattivato) anche l'alarm di stop
							//successivo)
							nextAlarm=e;
							stop=true;
						}
						else{//l'alarm non è attivato per questo giorno;
							//se questo è un alarm di start vuol dire che il relativo intervallo 
							//non è stato attivato per questo giorno	
							if(e.get_actionType()){					
								//è un alarm di start
										
								//si prova ad effettuare la mutazione, attivando l'intervallo
								stop=intervalMutated(e, alarms, artificialIndex, alarmDao,context);
								if(stop){
									nextAlarm=e;
								}
								////////////////////////////
								//LOG
								else{ //l'intervallo non è mutato
									
									//si scrive nel file di log che questo intervallo non è mutato
									//e, quindi, non viene valutato
									String status="";							
									if(e.isStepsInterval(artificialIndex)){
										//status="Intervallo con scalini non attivo";
										status="S,0";
									}
									else{
										//status="Intervallo di esplorazione non attivo";
										status="E,0";
									}			
									
									int id_start=e.get_id();
									
									/*if(id_start==1){
										int month = alarmTime.get(Calendar.MONTH)+1;
										LogUtils.writeLogFile(context,"Indice giorno: "+artificialIndex+" - "+alarmTime.get(Calendar.DATE)+"/"+month+"/"+alarmTime.get(Calendar.YEAR));
									}*/
									
									//si ottiene il relativo alarm di stop (esiste sicuramente)
									Alarm next_stop= getAlarm(context, id_start+1); 							
									/*
									LogUtils.writeLogFile(context,status+": " + e.get_hour()+":"+e.get_minute()+
											":"+e.get_second()+" - "+next_stop.get_hour()+":"+next_stop.get_minute()+
											":"+next_stop.get_second()+ " | Non valutato perche' non mutato | "+
											status+" la prossima settimana");
									*/
									LogUtils.writeIntervalStatus(context, artificialIndex, e, next_stop, "|"+status+";NM;-;"+status);								
									
								}
								////////////////////////////
							}
						}
					}
					else{
						////////////////////////////
						//utile per scrivere il LOG					
						writeSkippedInterval(context, e, artificialIndex, 0);
						////////////////////////////
					}
				}
			}				
			
			/*
			for(int i=0; i<alarms.size() && !stop; i++){
			
				Alarm e = alarms.get(i);
				
				//Calendar alarmTime=Calendar.getInstance();	
				//si impostano ora, minuti e secondi prendendo tali parametri dall'alarm salvato
				alarmTime.set(Calendar.HOUR_OF_DAY, e.get_hour());
				alarmTime.set(Calendar.MINUTE, e.get_minute());
				alarmTime.set(Calendar.SECOND, e.get_second());
				
				
				/////////
				//PER TEST ALGORITMO: l'indice del giorno corrente è impostato "artificialmente",
				//in quanto lo deve rappresentare all'interno della settimana "corta";
				//normalmente l'indice è dato dalla data corrente: e.getRepeatingDay(today)
				/////////
				
				
				//l'alarm è attivato per questo giorno
				//cio' significa che è attivato anche l'altro alarm a lui associato (perchè una
				//coppia di alarm (start,stop) definisce un intervallo: quando un alarm di start
				//viene attivato (disattivato) viene attivato (disattivato) anche l'alarm di stop
				//successivo)
				if(e.getRepeatingDay(artificialIndex)){
				
					if(alarmTime.after(now)){					
						//se si è arrivati qui vuol dire che l'alarm è attivo in questo
						//giorno della settimana ed è valido per essere lanciato e, quindi, 
						//lo si seleziona per essere il prossimo alarm ad essere lanciato								
						
						nextAlarm=e;
						stop=true;
					}
				}
				else{ //l'alarm non è attivato per questo giorno;
				    //se questo è un alarm di start vuol dire che il relativo intervallo 
					//non è stato attivato per questo giorno				
					
					//si controlla se è un alarm di start; solo in questo caso si può provare
					//ad eseguire la mutazione (intervallo che da 0 va a 1 con una certa
					//probabilità) in quanto per attivare un intervallo è necessario agire sulla
					//coppia di alarm (start,stop) che lo definisce
					if(e.get_actionType() && alarmTime.after(now)){					
						//è un alarm di start
						
						//si prova ad effettuare la mutazione, attivando l'intervallo
						stop=intervalMutated(e, alarms, i, artificialIndex, alarmDao);
						if(stop){
							nextAlarm=e;
						}
					}
				 }					
			}*/			
		}
		else{ //non si è al primo avvio dell'app o al boot del device
			  //dato un certo alarm che è stato consumato, il prossimo alarm che viene
			  //settato ha un id maggiore del precedente (infatti gli alarm sono ordinati
			  //per orario); quindi, per cercare il prossimo alarm si parte dall'id
			  //successivo a quello dell'alarm corrente (che ha id>=1, visto che nel database
			  //gli id autoincrementanti partono da 1)
					
			for(int i=current_alarm_id+1; i<=alarms.size() && !stop; i++){
				Alarm e = alarms.get(i-1);
				
				
				if(isLastListenedIntervalFarEnough(context, e, prevAlarmNotAvailable,0)){
					
					if(e.getRepeatingDay(artificialIndex)){
						nextAlarm=e;
						stop=true;
					}
					else{
						if(e.get_actionType()){
							//è un alarm di start						
							//si prova ad effettuare la mutazione, attivando l'intervallo
							stop=intervalMutated(e, alarms, artificialIndex, alarmDao, context);
							if(stop){
								nextAlarm=e;
							}
							///////////////////////////
							//LOG
							else{ //l'intervallo non è mutato
								  //si scrive nel file di log che questo intervallo non è mutato
								  //e, quindi, non viene valutato
								String status="";							
								if(e.isStepsInterval(artificialIndex)){
									//status="Intervallo con scalini non attivo";
									status="S,0";
								}
								else{
									//status="Intervallo di esplorazione non attivo";
									status="E,0";
								}			
								
								int id_start=e.get_id();
								
								/*if(id_start==1){
									int month = alarmTime.get(Calendar.MONTH)+1;
									LogUtils.writeLogFile(context,"Indice giorno: "+artificialIndex+" - "+alarmTime.get(Calendar.DATE)+"/"+month+"/"+alarmTime.get(Calendar.YEAR));
								}*/
								
								//si ottiene il relativo alarm di stop (esiste sicuramente)
								Alarm next_stop= getAlarm(context, id_start+1); 							
								/*
								LogUtils.writeLogFile(context,status+": " + e.get_hour()+":"+e.get_minute()+
										":"+e.get_second()+" - "+next_stop.get_hour()+":"+next_stop.get_minute()+
										":"+next_stop.get_second()+ " | Non valutato perche' non mutato | "+
										status+" la prossima settimana");
								*/
								LogUtils.writeIntervalStatus(context, artificialIndex, e, next_stop, "|"+status+";NM;-;"+status);						
								
							}
							////////////////////////////
						}							
					}					
				}
				else{
					////////////////////////////
					//utile per scrivere il LOG					
					writeSkippedInterval(context, e, artificialIndex, 0);
					////////////////////////////
				}
				
			}			
		}
		
		alarm_artificial_day_index=artificialIndex;
		
		//se nessun alarm scatta nel giorno corrente, allora si cerca un primo alarm che
		//scatta in un giorno successivo a questo (ci può essere anche il caso in cui tutti 
		//gli intervalli della settimana non sono attivi, ma grazie alla mutazione prima o
		//poi ne verrà attivato uno in un certo giorno)
		if(nextAlarm==null)	{
			
			System.out.println("Giorni successivi");
			
			
			//si resettano ora, minuti e secondi
			alarmTime.set(Calendar.HOUR_OF_DAY, 0);
			alarmTime.set(Calendar.MINUTE, 0);
			alarmTime.set(Calendar.SECOND, 0);
			
			/////////
			//PER TEST ALGORITMO: si inizializza l'indice artificiale 
			int currentIndex = artificialIndex;
			
			if(ClimbApplication.logEnabled){
				int al_m=alarmTime.get(Calendar.MONTH)+1;    	
				Log.d(MainActivity.AppName, "AlarmUtils - next alarm reset hms: h:m:s=" 
						+ alarmTime.get(Calendar.HOUR_OF_DAY)+":"+ alarmTime.get(Calendar.MINUTE)+":"+ alarmTime.get(Calendar.SECOND) +
						"  "+alarmTime.get(Calendar.DATE)+"/"+al_m+"/"+alarmTime.get(Calendar.YEAR));		    	
				Log.d(MainActivity.AppName, "AlarmUtils - milliseconds of the resetted alarm: " + alarmTime.getTimeInMillis());
			}
			/////////	
			int days_added=0;
			
			while(!stop){
				
				//si incrementa il giorno in quanto un opportuno alarm non è stato trovato nel
				//giorno precedente
				alarmTime.add(Calendar.DATE, 1);
				days_added++;
				
				if(ClimbApplication.logEnabled){
					int alr_m=alarmTime.get(Calendar.MONTH)+1;    	
					Log.d(MainActivity.AppName, "AlarmUtils - next alarm add 1 day: h:m:s=" 
							+ alarmTime.get(Calendar.HOUR_OF_DAY)+":"+ alarmTime.get(Calendar.MINUTE)+":"+ alarmTime.get(Calendar.SECOND) +
							"  "+alarmTime.get(Calendar.DATE)+"/"+alr_m+"/"+alarmTime.get(Calendar.YEAR));
					Log.d(MainActivity.AppName, "AlarmUtils - milliseconds of the alarm added 1 day: " + alarmTime.getTimeInMillis());
				}
				
				
				/////////	
				//PER TEST ALGORITMO: si aggiorna l'indice artificiale man mano che
				//si incrementa la data
				currentIndex=getNextDayIndex(currentIndex);
				/////////
				
				for(int i=0; i<alarms.size() && !stop; i++){
					
					Alarm e = alarms.get(i);
					
					/////////
					//PER TEST ALGORITMO: si fa sempre riferimento all'indice artificiale;
					//normalmente: e.getRepeatingDay(alarmTime.get(Calendar.DAY_OF_WEEK)-1)
					/////////
					
					
					if(isLastListenedIntervalFarEnough(context, e, prevAlarmNotAvailable, days_added)){
						
						//gli alarm hanno un istante di inizio sicuramente > di ora in quanto
						//si stanno cercando in un giorno successivo a quello corrente
						
						if(e.getRepeatingDay(currentIndex)){ 
							nextAlarm=e;
							stop=true;
						}
						else{						
							if(e.get_actionType()){					
								//è un alarm di start
								
								//si prova ad effettuare la mutazione, attivando l'intervallo
								stop=intervalMutated(e, alarms, currentIndex, alarmDao, context);
								if(stop){
									nextAlarm=e;
								}
								////////////////////////////
								//LOG
								else{ //l'intervallo non è mutato
									  //si scrive nel file di log che questo intervallo non è mutato
									  //e, quindi, non viene valutato
									String status="";							
									if(e.isStepsInterval(currentIndex)){
										//status="Intervallo con scalini non attivo";
										status="S,0";
									}
									else{
										//status="Intervallo di esplorazione non attivo";
										status="E,0";
									}			
									
									int id_start=e.get_id();
									
									/*if(id_start==1){
										int month = alarmTime.get(Calendar.MONTH)+1;
										LogUtils.writeLogFile(context,"Indice giorno: "+currentIndex+" - "+alarmTime.get(Calendar.DATE)+"/"+month+"/"+alarmTime.get(Calendar.YEAR));
									}*/
									
									//si ottiene il relativo alarm di stop (esiste sicuramente)
									Alarm next_stop= getAlarm(context, id_start+1); 							
									/*
									LogUtils.writeLogFile(context,status+": " + e.get_hour()+":"+e.get_minute()+
											":"+e.get_second()+" - "+next_stop.get_hour()+":"+next_stop.get_minute()+
											":"+next_stop.get_second()+ " | Non valutato perche' non mutato | "+
											status+" la prossima settimana");	
									*/
									LogUtils.writeIntervalStatus(context, currentIndex, e, next_stop, "|"+status+";NM;-;"+status);						
									
								}
								////////////////////////////
							}
						}						
					}
					else{
						////////////////////////////
						//utile per scrivere il LOG					
						writeSkippedInterval(context, e, currentIndex, days_added);
						////////////////////////////
					}
				}
			}
			
			alarm_artificial_day_index=currentIndex;
		}
						
		//si impostano ora, minuti e secondi prendendo tali parametri dall'alarm selezionato;
		//quest'ultimo sarà il prossimo alarm che verrà lanciato attraverso l'alarm manager
		alarmTime.set(Calendar.HOUR_OF_DAY, nextAlarm.get_hour());
		alarmTime.set(Calendar.MINUTE, nextAlarm.get_minute());
		alarmTime.set(Calendar.SECOND, nextAlarm.get_second());
		
		if(ClimbApplication.logEnabled){
			int month=alarmTime.get(Calendar.MONTH)+1;	
			Log.d(MainActivity.AppName, "AlarmUtils - NEXT ALARM: id=" + nextAlarm.get_id() + "  h:m:s=" 
					+ alarmTime.get(Calendar.HOUR_OF_DAY)+":"+ alarmTime.get(Calendar.MINUTE)+":"+ alarmTime.get(Calendar.SECOND) +
					"  "+alarmTime.get(Calendar.DATE)+"/"+month+"/"+alarmTime.get(Calendar.YEAR));		
			Log.d(MainActivity.AppName, "AlarmUtils - MILLISECONDS OF THE NEXT ALARM: " + alarmTime.getTimeInMillis());		
		}		
		
		//nell'oggetto shared preferences si imposta l'id del prossimo alarm e degli interi che indicano il giorno, il mese e 
		//l'anno in cui scatta (ora, minuti e secondi sono all'interno dell'oggetto Alarm)
		Editor editor = prefs.edit();	
		//si imposta l'id del prossimo alarm nelle preferenze
		editor.putInt("alarm_id", nextAlarm.get_id());
		//si impostano giorno, mese e anno dell'alarm
		editor.putInt("alarm_date", alarmTime.get(Calendar.DATE));
		editor.putInt("alarm_month", alarmTime.get(Calendar.MONTH));
		editor.putInt("alarm_year", alarmTime.get(Calendar.YEAR));   
		
		/////////				
		//PER TEST ALGORITMO 
		editor.putInt("alarm_artificial_day_index", alarm_artificial_day_index);
		//indice del giorno quando verrà lanciato l'alarm (normalmente, l'indice è
		//ottenuto dalla data dell'alarm)
		///////// 
		
		//si salvano le credenziali
		editor.commit();    	    	
		
		
		//prevAlarmNotAvailable==true: questo metodo è stato chiamato dopo il completamento
		//del boot perché l'alarm precedentemente impostato non è più valido; dopo aver
		//trovato un nuovo alarm, se quest'ultimo è di stop significa che si è all'interno di
		//un nuovo intervallo attivo: si fa ripartire il classificatore Google/scalini
		if((prevAlarmNotAvailable || current_alarm_id==-1) && !nextAlarm.get_actionType()){
						 
			//si resettano i valori relativi alle attività/scalini rilevati in precedenza			
			ActivityRecognitionIntentService.clearValuesCount(prefs);
			StairsClassifierReceiver.clearStepsNumber(prefs);	
			//si resetta il valore salvato nelle preferences che indica se l'ultimo
		   	//intervallo considerato ha avuto almeno un periodo di gioco con scalini
			prefs.edit().putBoolean("last_interval_with_steps", false).commit();
			
			//è un "intervallo di esplorazione"
			if(!nextAlarm.isStepsInterval(artificialIndex)){ //normalmente alarmTime.get(Calendar.DAY_OF_WEEK))-1
					
				context.startService(new Intent(context, ActivityRecognitionRecordService.class));
				//si registra anche il receiver per la registrazione dell'attività utente
				//context.getApplicationContext().registerReceiver(userMotionReceiver, userMotionFilter);
				//context.getPackageManager().setComponentEnabledSetting(new ComponentName(context, UserMotionReceiver.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
			}				
			else{ //è un "intervallo con scalini"
				
				context.getApplicationContext().startService(new Intent(context, SamplingClassifyService.class));
				//si registra anche il receiver
				context.getPackageManager().setComponentEnabledSetting(new ComponentName(context, StairsClassifierReceiver.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);					
			}
			
			////////////////////////////
			//utile per scrivere il LOG
			if(nextAlarm.get_id()!=current_alarm_id+1){			
				System.out.println("prev alarm not valid - l'intervallo NON è l'ultimo considerato");
				prefs.edit().putBoolean("next_alarm_mutated", false).commit();				
			}
			////////////////////////////			
		}
		
		
		//si crea il pending intent creando dapprima un intent con tutti i dati dell'alarm
		//per identificarlo in modo univoco
		PendingIntent pi = createPendingIntent(context, nextAlarm, new int[]{alarmTime.get(Calendar.DATE), alarmTime.get(Calendar.MONTH), alarmTime.get(Calendar.YEAR)});
		
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		
		if(Build.VERSION.SDK_INT < 19){
			alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), pi);
		}
		else{
			//se nel sistema sta eseguendo una versione di Android con API >=19
    		//allora è necessario invocare il metodo setExact
			alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), pi);
		}
		
		
		if(ClimbApplication.logEnabled){
			Log.d(MainActivity.AppName,"AlarmUtils - pending intent for next alarm set in the alarm manager");		
		}	
	}
	
	//non cancello le preferences perché vengono sovrascritte con i dati del prossimo alarm
	public static void cancelAlarm(Context context, Alarm alarm) {
		 
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);//context.getSharedPreferences("appPrefs", 0);
		 			 
		PendingIntent pIntent = createPendingIntent(context, alarm, new int[]{pref.getInt("alarm_date", -1),pref.getInt("alarm_month", -1),pref.getInt("alarm_year", -1)});	
	    	
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(pIntent);
	}
	 
	 
	public static PendingIntent createPendingIntent(Context context, Alarm alarm, int[] params) {
		 
		Intent intent = new Intent();
	 	    
		//si imposta il tipo di action dell'intent a seconda se è un alarm per far iniziare o
		//finire l'intervallo
		if(alarm.get_actionType()){
			intent.setAction("org.unipd.nbeghin.climbtheworld.INTERVAL_START");
		}
		else{
			intent.setAction("org.unipd.nbeghin.climbtheworld.INTERVAL_STOP");
		}
	 	    
		intent.putExtra("id", alarm.get_id());
		intent.putExtra("hour", alarm.get_hour());
		intent.putExtra("minute", alarm.get_minute());
		intent.putExtra("second", alarm.get_second());
		intent.putExtra("date", params[0]);
		intent.putExtra("month", params[1]);
		intent.putExtra("year", params[2]);
	 	 
		return PendingIntent.getBroadcast(context, alarm.get_id(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}
	 
	
	
	private static boolean intervalMutated(Alarm a_start, List<Alarm> alarms, 
			int current_day_index, RuntimeExceptionDao<Alarm, Integer> alarmDao, Context context){
		
		//la probabilità di attivare l'intervallo è data dalla sua valutazione v
		//(0 <= v <= valore_soglia) 
		float probability = a_start.getEvaluation(current_day_index);
						
		//se la valutazione è 0 o quasi, si pone la probabilità a 0.1
		if(probability<0.1f){						
			probability=0.1f;
		}
		
		int id_start=a_start.get_id();
		int id_stop=id_start+1;
		Log.d(MainActivity.AppName,"Mutation " + id_start + "-" + id_stop + " - probability: " + probability);	
		
		//con una certa probabilità si attiva l'intervallo (la coppia di alarm
		//start-stop)
		float nn = rand.nextFloat();
		Log.d(MainActivity.AppName,"Mutation - rand: " + nn);
		
		if(nn <= probability){
			
			Log.d(MainActivity.AppName,"Set next alarm - interval " + id_start + "-" + id_stop + " mutated");		
			
			a_start.setRepeatingDay(current_day_index, true);
			a_start.setStepsInterval(current_day_index, false);						
			//si recupera dalla lista il relativo alarm di stop 
			//(c'è sicuramente visto che un alarm di start deve essere seguito
			//dal suo alarm di stop)
			Alarm a_stop = alarms.get(id_start);//'id_start' perché indice_lista = alarm_id-1 
			System.out.println("Alarm stop: id: "+a_stop.get_id()+" index: "+id_start);
			a_stop.setRepeatingDay(current_day_index, true);
			a_stop.setStepsInterval(current_day_index, false);
			//la valutazione dell'intervallo verrà aggiornata quando lo si esplorerà
			
			//si salvano le modifiche anche nel db
			alarmDao.update(a_start);
			alarmDao.update(a_stop);
			
			////////////////////////////
			//utile per scrivere il LOG
			PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("next_alarm_mutated", true).commit();
			////////////////////////////	
			
			return true;
		}
		
		//altrimenti l'intervallo (coppia alarm start-stop) rimane disattivato (0)
		return false;		
	}
	
	
	
	private static boolean isPreviousIntervalListened(Context context, Alarm current_alarm, boolean prevAlarmNotAvailable, int days_number_to_add){
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		//si recupera l'id dell'alarm di stop dell'ultimo intervallo valutato
		int last_evaluated_stop_alarm_id = prefs.getInt("last_evaluated_interval_stop_id",-1);
		
		//se il metodo di set alarm è stato chiamato a causa del fatto che l'alarm precedentemente impostato
		//non è più valido allora significa che l'alarm precedente si è concluso senza essere valutato;
		//se non è mai stato valutato alcun intervallo allora si esce subito
		if(prevAlarmNotAvailable || last_evaluated_stop_alarm_id==-1){
			return false;
		}
		
		//se l'alarm corrente è di start, l'alarm di stop dell'ultimo intervallo valutato deve essere
		//il precedente
		int interval_id_diff = 1;
		//se l'alarm corrente è di stop, l'alarm di stop dell'ultimo intervallo valutato deve distare 2 id
		if(!current_alarm.get_actionType()){	
			interval_id_diff = 2;
		}

		//se l'intervallo corrente e l'ultimo intervallo valutato hanno id contigui si 
		//controlla se sono vicini anche per quanto riguarda il tempo che intercorre tra loro
		if(current_alarm.get_id()-last_evaluated_stop_alarm_id==interval_id_diff){
			
			//si recupera l'alarm di stop dell'ultimo intervallo valutato
			Alarm last_evaluated=getAlarm(context, last_evaluated_stop_alarm_id);
			
			Calendar current_alarm_time = Calendar.getInstance();
			Calendar last_interval_time = (Calendar) current_alarm_time.clone();
			
			if(days_number_to_add>0){
				current_alarm_time.add(Calendar.DATE, days_number_to_add);
			}
			int current_h=current_alarm.get_hour();
			int current_m=current_alarm.get_minute();
			current_alarm_time.set(Calendar.HOUR_OF_DAY, current_h);
			current_alarm_time.set(Calendar.MINUTE, current_m);
			current_alarm_time.set(Calendar.SECOND, current_alarm.get_second());
			last_interval_time.set(Calendar.HOUR_OF_DAY, last_evaluated.get_hour());
			last_interval_time.set(Calendar.MINUTE, last_evaluated.get_minute());
			last_interval_time.set(Calendar.SECOND, last_evaluated.get_second());
			last_interval_time.set(Calendar.DATE, prefs.getInt("last_evaluated_interval_alarm_date", -1));
			last_interval_time.set(Calendar.MONTH, prefs.getInt("last_evaluated_interval_alarm_month", -1));
			last_interval_time.set(Calendar.YEAR, prefs.getInt("last_evaluated_interval_alarm_year", -1));
			
			//differenza di tempo tra i due alarm
			long time_diff = current_alarm_time.getTime().getTime() - last_interval_time.getTime().getTime();
			
			//se l'alarm corrente è di start, la differenza tra esso e il precedente alarm di stop che
			//definisce l'intervallo ascoltato deve essere pari a 1 secondo (in tal modo l'ultimo ascoltato
			//è quello immediatamente precedente)
						
			long target_time_diff = 1000;
			//se l'alarm corrente è di stop, la differenza deve essere pari a 5 minuti (o 4 minuti e 55 secondi
			//se l'alarm corrente è l'ultimo della giornata, quello delle 23:59:55)
			if(!current_alarm.get_actionType()){
				if(current_h==23 && current_m==59){
					target_time_diff=295000; //4 minuti e 55 secondi
				}
				else{
					target_time_diff=300000; //5 minuti
				}
			}
			
			//se i due intervalli sono vicini anche per quanto riguarda il tempo, allora l'ultimo 
			//intervallo ascoltato precede immediatamente quello corrente
			if(time_diff==target_time_diff){
				return true;
			}
			return false;			
		}
		return false;	
	}
	
	
	private static boolean isNextIntervalActive(Context context, Alarm current_alarm, int current_day_index){
				
		//se l'alarm corrente è di stop, l'id di start del prossimo intervallo è quello successivo; il
		//tempo che intercorre tra i due alarm deve essere pari a 1 secondo
		int next_id=current_alarm.get_id()+1;		
		long target_time_diff = 1000;
		//se l'alarm corrente è di start, l'id di start dell'intervallo successivo dista 2 lunghezze da
		//quello corrente; il tempo che intercorre tra i due alarm deve essere pari a 5 minuti
		if(current_alarm.get_actionType()){
			next_id=current_alarm.get_id()+2;	
			target_time_diff=300000; //5 minuti
		}
		
		//si recupera il prossimo alarm
		Alarm next_alarm=getAlarm(context, next_id);
		//se il prossimo alarm esiste ed è attivo per il giorno corrente, si controlla che questo sia
		//consecutivo all'alarm corrente
		if(next_alarm!=null && next_alarm.getRepeatingDay(current_day_index)){
			
			Calendar current_alarm_time = Calendar.getInstance();
			Calendar next_interval_time = (Calendar) current_alarm_time.clone();
			int current_h=current_alarm.get_hour();
			int current_m=current_alarm.get_minute();
			current_alarm_time.set(Calendar.HOUR_OF_DAY, current_h);
			current_alarm_time.set(Calendar.MINUTE, current_m);
			current_alarm_time.set(Calendar.SECOND, current_alarm.get_second());
			next_interval_time.set(Calendar.HOUR_OF_DAY, next_alarm.get_hour());
			next_interval_time.set(Calendar.MINUTE, next_alarm.get_minute());
			next_interval_time.set(Calendar.SECOND, next_alarm.get_second());
			
			//differenza di tempo tra i due alarm
			long time_diff = next_interval_time.getTime().getTime() - current_alarm_time.getTime().getTime();
			
			//si ritorna 'true' se il prossimo intervallo è attivo e viene immediatamente dopo in ordine
			//di tempo rispetto all'alarm corrente
			if(time_diff==target_time_diff){
				return true;
			}
			return false;			
		}		
		return false;
	}
	
	
	private static boolean isLastListenedIntervalFarEnough(Context context, Alarm current_alarm, boolean prevAlarmNotAvailable, int days_number_to_add){
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		//si recupera l'id dell'alarm di stop dell'ultimo intervallo valutato
		int last_evaluated_stop_alarm_id = prefs.getInt("last_evaluated_interval_stop_id",-1);
		
		//se il metodo di set alarm è stato chiamato a causa del fatto che l'alarm precedentemente impostato
		//non è più valido allora significa che l'alarm precedente si è concluso senza essere valutato;
		//se non è mai stato valutato alcun intervallo allora si esce subito
		if(prevAlarmNotAvailable || last_evaluated_stop_alarm_id==-1){
			Log.d(MainActivity.AppName, "AlarmUtils - filtro bilanciamento energetico: OK (non più valido o nessuno ancora valutato)");
			return true;
		}
				
		long target_time_diff = 600000; 
		
		//si recupera l'alarm di stop dell'ultimo intervallo valutato
		Alarm last_evaluated=getAlarm(context, last_evaluated_stop_alarm_id);	
		
		Calendar current_alarm_time = Calendar.getInstance();
		Calendar last_interval_time = (Calendar) current_alarm_time.clone();
		
		if(days_number_to_add>0){
			current_alarm_time.add(Calendar.DATE, days_number_to_add);
		}
		current_alarm_time.set(Calendar.HOUR_OF_DAY, current_alarm.get_hour());
		current_alarm_time.set(Calendar.MINUTE, current_alarm.get_minute());
		current_alarm_time.set(Calendar.SECOND, current_alarm.get_second());
		
		int last_val_hh=last_evaluated.get_hour();
		int last_val_mm=last_evaluated.get_minute();
		
		if(last_val_hh==23 && last_val_mm==59){
			target_time_diff+=5000;
		}
		
		last_interval_time.set(Calendar.HOUR_OF_DAY, last_val_hh);
		last_interval_time.set(Calendar.MINUTE, last_val_mm);
		last_interval_time.set(Calendar.SECOND, last_evaluated.get_second());
		last_interval_time.set(Calendar.DATE, prefs.getInt("last_evaluated_interval_alarm_date", -1));
		last_interval_time.set(Calendar.MONTH, prefs.getInt("last_evaluated_interval_alarm_month", -1));
		last_interval_time.set(Calendar.YEAR, prefs.getInt("last_evaluated_interval_alarm_year", -1));
				
		//differenza di tempo tra i due alarm
		long time_diff = current_alarm_time.getTime().getTime() - last_interval_time.getTime().getTime();
		
		if(ClimbApplication.logEnabled){
			int alr_m=current_alarm_time.get(Calendar.MONTH)+1;  
			int last_m=last_interval_time.get(Calendar.MONTH)+1;  
			Log.d(MainActivity.AppName, "AlarmUtils - filtro bilanciamento energetico: CURR:" 
					+ current_alarm_time.get(Calendar.HOUR_OF_DAY)+":"+ current_alarm_time.get(Calendar.MINUTE)+":"+ current_alarm_time.get(Calendar.SECOND) +
					"  "+current_alarm_time.get(Calendar.DATE)+"/"+alr_m+"/"+current_alarm_time.get(Calendar.YEAR)+
					"; LAST: "+ last_interval_time.get(Calendar.HOUR_OF_DAY)+":"+ last_interval_time.get(Calendar.MINUTE)+":"+ 
					last_interval_time.get(Calendar.SECOND) + "  "+last_interval_time.get(Calendar.DATE)+"/"+last_m+"/"+
					last_interval_time.get(Calendar.YEAR));
		}		
		
		//se il tempo che intercorre tra la fine dell'ultimo intervallo valutato e l'intervallo
		//corrente è > 10 minuti (si saltano 2 intervalli) allora si ritorna 'true'
		if(time_diff > target_time_diff){
			Log.d(MainActivity.AppName, "AlarmUtils - filtro: diff > 10 minuti: " +time_diff+" OK");
			return true;			
		}
		Log.d(MainActivity.AppName, "AlarmUtils - filtro: diff <= 10 minuti: " +time_diff+" NO");
		return false;
	}
	
	
	
	public static Alarm secondInterval(Context context, Alarm current_alarm, boolean next){
		
		if(current_alarm==null){
			return null;
		}
		
		//in input si ha l'alarm di stop (se si vuole controllare se esiste l'intervallo
		//successivo) o di start (se si vuole controllare se esiste l'intervallo precedente)
		//dell'intervallo preso in considerazione; 
		//l'alarm qui ottenuto può essere di stop quando si vuole controllare che l'intervallo 
		//precedente (come id) venga immediatamente prima in ordine di tempo rispetto all'intervallo
		//preso in esame; è di start quando si vuole controllare che l'intervallo successivo venga
		//immediatamente dopo in ordine di tempo
		Alarm second_alarm = null;
		
		if(next){
			second_alarm = getAlarm(context, current_alarm.get_id()+1);
		}
		else{
			second_alarm = getAlarm(context, current_alarm.get_id()-1);
		}
		
		
		if(second_alarm!=null){
			//se quest'ultimo esiste, si controlla se tale l'intervallo viene immediatamente prima 
			//o dopo in ordine di tempo rispetto all'intervallo corrente
			Calendar current_alarm_time = Calendar.getInstance();
			Calendar second_alarm_time = (Calendar) current_alarm_time.clone();
			current_alarm_time.set(Calendar.HOUR_OF_DAY, current_alarm.get_hour());
			current_alarm_time.set(Calendar.MINUTE, current_alarm.get_minute());
			current_alarm_time.set(Calendar.SECOND, current_alarm.get_second());
			second_alarm_time.set(Calendar.HOUR_OF_DAY, second_alarm.get_hour());
			second_alarm_time.set(Calendar.MINUTE, second_alarm.get_minute());
			second_alarm_time.set(Calendar.SECOND, second_alarm.get_second());
			
			//differenza di tempo tra i due alarm
			long time_diff=0;
			
			if(next){
				time_diff = second_alarm_time.getTime().getTime() - current_alarm_time.getTime().getTime();		
				Log.d(MainActivity.AppName, "AlarmUtils - intervallo next diff: " +time_diff);
			}
			else{
				time_diff = current_alarm_time.getTime().getTime() - second_alarm_time.getTime().getTime();
				Log.d(MainActivity.AppName, "AlarmUtils - intervallo prev diff: " +time_diff);
			}
					
			
			//se il tempo che intercorre tra i due intervalli è pari a 1 secondo allora significa che
			//sono consecutivi
			if(time_diff == 1000){
				return second_alarm;
			}
			return null;
		}
		return null;
	}
	
	
	
	private static void writeSkippedInterval(Context context, Alarm a, int day_index, int days_number_to_add){
		
		//nel logfile si scrive che tale intervallo è stato saltato solo quando si arriva
		//al relativo alarm di stop		
		if(!a.get_actionType() && !isPreviousIntervalListened(context, a, false, days_number_to_add)){			
			//si ottiene il relativo alarm di start (esiste sicuramente)
			Alarm prev_start= getAlarm(context, a.get_id()-1); 						

			String status="";							
			if(a.isStepsInterval(day_index)){
				status="S,";
			}
			else{
				status="E,";
			}			
			if(a.getRepeatingDay(day_index)){
				status+="1";
			}
			else{
				status+="0";
			}						
			LogUtils.writeIntervalStatus(context, day_index, prev_start, a, "|"+status+";-;-(J);"+status);						
		}
		
	}
	
	
	
	private static boolean areIntervalSetsFarEnough(Context context, Alarm last_alarm_first_set, Alarm first_alarm_second_set){
		
		Calendar alarm_first_set_time = Calendar.getInstance();
		Calendar alarm_second_set_time = (Calendar) alarm_first_set_time.clone();
		
		alarm_first_set_time.set(Calendar.HOUR_OF_DAY, last_alarm_first_set.get_hour());
		alarm_first_set_time.set(Calendar.MINUTE, last_alarm_first_set.get_minute());
		alarm_first_set_time.set(Calendar.SECOND, last_alarm_first_set.get_second());
		alarm_second_set_time.set(Calendar.HOUR_OF_DAY, first_alarm_second_set.get_hour());
		alarm_second_set_time.set(Calendar.MINUTE, first_alarm_second_set.get_minute());
		alarm_second_set_time.set(Calendar.SECOND, first_alarm_second_set.get_second());
		
		if(alarm_second_set_time.getTime().getTime() - alarm_first_set_time.getTime().getTime() >= 21600000){
			return true;
		}
		return false;		
	}
	
	
	
	private static long getTimeDistance(Alarm current_alarm, Alarm start_alarm, boolean from_beginning){
		
		Calendar current_alarm_time = Calendar.getInstance();
		Calendar start_alarm_time = (Calendar) current_alarm_time.clone();
		
		current_alarm_time.set(Calendar.HOUR_OF_DAY, current_alarm.get_hour());
		current_alarm_time.set(Calendar.MINUTE, current_alarm.get_minute());
		current_alarm_time.set(Calendar.SECOND, current_alarm.get_second());
		start_alarm_time.set(Calendar.HOUR_OF_DAY, start_alarm.get_hour());
		start_alarm_time.set(Calendar.MINUTE, start_alarm.get_minute());
		start_alarm_time.set(Calendar.SECOND, start_alarm.get_second());
		
		
		if(from_beginning){
			return current_alarm_time.getTime().getTime() - start_alarm_time.getTime().getTime();
		}
		else{
			return start_alarm_time.getTime().getTime() - current_alarm_time.getTime().getTime();
		}		
	}
	
	
	
	private static ArrayList<ArrayList<Alarm>> getActiveIntervalSets(Context context){
		
		//si recupera l'indice del giorno corrente
		int day_index = PreferenceManager.getDefaultSharedPreferences(context).getInt("artificialDayIndex", 0);//context.getSharedPreferences("appPrefs", 0).getInt("artificialDayIndex", 0);
		
		ArrayList<ArrayList<Alarm>> intervalSets = new ArrayList<ArrayList<Alarm>>();
				
		//si recuperano tutti gli alarm salvati nel database
		List<Alarm> all_alarms = getAllAlarms(context);
		
		//indice per scorrere gli intervalli 
		int i=1;
		while(i<=all_alarms.size()){
			
			boolean active=true;
			ArrayList<Alarm> set = new ArrayList<Alarm>();
				
			Alarm previous = null;
			
			do {				
				//se 'previous' (l'alarm precedente) viene immediatamente prima dell'alarm di indice i,
				//significa che i due intervalli sono consecutivi; in tal caso si ritorna l'alarm di
				//indice i, altrimenti null
				Alarm a = secondInterval(context, previous, true);
								
				if(a!=null || set.size()==0){
					
					if(a==null){
						a = getAlarm(context, i);
					}						
					
					//a è un alarm di start; si controlla se è attivo nel giorno considerato
					if(a!=null && a.getRepeatingDay(day_index)){
						set.add(a);
					}
					else{
						active=false;
					}
					
					//si salva il corrispondente alarm di stop; questo serve all'iterazione 
					//successiva per vedere se i due intervalli sono consecutivi come orario
					previous = getAlarm(context, i+1);
										
					//si passa all'alarm di start successivo
					i+=2;
				}
				else{ //l'intervallo precedente e questo che si sta considerando sono entrambi
					  //attivi, ma non sono consecutivi come orario; in tal caso si interrompe
					  //il gruppo e se ne inizia un altro, sempre a partire dall'intervallo corrente
					  //(non si incrementa i)
					active=false;
					previous=null;
				}
				
			}
			while(set.size()<=12 && active); 
			//un gruppo può essere composto al massimo da 12 intervalli (questo per andare meglio
			//a calcolare la distanza temporale minima tra i 2 trigger)
			
			//si tengono i gruppi di 3 o più intervalli attivi
			if(set.size()>=3){
				intervalSets.add(set);
			}
		}		
		return intervalSets;
	}
	

	@SuppressLint("UseSparseArrays")
	private static Map<Long,IntPair> getPossibleTriggerSetPairs(Context context, ArrayList<ArrayList<Alarm>> activeSets){
		
		//in input si ha la lista di gruppi di intervalli consecutivi attivi (un gruppo è formato
		//da almeno 3 e al massimo da 12 intervalli); questo metodo viene chiamato nel caso ci siano
		//almeno 2 gruppi di intervalli (altrimenti viene considerato l'unico gruppo)
		
		int i=0;
		//si ottiene il numero di gruppi trovati
		int size = activeSets.size();
		
		//considerando una coppia di gruppi, la distanza temporale tra il primo intervallo del primo
		//gruppo e il primo intervallo del secondo gruppo deve essere >= 6 ore; dal momento che
		//un gruppo è formato al massimo da 12 intervalli da 5 minuti, ci sono almeno 6 ore di
		//differenza tra un trigger e l'altro (i due trigger saranno infatti i primi intervalli dei gruppi)
		
		long time_distance = 21600000;  //6 ore in millisecondi
		
		//lista di coppie di interi che indicano le possibili coppie di gruppi dalle quali ricavare i
		//2 trigger; si ritorna una HashMap per poi ordinarla per chiave
		Map<Long, IntPair> pairs = new HashMap<Long, IntPair>();
						
		boolean stop=false;
		
		while(!stop){
			
			//si recupera la coppia di gruppi di indice (i, size-i-1), cioè formata dal primo
			//gruppo non considerato a partire dall'inizio e dal primo gruppo non considerato a
			//partire dalla fine (così facendo, dal momento che i gruppi sono ordinati per orario
			//crescente, se una coppia di gruppi più esterna non soddisfa la condizione >= 6 ore,
			//ci si ferma subito perché neanche quelle più interne la soddisferanno)
			
			//si recupera il primo alarm (di start) del primo gruppo non considerato a partire
			//dall'inizio
			Alarm first_alarm_first_set =(activeSets.get(i)).get(0);
			int first_alarm_first_set_id = first_alarm_first_set.get_id();
			//si recupera il primo alarm (di start) del primo gruppo non considerato a partire
			//dalla fine
			int end_index=size-i-1;
			Alarm first_alarm_second_set = (activeSets.get(end_index)).get(0); //per ultimo: secondSet.size()-1
			int first_alarm_second_set_id = first_alarm_second_set.get_id();
			//Alarm last_alarm_second_set = getAlarm(context, last_start_alarm_second_set.get_id()+1);			
						
			//si recupera anche il primo alarm del gruppo precedente al primo
			Alarm first_alarm_prev_first_set = (activeSets.get(i-1)).get(0);
			int first_alarm_prev_first_set_id = first_alarm_prev_first_set.get_id();
			//si recupera anche il primo alarm del gruppo successivo al secondo
			Alarm first_alarm_next_second_set = (activeSets.get(end_index+1)).get(0);		
			int first_alarm_next_second_set_id = first_alarm_next_second_set.get_id();
			
			//se il primo intervallo del primo gruppo e il primo intervallo del secondo gruppo 
			//distano almeno 6 ore, allora si tiene la coppia di alarm
			long time = getTimeDistance(first_alarm_first_set, first_alarm_second_set, false);
						
			if(i < end_index){
				
				if(time >= time_distance){
					
					pairs.put(time, new IntPair(first_alarm_first_set_id,first_alarm_second_set_id));
					
					if(i!=0){
						//si tengono anche le coppie (1o alarm gruppo precedente al primo, 1o alarm secondo gruppo)
						//e (1o alarm primo gruppo, 1o alarm gruppo successivo al secondo) in quanto
						//anch'esse soddisfano la condizione >= 6 ore;
						//la coppia indicata con (1o alarm gruppo precedente al primo, 1o alarm gruppo successivo al secondo)
						//è stata aggiunta all'iterazione precedente
											
						pairs.put(getTimeDistance(first_alarm_first_set, first_alarm_next_second_set, false),
								new IntPair(first_alarm_first_set_id,first_alarm_next_second_set_id));
											
						pairs.put(getTimeDistance(first_alarm_prev_first_set, first_alarm_second_set, false),
								new IntPair(first_alarm_prev_first_set_id,first_alarm_second_set_id));					
					}
				}
				else{
					
					if(i!=0){
						
						long prev_first_second_time = getTimeDistance(first_alarm_prev_first_set, first_alarm_second_set, false);
						if( prev_first_second_time >= time_distance){
							pairs.put(prev_first_second_time, new IntPair(first_alarm_prev_first_set_id,first_alarm_second_set_id));
						}					
						
						long first_next_second_time = getTimeDistance(first_alarm_first_set, first_alarm_next_second_set, false);
						if( first_next_second_time >= time_distance){
							pairs.put(first_next_second_time, new IntPair(first_alarm_first_set_id,first_alarm_next_second_set_id));
						}
					}
					
					stop=!stop;
				}
				
				
				if(i!=0){
					//si misura la distanza temporale tra il primo alarm del gruppo di indice 'i'
					//e quello del gruppo di indice 'i-1'								
					long prev_time = getTimeDistance(first_alarm_prev_first_set, first_alarm_first_set, false);
					if(prev_time >= time_distance){
						pairs.put(prev_time, new IntPair(first_alarm_prev_first_set_id,first_alarm_first_set_id));
					}
					
					//si misura la distanza temporale tra il primo alarm del gruppo di indice 'end_index'
					//e quello del gruppo di indice 'end_index+1'					
					long next_time = getTimeDistance(first_alarm_second_set, first_alarm_next_second_set, false);
					if(next_time >= time_distance){
						pairs.put(next_time, new IntPair(first_alarm_second_set_id,first_alarm_next_second_set_id));
					}
				}
				
				i++; //si incrementa l'indice per considerare la prossima coppia
			}
			else{
				stop=!stop;
			}
			
		}
		
		return pairs;		
	}
	
	public static JSONObject toJSONObject(List<Alarm> alarms){
        JSONObject json_alarms = new JSONObject();
        for(Alarm alarm: alarms){
            try {
                json_alarms.put(String.valueOf(alarm.get_id()), alarm.toJSON());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return json_alarms;
    }

    public static void fromJSONtoAlarm(Context context, JSONObject alarms){
        DbHelper helper = DbHelper.getInstance(context);
        RuntimeExceptionDao<Alarm, Integer> alarmDao = helper.getAlarmDao();

        Iterator<String> it = alarms.keys();
        while(it.hasNext()){
            try {
                String alarm_id = it.next();
                JSONObject json_alarm = alarms.getJSONObject(alarm_id);
                Alarm alarm  = getAlarm(context, Integer.valueOf(alarm_id)); //get per id e utente
                if(alarm == null) alarm = new Alarm();
                alarm.set_actionType(Boolean.valueOf(json_alarm.getString("actionType")));
                alarm.set_hour(Integer.valueOf(json_alarm.getInt("hour")));
                alarm.set_minute(json_alarm.getInt("minute"));
                alarm.set_second(json_alarm.getInt("seconds"));
                JSONArray repeatingDays = json_alarm.getJSONArray("repeatingDays");
                for(int i = 0; i < repeatingDays.length(); i++)
                    alarm.setRepeatingDay(i, repeatingDays.getBoolean(i));
                JSONArray evaluations = json_alarm.getJSONArray("evaluations");
                for(int i = 0; i < evaluations.length(); i++)
                    alarm.setEvaluation(i, (float) evaluations.getDouble(i));
                JSONArray stepsInterval = json_alarm.getJSONArray("stepsInterval");
                for(int i = 0; i < stepsInterval.length(); i++)
                    alarm.setStepsInterval(i, stepsInterval.getBoolean(i));

                alarmDao.createOrUpdate(alarm);




            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
	
	
	
	
	
	
	
	
	
	/////////
	//PER TEST ALGORITMO
	/**
	 * Metodo che ritorna l'indice "artificiale" che rappresenta il prossimo giorno 
	 * considerando una settimana corta, composta da un numero di giorni inferiore a 7.
	 * @param i indice che rappresenta il giorno corrente
	 * @return indice che rappresenta il prossimo giorno
	 */	
	public static int getNextDayIndex(int i){
		return (i == GeneralUtils.daysOfWeek-1) ? 0 : i+1;
	}
	/////////
	 
	 
	 
	 // fare metodo per considerare una coppia di alarm start-stop consecutivi
	 //      l'idea è che se si seleziona un alarm di start scartato per dargli una 
	 //      probabilità (peso) di attivarsi la prossima volta, allora si deve
	 //      attivare anche il relativo alarm di stop (attivare con day=true)
	 
	 // fare metodo che metta day=true ad un alarm (da usare, ad esempio, dopo
	 //      che si vede che l'intervallo a false per tot volte consecutive in cui
	 //      si è attivato è risultato sempre "buono")
	 
	 // fare metodo che ritorni tutti gli intervalli a false per un certo 
	 //      giorno (utile, ad esempio, per diminuire la probabilità a tutti di
	 //      essere scelti in quanto c'è poca batteria)
	 
	 
	 
	 
	 //I SEGUENTI METODI NON SERVONO se gli intervalli (start-stop alarm) vengono 
	 //già pre-impostati così da non estrarli, crearli e aggiornarli 
	 //SI POSSONO FARE INTERVALLI BREVI (ad es. 10 minuti) separati tra di loro
	 //da pochi secondi (meglio per chiarezza di codice, di spiegazione, più utili
	 //e semplici in un algoritmo genetico)
	 /*
	 public static void setSimilarOrNewAlarm(Context context, Calendar startTime, Calendar stopTime, boolean isConsecutive){
		 
		 
		 if(!isConsecutive){
			 
			 Alarm startAlarm = searchSimilarAlarm(context, startTime, true);
			 
			 if(startAlarm!=null){				 
				startAlarm.setRepeatingDay(startTime.get(Calendar.DAY_OF_WEEK-1), true);				 
			 }
			 else{
				 //si crea un nuovo alarm
				 Alarm newStartAlarm = new Alarm(startTime.get(Calendar.HOUR_OF_DAY), startTime.get(Calendar.MINUTE),
						 startTime.get(Calendar.SECOND), true);
				 //
				 newStartAlarm.setRepeatingDay(startTime.get(Calendar.DAY_OF_WEEK-1), true);
				 
				 saveAlarmInDB(context, newStartAlarm, 2);
			 }
			 
			 
			 Alarm stopAlarm = searchSimilarAlarm(context, stopTime, false);
			 
			 if(stopAlarm!=null){				 
				stopAlarm.setRepeatingDay(stopTime.get(Calendar.DAY_OF_WEEK-1), true);				 
			 }
			 else{
				 //si crea un nuovo alarm
				 Alarm newStopAlarm = new Alarm(stopTime.get(Calendar.HOUR_OF_DAY), stopTime.get(Calendar.MINUTE),
						 stopTime.get(Calendar.SECOND), false);
				 //
				 newStopAlarm.setRepeatingDay(stopTime.get(Calendar.DAY_OF_WEEK-1), true);
				 
				 saveAlarmInDB(context, newStopAlarm, 2);
			 }
			 
			 
		 }
		 else{
			 
		 }
			 
		
		 
	 }
	 
	 
	 
	 
	 private static Alarm searchSimilarAlarm(Context context, Calendar time, boolean alarmType){
		 
		 //si recuperano tutti gli alarm 
		 List<Alarm> sameTypeAlarms = getAlarmsByType(context, alarmType);
		 
		 Collections.sort(sameTypeAlarms,new AlarmComparator());
		 
		 //se c'è almeno un alarm del tipo passato come parametro (tipo che può indicare start/stop classify)
		 if(sameTypeAlarms!=null){
			 
			 Calendar alarmTime=Calendar.getInstance();
				
			 for(int i=0; i<sameTypeAlarms.size(); i++){
					
				 Alarm e = sameTypeAlarms.get(i);
							
				 alarmTime.set(Calendar.HOUR_OF_DAY, e.get_hour());
				 alarmTime.set(Calendar.MINUTE, e.get_minute());
				 alarmTime.set(Calendar.SECOND, e.get_second());
			 			 
				 //se è un alarm che ha un tempo di inizio uguale al tempo di inizio/fine dell'intervallo
				 //considerato o comunque scatta in un tempo più o meno simile (differenza di 2 minuti) allora si 
				 //imposta tale alarm settandolo come attivo nel giorno corrente
				 if(Math.abs(time.getTimeInMillis() - alarmTime.getTimeInMillis()) <= 120000
						 || Math.abs(alarmTime.getTimeInMillis()- time.getTimeInMillis()) <= 120000){
						
					 //e.setRepeatingDay(time.get(Calendar.DAY_OF_WEEK-1), true);						
					 return e;					
				 }
				 
				 
				
				 //per fare un metodo che vada bene sempre bisogna considerare anche l'intorno a cavallo 
				 //di due giorni
				 
				 //if(time.get(Calendar.HOUR_OF_DAY)==23){
							
				//	 time.add(Calendar.DATE, 1);		
				// }
				 
				 
		 
			 }
		 }
		 
	 return null;
		 
	 }
	 */
}