package org.unipd.nbeghin.climbtheworld.comparator;

import java.util.Comparator;

import org.unipd.nbeghin.climbtheworld.models.UserBadge;

public class UserBadgeComperator implements Comparator<UserBadge> {

	@Override
	public int compare(UserBadge lhs, UserBadge rhs) {
		if(lhs.getPercentage() == rhs.getPercentage())
			return 0;
		else
			return lhs.getPercentage() < rhs.getPercentage() ? 1 : -1;
	}


}
