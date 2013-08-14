package action;
import info.Card;

import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import walker.Process;
public class ParseCardList {
	public static void parse(Document doc) throws NumberFormatException, XPathExpressionException {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		int cardCount = ((NodeList)xpath.evaluate("//owner_card_list/user_card", doc, XPathConstants.NODESET)).getLength();
		if (cardCount > 0) Process.info.cardList = new ArrayList<Card>();
		for (int i = 1; i < cardCount + 1; i++) {
			Card c = new Card();
			String p = String.format("//owner_card_list/user_card[%d]", i);
			
			c.serialId = xpath.evaluate(p+"/serial_id", doc);
			c.cardId = xpath.evaluate(p+"/master_card_id", doc);
			c.holo = !xpath.evaluate(p+"/holography", doc).equals("0");
			c.lv = Integer.parseInt(xpath.evaluate(p+"/lv", doc));
			c.lvMax = Integer.parseInt(xpath.evaluate(p+"/lv_max", doc));
			c.hp = Integer.parseInt(xpath.evaluate(p+"/hp", doc));
			c.atk = Integer.parseInt(xpath.evaluate(p+"/power", doc));
			c.plusLimit = Integer.parseInt(xpath.evaluate(p+"/plus_limit_count", doc));
			c.exist = true;
			Process.info.cardList.add(c);
		}
		
	}

}
