package info;

public class FairyBattleInfo {
	public String GuildId = "";
	public String UserId = "";
	public String SerialId = "";
	public String No = "0";
	public final String Spp = "dummy";

	public static final int RARE = 0x1;
	public static final int SELF = 0x2;
	public static final int PRIVATE = 0x4;
	/**
	 * 故： <br />
	 * 妖精（自）：P|S|~R = 6 <br />
	 * 妖精（自，觉醒）：P|S|R = 7 <br />
	 * 妖精（他） P|~S|~R = 4 <br />
	 * 妖精（他，觉醒）：P|~S|R = 5 <br />
	 * 外敌：~P|-|- = 0
	 */
	public int Type = 0;
	public String FairyName = "";
	public int FairyLevel = 1;
	public String Finder = "";
	public int FairyHp = 0;
	public int FairyHpMax = 1;
	public boolean ForceKill = false;

	public boolean equals(FairyBattleInfo b) {
		return this.UserId.equals(b.UserId) && this.SerialId.equals(b.SerialId);
	}

	public FairyBattleInfo(FairyBattleInfo fbi) {
		this.GuildId = fbi.GuildId;
		this.UserId = fbi.UserId;
		this.SerialId = fbi.SerialId;
		this.No = fbi.No;
		this.Type = fbi.Type;
		this.FairyName = fbi.FairyName;
		this.FairyLevel = fbi.FairyLevel;
		this.Finder = fbi.Finder;
	}

	public FairyBattleInfo() {

	}
}
