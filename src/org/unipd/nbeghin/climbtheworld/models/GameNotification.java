package org.unipd.nbeghin.climbtheworld.models;

import java.util.ArrayList;

public class GameNotification extends Notification{

	private ArrayList<String> text;
	
	public GameNotification(ArrayList<String> text) {
		id = "";
		sender = "";
		groupName = "";
		type = NotificationType.values()[0];
		read = false;
		
		this.text = text;
		
	}

	public ArrayList<String> getText() {
		return text;
	}

	public void setText(ArrayList<String> text) {
		this.text = text;
	}
	
	
}
