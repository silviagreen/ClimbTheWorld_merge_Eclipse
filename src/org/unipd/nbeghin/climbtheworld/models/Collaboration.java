package org.unipd.nbeghin.climbtheworld.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "collaborations")
public class Collaboration {
	@DatabaseField(generatedId = true)
	private int _id;
	@DatabaseField
	private String id_online;	
/*	@DatabaseField
	private String groupId;*/
	@DatabaseField
	private int my_stairs;
	@DatabaseField
	private int others_stairs;
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, columnName = "building_id")
	private Building	 building;
/*	@DatabaseField
	private String group_name;*/
	@DatabaseField
	private int saved;
	@DatabaseField
	private int leaved;
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, columnName = "user_id")
	private User	 user;
	@DatabaseField
	private int completed;
	@DatabaseField
	private int amICreator;

	public Collaboration(){}
	
	public User getUser() {
		return user;
	}



	public void setUser(User user) {
		this.user = user;
	}



	public String getId() {
		return id_online;
	}

	public void setId(String id) {
		this.id_online = id;
	}

/*	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}*/

	public int getMy_stairs() {
		return my_stairs;
	}

	public void setMy_stairs(int my_stairs) {
		this.my_stairs = my_stairs;
	}

	public int getOthers_stairs() {
		return others_stairs;
	}

	public void setOthers_stairs(int others_stairs) {
		this.others_stairs = others_stairs;
	}

	public Building getBuilding() {
		return building;
	}

	public void setBuilding(Building building) {
		this.building = building;
	}

/*	public String getGroup_name() {
		return group_name;
	}

	public void setGroup_name(String group_name) {
		this.group_name = group_name;
	}*/
	
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
	
	public void setLeaved(boolean leaved){
		if(leaved){
			this.leaved = 1;
		}else{
			this.leaved = 0;
		}
	}
	
	public boolean isLeaved(){
		if(leaved == 1)
			return true;
		else
			return false;
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
	
	public boolean getAmICreator() {
		if(amICreator == 1)
			return true;
		else
			return false;
	}


	public void setAmICreator(boolean amICreator) {
		if(amICreator)
			this.amICreator = 1;
		else
			this.amICreator = 0;
	}
	
}
