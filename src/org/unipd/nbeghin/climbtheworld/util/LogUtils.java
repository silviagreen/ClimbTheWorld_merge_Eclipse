package org.unipd.nbeghin.climbtheworld.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.unipd.nbeghin.climbtheworld.ClimbApplication;
import org.unipd.nbeghin.climbtheworld.MainActivity;
import org.unipd.nbeghin.climbtheworld.models.Alarm;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.Spanned;
import android.text.SpannedString;
import android.util.Log;

public final class LogUtils {

	private LogUtils() {
	}

	// start-stop, 0/1 attuale, se attivo valutazione, 0/1 la prossima settimana
	public static void writeLogFile(Context context, String text) {

		String log_file_name = "";
		int log_file_id = PreferenceManager
				.getDefaultSharedPreferences(context).getInt("log_file_id", -1);

		if (log_file_id == -1) {
			log_file_name = "algorithm_log";
		} else {
			log_file_name = "algorithm_log_" + log_file_id;
		}

		final File logFile = new File(context.getDir("climbTheWorld_dir",
				Context.MODE_PRIVATE), log_file_name);

		try {
			if (!logFile.exists()) {
				Log.e(MainActivity.AppName, "Log file not exists");
				logFile.createNewFile();
			}

			// 'true' per aggiungere il testo al file esistente
			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile,
					true));
			buf.append(text);
			buf.newLine();
			buf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// carica il contenuto del file di log
	public static List<Spanned> loadLogFile(File logFile) throws IOException {

		// get a new list of spanned strings
		List<Spanned> content = new ArrayList<Spanned>();

		// if no log file exists yet, return the empty List
		if (!logFile.exists()) {
			return content;
		}

		// create a new buffered file reader based on the log file
		BufferedReader reader = new BufferedReader(new FileReader(logFile));

		// get a string instance to hold input from the log file
		String line;

		// read until end-of-file from the log file, and store the input line as
		// a
		// spanned string in the List
		while ((line = reader.readLine()) != null) {
			content.add(new SpannedString(line));
		}

		// close the file
		reader.close();

		// return the data from the log file
		return content;
	}

	//
	public static void offIntervalsTracking(Context context, int alarm_id) {

		// riferimento alle SharedPreferences
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);

		// si considerano gli intervalli che non sono stati valutati durante il
		// periodo di tempo in cui l'algoritmo non era attivo (a causa del
		// device spento o del
		// livello critico della batteria); è utile per tracciarli nel file di
		// output e, se si
		// vuole, per cambiare la loro valutazione

		Calendar time_now = Calendar.getInstance();
		time_now.set(Calendar.HOUR_OF_DAY, 0);
		time_now.set(Calendar.MINUTE, 0);
		time_now.set(Calendar.SECOND, 0);
		Calendar time_before = (Calendar) time_now.clone();// Calendar.getInstance();
		/*
		 * time_before.set(Calendar.HOUR_OF_DAY, 0);
		 * time_before.set(Calendar.MINUTE, 0); time_before.set(Calendar.SECOND,
		 * 0);
		 */

		int dd = 0;
		int mm = 0;
		int yyyy = 0;
		String not_evaluated_cause = "";

		int lastDayIndex = 0;

		// se ancora non è stato impostato alcun alarm, significa che questo
		// metodo è
		// chiamato al primo avvio per tracciare gli intervalli che hanno orario
		// di inizio
		// già passato
		if (alarm_id == -1) {
			lastDayIndex = pref.getInt("artificialDayIndex", 0);// normalmente,
																// indice dato
																// dalla data
																// corrente
			alarm_id = 1;
			dd = time_before.get(Calendar.DATE);
			mm = time_before.get(Calendar.MONTH);
			yyyy = time_before.get(Calendar.YEAR);
			// not_evaluated_cause="Non considerato perche' algoritmo non ancora configurato";
			not_evaluated_cause = "NC";
		} else {
			lastDayIndex = pref.getInt("alarm_artificial_day_index", 0); // normalmente,
																			// indice
																			// ottenuto
																			// dalla
																			// data
																			// dell'alarm
			dd = pref.getInt("alarm_date", -1);
			mm = pref.getInt("alarm_month", -1);
			yyyy = pref.getInt("alarm_year", -1);
			// not_evaluated_cause="Non considerato perche' algoritmo non attivo";
			// //device spento o livello critico della batteria
			not_evaluated_cause = "NA";
		}

		time_before.set(Calendar.DATE, dd);
		time_before.set(Calendar.MONTH, mm);
		time_before.set(Calendar.YEAR, yyyy);

		// si recupera il secondo alarm (alarm di stop del primo intervallo)
		Alarm first_stop_alarm = AlarmUtils.getAlarm(context, 2);

		// si calcola il numero di giorni passati dalla data dell'ultimo alarm
		// settato
		// se 0 l'ultimo alarm settato è impostato in questo stesso giorno, se 1
		// nel giorno
		// successivo, e così via
		double time_diff = time_now.getTimeInMillis()
				- time_before.getTimeInMillis();
		time_diff = time_diff / (24 * 60 * 60 * 1000); // hours in a day,
														// minutes in a hour,
														// seconds in a minute,
														// millis in a second
		int days_diff = (int) Math.round(time_diff);

		Log.d(MainActivity.AppName,
				"Intervals tracking - algoritmo non attivo, day diff since next alarm: "
						+ days_diff);

		List<Alarm> alarms_lst = AlarmUtils.getAllAlarms(context);

		// si resettano ora, minuti e secondi per riportarli all'orario corrente
		time_now = Calendar.getInstance();

		if (ClimbApplication.logEnabled) {
			int month = time_now.get(Calendar.MONTH) + 1;
			Log.d(MainActivity.AppName,
					"TIME NOW: h:m:s=" + time_now.get(Calendar.HOUR_OF_DAY)
							+ ":" + time_now.get(Calendar.MINUTE) + ":"
							+ time_now.get(Calendar.SECOND) + "  "
							+ time_now.get(Calendar.DATE) + "/" + month + "/"
							+ time_now.get(Calendar.YEAR));

			month = time_before.get(Calendar.MONTH) + 1;
			Log.d(MainActivity.AppName,
					"TIME BEFORE: h:m:s="
							+ time_before.get(Calendar.HOUR_OF_DAY) + ":"
							+ time_before.get(Calendar.MINUTE) + ":"
							+ time_before.get(Calendar.SECOND) + "  "
							+ time_before.get(Calendar.DATE) + "/" + month
							+ "/" + time_before.get(Calendar.YEAR));
		}

		// si imposta l'orario dell'alarm di stop del primo intervallo (utile
		// per capire se si
		// tratta del primo intervallo e se questo è già finito: in tal caso si
		// scrive l'indice
		// del giorno nel file di log)
		time_before.set(Calendar.HOUR_OF_DAY, first_stop_alarm.get_hour());
		time_before.set(Calendar.MINUTE, first_stop_alarm.get_minute());
		time_before.set(Calendar.SECOND, first_stop_alarm.get_minute());

		// se si tratta del primo intervallo (id_start=1 e id_stop=2) si è in
		// presenza di un nuovo giorno; se tale intervallo è finito allora si
		// scrive
		// l'indice del giorno nel file di output
		if ((alarm_id == 1 || alarm_id == 2) && time_before.before(time_now)) {
			Log.d(MainActivity.AppName, "Intervals tracking - day index: "
					+ lastDayIndex);
			mm = mm + 1;
			// writeLogFile(context,"Indice giorno: "+lastDayIndex+" - "+dd+"/"+mm+"/"+yyyy);
		}

		// stringa per scrivere l'orario di un alarm di start nel file di log
		String previous_start_time = "";
		Alarm previous = null;
		// se il primo alarm considerato è di stop, si inizializza questa
		// stringa con
		// l'orario del precedente alarm di start
		if (alarm_id > 1) {
			previous = AlarmUtils.getAlarm(context, alarm_id - 1);
			// se il precedente alarm è di start si memorizza la stringa
			// dell'orario
			if (previous.get_actionType()) {
				previous_start_time = previous.get_hour() + ":"
						+ previous.get_minute() + ":" + previous.get_second();
			}
		}

		boolean stop = false;
		// prima si considerano gli intervalli saltati nel giorno in cui era
		// stato
		// impostato l'alarm (days_diff=0)
		for (int i = alarm_id; i <= alarms_lst.size() && !stop; i++) {

			Alarm e = alarms_lst.get(i - 1);
			// si impostano ora, minuti e secondi prendendo tali parametri
			// dall'alarm salvato
			time_before.set(Calendar.HOUR_OF_DAY, e.get_hour());
			time_before.set(Calendar.MINUTE, e.get_minute());
			time_before.set(Calendar.SECOND, e.get_second());

			Log.d(MainActivity.AppName, "for - id: " + e.get_id());

			// se l'alarm è già passato
			if (time_before.before(time_now)) {
				Log.d(MainActivity.AppName, "for - before ");
				// ed è un alarm di stop
				if (!e.get_actionType()) {
					int stop_id = e.get_id();
					int start_id = stop_id - 1;
					Log.d(MainActivity.AppName,
							"Algoritmo non attivo nell'intervallo: start-stop: "
									+ start_id + "-" + stop_id);

					String status = "";
					String after_mutation_string = "";

					if (e.isStepsInterval(lastDayIndex)) {
						// status="Intervallo con scalini";
						status = "S";
					} else {
						// status="Intervallo di esplorazione";
						status = "E";
					}

					if (e.getRepeatingDay(lastDayIndex)) {
						// status=status+" attivo";
						status = status + ",1";

						if (pref.getBoolean("next_alarm_mutated", false)) {
							// after_mutation_string=" dopo mutazione";
							after_mutation_string = "M";
							pref.edit().putBoolean("next_alarm_mutated", false)
									.commit();
						} else {
							after_mutation_string = "-";
						}
					} else {
						// status=status+" non attivo";
						status = status + ",0";
						after_mutation_string = "-";
					}

					/*
					 * writeLogFile(context,status+after_mutation_string+": " +
					 * previous_start_time+" - "+e.get_hour()+":"
					 * +e.get_minute()
					 * +":"+e.get_second()+" | "+not_evaluated_cause+" | "+
					 * status+" la prossima settimana");
					 */
					writeIntervalStatus(context, lastDayIndex, previous, e, "|"
							+ status + ";" + after_mutation_string + ";-("
							+ not_evaluated_cause + ");" + status);

				} else {
					previous_start_time = e.get_hour() + ":" + e.get_minute()
							+ ":" + e.get_second();
					previous = e;
				}
			} else { // l'alarm è valido, per cui l'intervallo potrà essere
						// valutato
						// (anche parzialmente se è già iniziato, cioè se
						// l'alarm è di stop)
				Log.d(MainActivity.AppName, "for - after ");
				stop = true;
			}
		}

		if (stop == false) {

			Log.d(MainActivity.AppName,
					"Algoritmo non attivo; si passa al giorno successivo");

			// ///////
			// PER TEST ALGORITMO: si inizializza l'indice artificiale
			int ii = lastDayIndex;

			// indice per scorrere il numero di giorni di differenza
			int day_i = 0;
			// se gli alarm del giorno precedente sono tutti passati, si
			// incrementa
			// il giorno
			while (day_i < days_diff) {

				time_before.add(Calendar.DATE, 1);

				// ///////
				// PER TEST ALGORITMO: si aggiorna l'indice artificiale man mano
				// che
				// si incrementa la data
				ii = AlarmUtils.getNextDayIndex(ii);
				// ///////
				// time_before.get(Calendar.DAY_OF_WEEK)-1

				Log.d(MainActivity.AppName,
						"Algoritmo non attivo, indice giorno: " + ii);

				// se si tratta del primo intervallo (id_start=1 e id_stop=2) si
				// è in
				// presenza di un nuovo giorno; si scrive il suo indice nel file
				// di output
				Alarm first_interval_stop = AlarmUtils.getAlarm(context, 2);
				time_before.set(Calendar.HOUR_OF_DAY,
						first_interval_stop.get_hour());
				time_before.set(Calendar.MINUTE,
						first_interval_stop.get_minute());
				time_before.set(Calendar.SECOND,
						first_interval_stop.get_second());
				if (time_before.before(time_now)) { // time_before.get(Calendar.DAY_OF_WEEK)-1;
					Log.d(MainActivity.AppName,
							"Intervals tracking - day index: " + ii);
					dd = time_before.get(Calendar.DATE);
					mm = time_before.get(Calendar.MONTH) + 1;
					yyyy = time_before.get(Calendar.YEAR);
					// writeLogFile(context,"Indice giorno: "+ii+" - "+dd+"/"+mm+"/"+yyyy);

					// se l'alarm di stop per il giorno considerato è passato, è
					// passato anche
					// il precedente alarm di start; quindi, si scrive
					// l'intervallo nel log file
					Alarm first_interval_start = AlarmUtils
							.getAlarm(context, 1);

					String status = "";
					String after_mutation_string = "";

					if (first_interval_stop.isStepsInterval(ii)) {
						// status="Intervallo con scalini";
						status = "S";
					} else {
						// status="Intervallo di esplorazione";
						status = "E";
					}

					if (first_interval_stop.getRepeatingDay(ii)) {
						// status=status+" attivo";
						status = status + ",1";

						if (pref.getBoolean("next_alarm_mutated", false)) {
							// after_mutation_string=" dopo mutazione";
							after_mutation_string = "M";
							pref.edit().putBoolean("next_alarm_mutated", false)
									.commit();
						} else {
							after_mutation_string = "-";
						}
					} else {
						// status=status+" non attivo";
						status = status + ",0";
						after_mutation_string = "-";
					}

					/*
					 * writeLogFile(context,status+after_mutation_string+": " +
					 * first_interval_start.get_hour()+":"+
					 * first_interval_start.
					 * get_minute()+":"+first_interval_start.get_second()+" - "+
					 * first_interval_stop
					 * .get_hour()+":"+first_interval_stop.get_minute()+":"+
					 * first_interval_stop
					 * .get_second()+" | "+not_evaluated_cause+" | "+
					 * status+" la prossima settimana");
					 */
					writeIntervalStatus(context, ii, first_interval_start,
							first_interval_stop, "|" + status + ";"
									+ after_mutation_string + ";-("
									+ not_evaluated_cause + ");" + status);
				}

				// si resettano ora, minuti e secondi
				time_before.set(Calendar.HOUR_OF_DAY, 0);
				time_before.set(Calendar.MINUTE, 0);
				time_before.set(Calendar.SECOND, 0);

				for (int i = 2; i < alarms_lst.size() && !stop; i++) {
					Alarm e = alarms_lst.get(i);

					time_before.set(Calendar.HOUR_OF_DAY, e.get_hour());
					time_before.set(Calendar.MINUTE, e.get_minute());
					time_before.set(Calendar.SECOND, e.get_second());

					// se l'alarm è già passato
					if (time_before.before(time_now)) {
						// ed è un alarm di stop
						if (!e.get_actionType()) {
							int stop_id = e.get_id();
							int start_id = stop_id - 1;
							Log.d(MainActivity.AppName,
									"Algoritmo non attivo nell'intervallo: start-stop: "
											+ start_id + "-" + stop_id);

							String status = "";
							String after_mutation_string = "";

							if (e.isStepsInterval(ii)) {
								// status="Intervallo con scalini";
								status = "S";
							} else {
								// status="Intervallo di esplorazione";
								status = "E";
							}

							if (e.getRepeatingDay(ii)) {
								// status=status+" attivo";
								status = status + ",1";

								if (pref.getBoolean("next_alarm_mutated", false)) {
									// after_mutation_string=" dopo mutazione";
									after_mutation_string = "M";
									pref.edit()
											.putBoolean("next_alarm_mutated",
													false).commit();
								} else {
									after_mutation_string = "-";
								}
							} else {
								// status=status+" non attivo";
								status = status + ",0";
								after_mutation_string = "-";
							}

							/*
							 * writeLogFile(context,status+after_mutation_string+
							 * ": " + previous_start_time+" - "+e.get_hour()+":"
							 * +e.get_minute()+":"+e.get_second()+" | "+
							 * not_evaluated_cause+" | "+
							 * status+" la prossima settimana");
							 */
							writeIntervalStatus(context, ii, previous, e, "|"
									+ status + ";" + after_mutation_string
									+ ";-(" + not_evaluated_cause + ");"
									+ status);

						} else {
							previous_start_time = e.get_hour() + ":"
									+ e.get_minute() + ":" + e.get_second();
							previous = e;
						}
					} else { // l'alarm è valido, per cui l'intervallo potrà
								// essere valutato
								// (anche parzialmente se è già iniziato, cioè
								// se l'alarm è di stop)
						stop = true;
					}
				}

				day_i++;
			}
		}
	}

	public static void initLogFile(Context context, List<Alarm> alarms) {

		String log_file_name = "";
		int log_file_id = PreferenceManager
				.getDefaultSharedPreferences(context).getInt("log_file_id", -1);

		if (log_file_id == -1) {
			log_file_name = "algorithm_log";
		} else {
			log_file_name = "algorithm_log_" + log_file_id;
		}

		final File logFile = new File(context.getDir("climbTheWorld_dir",
				Context.MODE_PRIVATE), log_file_name);

		try {
			if (!logFile.exists()) {
				Log.e(MainActivity.AppName, "Log file not exists");
				logFile.createNewFile();
			}

			// 'true' per aggiungere il testo al file esistente
			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile,
					true));

			if (alarms != null) {
				buf.append("LOG ALGORITMO");
				buf.newLine();
				buf.newLine();

				for (int i = 0; i < GeneralUtils.daysOfWeek; i++) { // i<7 con
																	// settimana
																	// normale

					buf.append("Indice giorno: " + i);
					buf.newLine();

					for (int j = 0; j < alarms.size(); j = j + 2) {

						Alarm start = alarms.get(j);
						Alarm stop = alarms.get(j + 1);

						buf.append(i + " - "
								+ String.format("%02d", start.get_hour()) + ":"
								+ String.format("%02d", start.get_minute())
								+ ":"
								+ String.format("%02d", start.get_second())
								+ "-" + String.format("%02d", stop.get_hour())
								+ ":"
								+ String.format("%02d", stop.get_minute())
								+ ":"
								+ String.format("%02d", stop.get_second())
								+ " : ");
						buf.newLine();
					}
				}
			} else {
				buf.append("NON FAI MAI ATTIVITA' FISICA/SCALINI: NON E' STATO CREATO ALCUN INTERVALLO");
			}

			buf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void writeIntervalStatus(Context context, int day_index,
			Alarm start, Alarm stop, String text) {

		String log_file_name = "";
		int log_file_id = PreferenceManager
				.getDefaultSharedPreferences(context).getInt("log_file_id", -1);

		if (log_file_id == -1) {
			log_file_name = "algorithm_log";
		} else {
			log_file_name = "algorithm_log_" + log_file_id;
		}

		final File logFile = new File(context.getDir("climbTheWorld_dir",
				Context.MODE_PRIVATE), log_file_name);

		String interval = "";
		if (start != null && stop != null) {
			interval = String.format("%02d", start.get_hour()) + ":"
					+ String.format("%02d", start.get_minute()) + ":"
					+ String.format("%02d", start.get_second()) + "-"
					+ String.format("%02d", stop.get_hour()) + ":"
					+ String.format("%02d", stop.get_minute()) + ":"
					+ String.format("%02d", stop.get_second());
		}

		try {
			BufferedReader buf_r = new BufferedReader(new FileReader(logFile));
			String line;
			String input = "";
			while ((line = buf_r.readLine()) != null) {

				int charIndex = line.indexOf(" :");

				if (charIndex != -1
						&& (line.substring(0, charIndex)).equals(day_index
								+ " - " + interval)) {

					line = line.replace(line, line + text);
				}
				input += line + System.getProperty("line.separator");
			}

			buf_r.close();

			// dopo la modifica, si sovrascrive il file (parametro 'append'
			// messo a 'false')
			BufferedWriter buf_w = new BufferedWriter(new FileWriter(logFile,
					false));
			buf_w.write(input);
			buf_w.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// ---------------------------LOG
	// SILVIA--------------------------------------

	public static void initGameLog(Context context, String username) {

		String log_file_name = "";
		int log_file_id = PreferenceManager
				.getDefaultSharedPreferences(context).getInt("log_game_file_id", -1);

		if (log_file_id == -1) {
			log_file_name = "game_log";
		} else {
			log_file_name = "game_log_" + log_file_id;
		}

		final File logFile = new File(context.getDir("climbTheWorld_dir",
				Context.MODE_PRIVATE), log_file_name);

		try {
			if (!logFile.exists()) {
				Log.e(MainActivity.AppName, "Log file not exists");
				logFile.createNewFile();
			}

			// 'true' per aggiungere il testo al file esistente
			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile,
					true));

			buf.append("LOG GAME");
			buf.newLine();
			buf.newLine();
			buf.append("USER " + username);
			buf.newLine();

			buf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public static void writeGameUpdate(Context context, String text) {

		String log_file_name = "";
		int log_file_id = PreferenceManager
				.getDefaultSharedPreferences(context).getInt("log_game_file_id", -1);

		if (log_file_id == -1) {
			log_file_name = "game_log";
		} else {
			log_file_name = "game_log" + log_file_id;
		}

		final File logFile = new File(context.getDir("climbTheWorld_dir",
				Context.MODE_PRIVATE), log_file_name);

		try {
			if (!logFile.exists()) {
				Log.e(MainActivity.AppName, "Log file not exists");
				logFile.createNewFile();
			}

			// 'true' per aggiungere il testo al file esistente
			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile,
					true));
			buf.append(text);
			buf.newLine();
			buf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}