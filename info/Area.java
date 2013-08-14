package info;

public class Area {
	public String areaName = "";
	public int areaId = 0;
	public int minAP = Integer.MAX_VALUE;
	public int maxAP = Integer.MIN_VALUE;
	public int exploreProgress = 0;
	
	public Area() {
		
	}
	
	public Area(String name, int id, int prog) {
		this.areaName = name;
		this.areaId = id;
		this.exploreProgress = prog;
	}
}
