package action;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

import walker.Info;
import walker.Process;

public class ParseUserDataInfo {
	public static void parse(Document doc) throws NumberFormatException, XPathExpressionException {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		Process.info.username = xpath.evaluate("//your_data/name", doc);
		Process.info.lv = Integer.parseInt(xpath.evaluate("//your_data/town_level", doc));
		Process.info.ap = Integer.parseInt(xpath.evaluate("//your_data/ap/current", doc));
		Process.info.apMax = Integer.parseInt(xpath.evaluate("//your_data/ap/max", doc));
		Process.info.bc = Integer.parseInt(xpath.evaluate("//your_data/bc/current", doc));
		Process.info.bcMax = Integer.parseInt(xpath.evaluate("//your_data/bc/max", doc));
		Process.info.guildId = xpath.evaluate("//your_data/party_id", doc);
		if ((boolean)xpath.evaluate("count(//your_data/free_ap_bc_point)>0", doc, XPathConstants.BOOLEAN)) {
			Process.info.pointToAdd = Integer.parseInt(xpath.evaluate("//your_data/free_ap_bc_point", doc));
			if (Process.info.pointToAdd > 0) Process.info.events.push(Info.EventType.levelUp);
		}
		if ((boolean)xpath.evaluate("count(//your_data/itemlist[item_id=202])>0", doc, XPathConstants.BOOLEAN)) {
			Process.info.ticket = Integer.parseInt(xpath.evaluate("//your_data/itemlist[item_id=202]/num", doc));
			if (Process.info.ticket > 0) Process.info.events.push(Info.EventType.ticketFull);
		}
			
	}
}
