package kanjava.aws;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;

public class Utils {

	private static Properties KANJAVA_PROPERTIES;

	static {
		KANJAVA_PROPERTIES = new Properties();
		try {
			KANJAVA_PROPERTIES.load(getResource("kanjava.properties"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static final InputStream getResource(String url) {
		return Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(url);
	}

	public static final boolean isTweetMonitor() {
		String key = KANJAVA_PROPERTIES.getProperty("tweet.monitor");
		return "true".equalsIgnoreCase(key.trim());
	}

	public static final <T> String join(Collection<T> col, String joinStr) {
		if (col == null || col.size() == 0) {
			return null;
		}
		if (joinStr == null) {
			joinStr = "";
		}
		StringBuilder buf = new StringBuilder();
		for (T val : col) {
			buf.append(val.toString() + joinStr);
		}
		return buf.toString();
	}

}
