package server.core.models;

import java.util.ArrayList;
import java.util.List;

public class Room {
	private String name;
	private int memberLimit;
	private List<User> members;

	public Room(String name, User creator, int limit) {
		this.name = name;
		this.memberLimit = limit;
		members = new ArrayList<User>();
		members.add(creator);
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<User> getMembers() {
		return members;
	}
	public void setMembers(List<User> members) {
		this.members = members;
	}
	public boolean join(User user) {
		synchronized(members) {
			if (members.size() < memberLimit) {
				user.joinRoom(this);
				members.add(user);
				return true;
			}
			return false;
		}
	}
	public void leave(User user) {
		members.remove(user);
		user.leaveRoom();
	}
	public boolean isPresent(User user) {
		return members.contains(user);
	}
	public boolean isEmpty() {
		return members.isEmpty();
	}
}