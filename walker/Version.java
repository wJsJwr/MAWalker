package walker;

public class Version {
	private static String major = "1";
	private static String minor = "3";
	private static String release = "0";
	private static String thanks = "@wjsjwr, @tsubasa617, @AsakuraFuuko, @lucky83, @innocentius, @kimbaol";

	public static String strVersion() {
		return String.format("MAWalker(java) v%s.%s.%s", major, minor, release);

	}

	public static String strThanks() {
		return String.format("对下列网友表示感谢（排名不分先后）: %s", thanks);
	}

	public static String strInfo() {
		return "本软件为免费开源软件，不收取任何费用。\n最新版本及提交bug，请到https://github.com/tsubasa617/MAWalker";
	}
}
