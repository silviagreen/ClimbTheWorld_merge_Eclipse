package org.unipd.nbeghin.climbtheworld.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "badges")
public class Badge {
	@DatabaseField(generatedId = true)
	private int _id;
	@DatabaseField
	private int category;
	@DatabaseField
	private int reward; //XP
	@DatabaseField
	private int n_steps; //for cotegory "steps to complete"
	@DatabaseField
	private int multiplier; // how many times conditions has to match to obtain reward
	
	public Badge(){}

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public BadgeCategory getCategory() {
		switch(category){
		case 0:
			return BadgeCategory.BUILDING_COMPLETED;
		case 1: 
			return BadgeCategory.TOUR_COMPLETED;
		case 2:
			return BadgeCategory.STEPS_COMPLETED;
		default:
			return BadgeCategory.BUILDING_COMPLETED;
		}
	}

	public void setCategory(int category) {
		this.category = category;
	}

	public int getReward() {
		return reward;
	}

	public void setReward(int reward) {
		this.reward = reward;
	}

	public int getN_steps() {
		return n_steps;
	}

	public void setN_steps(int n_steps) {
		this.n_steps = n_steps;
	}

	public int getMultiplier() {
		return multiplier;
	}

	public void setMultiplier(int multiplier) {
		this.multiplier = multiplier;
	}
	
	
	
	
	
}
