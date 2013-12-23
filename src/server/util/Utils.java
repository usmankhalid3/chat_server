package server.util;

import java.text.MessageFormat;

public class Utils {
	
	public static void stdOut(String msg) {
		System.out.println(msg);
	}
	
	public static void stdErr(String msg) {
		System.err.println(msg);
	}
	
	public static String chomp(String str) {
		return str.replaceAll("(\\r|\\n|\\r\\n)", "");
	}
	
	public static String formatMessage(String message, String ... args) {
		MessageFormat formatter = new MessageFormat(message);
		return formatter.format(args);
	}
	
	public static boolean isValidName(String name) {
		return name.matches("^[a-zA-Z0-9]+$");
	}
	
	public static long getTime() {
		return System.currentTimeMillis();
	}
}
