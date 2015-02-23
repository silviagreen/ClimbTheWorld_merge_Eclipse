package org.unipd.nbeghin.climbtheworld.comparator;

import java.util.Calendar;
import java.util.Comparator;

import org.unipd.nbeghin.climbtheworld.models.Alarm;

public class AlarmComparator implements Comparator<Alarm> {

	@Override
	public int compare(Alarm lhs, Alarm rhs) {
		
		Calendar xcal = Calendar.getInstance();
		Calendar ycal = Calendar.getInstance();
		
		xcal.set(Calendar.HOUR_OF_DAY, lhs.get_hour());
		xcal.set(Calendar.MINUTE, lhs.get_minute());
		xcal.set(Calendar.SECOND, lhs.get_second());
		
		ycal.set(Calendar.HOUR_OF_DAY, rhs.get_hour());
		ycal.set(Calendar.MINUTE, rhs.get_minute());
		ycal.set(Calendar.SECOND, rhs.get_second());
		
		if ( xcal.before(ycal) ) return -1;
	    if ( xcal.after(ycal) ) return 1;
		return 0;
	}

}
