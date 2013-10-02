package info;

public class Deck {
	public String DeckName = "";
	public String CustomDeckName = "";
	public String No = "";
	public int BC = 0;
	public boolean UseBcInsufficientDeck = false;
	public double BcInsufficientScale = 0.1;
	public long BcInsufficientHpMax = 0;
	//public double BcKillFairy = 0.5;
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
