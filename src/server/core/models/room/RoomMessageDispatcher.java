package server.core.models.room;

import java.util.List;

import server.core.Messages;
import server.core.Session;
import server.util.Utils;

public class RoomMessageDispatcher extends Thread {

	private Room room;
	private Session sender;
	private String message;
	private boolean isNotif;
	
	public RoomMessageDispatcher(Room room, Session sender, String message, boolean sendToSelf, boolean isNotif) {
		this.room = room;
		this.sender = sender;
		this.message = message;
		this.isNotif = isNotif;
		if (sendToSelf) {
			sender.sendMessage(sender, message, isNotif);
		}
		start();
	}
	
	public void run() {
		List<Session> members = room.getMembers();
		for (Session member : members) {
			if (!member.getUser().equals(sender.getUser())) {
				member.sendMessage(sender, message, isNotif);
			}
		}
	}
	
	public static void notifyUserJoin(Session user) {
		String nick = user.getUser().getNick();
		Room room = user.getUser().getRoom();
		String message = Utils.formatMessage(Messages.USER_JOINED, nick);
		new RoomMessageDispatcher(room, user, message, false, true);
	}
	
	public static void notifyUserLeave(Session user, Room room) {
		String nick = user.getUser().getNick();
		String message = Utils.formatMessage(Messages.USER_LEFT, nick, room.getName());
		new RoomMessageDispatcher(room, user, message, true, true);
	}
}
