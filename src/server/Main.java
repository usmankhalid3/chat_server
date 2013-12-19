package server;

import server.core.Server;

public class Main {

	public static void main(String[] args) throws Exception {
		// Get the port # from the command line
		int port = Integer.parseInt(args[0]);
		// Create a Server object, which will automatically begin
		// accepting connections.
		new Server(port);
	}
}
