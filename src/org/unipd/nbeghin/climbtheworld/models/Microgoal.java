package org.unipd.nbeghin.climbtheworld.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "micro_goals")
public class Microgoal {
	@DatabaseField(generatedId = true)
	private int _id;
	@DatabaseField
	private int story_id;
	@DatabaseField
	private int tot_steps;
	@DatabaseField
	private int done_steps;
	@DatabaseField
	private int saved;
	@DatabaseField
	private int deleted;
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, columnName = "building_id")
	private Building building;
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, columnName = "user_id")
	private User user;
	@DatabaseField
	private int reward;
	
	public Microgoal() {
		// TODO Auto-generated constructor stub
	}

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public int getStory_id() {
		return story_id;
	}

	public void setStory_id(int story_id) {
		this.story_id = story_id;
	}

	public int getTot_steps() {
		return tot_steps;
	}

	public void setTot_steps(int tot_steps) {
		this.tot_steps = tot_steps;
	}

	public int getDone_steps() {
		return done_steps;
	}

	public void setDone_steps(int done_steps) {
		this.done_steps = done_steps;
	}

	public boolean getSaved() {
		if(saved == 0)
			return false;
		else
			return true;
	}

	public void setSaved(boolean saved) {
		if(saved)
			this.saved = 1;
		else
			this.saved = 0;
	}

	public boolean getDeleted() {
		if(deleted == 0)
			return false;
		else
			return true;
	}

	public void setDeleted(boolean deleted) {
		if(deleted)
			this.deleted = 1;
		else
			this.deleted = 0;
	}

	public Building getBuilding() {
		return building;
	}

	public void setBuilding(Building building) {
		this.building = building;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public int getReward() {
		return reward;
	}

	public void setReward(int reward) {
		this.reward = reward;
	}
	
	
}
