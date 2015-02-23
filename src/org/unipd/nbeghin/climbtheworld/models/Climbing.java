package org.unipd.nbeghin.climbtheworld.models;

import java.util.Observable;
import java.util.concurrent.TimeUnit;

import org.unipd.nbeghin.climbtheworld.ClimbApplication;
import org.unipd.nbeghin.climbtheworld.R;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "climbings")
public class Climbing extends Observable{
	@DatabaseField(generatedId = true)
	private int			_id;
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, columnName = "building_id")
	private Building	building;
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, columnName = "users_id")
	private User user;
	@DatabaseField
	private int			completed_steps;
	@DatabaseField
	private int			remaining_steps;
	@DatabaseField
	private double		percentage;
	@DatabaseField
	private long		created;
	@DatabaseField
	private long		modified;
	@DatabaseField
	private long		completed;
	@DatabaseField
	private int				game_mode;
	@DatabaseField
	private int 		saved;
	@DatabaseField
	private String id_mode;
	@DatabaseField
	private int deleted;
	@DatabaseField
	private String id_online;
	@DatabaseField
	private int checked;
	

	public String getId_mode() {
		return id_mode;
	}

	public void setId_mode(String id_mode) {
		this.id_mode = id_mode;
	}

	public int getGame_mode() {
		return game_mode;
	}

	public void setGame_mode(int game_mode) {
		this.game_mode = game_mode;
	}
	

	public Climbing() {} // needed by ormlite

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public Building getBuilding() {
		return building;
	}

	public void setBuilding(Building building) {
		this.building = building;
	}

	public int getCompleted_steps() {
		return completed_steps;
	}

	public void setCompleted_steps(int completed_steps) {
		this.completed_steps = completed_steps;
	}

	public int getRemaining_steps() {
		return remaining_steps;
	}

	public boolean isCompleted() {
		return this.completed > 0;
	}

	public String getFBStatusMessage(String building_name) {
		switch (game_mode) {
		case 0:
				return ClimbApplication.getContext().getString(R.string.solo_climb_win_post, building_name, completed_steps);
		case 1:
				return ClimbApplication.getContext().getString(R.string.social_climb_win_post, building_name);
		case 2:
				return ClimbApplication.getContext().getString(R.string.social_challenge_win_post, building_name);
		case 3:
				return ClimbApplication.getContext().getString(R.string.team_vs_team_win_post, building_name);
		default:
				return "";

		}
	}
	
	public String getUpdateMessage(int newSteps, String building_name){
		switch (game_mode) {
		case 0:
				return ClimbApplication.getContext().getString(R.string.solo_climb_improve_post, building_name, newSteps);
		case 1:
				return ClimbApplication.getContext().getString(R.string.social_climb_improve_post, building_name, newSteps);
		case 2:
				return ClimbApplication.getContext().getString(R.string.social_challenge_improve_post, building_name, newSteps);
		case 3:
				return ClimbApplication.getContext().getString(R.string.team_vs_team_improve_post, building_name, newSteps);
		default:
				return "";

		}
	}

	public String totalTime() {
		long diff = 0;
		if (isCompleted() == false) diff = this.modified - this.created; // not completed
		else
			diff = this.completed - this.created; // completed
		return String.format("%dm%ds", TimeUnit.MILLISECONDS.toMinutes(diff), TimeUnit.MILLISECONDS.toSeconds(diff) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(diff)));
	}

	public void setRemaining_steps(int remaining_steps) {
		this.remaining_steps = remaining_steps;
	}

	public double getPercentage() {
		return percentage;
	}

	public void setPercentage(double percentage2) {
		this.percentage = percentage2;
		if(completed == 0)
			changedState(this);
	}

	public long getCreated() {
		return created;
	}

	public void setCreated(long created) {
		this.created = created;
	}

	public long getModified() {
		return modified;
	}

	public void setModified(long modified) {
		this.modified = modified;
	}

	public long getCompleted() {
		return completed;
	}

	public void setCompleted(long completed) {
		this.completed = completed;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	public void setSaved(boolean saved){
		if(saved){
			this.saved = 1;
		}else{
			this.saved = 0;
		}
	}
	
	public boolean isSaved(){
		if(saved == 1)
			return true;
		else
			return false;
	}
	
	public void setDeleted(boolean deleted){
		if(deleted){
			this.deleted = 1;
		}else{
			this.deleted = 0;
		}
	}
	
	public boolean isDeleted(){
		if(deleted == 1)
			return true;
		else
			return false;
	}

	public String getId_online() {
		return id_online;
	}

	public void setId_online(String id_online) {
		this.id_online = id_online;
	}
	
	public void setChecked(boolean checked){
		if(checked){
			this.checked = 1;
		}else{
			this.checked = 0;
		}
	}
	
	public boolean isChecked(){
		if(checked == 1)
			return true;
		else
			return false;
	}
	
	private void changedState(Object obj){
		setChanged();
		notifyObservers(obj);
	}
}
