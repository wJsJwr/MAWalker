package info;

public class Deck {
	public String DeckName = "";
	public String No = "";
	public int BC = 0;
	public boolean ForceBattle = false;
	public double BcForceBattle = 0.9;
	public double BcKillFairy = 0.5;
	public String card = "";
	public String leader = "";

	public Deck(String Name) {
		DeckName = Name;
	}

	public Deck() {

	}
}
