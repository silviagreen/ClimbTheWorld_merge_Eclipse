package org.unipd.nbeghin.climbtheworld;

import org.unipd.nbeghin.climbtheworld.adapters.RectangleShapeAdapter;
import org.unipd.nbeghin.climbtheworld.util.AlarmUtils;
import org.unipd.nbeghin.climbtheworld.util.GeneralUtils;
import org.unipd.nbeghin.climbtheworld.R;
import com.etsy.android.grid.StaggeredGridView;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.HoloCircleSeekBar;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class AlgorithmConfigFragment extends Fragment {

	//questo codice fino a 'onDetach()' è cio' che serve per lavorare con i
	//callbacks dell'Activity
	private Callbacks mCallbacks = sDummyCallbacks;
	
	private static int screen_width;
	private static int screen_height;
	
	//intero che indica il colore selezionato (-1 se non è ancora stato selezionato alcun colore)
	private int current_color=-1;
	//map che contiene le coppie di valori <posizione_item, colore>
	private static SparseIntArray positions_colors;
	
	//campi per i vari componenti grafici utilizzati
	private TextView yellow_picker;
	private TextView red_picker;
	private TextView green_picker;
	private StaggeredGridView gridView;
	
	
	/**
	 * 
	 * Interfaccia contenente un metodo che permette a questo fragment di informare la relativa
	 * activity che il task eseguito è finito. Tale interfaccia è implementata dall'activity.
	 *
	 */
	public interface Callbacks
	{
		/**
		 * Metodo usato dal fragment per informare la relativa activity che il task eseguito è finito.
		 * @param context contesto in cui viene chiamato il metodo
		 * @param intent intent con il quale viene lanciata un'opportuna activity in cui mostrare 
		 *               il risultato del task
		 */
		public void onTaskFinished(Context context, Intent intent);
	}
	    
	//si inizializza il campo per il callback
	private static Callbacks sDummyCallbacks = new Callbacks()
	{
		@Override
		public void onTaskFinished(Context context, Intent intent) { }
	};
	
	
	public void onAttach(Activity activity) {
		
		super.onAttach(activity);    	
    	
		if (!(activity instanceof Callbacks))
			{
			throw new IllegalStateException("Activity must implement fragment's callbacks.");
			}
		mCallbacks = (Callbacks) activity;
	};
	
	
	@Override
	public void onDetach() {
		super.onDetach();
		mCallbacks = sDummyCallbacks;
	}
	
	
	//si salva un riferimento al fragment manager. Questo è inizializzato in 'onCreate()'
    private FragmentManager mFM;

    //intero per identificare il fragment che chiama 'onActivityResult()'; non se ne ha
    //veramente il bisogno visto che in tal caso si ha un solo fragment da gestire
    static final int TASK_FRAGMENT = 0;

    //stringa che indica un tag per fare in modo di ritrovare il task fragment, in un'altra
    //istanza di questo fragment dopo una rotazione
    static final String TASK_FRAGMENT_TAG = "task";
	
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	   
    	
    	//a tal punto il fragment può essere stato ricreato a causa di una rotazione,
        //e possiamo trovare il TaskFragment
        mFM = getFragmentManager();
        TaskFragment taskFragment = (TaskFragment)mFM.findFragmentByTag(TASK_FRAGMENT_TAG);

        if (taskFragment != null)
        {
         //si aggiorna il target fragment, per considerare il nuovo fragment invece del vecchio
         taskFragment.setTargetFragment(this, TASK_FRAGMENT);
        }
        
    }
    
    
    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) {
    	return inflater.inflate(R.layout.fragment_algorithm_config, container, false);
    }
    
    
    
    @SuppressLint("InflateParams")
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);    	
    	
    	if(positions_colors==null){
    		positions_colors=new SparseIntArray(24);
    		initializeMap();
    	}
    	
    	
    	DisplayMetrics metrics = new DisplayMetrics();
    	getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

    	screen_height = metrics.widthPixels;
    	screen_width = metrics.heightPixels;
    	
    	GradientDrawable rectangle_shape = (GradientDrawable) getResources().getDrawable(R.drawable.rectangle_shape);
    	((GradientDrawable)rectangle_shape.mutate()).setColor(Color.GREEN);    	
    	green_picker = (TextView) getActivity().findViewById(R.id.green_picker);    	
    	green_picker.setBackground(rectangle_shape);
    	
    	rectangle_shape=((GradientDrawable)rectangle_shape.getConstantState().newDrawable());
    	rectangle_shape.setColor(Color.RED);  
    	red_picker = (TextView) getActivity().findViewById(R.id.red_picker);    	
    	red_picker.setBackground(rectangle_shape);
    	
    	rectangle_shape=((GradientDrawable)rectangle_shape.getConstantState().newDrawable());
    	rectangle_shape.setColor(Color.YELLOW);  
    	yellow_picker = (TextView) getActivity().findViewById(R.id.yellow_picker);    	
    	yellow_picker.setBackground(rectangle_shape);
    	
    	
    	gridView = (StaggeredGridView) getActivity().findViewById(R.id.alg_conf_gridview);
    	ListView lstV = new ListView(getActivity());
    	
    	Button config_btt = (Button) getActivity().getLayoutInflater().inflate(R.layout.gridview_config_button, null);
    	config_btt.setText(R.string.config_button_text);
    	config_btt.setId(R.string.config_button_id);
    	//config_btt.setBackgroundResource(R.drawable.blue_button_style);
    	//config_btt.setCompoundDrawablesWithIntrinsicBounds( R.drawable.ic_action_settings, 0, 0, 0);
    	//config_btt.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.card_small_text));
    	//config_btt.setTypeface(Typeface.SERIF);	
    	
    	config_btt.setOnClickListener(new OnClickListener() {
    	@Override
		public void onClick(View v) {
				
    		if(!allTimeSlotsSetted()){
    			Toast.makeText(getActivity(), getActivity().getResources().getText(R.string.set_timeslots_btt), Toast.LENGTH_SHORT).show();
    		}
    		else{	    			
    			TaskFragment taskFragment = new TaskFragment();
    			
    			//si crea un task per il processo di creazione intervalli
    			CreateIntervalsTask intervalsCreationTask = new CreateIntervalsTask(getActivity());
    			intervalsCreationTask.setFragment(taskFragment);
    			    			
 		        //il taskFragment esegue il task per la creazione degli intervalli
 		        taskFragment.setTask(intervalsCreationTask);
 		        taskFragment.setTargetFragment(AlgorithmConfigFragment.this, TASK_FRAGMENT);
 		        
		        //si mostra il fragment
		        taskFragment.show(mFM, TASK_FRAGMENT_TAG);
    		}
    	}
    	});
    	
    	lstV.addFooterView(config_btt);
    	
    	lstV.setAdapter(new RectangleShapeAdapter(this.getActivity()));
    	    	
    	gridView.setAdapter(lstV.getAdapter());
    	gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				if(current_color!=-1){ //è stato selezionato uno dei tre colori
					positions_colors.put(position, current_color);
					GradientDrawable rect_shape_view = (GradientDrawable) view.getBackground();
					rect_shape_view.setColor(current_color);
				}
				else{ //non è stato ancora selezionato alcun colore
					Toast.makeText(getActivity(), getActivity().getResources().getText(R.string.select_color_toast), Toast.LENGTH_SHORT).show();
				}
			}
		});
    }
    
    
    
    //metodo chiamato quando il task è concluso che, a sua volta, chiama il metodo 'onTaskFinished' 
  	//dell'oggetto Callbacks per informare l'activity dell'avvenuta conclusione del processo
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    	if (requestCode == TASK_FRAGMENT && resultCode == Activity.RESULT_OK)
        {
            //si informa l'activity che è finito il task
            mCallbacks.onTaskFinished(getActivity(),data);
        }
    }
    
    
    //il seguente metodo è chiamato quando tutti gli stati salvati sono stati ripristinati nel
  	//fragment. E' qui usato per fare un'inizializzazione basata sullo stato salvato che si lascia
  	//in gestione alla vista stessa (ad esempio le label).
  	//Tale metodo è chiamato dopo onActivityCreated(Bundle) e prima di onStart()
    @Override
    public void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
		    	
		//si salva l'intero che indica il colore selezionato (-1 se ancora nessun colore selezionato)
		outState.putInt("current_color", current_color);
    }
    
    //il seguente metodo è chiamato quando tutti gli stati salvati sono stati ripristinati nel
  	//fragment. E' qui usato per fare un'inizializzazione basata sullo stato salvato che si lascia
  	//in gestione alla vista stessa (ad esempio le label).
  	//Tale metodo è chiamato dopo onActivityCreated(Bundle) e prima di onStart()
    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
    	super.onViewStateRestored(savedInstanceState);
    	
    	if(savedInstanceState!=null){ 
    		
    		//si recupera lo stato salvato che indica il colore selezionato
    		current_color=savedInstanceState.getInt("current_color");
    	}
    }
    
    
    
    
    public static class CreateIntervalsTask extends AsyncTask<Void, Integer, Void> {
    	
    	//riferimento alla finestra di dialogo per il task
    	TaskFragment mFragment;

    	Context mContext;
    	
    	public CreateIntervalsTask(Context context) {
			mContext=context;
		}
    	
    	
    	
    	/**
		 * Metodo che permette di settare il campo che definisce la finestra di dialogo per il task.
		 * @param fragment riferimento alla finestra di dialogo
		 */
    	void setFragment(TaskFragment fragment)
    	{    		
    		mFragment = fragment;
    	}
    	
    	
    	
		@Override
		protected Void doInBackground(Void... params) {

			AlarmUtils.createIntervals(mContext, positions_colors, this);	
			PreferenceManager.getDefaultSharedPreferences(ClimbApplication.getContext()).edit().putBoolean("algorithm_configured", true).commit();
			
			return null;
		}

		
		
		
		public void doProgress(int percentage_progress){
			
			publishProgress(percentage_progress);
		}
		
		
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			
			mFragment.updateProgressBar(values[0]);
		}
		
		
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (mFragment == null)
                return;
		
			Context context = ClimbApplication.getContext();			
			GeneralUtils.initializeAlgorithm(context, PreferenceManager.getDefaultSharedPreferences(context));
			
			mFragment.taskFinished();
		}
    	
    }    
    

	/**
	 * Classe per la finestra di dialogo che visualizza un messaggio mentre viene eseguito 
	 * il task di creazione intervalli.
	 */
    public static class TaskFragment extends DialogFragment {
    	
    	
    	//il processo che si sta eseguendo
        CreateIntervalsTask mTask;

        HoloCircleSeekBar progress_bar;
        
        /**
		 * Metodo che permette di impostare il campo che definisce il task la cui finestra
		 * di dialogo è implementata con tale classe.
		 * @param task il task da eseguire
		 */
        public void setTask(CreateIntervalsTask task)
        {
        	mTask = task;
        	mTask.setFragment(this);
        }

        
        @Override
        public void onCreate(Bundle savedInstanceState) {
        	super.onCreate(savedInstanceState);

            //si conserva l'istanza cosicchè non sia distrutta quando l'activity AlgorithmConfigActivity
            //e il fragment AlgorithmConfigFragment cambiano configurazione
            setRetainInstance(true);
          
            //per evitare che, durante il task, l'utente possa cancellare la 
            //finestra di dialog toccando lo screen o premendo qualche pulsante
            setCancelable(false);
            
            //si fa partire il task (ci si può muovere al di fuori dell'activity se si vuole)
            if (mTask != null){
            	mTask.execute();
            }
        	
        }
        
        
        
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
        		Bundle savedInstanceState) {
        	  
        	  View view = inflater.inflate(R.layout.fragment_algorithm_config_task, container);
        	          	  
              getDialog().setTitle(R.string.lab_configdialog_title);              
              
              progress_bar=(HoloCircleSeekBar) view.findViewById(R.id.config_task_progressbar);

              return view;
        }
        
        
        
        
        //bisogna fare l'override di questo metodo perché altrimenti la finestra di
        //dialogo viene cancellata ruotando lo schermo
        @Override
        public void onDestroyView() {

            if (getDialog() != null && getRetainInstance())
                getDialog().setDismissMessage(null);
            super.onDestroyView();
        }
        
        
        //quando la finestra di dialogo sparisce, bisogna far terminare il task e poi
        //ritornare il risultato all'activity relativa a questo fragment
        @Override
        public void onDismiss(DialogInterface dialog) {
        	super.onDismiss(dialog);
        	
        	 if (mTask != null){
        		 mTask.cancel(false);
        	 }
        	 
             //si ritorna il risultato
        	 if (getTargetFragment() != null)
             	getTargetFragment().onActivityResult(TASK_FRAGMENT, Activity.RESULT_CANCELED, null);
        }
    	
        
        
        @Override
        public void onResume() {
        	
        	super.onResume();
            //se il task ha finito mentre l'utente non era in questa activity, allora
            //si può dismettere la finestra di dialogo
            if (mTask == null)
                dismiss();
        }
        
               
        
        /**
		 * Metodo chiamato dall'AsyncTask quando finisce il task. 
		 */
        public void taskFinished(){
        	
        	//si controlla che il fragment sia visibile nell'activity 'running', perché altrimenti
        	//l'app crasha se si cerca di dismettere la finestra di dialog dopo che l'utente ha
            //switchato in un'altra activity
            if (isResumed()){
            	dismiss();
            }
               

            //se 'isResumed()' ritorna 'false', si setta il task a 'null' permettendo, così, di
            //cancellare il dialog nel metodo 'onResume()'
            mTask = null;
            
            
            //si informa il fragment che il task è finito
            if (getTargetFragment() != null){            	
            	getTargetFragment().onActivityResult(TASK_FRAGMENT, Activity.RESULT_OK, new Intent(getActivity(),EndConfigActivity.class));
            }
        }
        
        
        
        public void updateProgressBar(int value){
        	
        	progress_bar.setValue(value, 100);
        }
        
    }
     
    
    
    
    public void selectAction(View v){
    	
    	switch(v.getId()) {
    		case R.id.red_picker:
    			current_color=Color.RED;
    			break;
    		case R.id.green_picker:
    			current_color=Color.GREEN;
    			break;
    		case R.id.yellow_picker: 
    			current_color=Color.YELLOW;
    			break;
    	}
    }
    
    
    
    private void initializeMap(){ 
    	
    	for(int i = 0; i < 24; i++) {       		
    		positions_colors.put(i,-1);
    	}
    }
    
    
    private boolean allTimeSlotsSetted(){ 
    	
    	for(int i = 0; i < positions_colors.size(); i++) { 
    		
    		if(positions_colors.get(i)==-1)
    			return false;
    	}    	
    	return true;
    }
    
    
    
    public static int getPositionColor(int position){
    	return positions_colors.get(position, -1);
    }
    
    
    
    public static int getScreenWidth(){
    	return screen_width;
    }
    
    public static int getScreenHeight(){
    	return screen_height;
    }
}