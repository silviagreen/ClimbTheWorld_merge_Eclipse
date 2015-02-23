package org.unipd.nbeghin.climbtheworld.comparator;

import java.util.Comparator;

import org.unipd.nbeghin.climbtheworld.models.Building;

public class BuildingComparator implements Comparator<Building> {

	@Override
	public int compare(Building lhs, Building rhs) {
		if(lhs.getBase_level() == rhs.getBase_level())
			return 0;
		else
			return lhs.getBase_level() > rhs.getBase_level() ? 1 : -1;
	}

}
