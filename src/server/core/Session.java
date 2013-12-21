package server.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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

public class Session extends Thread {
	// The Server that spawned us
	private Server server;
	// The Socket connected to our client
	private Socket socket;
	private PrintWriter writer = null;
	private User user = null;
	private boolean askedForLogin = false;
    boolean quit = false;

	// Constructor.
	public Session(Server server, Socket socket) throws IOException {
		// Save the parameters
		this.server = server;
		this.socket = socket;
		this.writer = new PrintWriter(socket.getOutputStream(), true);
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
			        			new RoomMessageDispatcher(user.getRoom(), this, line, true, false);
			        		}
			        		else {
			        			write(Messages.SYS_NOT_IN_ROOM);
			        		}
			        	}
			        	else if (parsedInput.isInvalidCommand()) {
			        		write(Messages.SYS_INVALID_COMMAND);
			        	}
			        	else if (parsedInput.isMissingArgs()) {
			        		write(Messages.SYS_CMD_MISSING_ARGS);
			        	}
			        	else {
			        		Command command = parsedInput.getCommand();
	        				dispatchCommand(command);
			        	}
	        		}
	        		//writer.flush();
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
				write(Messages.SYS_NAME_TAKEN);
				askedForLogin = false;
			}
			else { 
				if (Utils.isValidName(nick)){
					user = new User();
					user.setAllowsPrivateMessages(true);
					user.setNick(nick);
				}
				else {
					write(Messages.SYS_INVALID_NAME);
				}
			}
		}
	}
	
	private ParsedInput parseInput(String line) {
		ParsedInput input = new ParsedInput();
		if (isCommand(line)) {
			Command command = new Command(line);
			input.setResult(ParseResult.VALID_COMMAND);
			input.setCommand(command);
			boolean valid = command.isValid();
			boolean missingArgs = command.isMissingArgs(); 
			if (!valid) {
				input.setResult(ParseResult.INVALID_COMMAND);
			}
			if (missingArgs) {
				input.setResult(ParseResult.CMD_MISSING_ARGS);
			}
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
			case LIST_MEMBERS: listMembers(); break;
			case ENTER_ROOM: enterRoom(cmd); break;
			case LEAVE_ROOM: leaveRoom(false); break;
			//case START_PRIVATE: startPrivateChat(cmd); break;
			case HELP: showHelp(); break;
			case QUIT: quit(); break;
		}
	}
	
	private void createRoom(Command cmd) throws IOException {
		if (user.hasJoinedRoom()) {
			write(Messages.ROOM_ACTION);
			return;
		}
		List<String> args = cmd.getArgs();
		String name = args.get(0);
		if (!Utils.isValidName(name)) {
			write(Messages.SYS_INVALID_NAME);
			return;
		}
		if (server.roomExists(name)) {
			write(Messages.ROOM_EXISTS);
			return;
		}
		else {
			Room room = new Room(name, this, Constants.ROOM_MEMBER_LIMIT);
			server.createRoom(room);
			write(Utils.formatMessage(Messages.ROOM_CREATED, name));
			joinRoom(room, true);
		}
	}
	
	private void listRooms(Command cmd) throws IOException {
		List<Room> rooms = server.getRooms();
		if (rooms == null || rooms.isEmpty()) {
			write(Messages.ROOM_NO_ACTIVE);
			return;
		}
		write(Messages.ROOM_ACTIVE);
		for (Room room : rooms) {
			String name = room.getName();
			String members = String.valueOf(room.getMembers().size());
			write(Utils.formatMessage(Messages.ROOM_NAME, name, members));
		}
		write(Messages.SYS_END_OF_LIST);
	}
	
	private void joinRoom(Room room, boolean onCreation) throws IOException {
		boolean couldJoin = room.join(this);
		if (couldJoin) {
			write(Utils.formatMessage(Messages.ROOM_JOINED, room.getName()));
			if (!onCreation) {
				listMembers(room);
				RoomMessageDispatcher.notifyUserJoin(this);
			}
		}
		else {
			write(Messages.ROOM_FULL);
		}	
	}
	
	private void enterRoom(Command cmd) throws IOException {
		if (user.hasJoinedRoom()) {
			write(Messages.ROOM_ACTION);
			return;
		}
		List<String> args = cmd.getArgs();
		String name = args.get(0);
		if (server.roomExists(name)) {
			Room room = server.getRoom(name);
			joinRoom(room, false);
		}
		else {
			write(Messages.ROOM_INVALID);
		}
	}
	
	private void listMembers() throws IOException {
		if (!user.hasJoinedRoom()) {
			write(Messages.ROOM_NOT_JOINED);
			return;
		}
		listMembers(user.getRoom());
	}
	
	private void listMembers(Room room) throws IOException {
		write(Messages.ROOM_MEMBERS);
		for (Session member : room.getMembers()) {
			String message = "\t* " + member.getUser().getNick();
			if (member.getUser().equals(user)) {
				message += Messages.USER_IDENTIFY;
			}
			write(message);
		}
		write(Messages.SYS_END_OF_LIST);
	}

	private void leaveRoom(boolean forcefully) throws IOException {
		if (user.hasJoinedRoom()) {
			Room room = server.getRoom(user.getRoom().getName());
			room.leave(this);
			RoomMessageDispatcher.notifyUserLeave(this, room);
		}
		else if (!forcefully) {
			write(Messages.ROOM_NOT_JOINED);
		}
	}
	
	private void startPrivateChat(Command cmd) {
		//TODO: implement this feature
	}
	
	private void showHelp() throws IOException {
		CommandType[] commands = CommandType.values();
		for (CommandType command : commands) {
			write("\t* " + command.getHelpText());
		}
	}
	
	private void quit() throws IOException {
		leaveRoom(true);
		quit = true;
		write(Utils.formatMessage(Messages.GOOD_BYE, user.getNick()));		
	}
	
	private void terminateSession() {
		if (server != null) {
			server.removeConnection(this);
			// Make sure it's closed
			Utils.stdOut("Shutting down..." + socket);
			try {
				writer.close();
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
		writer.println("<= " + message);
	}
	
	private void prompt(String message) throws IOException {
		writer.write(message);
		writer.flush();
	}
	
	public User getUser() {
		return user;
	}
	
	private String createOutgoingMessage(String message, Session sender, boolean isNotif) {
		boolean self = false;
		if (sender.belongsTo(user.getNick())) {
			self = true;
		}
		StringBuilder outgoing = new StringBuilder("");
		if (!self) { 
			outgoing.append("\n");
		
		}
		outgoing.append("<= ");
		if (!isNotif) {
			outgoing.append(sender.user.getNick());
			outgoing.append(" says: ");
		}
		outgoing.append(message);
		outgoing.append("\n");
		if (!self && !isNotif) { 
			outgoing.append("=> ");
		
		}
		return outgoing.toString();
	}
	
	public void sendMessage(Session sender, String message, boolean isNotif) {
		try { 
			String outgoing = createOutgoingMessage(message, sender, isNotif);
			prompt(outgoing);
		} catch (IOException ie) {
			Utils.stdOut(ie.getMessage());
		}
	}
	
	public boolean belongsTo(String nick) {
		return loggedIn() && user.getNick().equalsIgnoreCase(nick);
	}
}
