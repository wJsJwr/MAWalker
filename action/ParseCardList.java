package action;

import info.Card;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import walker.Process;

public class ParseCardList {

	private static String myCardDatafile = "myCard.xls";

	public static void parse(Document doc) throws NumberFormatException,
			XPathExpressionException {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		int cardCount = ((NodeList) xpath.evaluate(
				"//owner_card_list/user_card", doc, XPathConstants.NODESET))
				.getLength();
		if (cardCount > 0) {
			Process.info.cardList = new ArrayList<Card>();
			Process.info.myCardList = new Hashtable<String, Card>();
			for (int i = 1; i < cardCount + 1; i++) {
				Card c = new Card();
				String p = String.format("//owner_card_list/user_card[%d]", i);

				c.serialId = xpath.evaluate(p + "/serial_id", doc);
				c.cardId = xpath.evaluate(p + "/master_card_id", doc);
				c.holo = !xpath.evaluate(p + "/holography", doc).equals("0");
				c.lv = Integer.parseInt(xpath.evaluate(p + "/lv", doc));
				c.lvMax = Integer.parseInt(xpath.evaluate(p + "/lv_max", doc));
				c.hp = Integer.parseInt(xpath.evaluate(p + "/hp", doc));
				c.atk = Integer.parseInt(xpath.evaluate(p + "/power", doc));
				c.plusLimit = Integer.parseInt(xpath.evaluate(p
						+ "/plus_limit_count", doc));
				c.exist = true;
				Card tmpCard = Process.CardData.getCardData(c.cardId);
				c.star = tmpCard.star;
				c.cost = tmpCard.cost;
				c.cardNameCn = tmpCard.cardNameCn;
				c.cardNameJp = tmpCard.cardNameJp;
				Process.info.cardList.add(c);
				Process.info.myCardList.put(c.serialId, c);
			}
			saveCardData();
		}

	}

	private static void saveCardData() {

		WritableWorkbook wwb = null;
		try {
			wwb = Workbook.createWorkbook(new File(myCardDatafile));
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (wwb != null) {
			WritableSheet ws = wwb.createSheet("My cards", 0);
			for (int i = 0; i < Process.info.cardList.size() + 1; i++) {
				String singleCardData = "";
				if (i == 0) {
					singleCardData = "SerialID,CardID,CnName,JpName,Star,Cost,Holo,Level,Hp,Atk";
				} else {
					Card c = Process.info.cardList.get(i - 1);
					singleCardData = String.format(
							"%s,%s,%s,%s,%d,%d,%b,%d,%d,%d,", c.serialId,
							c.cardId, c.cardNameCn, c.cardNameJp, c.star,
							c.cost, c.holo, c.lv, c.hp, c.atk);
				}
				for (int j = 0; j < singleCardData.split(",").length; j++) {
					Label labelC = new Label(j, i, singleCardData.split(",")[j]);
					try {
						ws.addCell(labelC);
					} catch (RowsExceededException e) {
						e.printStackTrace();
					} catch (WriteException e) {
						e.printStackTrace();
					}
				}
			}
			try {
				wwb.write();
				wwb.close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (WriteException e) {
				e.printStackTrace();
			}
		}
	}
}
