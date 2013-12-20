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
import server.core.models.User;
import server.core.models.command.Command;
import server.core.models.command.CommandType;
import server.core.models.room.Room;
import server.core.models.room.RoomMessageDispatcher;
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
	        			write("Login Name?");
	        			askedForLogin = true;
	        		}
	        	}
	        	else {
	        		line = read(input);
	        		if (!loggedIn()) {
	        			login(line);
	        		}
	        		else if (!line.isEmpty()){
			        	ParsedInput parsedInput = parseInput(line);
			        	if (parsedInput.isMessage()) {	// it's a message
			        		if (user.hasJoinedRoom()) {
			        			new RoomMessageDispatcher(user.getRoom(), this, line);
			        		}
			        		else {
			        			write("Unidentified command");
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
			terminateSession();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		} 
		finally {
			// The connection is closed for one reason or another,
			// so have the server dealing with it
			terminateSession();
		}
	}
	
	private String read(BufferedReader input) throws IOException {
		prompt("=> ");
		String line = input.readLine();
		if (line != null) {
			return Utils.chomp(line.trim());
		}
		else {
			return "";
		}
	}
	
	private boolean loggedIn() {
		return user != null;
	}
	
	private void login(String nick) throws IOException {
		if (nick == null || nick.isEmpty()) {
			askedForLogin = false;
		}
		else {
			if (server.userExists(nick)) {
				write("Sorry, this name is taken");
				askedForLogin = false;
			}
			else {
				user = new User();
				user.setAllowsPrivateMessages(true);
				user.setNick(nick);
			}
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
	
	private void dispatchCommand(Command cmd) throws IOException {
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
	
	private void createRoom(Command cmd) throws IOException {
		if (user.hasJoinedRoom()) {
			write("Cannot create a room while being in one");
			return;
		}
		List<String> args = cmd.getArgs();
		String name = args.get(0);		
		if (server.roomExists(name)) {
			write("A room with this name already exists. Please select a different name.");
			return;
		}
		else {
			Room room = new Room(name, this, Constants.ROOM_MEMBER_LIMIT);
			server.createRoom(room);
			write("Room created: " + name);
			joinRoom(room);
		}
	}
	
	private void listRooms(Command cmd) throws IOException {
		List<Room> rooms = server.getRooms();
		if (rooms == null || rooms.isEmpty()) {
			write("No active rooms");
			return;
		}
		for (Room room : rooms) {
			write("\t* " + room.getName());
		}
		write("End of list");
	}
	
	private void joinRoom(Room room) throws IOException {
		boolean couldJoin = room.join(this);
		if (couldJoin) {
			write("Entering room: " + room.getName());
			listMembers(room);
		}
		else {
			write("Room is already full. Please try again later!");
		}	
	}
	
	private void enterRoom(Command cmd) throws IOException {
		if (user.hasJoinedRoom()) {
			write("Cannot join a room while being in one");
			return;
		}
		List<String> args = cmd.getArgs();
		String name = args.get(0);
		if (server.roomExists(name)) {
			Room room = server.getRoom(name);
			joinRoom(room);
		}
		else {
			write("This room does not exist");
		}
	}
	
	private void listMembers(Room room) throws IOException {
		for (ServerThread member : room.getMembers()) {
			String message = "\t* " + member.getUser().getNick();
			if (member.equals(user)) {
				message += " (** this is you)";
			}
			write(message);
		}
		write("End of list");
	}

	private void leaveRoom(Command cmd) throws IOException {
		if (user.hasJoinedRoom()) {
			Room room = server.getRoom(user.getRoom().getName());
			room.leave(this);
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
	
	private void terminateSession() {
		if (server != null) {
			server.removeConnection(this);
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
	}
	
	public Socket getClientId() {
		return socket;
	}
	
	private void write(String message) throws IOException {
		dout.writeUTF("<= " + message + "\n");
		dout.flush();
	}
	
	private void prompt(String message) throws IOException {
		dout.writeUTF(message);
		dout.flush();		
	}
	
	public User getUser() {
		return user;
	}
	
	public String createOutgoingMessage(String message, ServerThread sender) {
		boolean self = sender.belongsTo(user.getNick());
		StringBuilder outgoing = new StringBuilder("");
		if (!self) { 
			outgoing.append("\n");
		
		}
		outgoing.append("<= ");
		outgoing.append(sender.user.getNick());
		outgoing.append(" says: ");
		outgoing.append(message);
		outgoing.append("\n");
		if (!self) { 
			outgoing.append("=> ");
		
		}
		return outgoing.toString();
	}
	
	public void sendMessage(ServerThread sender, String message) {
		try { 
			String outgoing = createOutgoingMessage(message, sender);
			prompt(outgoing);
		} catch (IOException ie) {
			Utils.stdOut(ie.getMessage());
		}
	}
	
	public boolean belongsTo(String nick) {
		return loggedIn() && user.getNick().equalsIgnoreCase(nick);
	}
}
