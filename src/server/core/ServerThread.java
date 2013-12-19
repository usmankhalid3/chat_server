package server.core;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

import server.common.Constants;
import server.core.models.ParsedInput;
import server.core.models.ParsedInput.ParseResult;
import server.core.models.Room;
import server.core.models.User;
import server.core.models.command.Command;
import server.core.models.command.CommandType;
import server.util.Utils;

public class ServerThread extends Thread {
	// The Server that spawned us
	private Server server;
	// The Socket connected to our client
	private Socket socket;
	private DataOutputStream dout = null;
	private User user = null;
	private boolean askedForLogin = false;

	// Constructor.
	public ServerThread(Server server, Socket socket) throws IOException {
		// Save the parameters
		this.server = server;
		this.socket = socket;
		this.dout = new DataOutputStream(socket.getOutputStream());
		// Start up the thread
		start();
	}
	
	// This runs in a separate thread when start() is called in the
	// constructor.
	public void run() {
		try {
			socket.setTcpNoDelay(true);
			BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	        String line = null;
	        boolean quit = false;
	        while (!quit) {
	        	if (!askedForLogin && !loggedIn()) {
	        		if (!askedForLogin) {
	        			write("<= Login Name?\n");
	        			askedForLogin = true;
	        		}
	        	}
	        	else {
	        		line = read(input);
	        		if (!loggedIn()) {
	        			login(line);
	        		}
	        		else {
			        	ParsedInput parsedInput = parseInput(line);
			        	if (parsedInput.isMessage()) {	// it's a message
			        		if (!line.isEmpty()) {
			        			server.broadcast(this, line);
			        		}
			        	}
			        	else {	// it's a command
			        		Command command = parsedInput.getCommand();
			        		if (command.isValid()) {
			        			dispatchCommand(command);
			        		}
			        		else {
			        			//TODO:report this blasphemy!
			        		}
			        	}
	        		}
	        	}
	        }
		}
		catch (SocketException e) {
			server.removeConnection(this);
			shutDown();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		} 
		finally {
			// The connection is closed for one reason or another,
			// so have the server dealing with it
			if (server != null) {
				server.removeConnection(this);
				shutDown();
			}
		}
	}
	
	private String read(BufferedReader input) throws IOException {
		write("=> ");
		String line = input.readLine();
		if (line != null) {
			return line;
		}
		else {
			return "";
		}
	}
	
	private boolean loggedIn() {
		return user != null;
	}
	
	private void login(String nick) {
		if (server.userExists(nick)) {
			//TODO: implement the logic of returning an error
		}
		else {
			user = new User();
			user.setAllowsPrivateMessages(true);
			user.setNick(nick);
		}
	}
	
	private ParsedInput parseInput(String line) {
		ParsedInput input = new ParsedInput();
		if (isCommand(line)) {
			Command command = new Command(line);
			if (!command.isValid()) {
				input.setResult(ParseResult.INVALID_COMMAND);
			}
			else if (command.isMissingArgs()) {
				input.setResult(ParseResult.ERROR_MISSING_ARGS);
			}
			else {
				input.setResult(ParseResult.VALID_COMMAND);
			}
			input.setCommand(command);
		}
		return input;
	}
	
	private boolean isCommand(String line) {
		return line != null && line.startsWith(Constants.commandSpecifier);
	}
	
	private void dispatchCommand(Command cmd) {
		CommandType type = cmd.getType();
		switch(type) {
			case CREATE_ROOM: createRoom(cmd); break;
			case LIST_ROOMS: listRooms(cmd); break;
			case ENTER_ROOM: enterRoom(cmd); break;
			case LEAVE_ROOM: leaveRoom(cmd); break;
			case START_PRIVATE: startPrivateChat(cmd); break;
			case HELP: showHelp(); break;
			case QUIT: quit(); break;
		}
	}
	
	private void createRoom(Command cmd) {
		List<String> args = cmd.getArgs();
		String name = args.get(0);
		if (server.roomExists(name)) {
			//TODO: implement the logic of returning an error
		}
		else {
			server.createRoom(new Room(name, user, Constants.ROOM_MEMBER_LIMIT));
			//TODO: implement the logic of returning success
		}
	}
	
	private void listRooms(Command cmd) {
		List<Room> rooms = server.getRooms();
		//TODO: implement the logic of returning list
	}
	
	private void enterRoom(Command cmd) {
		List<String> args = cmd.getArgs();
		String name = args.get(0);
		if (server.roomExists(name)) {
			Room room = server.getRoom(name);
			boolean couldJoin = room.join(user);
			if (couldJoin) {
				//TODO: implement the logic of returning success
			}
			else {
				//TODO: implement the logic of returning an error
			}	
		}
		else {
			//TODO: implement the logic of returning an error
		}
	}

	private void leaveRoom(Command cmd) {
		if (user.hasJoinedARoom()) {
			Room room = server.getRoom(user.getRoom().getName());
			room.leave(user);
		}
		else {
			//TODO: implement the logic of returning an error
		}
	}
	
	private void startPrivateChat(Command cmd) {
		//TODO: implement this feature
	}
	
	private void showHelp() {
		//TODO: implement this feature		
	}
	
	private void quit() {
		//TODO: implement this feature		
	}
	
	public void shutDown() {
		// Make sure it's closed
		Utils.stdOut("Shutting down..." + socket);
		try {
			dout.close();
			socket.close();
			server = null;
		} catch (IOException ie) {
			Utils.stdOut("Error closing " + socket);
			throw new RuntimeException(ie);
		}
	}
	
	public Socket getClientId() {
		return socket;
	}
	
	private void write(String message) throws IOException {
		dout.writeUTF(message);
		dout.flush();
	}
	
	public void sendMessage(String message) {
		try {
			dout.writeUTF("<= " + message + "\n");
		} catch (IOException ie) {
			Utils.stdOut(ie.getMessage());
		}
	}
	
	public boolean belongsTo(String nick) {
		return loggedIn() && user.getNick().equalsIgnoreCase(nick);
	}
}
