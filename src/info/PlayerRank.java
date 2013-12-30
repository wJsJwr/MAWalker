package info;

public class PlayerRank implements Comparable<PlayerRank> {
	public int ilv;
	public int irank;
	public String logintime = "";
	public String name = "";
	public int point;

	@Override
	public int compareTo(PlayerRank other) {
		if (other == null) {
			return 1;
		}
		int num = other.point - this.point;
		if (num == 0) {
			num = this.ilv - other.ilv;
		}
		return num;
	}

	@Override
	public String toString() {
		return String.format("%s，等级：%d，贡献：%d，排名：%d，最后登录：%s\r\n", this.name,
				this.ilv, this.point, this.irank, this.logintime);
	}
}
