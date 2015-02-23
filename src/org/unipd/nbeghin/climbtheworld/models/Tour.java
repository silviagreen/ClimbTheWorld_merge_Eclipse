package org.unipd.nbeghin.climbtheworld.models;

import java.util.List;

import org.unipd.nbeghin.climbtheworld.ClimbApplication;

import android.content.SharedPreferences;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * A set of pre-determined buildings to be climbed one after the other
 *
 */
@DatabaseTable(tableName = "tours")
public class Tour {
	@DatabaseField(generatedId = true)
	private int		_id;
	@DatabaseField
	private String	title;
	@DatabaseField
	private String	description;
	@DatabaseField
	private int		num_buildings;

	Tour() {} // needed by ormlite

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getNum_buildings() {
		return num_buildings;
	}

	public void setNum_buildings(int num_buildings) {
		this.num_buildings = num_buildings;
	}
	
	public int getTotalSteps(){
		int total = 0;
		List<BuildingTour> buildingsTour = ClimbApplication.getBuildingsForTour(_id);
		for(BuildingTour bt : buildingsTour){
			total += bt.getBuilding().getSteps();
		}
		return total;

	}
	
	public int getDoneSteps(int new_steps, int currentBuilding_id){
		SharedPreferences pref = ClimbApplication.getContext().getSharedPreferences("UserSession", 0);
		int total = 0;
		List<BuildingTour> buildingsTour = ClimbApplication.getBuildingsForTour(_id);
		for(BuildingTour bt : buildingsTour){
			if(bt.get_id() != currentBuilding_id){
				Climbing climb = ClimbApplication.getClimbingForBuildingAndUser(bt.getBuilding().get_id(), pref.getInt("local_id", -1));
				if(climb != null) total += climb.getCompleted_steps();
			}else
				total += new_steps;
		}
		return total;
	}
	
}
