package org.unipd.nbeghin.climbtheworld.comparator;

import java.util.Comparator;

import org.unipd.nbeghin.climbtheworld.models.BuildingText;

public class BuildingTextComparator implements Comparator<BuildingText>{

	@Override
	public int compare(BuildingText lhs, BuildingText rhs) {
			if(lhs.getBuilding().getBase_level() == rhs.getBuilding().getBase_level())
				return 0;
			else
				return lhs.getBuilding().getBase_level() > rhs.getBuilding().getBase_level() ? 1 : -1;
		
	}

}
