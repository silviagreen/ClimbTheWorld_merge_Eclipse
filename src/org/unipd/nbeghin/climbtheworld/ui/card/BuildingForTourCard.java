package org.unipd.nbeghin.climbtheworld.ui.card;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.unipd.nbeghin.climbtheworld.ClimbApplication;
import org.unipd.nbeghin.climbtheworld.MainActivity;
import org.unipd.nbeghin.climbtheworld.R;
import org.unipd.nbeghin.climbtheworld.models.Building;
import org.unipd.nbeghin.climbtheworld.models.BuildingText;
import org.unipd.nbeghin.climbtheworld.models.Climbing;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.fima.cardsui.objects.Card;

/**
 * CardsUI card for a single building associated to a tour
 *	UNUSED
 */
public class BuildingForTourCard extends Card {
	final private Building	building;
	final private int		order;
	private BuildingText buildingText;
	public BuildingForTourCard(Building building, int order) {
		super(building.getName());
		this.building = building;
		this.order = order;
	}

	@Override
	public View getCardContent(Context context) {
		buildingText = ClimbApplication.getBuildingTextByBuilding(building.get_id());
		View view = LayoutInflater.from(context).inflate(R.layout.card_building_for_tour_ex, null);
		((TextView) view.findViewById(R.id.title)).setText(buildingText.getName());
		int imageId = context.getResources().getIdentifier(building.getPhoto(), "drawable", context.getPackageName());
		if (imageId > 0) ((ImageView) view.findViewById(R.id.photo)).setImageResource(imageId);
		((TextView) view.findViewById(R.id.buildingStat)).setMinLines(2);
		((TextView) view.findViewById(R.id.buildingStat)).setText(building.getSteps() + " " + ClimbApplication.getContext().getString(R.string.steps) + building.getHeight() + "m)"
				+ "\n" + ClimbApplication.getContext().getString(R.string.reward, ClimbApplication.XPforStep(building.getSteps(), false)));

		((TextView) view.findViewById(R.id.location)).setText(buildingText.getLocation());
		((TextView) view.findViewById(R.id.description)).setText(buildingText.getDescription());
		((TextView) view.findViewById(R.id.tourOrder)).setText(Integer.toString(order));
		TextView climbingStatus = (TextView) view.findViewById(R.id.climbingStatus);
		Climbing climbing = ClimbApplication.getClimbingForBuilding(building.get_id());
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy.MM.dd");
		if (climbing != null) {
			if (climbing.getPercentage() >= 100) {
				climbingStatus.setText(ClimbApplication.getContext().getString(R.string.climb_complete, sdf.format(new Date(climbing.getModified()))));
			} else {
				climbingStatus.setText(ClimbApplication.getContext().getString(R.string.climb_status, new DecimalFormat("#.##").format(climbing.getPercentage()*100), sdf.format(new Date(climbing.getModified()))));
			}
		} else {
			climbingStatus.setText(ClimbApplication.getContext().getString(R.string.notClimbedYet));
		}
		return view;
	}

	@Override
	public boolean convert(View convertCardView) {
		// TODO Auto-generated method stub
		return false;
	}
}
