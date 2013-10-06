package walker;

import info.Card;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CardDataBase {

	private static String dbName = "macard.db";

	public CardDataBase() {
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Card getCardData(String cardId) {
		Connection conn = null;
		Card tmpCard = new Card();
		tmpCard.cardId = cardId;
		tmpCard.star = 99;
		tmpCard.cost = 99;
		tmpCard.cardNameCn = "未知";
		tmpCard.cardNameJp = "未知";
		try {
			conn = DriverManager.getConnection("jdbc:sqlite:" + dbName);

			String queryComm = "SELECT * FROM card WHERE id=" + cardId;
			Statement st = conn.createStatement();
			st.setQueryTimeout(30);
			ResultSet rs = st.executeQuery(queryComm);
			if (rs.next()) {
				tmpCard.cardId = cardId;
				tmpCard.star = rs.getInt("rare");
				tmpCard.cost = rs.getInt("cost");
				tmpCard.cardNameCn = rs.getString("cnName");
				tmpCard.cardNameJp = rs.getString("name");
			}
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (conn != null)
				conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return tmpCard;

	}

	private void init() {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

}