package net.yangziwen.hqlformatter;

import java.util.concurrent.TimeUnit;

import net.yangziwen.hqlformatter.controller.SqlController;
import spark.Spark;

public class Server {
	
	public static final int DEFAULT_PORT = 8060;
	
	public static void main(String[] args) {
		
		final int port = getPort();
		
		Spark.port(port);
		
//		Spark.ipAddress("127.0.0.1");
		
		Spark.staticFileLocation("/static");
		
		// 看上去没有办法获取底层的jettyServer
		// 因此没法通过LifeCycleLisener来注册开启浏览器的操作
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					TimeUnit.SECONDS.sleep(1L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				openApp(port);
			}
		}).start();
		
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
	
	private static void openApp(int port) {
		String url = "http://localhost:" + port;
		Boolean autoOpen = false;
		try {
			autoOpen = Boolean.valueOf(System.getProperty("autoOpen"));
		} catch (Exception e) {
		}
		if(!Boolean.TRUE.equals(autoOpen)) {
			return;
		}
		String cmd = "cmd /c start " + url;
		try {
			Process process = Runtime.getRuntime().exec(cmd);
			process.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
