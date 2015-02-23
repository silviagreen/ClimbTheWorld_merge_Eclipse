package org.unipd.nbeghin.climbtheworld.models;

public class AskCollaborationNotification extends Notification {
	
	private String collaborationId;
	private int building_id;
	private String building_name;
	
	public AskCollaborationNotification(String _id, String _sender, String _collabIb, int _type) {
		id = _id;
		sender = _sender;
		collaborationId = _collabIb;
		type = NotificationType.values()[_type];
		read = false;
		
	}



	public String getCollaborationId() {
		return collaborationId;
	}



	public void setCollaborationId(String collaborationId) {
		this.collaborationId = collaborationId;
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
	
	
	
	
}
