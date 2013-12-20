package server.core.models.room;

import java.util.List;

import server.core.ServerThread;

public class RoomMessageDispatcher extends Thread {

	private Room room;
	private ServerThread sender;
	private String message;
	
	public RoomMessageDispatcher(Room room, ServerThread sender, String message) {
		this.room = room;
		this.sender = sender;
		this.message = message;
		sender.sendMessage(sender, message);
		start();
	}
	
	public void run() {
		List<ServerThread> members = room.getMembers();
		for (ServerThread member : members) {
			if (!member.getUser().equals(sender.getUser())) {
				member.sendMessage(sender, message);
			}
		}
	}
}
