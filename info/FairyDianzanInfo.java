package info;

public class FairyDianzanInfo {
	public String SerialId = "";
	public String UserId = "";
	
	public boolean equals(FairyDianzanInfo b) {
		return this.UserId.equals(b.UserId) && this.SerialId.equals(b.SerialId);
	}

	public FairyDianzanInfo(String _serialId, String _userId) {
		SerialId = _serialId;
		UserId = _userId;
	}

	public FairyDianzanInfo() {

	}
}
