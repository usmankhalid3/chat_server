package server.core;

public class Messages {

	public static String WELCOME_MESSAGE = "\n*********Welcome to my Chat Server!*******\nType /help to get help!\n\n";
	public static String GOOD_BYE = "Good Bye, {0}!";
	public static String ROOM_MEMBERS = "Active members are:";
	public static String ROOM_NAME = "\t* {0} ({1})";
	public static String ROOM_ACTIVE = "Active rooms are:";
	public static String ROOM_NO_ACTIVE = "No active rooms";
	public static String ROOM_CREATED = "Created room: {0}";
	public static String ROOM_JOINED = "Entered room: {0}";
	public static String ROOM_EXISTS = "A room with this name already exists. Please select a different name.";
	public static String ROOM_INVALID = "A room with this name doesn't exist. Please select a different name.";
	public static String ROOM_FULL = "Room is full. Please try again later.";
	public static String ROOM_ACTION = "Please leave the room to perform this action";
	public static String ROOM_NOT_JOINED = "You are not in any room";
	public static String USER_JOINED = "\t* {0} has joined the room";
	public static String USER_LEFT = "\t* {0} has left the room: {1}";
	public static String USER_IDENTIFY = " (** this is you)";
	public static String SYS_END_OF_LIST = "End of list";
	public static String SYS_NOT_IN_ROOM = "Please join a room to chat.";
	public static String SYS_INVALID_COMMAND = "Invalid command.";
	public static String SYS_CMD_MISSING_ARGS = "Command missing arguments";
	public static String SYS_NAME_TAKEN = "Sorry, this name is taken. Please select another.";
	public static String SYS_INVALID_NAME = "This is an invalid name. Please select another.";
}
