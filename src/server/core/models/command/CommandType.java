package server.core.models.command;

public enum CommandType {
	CREATE_ROOM("create", 1),
	LIST_ROOMS("rooms", 0),
	ENTER_ROOM("join", 1),
	LEAVE_ROOM("leave", 0),
	START_PRIVATE("private", 1),
	HELP("help", 0),
	QUIT("quit", 0);
	
	private String cmd;
	private int args;
	private CommandType(String cmd, int args) {
		this.cmd = cmd;
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
}
