package walker;

public class Version {
	private static String major = "1";
	private static String minor = "0";
	private static String release = "38";
	private static String copyright = "2013©wjsjwr.org";
	private static String code = "Waive";
	private static String thanks = "@innocentius, @AsakuraFuuko, @tsubasa617";
	
	public static String strVersion() {
		return String.format("MAWalker(java) v%s.%s.%s %s, %s", major, minor, release, code, copyright); 
	}
	
	public static String strThanks(){
		return String.format("Special Thanks To: %s", thanks);
	}
	
}
