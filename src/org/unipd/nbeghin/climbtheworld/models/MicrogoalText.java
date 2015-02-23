package org.unipd.nbeghin.climbtheworld.models;

import org.json.JSONException;
import org.json.JSONObject;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "micro_goal_texts")
public class MicrogoalText {
	@DatabaseField(generatedId = true)
	private int _id;
	@DatabaseField
	private String language;
	@DatabaseField
	private String intro;
	@DatabaseField
	private String steps;
	@DatabaseField
	private int story_id;
	
	public MicrogoalText() {	}

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getIntro() {
		return intro;
	}

	public void setIntro(String intro) {
		this.intro = intro;
	}

	public JSONObject getSteps() throws JSONException {
		String str1 = steps.replaceAll("\u201C", "\\\"");//“
		String str2 = str1.replaceAll("\u201D", "\\\"");//”
		return new JSONObject(str2);
	}

	public void setSteps(JSONObject steps) {
		this.steps = steps.toString();
	}


	public int getStory_id() {
		return story_id;
	}

	public void setStory_id(int microgoal) {
		this.story_id = microgoal;
	}
	
	
}
