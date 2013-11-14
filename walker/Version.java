package walker;

public class Version {
	private static String major = "1";
	private static String minor = "1";
	private static String release = "0";
	private static String thanks = "@wjsjwr, @tsubasa617, @AsakuraFuuko, @lucky83, @innocentius";
<<<<<<< HEAD

	public static String strVersion() {
		return String.format("MAWalker(java) v%s.%s.%s", major, minor, release);
=======
	
	public static String strVersion() {
		return String.format("MAWalker(java) v%s.%s.%s", major, minor, release); 
>>>>>>> 4e9f9c0b61e3dc2368d98cfc8185512cb1be843a
	}

	public static String strThanks() {
		return String.format("对下列网友表示感谢（排名不分先后）: %s", thanks);
	}

}
