package server.core.models.command;

import java.util.ArrayList;
import java.util.List;

public class Command {

	private CommandType type = null;
	private List<String> args = null;
	
	public Command(String line) {
		parse(line);
	}
	
	private void parse(String line) {
		String[] parts = line.split(" ");
		String cmd = parts[0];
		this.type = CommandType.parse(cmd);
		if (type != null) {
			if (type.args() > 0 && parts.length > 1) {
				parseArgs(parts);
			}
		}
	}
	
	private void parseArgs(String[] parts) {
		this.args = new ArrayList<String>();
		for (int i = 1; i < parts.length; i++) {	// i starts from 1 because 0 is the cmd type
			this.args.add(parts[i]);
		}
	}
	
	public CommandType getType() {
		return type;
	}
	
	public List<String> getArgs() {
		return args;
	}
	
	public boolean isValid() {
		return type != null;
	}
	
	public boolean isMissingArgs() {
		return type.args() > 0 && (args == null || args.size() < type.args());
	}
}
