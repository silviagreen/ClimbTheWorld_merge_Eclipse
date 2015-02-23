package org.unipd.nbeghin.climbtheworld.models;

public class AskTeamDuelNotification extends Notification{
	private String teamDuelId;
	private int building_id;
	private String building_name;
	private boolean challenger;
	private boolean isSenderCreator;
	
	public AskTeamDuelNotification(String _id, String _sender, String _collabIb, int _type, boolean challenger, boolean isSenderCreator) {
		id = _id;
		sender = _sender;
		teamDuelId = _collabIb;
		type = NotificationType.values()[_type];
		read = false;
		this.challenger = challenger;
		this.isSenderCreator = isSenderCreator;
		
	}

	public String getTeamDuelId() {
		return teamDuelId;
	}

	public void setTeamDuelId(String teamDuelId) {
		this.teamDuelId = teamDuelId;
	}

	public int getBuilding_id() {
		return building_id;
	}

	public void setBuilding_id(int building_id) {
		this.building_id = building_id;
	}

	public String getBuilding_name() {
		return building_name;
	}

	public void setBuilding_name(String building_name) {
		this.building_name = building_name;
	}

	public boolean isChallenger() {
		return challenger;
	}

	public void setChallenger(boolean challenger) {
		this.challenger = challenger;
	}

	public boolean isSenderCreator() {
		return isSenderCreator;
	}

	public void setSenderCreator(boolean isSenderCreator) {
		this.isSenderCreator = isSenderCreator;
	}
	
	
	
	
}
