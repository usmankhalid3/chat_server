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
		String args = parts[1];
		this.type = CommandType.parse(cmd);
		if (type.args() > 0) {
			if (args != null && !args.isEmpty()) {
				parseArgs(args);
			} 
		}
	}
	
	private void parseArgs(String args) {
		this.args = new ArrayList<String>();
		String[] parts = args.split(",");
		for (String arg : parts) {
			this.args.add(arg);
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
