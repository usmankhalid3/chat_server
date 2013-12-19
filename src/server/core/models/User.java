package server.core.models;

public class User {

	private int id;
	private String nick;
	private String username;
	private String password;
	private boolean allowsPrivateMessages = true;
	private Room room;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getNick() {
		return nick;
	}
	public void setNick(String nick) {
		this.nick = nick;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
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
