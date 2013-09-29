package action;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

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

public class ChangeCardDeck {

	private static final String URL_CHANGE_CARD_DECK = "http://web.million-arthurs.com/connect/app/cardselect/savedeckcard?cyt=1";
	private static byte[] response;

	public static boolean run() throws Exception {

		if (Info.LastDeckNo.equals(Process.info.CurrentDeck.No))
			return true;
		else
			Info.LastDeckNo = Process.info.CurrentDeck.No;

		Document doc;
		ArrayList<NameValuePair> post = new ArrayList<NameValuePair>();
		post.add(new BasicNameValuePair("C", Process.info.CurrentDeck.card));
		post.add(new BasicNameValuePair("lr", Process.info.CurrentDeck.leader));
		post.add(new BasicNameValuePair("no", "2"));
		try {
			response = walker.Process.network.ConnectToServer(
					URL_CHANGE_CARD_DECK, post, false);
		} catch (Exception ex) {
			ErrorData.currentDataType = ErrorData.DataType.text;
			ErrorData.currentErrorType = ErrorData.ErrorType.ConnectionError;
			ErrorData.text = ex.getMessage();
			throw ex;
		}

		if (Info.Debug) {
			File outputFile = new File("CHANGE_CARD_DECK.xml");
			FileOutputStream outputFileStream = new FileOutputStream(outputFile);
			outputFileStream.write(response);
			outputFileStream.close();
		}

		try {
			doc = Process.ParseXMLBytes(response);
		} catch (Exception ex) {
			ErrorData.currentDataType = ErrorData.DataType.bytes;
			ErrorData.currentErrorType = ErrorData.ErrorType.ChangeCardDeckDataError;
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
				ErrorData.currentErrorType = ErrorData.ErrorType.ChangeCardDeckResponse;
				ErrorData.currentDataType = ErrorData.DataType.text;
				ErrorData.text = xpath.evaluate(
						"/response/header/error/message", doc);
				return false;
			}
			if (!xpath.evaluate("/response/body/save_deck_card/result", doc)
					.equals("0"))
				return false;

			NodeList tempDeckList = (NodeList) xpath.evaluate(
					"/response/body/deck", doc, XPathConstants.NODESET);
			ArrayList<String> myDeckList = new ArrayList<String>();
			for (int i = 0; i < tempDeckList.getLength(); i++) {
				Node f = tempDeckList.item(i).getFirstChild();
				String tempDeck = "";
				do {
					if (f.getNodeName().equals("deck_cards")) {
						tempDeck = f.getFirstChild().getNodeValue();
					}
					f = f.getNextSibling();
				} while (f != null);
				myDeckList.add(tempDeck);
			}

			if (myDeckList.size() != 4)
				return false;

			Info.MyDeck0.card = myDeckList.get(0);
			Info.MyDeck1.card = myDeckList.get(1);
			Info.MyDeck2.card = myDeckList.get(2);

			walker.Go.saveDeck(0, Info.MyDeck0.card);
			walker.Go.saveDeck(1, Info.MyDeck1.card);
			walker.Go.saveDeck(2, Info.MyDeck2.card);

			if (!Info.MyDeck2.card.equals(Process.info.CurrentDeck.card))
				return false;

		} catch (Exception ex) {
			if (ErrorData.currentErrorType != ErrorData.ErrorType.none)
				throw ex;
			ErrorData.currentDataType = ErrorData.DataType.bytes;
			ErrorData.currentErrorType = ErrorData.ErrorType.ChangeCardDeckDataParseError;
			ErrorData.bytes = response;
			throw ex;
		}

		return true;
	}
}