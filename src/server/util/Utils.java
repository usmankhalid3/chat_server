package server.util;

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
}
