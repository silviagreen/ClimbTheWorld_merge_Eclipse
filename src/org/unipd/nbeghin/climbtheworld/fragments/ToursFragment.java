package org.unipd.nbeghin.climbtheworld.fragments;

import org.unipd.nbeghin.climbtheworld.ClimbApplication;
import org.unipd.nbeghin.climbtheworld.MainActivity;
import org.unipd.nbeghin.climbtheworld.R;
import org.unipd.nbeghin.climbtheworld.TourDetailActivity;
import org.unipd.nbeghin.climbtheworld.models.Tour;
import org.unipd.nbeghin.climbtheworld.models.TourText;
import org.unipd.nbeghin.climbtheworld.ui.card.TourCard;
import org.unipd.nbeghin.climbtheworld.ui.card.Updater;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.fima.cardsui.views.CardUI;

/**
 * Show a list of tours
 *
 */
public class ToursFragment extends Fragment implements Updater{
	public static final String	tour_intent_object	= "org.unipd.nbeghin.climbtheworld.intents.object.tour";
	public CardUI				toursCards;

	private class LoadToursTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... unused) {
			refresh();
			return null;
		}
	}

	@Override
	public void refresh() {
		toursCards.clearCards();
		for (final TourText tour : ClimbApplication.tourTexts) {
			TourCard tourCard = new TourCard(tour, getActivity());
			tourCard.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getActivity().getApplicationContext(), TourDetailActivity.class);
					intent.putExtra(tour_intent_object, tour.getTour().get_id());
					startActivity(intent);
				}
			});
			
			toursCards.addCard(tourCard);
		}
		toursCards.refresh();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View result = inflater.inflate(R.layout.fragment_tours, container, false);
		toursCards = (CardUI) result.findViewById(R.id.cardsTours);
		toursCards.setSwipeable(false);
		refresh();
		return (result);
	}

	@Override
	public void onResume() {
		super.onResume();
		refresh();
	}
}
