package server.core.models;

public class User {

	private String nick;
	private Room room;
	private boolean allowsPrivateMessages = true;
	
	public String getNick() {
		return nick;
	}
	public void setNick(String nick) {
		this.nick = nick;
	}
	public boolean isAllowsPrivateMessages() {
		return allowsPrivateMessages;
	}
	public void setAllowsPrivateMessages(boolean allowsPrivateMessages) {
		this.allowsPrivateMessages = allowsPrivateMessages;
	}
	public Room getRoom() {
		return room;
	}
	public void joinRoom(Room room) {
		this.room = room;
	}
	public void leaveRoom() {
		this.room = null;
	}
	public boolean hasJoinedARoom() {
		return room != null;
	}
}
