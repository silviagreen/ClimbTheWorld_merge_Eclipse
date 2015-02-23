package org.unipd.nbeghin.climbtheworld.fragments;

import java.util.List;

import org.unipd.nbeghin.climbtheworld.ClimbActivity;
import org.unipd.nbeghin.climbtheworld.ClimbApplication;
import org.unipd.nbeghin.climbtheworld.R;
import org.unipd.nbeghin.climbtheworld.TeamPreparationActivity;
import org.unipd.nbeghin.climbtheworld.models.Building;
import org.unipd.nbeghin.climbtheworld.models.BuildingText;
import org.unipd.nbeghin.climbtheworld.models.Climbing;
import org.unipd.nbeghin.climbtheworld.ui.card.BuildingCard;
import org.unipd.nbeghin.climbtheworld.ui.card.BuildingForTourCard;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.fima.cardsui.views.CardUI;

/**
 * Show a list of buildings for a given tour
 *
 */
public class BuildingsForTourFragment extends Fragment{
	public static final String	building_text_intent_object	= "org.unipd.nbeghin.climbtheworld.intents.object.buildingText";
	public CardUI				buildingCards;
	final SharedPreferences pref = ClimbApplication.getContext().getSharedPreferences("UserSession", 0);

	public void loadBuildings(List<BuildingText> buildings) {
		buildingCards.clearCards();
		int i=0;
		for (final BuildingText building : buildings) {
			i++;
			BuildingCard buildingCard = new BuildingCard(building, getActivity(), i);
			
			if (building.getBuilding().getBase_level() <= ClimbApplication.getUserById(pref.getInt("local_id", -1)).getLevel()) {
			
			buildingCard.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) { // start climbing for a given building
					final SharedPreferences pref = ClimbApplication.getContext().getSharedPreferences("UserSession", 0);
					if (pref.getInt("local_id", -1) != -1) {
						List<Climbing> climbs = ClimbApplication.getClimbingListForBuildingAndUser(building.get_id(), pref.getInt("local_id", -1));
						if ((climbs.size() == 2 && (climbs.get(0).getGame_mode() == 3 || climbs.get(1).getGame_mode() == 3)) || (climbs.size() == 1 && climbs.get(0).getGame_mode() == 3)) {
							Climbing climb = climbs.get(0).getGame_mode() == 3 ? climbs.get(0) : climbs.get(1);
							if (climb.getId_mode() == null || climb.getId_mode().equals(""))
								Toast.makeText(getActivity(), "Connect to save data online before playing", Toast.LENGTH_SHORT).show();
							else {
								Log.i("Building Fragment", "Building id clicked: " + building.get_id());
								Intent intent = new Intent(getActivity().getApplicationContext(), TeamPreparationActivity.class);
								intent.putExtra(ClimbApplication.counter_mode, false);
								intent.putExtra(ClimbApplication.building_text_intent_object, building.get_id());
								getActivity().startActivity(intent);
							}
						} else {
							Log.i("Building Fragment", "Building id clicked: " + building.get_id());
							Intent intent = new Intent(getActivity().getApplicationContext(), ClimbActivity.class);
							intent.putExtra(ClimbApplication.counter_mode, false);
							intent.putExtra(ClimbApplication.building_text_intent_object, building.get_id());
							startActivity(intent);
						}

					} else {
						Log.i("Building Fragment", "Building id clicked: " + building.get_id());
						Intent intent = new Intent(getActivity().getApplicationContext(), ClimbActivity.class);
						intent.putExtra(ClimbApplication.counter_mode, false);
						intent.putExtra(ClimbApplication.building_text_intent_object, building.get_id());
						startActivity(intent);
					}
				}
			});
			
			} else {

				buildingCard.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// 1. Instantiate an AlertDialog.Builder with its
						// constructor
						AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

						// 2. Chain together various setter methods to set the
						// dialog characteristics
						builder.setMessage(getString(R.string.lock_level_msg, building.getBuilding().getBase_level()))
							.setTitle(R.string.lock_level_title)
							.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
								}
							});

						// 3. Get the AlertDialog from create()
						AlertDialog dialog = builder.create();
						dialog.show();
					}
				});
			}
			buildingCards.addCard(buildingCard);
		}
		buildingCards.refresh();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View result = inflater.inflate(R.layout.fragment_buildings, container, false);
		buildingCards = (CardUI) result.findViewById(R.id.cardsBuildings);
		buildingCards.setSwipeable(false);
		return (result);
	}
}
