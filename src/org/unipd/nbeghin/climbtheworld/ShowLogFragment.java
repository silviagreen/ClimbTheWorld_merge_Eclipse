package org.unipd.nbeghin.climbtheworld;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.unipd.nbeghin.climbtheworld.util.GeneralUtils;
import org.unipd.nbeghin.climbtheworld.util.LogUtils;
import org.unipd.nbeghin.climbtheworld.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ShowLogFragment extends Fragment {

	private Context context;
	
	//questo codice fino a 'onDetach()' è cio' che serve per lavorare con i
	//callbacks dell'Activity
	private Callbacks mCallbacks = sDummyCallbacks;
	
	//holds activity recognition data, in the form of strings that can contain markup
    private ArrayAdapter<Spanned> logAdapter;
	
	//campi per i vari componenti grafici utilizzati
    
	//holds the ListView object in the UI
    private ListView logListView;
    
    //finestra di dialogo che mostra la non disponibilità della connessione dati
    private static AlertDialog.Builder alertBuilder;
  	//relativo booleano
  	private boolean alertIsShown = false;
	
	
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
    	return inflater.inflate(R.layout.fragment_show_log, container, false);
    }
	
	
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {    	
    	super.onActivityCreated(savedInstanceState);
    	
    	
    	context = getActivity();
		

        //get a handle to the activity update list
		logListView = (ListView) getActivity().findViewById(R.id.log_listview);

        //instantiate an adapter to store update data from the log
        logAdapter = new ArrayAdapter<Spanned>(
                getActivity(),
                R.layout.log_item,
                R.id.log_text
        );

        // Bind the adapter to the status list
        logListView.setAdapter(logAdapter);
       
        
        //create the dialog window for the case of no data connection
        alertBuilder = new AlertDialog.Builder(getActivity());
        alertBuilder.setTitle(R.string.netalert_title)
            .setMessage(R.string.netalert_msg)
            .setCancelable(false)
            .setIcon(R.drawable.error)
            .setPositiveButton(R.string.netalert_yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                	startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                	alertIsShown=false;
                }
            })
            .setNegativeButton(R.string.netalert_no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                    alertIsShown=false;
                }
            }).create();
        
        
        refreshLogData();    	
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
    
    
    //si fa l'override del metodo che recupera lo stato del fragment prima che 
    //venga killato cosicché possa essere ripristinato in vari metodi tra i quali onCreate e 
  	//onViewStateRestored; in particolare serve nelle situazioni di rotazione dello schermo
    @Override
    public void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	
    	//si salva un booleano che indica se l'alert dialog è mostrata o meno
    	outState.putBoolean("connAlertDialogVisibility", alertIsShown);
    }
    
    
    //il seguente metodo è chiamato quando tutti gli stati salvati sono stati ripristinati nel
  	//fragment. E' qui usato per fare un'inizializzazione basata sullo stato salvato che si lascia
  	//in gestione alla vista stessa (ad esempio le label).
  	//Tale metodo è chiamato dopo onActivityCreated(Bundle) e prima di onStart()
    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
    	super.onViewStateRestored(savedInstanceState);
    	    	
    	if(savedInstanceState!=null){
					
    		//si recupera lo stato salvato che indica la visibilità dell'alert dialog
			if(savedInstanceState.getBoolean("connAlertDialogVisibility")){
				alertBuilder.show();
				//si rimette a true perché ora l'alert è mostrata di nuovo
				alertIsShown=true;
			}
		}
    }
    
    
    
    
    
    public static class UploadTask extends AsyncTask<Void, Void, String> {

    	//riferimento alla finestra di dialogo per il task
    	TaskFragment mFragment;

    	Context mContext;
    	
    	
    	public UploadTask(Context context) {
			mContext=context;
		}
    	
    	
    	
    	/**
		 * Metodo che permette di settare il campo che definisce la finestra di dialogo per 
		 * il task.
		 * @param fragment riferimento alla finestra di dialogo
		 */
    	void setFragment(TaskFragment fragment)
    	{    		
    		mFragment = fragment;
    	}
    	
    	
    	
		@Override
		protected String doInBackground(Void... params) {
			
			String response = "";			
			
			try {
				response=GeneralUtils.uploadLogFile(mContext);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return response;
		}
    	
		
		
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (mFragment == null)
                return;
						
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
			
			mFragment.taskFinished(result);
		}
		
		
    }
    
    
    
    /**
   	 * Classe per la finestra di dialogo che visualizza un messaggio mentre viene eseguito
   	 * il task di upload del logfile.
   	 */
       public static class TaskFragment extends DialogFragment
       {
       	
    	   //il processo che si sta eseguendo
    	   UploadTask mTask;
       	

    	   /**
    	    * Metodo che permette di impostare il campo che definisce il task la cui finestra
    	    * di dialogo è implementata con tale classe.
    	    * @param task il task da eseguire
    	    */
    	   public void setTask(UploadTask task)
    	   {
    		   mTask = task;
    		   mTask.setFragment(this);
    	   }
       
    	   
    	   @Override
    	   public void onCreate(Bundle savedInstanceState) {
    		   super.onCreate(savedInstanceState);
    		   
    		   //si conserva l'istanza cosicchè non sia distrutta quando l'activity 
    		   //ShowLogActivity e il fragment ShowLogFragment cambiano configurazione
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
    	   
    		   View view = inflater.inflate(R.layout.fragment_show_log_task, container);
    		   
    		   getDialog().setTitle(R.string.lab_uploadialog_title);
    		   
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
    		   
    		   System.out.println("on dismiss upload dialog");
    		   
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
           public void taskFinished(String result){
        		
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
        		           		   
        		   getTargetFragment().onActivityResult(TASK_FRAGMENT, Activity.RESULT_OK, new Intent().putExtra("result_message", result));
        	   }
           }
       }
           
    
       public void selectAction(View v){
    	
    	   switch(v.getId()) {
    	   case R.id.refresh_log_data_button:
    		   refreshLogData();
    		   break;
    	   case R.id.upload_log_data_button:
    		   
    		   //si controlla dapprima se è attiva una qualche connessione dati; se
    		   //non è attiva, si mostra un alert dialog
    		   if(GeneralUtils.isInternetConnectionUp(getActivity())){
    			   //si crea un nuovo TaskFragment
    			   TaskFragment taskFragment = new TaskFragment();
    			   //si crea un task per il processo di upload del logfile; poi il 
    			   //taskFragment esegue tale task
    			   taskFragment.setTask(new UploadTask(getActivity()));
    			   taskFragment.setTargetFragment(this, TASK_FRAGMENT);

    			   //si mostra il fragment
    			   taskFragment.show(mFM, TASK_FRAGMENT_TAG);
    		   }
    		   else{
    			   alertBuilder.show();
    			   alertIsShown=true;
    		   }    		
    		   
    		   break;
    	   }
       }
    
    
       /**
        * Display the algorithm history stored in the
        * log file
        */
       private void refreshLogData() {
    	   
    	   //try to load data from the log file
    	   try {       
    		   
    		   int log_file_id = PreferenceManager.getDefaultSharedPreferences(context).getInt("log_file_id", -1);
    		   
    		   File logFile;        	
    		   if(log_file_id==-1){
    			   logFile = new File(context.getDir("climbTheWorld_dir", Context.MODE_PRIVATE), "algorithm_log");
    		   }
    		   else{
    			   logFile = new File(context.getDir("climbTheWorld_dir", Context.MODE_PRIVATE), "algorithm_log_"+log_file_id);
    		   }
        	
    		   
    		   //load log file records into the list
    		   List<Spanned> lines = LogUtils.loadLogFile(logFile);
    		   
    		   //clear the adapter of existing data
    		   logAdapter.clear();
    		   
    		   //add each element of the log to the adapter
    		   for (Spanned line : lines) {
    			   logAdapter.add(line);
    		   }
            
    		   //trigger the adapter to update the display
    		   logAdapter.notifyDataSetChanged();
    		   
    		   // If an error occurs while reading the history file
    	   } catch (IOException e) {
    		   Log.e(MainActivity.AppName, e.getMessage(), e);
    	   }
       }
    
}