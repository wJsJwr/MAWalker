package action;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import walker.ErrorData;
import walker.Info;
import walker.Process;

public class GetCardDeck {
	public static final ActionRegistry.Action Name = ActionRegistry.Action.GET_CARD_DECK;

	private static final String URL_GET_CARD_DECK = "http://web.million-arthurs.com/connect/app/roundtable/edit?cyt=1";
	private static byte[] response;

	public static boolean run() throws Exception {
		Document doc;
		ArrayList<NameValuePair> post = new ArrayList<NameValuePair>();
		post.add(new BasicNameValuePair("move", "1"));
		try {
			response = walker.Process.network.ConnectToServer(
					URL_GET_CARD_DECK, post, false);
		} catch (Exception ex) {
			ErrorData.currentDataType = ErrorData.DataType.text;
			ErrorData.currentErrorType = ErrorData.ErrorType.ConnectionError;
			ErrorData.text = ex.getMessage();
			throw ex;
		}

		Thread.sleep(Process.getRandom(1000, 2000));

		if (Info.Debug) {
			File outputFile = new File("GET_CARD_DECK.xml");
			FileOutputStream outputFileStream = new FileOutputStream(outputFile);
			outputFileStream.write(response);
			outputFileStream.close();
		}

		try {
			doc = Process.ParseXMLBytes(response);
		} catch (Exception ex) {
			ErrorData.currentDataType = ErrorData.DataType.bytes;
			ErrorData.currentErrorType = ErrorData.ErrorType.GetCardDeckDataError;
			ErrorData.bytes = response;
			throw ex;
		}

		try {
			return parse(doc);
		} catch (Exception ex) {
			throw ex;
		}
	}

	private static boolean parse(Document doc) throws Exception {

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		try {
			if (!xpath.evaluate("/response/header/error/code", doc).equals("0")) {
				ErrorData.currentErrorType = ErrorData.ErrorType.GetCardDeckResponse;
				ErrorData.currentDataType = ErrorData.DataType.text;
				ErrorData.text = xpath.evaluate(
						"/response/header/error/message", doc);
				return false;
			}

			// TODO:这种写法仍然很怪异，应该可以直接获得，需要重写
			NodeList tempDeckList = (NodeList) xpath.evaluate(
					"/response/body/roundtable_edit/deck", doc,
					XPathConstants.NODESET);
			Hashtable<Integer, String> myDeckList = new Hashtable<Integer, String>();
			for (int i = 0; i < tempDeckList.getLength(); i++) {
				Node f = tempDeckList.item(i).getFirstChild();
				int tempDeckNumber = -1;
				String tempDeck = "";
				String tempDeckName = "";
				do {
					if (f.getNodeName().equals("deck_cards")) {
						tempDeck = f.getFirstChild().getNodeValue();
					} else if (f.getNodeName().equals("deckname")) {
						tempDeckName = f.getFirstChild().getNodeValue();
						if (tempDeckName.contains("1"))
							tempDeckNumber = 1;
						else if (tempDeckName.contains("2"))
							tempDeckNumber = 2;
						else if (tempDeckName.contains("3"))
							tempDeckNumber = 3;
						else
							tempDeckNumber = -1;
					}
					f = f.getNextSibling();
				} while (f != null);
				if (tempDeckNumber > 0)
					myDeckList.put(tempDeckNumber, tempDeck);
			}

			if (myDeckList.size() != 3)
				return false;

			Info.MyDeck0.card = myDeckList.get(1);
			Info.MyDeck1.card = myDeckList.get(2);
			Info.MyDeck2.card = myDeckList.get(3);
			// leader不用获取
			for (String i : Info.MyDeck0.card.split(",")) {
				if (!i.equals("empty"))
					Info.MyDeck0.leader = i;
			}
			for (String i : Info.MyDeck1.card.split(",")) {
				if (!i.equals("empty"))
					Info.MyDeck1.leader = i;
			}
			for (String i : Info.MyDeck2.card.split(",")) {
				if (!i.equals("empty"))
					Info.MyDeck2.leader = i;
			}

			walker.Go.saveDeck(0, Info.MyDeck0.card);
			walker.Go.saveDeck(1, Info.MyDeck1.card);
			walker.Go.saveDeck(2, Info.MyDeck2.card);

		} catch (Exception ex) {
			if (ErrorData.currentErrorType != ErrorData.ErrorType.none)
				throw ex;
			ErrorData.currentDataType = ErrorData.DataType.bytes;
			ErrorData.currentErrorType = ErrorData.ErrorType.GetCardDeckDataParseError;
			ErrorData.bytes = response;
			throw ex;
		}

		return true;
	}
}