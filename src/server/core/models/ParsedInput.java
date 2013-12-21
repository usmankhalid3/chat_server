package server.core.models;

import server.core.models.command.Command;

public class ParsedInput {

	public enum ParseResult {
		MESSAGE,
		VALID_COMMAND,
		INVALID_COMMAND,
		CMD_MISSING_ARGS
	}
	
	private Command command = null;
	private ParseResult result = ParseResult.MESSAGE;
	
	public Command getCommand() {
		return command;
	}
	public void setCommand(Command command) {
		this.command = command;
	}
	public ParseResult getResult() {
		return result;
	}
	public void setResult(ParseResult result) {
		this.result = result;
	}
	public boolean isMessage() {
		return result == ParseResult.MESSAGE;
	}
	public boolean isInvalidCommand() {
		return result == ParseResult.INVALID_COMMAND;
	}
	public boolean isMissingArgs() {
		return result == ParseResult.CMD_MISSING_ARGS;
	}
}
