package server.core.models.room;

import java.util.ArrayList;
import java.util.List;

import server.core.ServerThread;

public class Room {
	private String name;
	private int memberLimit;
	private List<ServerThread> members;

	public Room(String name, ServerThread creator, int limit) {
		this.name = name;
		this.memberLimit = limit;
		members = new ArrayList<ServerThread>();
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<ServerThread> getMembers() {
		return members;
	}
	public void setMembers(List<ServerThread> members) {
		this.members = members;
	}
	public boolean join(ServerThread user) {
		synchronized(members) {
			if (members.size() < memberLimit) {
				user.getUser().joinRoom(this);
				members.add(user);
				return true;
			}
			return false;
		}
	}
	public void leave(ServerThread user) {
		members.remove(user);
		user.getUser().leaveRoom();
	}
	public boolean isPresent(ServerThread user) {
		return members.contains(user);
	}
	public boolean isEmpty() {
		return members.isEmpty();
	}
}