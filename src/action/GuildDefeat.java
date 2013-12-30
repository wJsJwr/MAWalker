package action;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

public class GuildDefeat {
	public static boolean judge(Document doc) throws XPathExpressionException {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		return (boolean)xpath.evaluate("count(//guild_defeat_event)>0", doc, XPathConstants.BOOLEAN);
	}
}
