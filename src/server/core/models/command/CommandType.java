package server.core.models.command;

public enum CommandType {
	CREATE_ROOM("create", 1, "/create <name> to create a new room."),
	LIST_ROOMS("rooms", 0, "/rooms to list all active rooms."),
	LIST_MEMBERS("members", 0, "/members to list all members in the room."),
	ENTER_ROOM("join", 1, "/join <name> to join a room."),
	LEAVE_ROOM("leave", 0, "/leave to leave the room."),
	START_PRIVATE("private", 1, "/private <name> to start private chat with a user"),
	QUIT("quit", 0, "/quit to quit chat."),
	HELP("help", 0, "/help for getting help");
	
	private String cmd;
	private int args;
	private String helpText;
	private CommandType(String cmd, int args, String helpText) {
		this.cmd = cmd;
		this.args = args;
		this.helpText = helpText;
	}
	public String cmd() {
		return cmd;
	}
	public int args() {
		return args;
	}
	public static CommandType parse(String cmd) {
		CommandType[] commands = CommandType.values();
		for (CommandType command: commands) {
			if (command.cmd.equalsIgnoreCase(cmd)) {
				return command;
			}
		}
		return null;
	}
	public String getHelpText() {
		return helpText;
	}
}
