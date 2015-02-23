package org.unipd.nbeghin.climbtheworld.models;

public class InviteNotification extends Notification{
	public InviteNotification(String _id, String _sender, String _groupName, int _type) {
		id = _id;
		sender = _sender;
		groupName = _groupName;
		type = NotificationType.values()[_type];
		read = false;
		
	}
}
