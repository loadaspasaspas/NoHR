package hybrid.query.model;

import org.apache.log4j.Level;

public final class Config {
	public static boolean isDebug = false;
	public static String nl = System.getProperty("line.separator");
	public static String tempDirProp = "java.io.tmpdir";
	public static String tempDir = System.getProperty(tempDirProp);
	public static Level logLevel = Level.ALL;
}
