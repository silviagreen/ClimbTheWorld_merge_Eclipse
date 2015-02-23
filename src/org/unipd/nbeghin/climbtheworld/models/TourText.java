package org.unipd.nbeghin.climbtheworld.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "tour_texts")
public class TourText {
	@DatabaseField(generatedId = true)
	private int			_id;
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, columnName="tour_id")
	private Tour		tour;
	@DatabaseField
	private String language;
	@DatabaseField
	private String title;
	@DatabaseField
	private String description;
	
	public TourText() {}

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public Tour getTour() {
		return tour;
	}

	public void setTour(Tour tour) {
		this.tour = tour;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
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

	public void setDescrption(String description) {
		this.description = description;
	}
	
	
}
