package net.yangziwen.hqlformatter.util;

import java.io.Closeable;
import java.io.IOException;

public class IOUtils {
	
	private IOUtils() {}
	
	public static void closeQuietly(Closeable closeable) {
		if(closeable == null) {
			return;
		}
		try {
			closeable.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
