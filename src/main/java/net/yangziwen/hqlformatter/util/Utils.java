package net.yangziwen.hqlformatter.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public class Utils {
	
	private Utils() {}
	
	public static boolean isEmpty(Collection<?> coll) {
		return coll == null || coll.isEmpty();
	}
	
	public static boolean isNotEmpty(Collection<?> coll) {
		return !isEmpty(coll);
	}
	
	public static boolean isEmpty(Map<?, ?> map) {
		return map == null || map.isEmpty();
	}
	
	public static boolean isNotEmpty(Map<?, ?> map) {
		return !isEmpty(map);
	}
	
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
