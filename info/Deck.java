package info;

public class Deck {
	public String DeckName = "";
	public String CustomDeckName = "自动配卡";
	public String No = "3";
	public int BC = 0;
	public boolean UseBcInsufficientDeck = false;
	public double BcInsufficientScale = 0.1;
	public long BcInsufficientHpMax = 0;
	public boolean UseKillFairyDeck = false;
	public long KillFairyHpMax = 0;
	public String card = "";
	public String leader = "";

	public Deck(String Name) {
		DeckName = Name;
	}

	public Deck() {

	}
}
