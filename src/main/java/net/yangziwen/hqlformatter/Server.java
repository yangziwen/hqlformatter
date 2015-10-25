package net.yangziwen.hqlformatter;

import net.yangziwen.hqlformatter.controller.SqlController;
import spark.Spark;

public class Server {
	
	public static final int DEFAULT_PORT = 8060;
	
	public static void main(String[] args) {
		
		int port = getPort();
		
		Spark.port(port);
		
//		Spark.ipAddress("127.0.0.1");
		
		Spark.staticFileLocation("/static");
		
		initControllers();
		
	}
	
	private static int getPort() {
		try {
			return Integer.valueOf(System.getProperty("port"));
		} catch (NumberFormatException e) {
			return DEFAULT_PORT;
		}
	}
	
	private static void initControllers() {
		
		SqlController.init();
		
	}

}
