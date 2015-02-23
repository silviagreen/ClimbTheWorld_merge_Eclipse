package org.unipd.nbeghin.climbtheworld.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "team_duel")
public class TeamDuel {
	@DatabaseField(generatedId = true)
	private int			_id;
	@DatabaseField
	private String challenger_name;
	@DatabaseField
	private int steps_my_group;
	@DatabaseField
	private int steps_other_group;
	@DatabaseField
	private int saved;
	@DatabaseField
	private String id_online;
	@DatabaseField
	private int creator;
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, columnName = "building_id")
	private Building building;
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, columnName = "user_id")
	private User user;
	@DatabaseField
	private int my_steps;
	@DatabaseField
	private int deleted;
	@DatabaseField
	private String creator_name;
	@DatabaseField
	private int challenger;
	@DatabaseField
	private int mygroup; // 0 -> user belongs to creator's group
						 // 1 -> user belongs to challenger's group
	@DatabaseField
	private int completed;
	@DatabaseField
	private int ready_to_play;
	@DatabaseField
	private int checks;
	@DatabaseField
	private long victory_time;
	@DatabaseField
	private String winner_id;
	
	public TeamDuel(){}
	
	public int get_id() {
		return _id;
	}
	public void set_id(int _id) {
		this._id = _id;
	}
	public int getSteps_my_group() {
		return steps_my_group;
	}
	public void setSteps_my_group(int steps_my_group) {
		this.steps_my_group = steps_my_group;
	}
	public int getSteps_other_group() {
		return steps_other_group;
	}
	public void setSteps_other_group(int steps_other_group) {
		this.steps_other_group = steps_other_group;
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

	public String getId_online() {
		return id_online;
	}

	public void setId_online(String id_online) {
		this.id_online = id_online;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Building getBuilding() {
		return building;
	}

	public void setBuilding(Building building) {
		this.building = building;
	}

	public void setCreator(boolean creator){
		if(creator){
			this.creator = 1;
		}else{
			this.creator = 0;
		}
	}
	
	public boolean isCreator(){
		if(creator == 1)
			return true;
		else
			return false;
	}

	public String getChallenger_name() {
		return challenger_name;
	}

	public void setChallenger_name(String challenger_name) {
		this.challenger_name = challenger_name;
	}

	public int getMy_steps() {
		return my_steps;
	}

	public void setMy_steps(int my_steps) {
		this.my_steps = my_steps;
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

	public String getCreator_name() {
		return creator_name;
	}

	public void setCreator_name(String creator_name) {
		this.creator_name = creator_name;
	}
	
	public void setChallenger(boolean challenger){
		if(challenger){
			this.challenger = 1;
		}else{
			this.challenger = 0;
		}
	}
	
	public boolean isChallenger(){
		if(challenger == 1)
			return true;
		else
			return false;
	}
	
	public void setMygroup(Group group){
		if(group == Group.CHALLENGER)
			mygroup = 1;
		else mygroup = 0;
	}
	
	public Group getMygroup(){
		if(mygroup == 0)
			return Group.CREATOR;
		else return Group.CHALLENGER;
	}
	public void setCompleted(boolean completed){
		if(completed){
			this.completed = 1;
		}else{
			this.completed = 0;
		}
	}
	
	public boolean isCompleted(){
		if(completed == 1)
			return true;
		else
			return false;
	}
	
	public void setReadyToPlay(boolean ready_to_play){
		if(ready_to_play){
			this.ready_to_play = 1;
		}else{
			this.ready_to_play = 0;
		}
	}
	
	public boolean isReadyToPlay(){
		if(ready_to_play == 1)
			return true;
		else
			return false;
	}

	public int getChecks() {
		return checks;
	}

	public void setChecks(int checks) {
		this.checks = checks;
	}

	public long getVictory_time() {
		return victory_time;
	}

	public void setVictory_time(long victory_time) {
		this.victory_time = victory_time;
	}

	public String getWinner_id() {
		return winner_id;
	}

	public void setWinner_id(String winner_id) {
		this.winner_id = winner_id;
	}
	
	
}
