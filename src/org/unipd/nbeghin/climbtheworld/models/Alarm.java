package org.unipd.nbeghin.climbtheworld.models;

import org.json.JSONException;
import org.json.JSONObject;
import org.unipd.nbeghin.climbtheworld.util.GeneralUtils;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * 
 * Class that defines a start/stop event of an interval which corresponds to a start/stop action of
 * a classify service. In an interval can be launched also a trigger.
 *
 */
@DatabaseTable(tableName = "alarms")
public class Alarm {
     
	//si usa questo nome del campo 'id' in modo da considerarlo quando viene costruita una query
    //per ottenere gli alarm aventi un certo id
    public final static String ID_FIELD_NAME = "id";
    //si usa questo nome del campo 'actionType' in modo da considerarlo quando viene costruita una query
    //per ottenere gli alarm che fanno partire o fermano il servizio di classificazione
    public final static String ACTION_TYPE_FIELD_NAME = "actionType";
    
    @DatabaseField(generatedId = true, canBeNull = false, columnName = ID_FIELD_NAME) 
    private int id;
    @DatabaseField
    private int hour;
    @DatabaseField
	private int minute;
    @DatabaseField
	private int second;
    @DatabaseField(columnName = ACTION_TYPE_FIELD_NAME) 
    private boolean actionType; 
        
    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private boolean repeatingDays[] = new boolean[GeneralUtils.daysOfWeek]; // =  new boolean[7];
    //la dimensione è decisa all'inizio a seconda del numero di giorni della settimana
    //(per test algoritmo meno di 7 giorni, es. 1,2)
            
    
    //array nel quale vengono inserite le valutazioni di un certo intervallo
    //(alarm start-stop) per i vari giorni della settimana;
    //se per l'algoritmo si considera una probabilità di mutazione legata alla
    //valutazione, i valori del seguente array indicano le varie probabilità di
    //riconsiderare un intervallo inattivo per un certo giorno della settimana;
    //tale array per un certo alarm viene popolato durante la settimana: in 
    //corrispondenza dell'indice relativo ad un certo giorno in cui l'alarm viene
    //considerato viene posta la valutazione calcolata
    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private float evaluations[] = new float[GeneralUtils.daysOfWeek]; // =  new float[7];
    
     
    //i valori di tale array indicano se l'alarm definisce un "intervallo con scalini",
    //cioè un intervallo in cui l'utente la settimana precedente ha fatto scalini (usando
    //il gioco o senza); in un intervallo di questo tipo non si fa partire il servizio di
    //activity recognition, bensì il classificatore di riconoscimento scalini
    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private boolean stepsInterval[] = new boolean[GeneralUtils.daysOfWeek]; // =  new float[7];
    
    
    /**
     * Costruttore di un oggetto Alarm (costruttore vuoto per ormlite).
     */
    public Alarm() {
    }
 
    /**
     * Costruttore di un oggetto Alarm.
     * @param hour
     * @param minute
     * @param second
     * @param actionType
     * @param repeatingDays
     * @param evaluations
     */
    public Alarm(int hour, int minute, int second, boolean actionType, boolean[] repeatingDays, float[] evaluations) {
		
    	//this.id=id;
    	this.hour=hour;
    	this.minute=minute;
    	this.second=second;
    	this.actionType=actionType;
    	    	
    	for(int i=0; i<repeatingDays.length; i++)
    		this.repeatingDays[i]=repeatingDays[i];
    	
    	for(int i=0; i<evaluations.length; i++)
    		this.evaluations[i]=evaluations[i];
    	
    	for(int i=0; i<stepsInterval.length; i++)
    		this.stepsInterval[i]=false;
    	
	}

    /**
     * Costruttore di un oggetto Alarm.
     * @param hour
     * @param minute
     * @param second
     * @param actionType
     */
    
    public Alarm(int hour, int minute, int second, boolean actionType) {
		
    	//this.id=id;
    	this.hour=hour;
    	this.minute=minute;
    	this.second=second;
    	this.actionType=actionType;
    	
    	for(int i=0; i<repeatingDays.length; i++)
    		this.repeatingDays[i]=true;    	
    
    	//si pone la probabilità di riconsiderare un alarm scartato ad un valore
    	//di default pari a 0,25 in ogni giorno della settimana    	
    	for(int i=0; i<evaluations.length; i++)
    		this.evaluations[i]=0.25f;
    	
    	for(int i=0; i<stepsInterval.length; i++)
    		this.stepsInterval[i]=false;    	
	}
    
    
	
    public int get_id() {
		return id;
	}

	public void set_id(int id) {
		this.id = id;
	}
	
	public int get_hour() {
		return hour;
	}

	public void set_hour(int hour) {
		this.hour = hour;
	}
    
	public int get_minute() {
		return minute;
	}

	public void set_minute(int minute) {
		this.minute = minute;
	}
	
	
	public int get_second() {
		return second;
	}

	public void set_second(int second) {
		this.second = second;
	}
	
	public boolean get_actionType() {
		return actionType;
	}

	public void set_actionType(boolean type) {
		actionType = type;
	}	
	
	public void setRepeatingDay(int dayOfWeek, boolean value) {
        repeatingDays[dayOfWeek] = value;
    }
 
    public boolean getRepeatingDay(int dayOfWeek) {
        return repeatingDays[dayOfWeek];
    }

    public void setEvaluation(int dayOfWeek, float value) {
    	
    	//il valore non può mai essere > 1 
    	//if(value>1.0f){
    	//	value=1.0f;
    	//}    	
    	evaluations[dayOfWeek] = value;
    }
 
    public float getEvaluation(int dayOfWeek) {
        return evaluations[dayOfWeek];
    }
    
    
    public void setStepsInterval(int dayOfWeek, boolean value) {
    	
    	stepsInterval[dayOfWeek] = value;
    }
    
    public boolean isStepsInterval(int dayOfWeek) {
        return stepsInterval[dayOfWeek];
    }

    public JSONObject toJSON(){
        JSONObject description = new JSONObject();
        try {
            description.put("hour", get_hour());
            description.put("minute", get_minute());
            description.put("seconds", get_second());
            description.put("actionType", get_actionType());
            description.put("repeatingDays", repeatingDays);
            description.put("evaluations", evaluations);
            description.put("stepsInterval", stepsInterval);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return description;
    }
    
}