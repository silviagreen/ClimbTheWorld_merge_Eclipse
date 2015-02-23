package org.unipd.nbeghin.climbtheworld.models;

import java.io.Serializable;
import java.util.Observable;

import org.unipd.nbeghin.climbtheworld.ClimbApplication;
import org.unipd.nbeghin.climbtheworld.R;

import android.util.Log;
import android.widget.Toast;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

@DatabaseTable(tableName = "users")
public class User extends Observable implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@DatabaseField(generatedId = true)
	private int				_id;
	@DatabaseField
	private String			FBid;
	@DatabaseField
	private String			name;
	@DatabaseField
	private int				XP;
	@DatabaseField
	private int				level;
	@DatabaseField
	private int 				owner;
	@DatabaseField
	private String			begin_date;
	@DatabaseField
	private double			mean;
	@DatabaseField
	private int				current_steps_value;
	@DatabaseField
	private int				n_measured_days;
	@DatabaseField
	private double				height;
	
	
	public User(){} //needed by ormlite

	public String getFBid() {
		return FBid;
	}

	public void setFBid(String fBid) {
		FBid = fBid;
	}

	public int getXP() {
		return XP;
	}

	public void setXP(int xP) {
		XP = xP;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}
	
	public boolean isOwner(){
		if(owner ==  1)
			return true;
		else return false;
	}
	
	public void setOwner(boolean isOwner){
		if(isOwner)
			owner = 1;
		else owner = 0 ;
	}

	public String getBegin_date() {
		return begin_date;
	}

	public void setBegin_date(String begin_date) {
		this.begin_date = begin_date;
	}

	public double getMean() {
		return mean;
	}

	public void setMean(double mean) {
		this.mean = mean;
	}

	public int getCurrent_steps_value() {
		return current_steps_value;
	}

	public void setCurrent_steps_value(int current_steps_value) {
		this.current_steps_value = current_steps_value;
	}

	public int getN_measured_days() {
		return n_measured_days;
	}

	public void setN_measured_days(int n_measured_days) {
		this.n_measured_days = n_measured_days;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	//to be used to create new achievements
	private void changedState(Object obj){
		setChanged();
		notifyObservers(obj);
	}
	
}
