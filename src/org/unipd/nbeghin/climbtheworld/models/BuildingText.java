package org.unipd.nbeghin.climbtheworld.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "building_texts")
public class BuildingText {
	@DatabaseField(generatedId = true)
	private int				_id;
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, columnName="building_id")
	private Building			building;
	@DatabaseField
	private String 			language;
	@DatabaseField
	private String			name;
	@DatabaseField
	private String			location;
	@DatabaseField
	private String			description;
	
	public BuildingText(){}
	
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
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	
	
}
