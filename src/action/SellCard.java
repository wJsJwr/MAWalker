package action;

import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;

import walker.ErrorData;
import walker.Process;
import action.ActionRegistry.Action;

public class SellCard {
	public static final Action Name = Action.SELL_CARD;
	
	private static final String URL_SELL_CARD = "http://web.million-arthurs.com/connect/app/trunk/sell?cyt=1";
	private static byte[] response;
	
	public static boolean run() throws Exception {
		if (Process.info.toSell.isEmpty()) return false;
		ArrayList<NameValuePair> post = new ArrayList<NameValuePair>();
		post.add(new BasicNameValuePair("serial_id", Process.info.toSell));
		try {
			response = Process.network.ConnectToServer(URL_SELL_CARD, post, false);
		} catch (Exception ex) {
			ErrorData.currentDataType = ErrorData.DataType.text;
			ErrorData.currentErrorType = ErrorData.ErrorType.ConnectionError;
			ErrorData.text = ex.getLocalizedMessage();
			throw ex;
		}

		Document doc;
		try {
			doc = Process.ParseXMLBytes(response);
		} catch (Exception ex) {
			ErrorData.currentDataType = ErrorData.DataType.bytes;
			ErrorData.currentErrorType = ErrorData.ErrorType.SellCardDataError;
			ErrorData.bytes = response;
			throw ex;
		}
		
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		
		try {
			if (!xpath.evaluate("/response/header/error/code", doc).equals("1010")) {
				ErrorData.currentErrorType = ErrorData.ErrorType.SellCardResponse;
				ErrorData.currentDataType = ErrorData.DataType.text;
				ErrorData.text = xpath.evaluate("/response/header/error/message", doc);
				return false;
			} else {
				ErrorData.text = xpath.evaluate("/response/header/error/message", doc);
				Process.info.toSell = "";
				return true;
			}
			
		} catch (Exception ex) {
			if (ErrorData.currentErrorType != ErrorData.ErrorType.none) throw ex;
			ErrorData.currentDataType = ErrorData.DataType.bytes;
			ErrorData.currentErrorType = ErrorData.ErrorType.SellCardDataError;
			ErrorData.bytes = response;
			throw ex;
		}
		

	}
	
}
