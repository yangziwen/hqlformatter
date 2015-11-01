package net.yangziwen.hqlformatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import net.yangziwen.hqlformatter.controller.SqlController;
import net.yangziwen.hqlformatter.util.IOUtils;
import spark.Spark;

public class Server {
	
	public static final int DEFAULT_PORT = 8060;
	
	public static void main(String[] args) throws IOException {
		
		final int port = getPort();
		
		if(!isPortAvailable("0.0.0.0", port)) {
			System.out.println(String.format("当前端口[%d]已被占用，请按回车键退出。", port));
			new BufferedReader(new InputStreamReader(System.in)).readLine();
			System.exit(0);
		}
		
		Spark.port(port);
		
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
		String cmd = isWindows()
				? "cmd /c start " + url
				: "firefox " + url;
		try {
			Process process = Runtime.getRuntime().exec(cmd);
			process.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static boolean isPortAvailable(String host, int port) {
		Socket s = new Socket();
		try {
			s.bind(new InetSocketAddress(host, port));
			return true;
		} catch (IOException e) {
			return false;
		} finally {
			IOUtils.closeQuietly(s);
		}
	}
	
	private static boolean isWindows() {
		String osName = System.getProperty("os.name");
		if(osName == null || osName.trim().length() == 0) {
			return false;
		}
		return osName.startsWith("Windows");
	}

}
