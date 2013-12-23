package server.core;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import server.core.models.room.Room;
import server.util.Utils;

import com.google.common.collect.Lists;

public class Server {
	// The ServerSocket we'll use for accepting new connections
	private ServerSocket serverSocket;
	// A mapping from sockets to sessions.
	private Map<Socket, Session> clients = new HashMap<Socket, Session>();
	private Map<String, Room> rooms = new HashMap<String, Room>();
	private Map<String, Long> lastLogged = new HashMap<String, Long>();
	private long loginDelay = 1000;

	// Constructor and while-accept loop all in one.
	public Server(int port) throws IOException {
		// All we have to do is listen
		listen(port);
	}

	private void listen(int port) throws IOException {
		// Create the ServerSocket
		serverSocket = new ServerSocket(port);
		// Tell the world we're ready to go
		Utils.stdOut("Listening on " + serverSocket);
		// Keep accepting connections forever
		while (true) {
			// Grab the next incoming connection
			Socket socket = serverSocket.accept();
			String IP = "" + socket.getInetAddress();
			Utils.stdOut("Incoming connection from " + socket);
			//if its not a potential DOS attack, allow the socket to be created
			if (canAccess(IP)) {
				createSession(socket);
			}
			else {
				Utils.stdErr("Refused connection to a potential DOS attack: " + IP);
			}
		}
	}
	
	private boolean canAccess(String ip) {
		boolean loggedBefore = lastLogged.containsKey(ip);
		long now = Utils.getTime();
		if (!loggedBefore) {
			lastLogged.put(ip, now);
			return true;
		}
		else {
			long lastTime = lastLogged.get(ip);
			if (now - lastTime > loginDelay) {
				lastLogged.put(ip, now);
				return true;
			}
		}
		return false;
	}
	
	private void createSession(Socket socket) throws IOException {
		// Create a new thread for this connection
		Session thread = new Session(this, socket);
		// Save this thread so we don't need to make it again
		clients.put(socket, thread);

	}
	
	boolean userExists(String nick) {
		for (Session client : clients.values()) {
			if (client.belongsTo(nick)) {
				return true;
			}
		}
		return false;
	}

	void createRoom(Room room) {
		rooms.put(room.getName().toLowerCase(), room);
	}
	
	boolean roomExists(String name) {
		synchronized(rooms) {
			return rooms.containsKey(name.toLowerCase());
		}
	}
	
	Room getRoom(String name) {
		synchronized (rooms) {
			return rooms.get(name);
		}
	}
	
	List<Room> getRooms() {
		return Lists.newArrayList(rooms.values());
	}
	
	// Remove a socket, and it's corresponding streams.
	void removeConnection(Session client) {
		// Synchronize so we don't mess up sending messages while it walks
		// down the list of all output streams
		synchronized (clients) {
			// Tell the world
			Socket clientId = client.getClientId();
			Utils.stdOut("Removing connection to " + clientId);
			// Remove it from our hashmap
			clients.remove(clientId);
		}
	}

}