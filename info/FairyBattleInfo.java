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
	 * 故： <br />
	 * 妖精（自）：P|S|~R = 6 <br />
	 * 妖精（自，觉醒）：P|S|R = 7 <br />
	 * 妖精（他） P|~S|~R = 4 <br />
	 * 妖精（他，觉醒）：P|~S|R = 5 <br />
	 * 外敌：~P|-|- = 0
	 */
	public int Type = 0;
	public String FairyName = "";
	public String FairyLevel = "";
	public String Finder = "";
	
	public int fairyCurrHp = 0;
	public int fairyMaxHp = 0;

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
		this.fairyCurrHp = fbi.fairyCurrHp;
		this.fairyMaxHp = fbi.fairyMaxHp;
	}

	public FairyBattleInfo() {
	}
}
