package info;

public class FairyBattleInfo {
	public String GuildId = "";
	public String UserId = "";
	public String SerialId = "";
	public String No = "2";
	public final String Spp = "dummy";
	
	public static final int RARE = 0x1;
	public static final int SELF = 0x2;
	public static final int PRIVATE = 0x4;
	/**
	 * 故：
	 * 妖精（自）：P|S = 6 
	 * 妖精（自，觉醒）：P|S|R = 7
	 * 妖精（他）//暂时没有：
	 * 妖精（他，觉醒）：P|R = 5
	 * 外敌：~P = 0
	 */
	public int Type = 0;
	public String FairyName = "";
	public String FairyLevel = "";
	public String Finder = "";
}
