package server.core;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import server.core.models.Room;
import server.util.Utils;

import com.google.common.collect.Lists;

public class Server {
	// The ServerSocket we'll use for accepting new connections
	private ServerSocket serverSocket;
	// A mapping from sockets to DataOutputStreams. This will
	// help us avoid having to create a DataOutputStream each time
	// we want to write to a stream.
	private Map<Socket, ServerThread> clients = new HashMap<Socket, ServerThread>();
	private Map<String, Room> rooms = new HashMap<String, Room>();

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
			// Tell the world we've got it
			Utils.stdOut("Incoming connection from " + socket);
			createClient(socket);
		}
	}
	
	private void createClient(Socket socket) throws IOException {
		// Create a DataOutputStream for writing data to the
		// other side
		DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
		// Create a new thread for this connection, and then forget
		// about it
        dout.writeUTF(Messages.WELCOME_MESSAGE);
		ServerThread thread = new ServerThread(this, socket);
		// Save this thread so we don't need to make it again
		clients.put(socket, thread);

	}
	
	boolean userExists(String nick) {
		for (ServerThread client : clients.values()) {
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
	
	// Send a message to all clients (utility routine)
	void broadcast(ServerThread sender, String message) {
		// We synchronize on this because another thread might be
		// calling removeConnection() and this would screw us up
		// as we tried to walk through the list
		synchronized (clients) {
			// For each client ...
			for (ServerThread client : clients.values()) {
				client.sendMessage(message);
			}
		}
	}

	// Remove a socket, and it's corresponding output stream, from our
	// list. This is usually called by a connection thread that has
	// discovered that the connection to the client is dead.
	void removeConnection(ServerThread client) {
		// Synchronize so we don't mess up sendToAll() while it walks
		// down the list of all output streams
		synchronized (clients) {
			// Tell the world
			Socket clientId = client.getClientId();
			Utils.stdOut("Removing connection to " + clientId);
			// Remove it from our hashmap
			clients.remove(clientId);
			client.shutDown();
		}
	}

}