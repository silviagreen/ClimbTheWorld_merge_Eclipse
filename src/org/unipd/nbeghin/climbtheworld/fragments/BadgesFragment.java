package org.unipd.nbeghin.climbtheworld.fragments;

import java.util.Collections;

import org.unipd.nbeghin.climbtheworld.ClimbApplication;
import org.unipd.nbeghin.climbtheworld.R;
import org.unipd.nbeghin.climbtheworld.comparator.UserBadgeComperator;
import org.unipd.nbeghin.climbtheworld.models.UserBadge;
import org.unipd.nbeghin.climbtheworld.ui.card.BadgeCard;
import org.unipd.nbeghin.climbtheworld.ui.card.Updater;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fima.cardsui.views.CardUI;


public class BadgesFragment extends Fragment implements Updater{
	public CardUI badgeCards;
	
	@Override
	public void refresh() {
		Collections.sort(ClimbApplication.userBadges, new UserBadgeComperator());
		badgeCards.clearCards();
		for (final UserBadge badge : ClimbApplication.userBadges) {
			BadgeCard badgeCard = new BadgeCard(badge);
			badgeCards.addCard(badgeCard);
		}
			badgeCards.refresh();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View result = inflater.inflate(R.layout.fragment_badges, container, false);
		badgeCards = (CardUI) result.findViewById(R.id.cardsBadges);
		badgeCards.setSwipeable(false);
		refresh();
		return (result);
	}

	@Override
	public void onResume() {
		super.onResume();
		refresh();
	}

	
}
