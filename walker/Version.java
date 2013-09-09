package walker;

public class Version {
	private static String major = "0";
	private static String minor = "2";
	private static String release = "23";
	private static String copyright = "2013©wjsjwr.org";
	private static String code = "Crudity";
	
	public static String printVersion() {
		return String.format("MAWalker(java) v%s.%s.%s %s, %s", major, minor, release, code, copyright); 
	}
}
