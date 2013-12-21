package server.core.models.room;

import java.util.ArrayList;
import java.util.List;

import server.core.Session;

public class Room {
	private String name;
	private int memberLimit;
	private List<Session> members;

	public Room(String name, Session creator, int limit) {
		this.name = name;
		this.memberLimit = limit;
		members = new ArrayList<Session>();
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Session> getMembers() {
		return members;
	}
	public void setMembers(List<Session> members) {
		this.members = members;
	}
	public boolean join(Session user) {
		synchronized(members) {
			if (members.size() < memberLimit) {
				user.getUser().joinRoom(this);
				members.add(user);
				return true;
			}
			return false;
		}
	}
	public void leave(Session user) {
		members.remove(user);
		user.getUser().leaveRoom();
	}
	public boolean isPresent(Session user) {
		return members.contains(user);
	}
	public boolean isEmpty() {
		return members.isEmpty();
	}
}