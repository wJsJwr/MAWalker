package action;

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
import walker.Go;
import walker.Process;
import action.ActionRegistry.Action;

public class PFBGood {
	public static final Action Name = Action.PFB_GOOD;

	private static final String URL_PFB_GOOD = "http://web.million-arthurs.com/connect/app/private_fairy/private_fairy_battle_good?cyt=1";
	private static final String URL_FAIRY_HISTORY = "http://web.million-arthurs.com/connect/app/private_fairy/private_fairy_history?cyt=1";
	private static byte[] response;
	private static String serial_Id;

	public static boolean run() throws Exception {
		Document doc;
		Boolean set = false;
		//for (info.PFBGood pg : Process.info.PFBGoodList) {
		while (!Process.info.PFBGoodList.empty()) { 
			info.PFBGood pg = Process.info.PFBGoodList.pop();
			try {
				serial_Id = pg.serialId;
				ArrayList<NameValuePair> al = new ArrayList<NameValuePair>();
				al.add(new BasicNameValuePair("serial_id", pg.serialId));
				al.add(new BasicNameValuePair("user_id", pg.userId));
				response = Process.network.ConnectToServer(URL_FAIRY_HISTORY,
						al, false);
			} catch (Exception ex) {
				ErrorData.currentDataType = ErrorData.DataType.text;
				ErrorData.currentErrorType = ErrorData.ErrorType.ConnectionError;
				ErrorData.text = ex.getMessage();
				throw ex;
			}

			try {
				doc = Process.ParseXMLBytes(response);
			} catch (Exception ex) {
				ErrorData.currentDataType = ErrorData.DataType.bytes;
				ErrorData.currentErrorType = ErrorData.ErrorType.FairyHistoryDataError;
				ErrorData.bytes = response;
				throw ex;
			}

			try {
				set = parse(doc);
			} catch (Exception ex) {
				if (ErrorData.currentErrorType == ErrorData.ErrorType.none) {
					throw ex;
				}
			}
		}

		return set;
	}

	private static boolean parse(Document doc) throws Exception {

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		try {
			if (!xpath.evaluate("/response/header/error/code", doc).equals("0")) {
				ErrorData.currentErrorType = ErrorData.ErrorType.FairyHistoryResponse;
				ErrorData.currentDataType = ErrorData.DataType.text;
				ErrorData.text = xpath.evaluate(
						"/response/header/error/message", doc);
				return false;
			}

			NodeList fairy = (NodeList) xpath.evaluate(
					"//fairy_history/attacker_history/attacker", doc,
					XPathConstants.NODESET);
			String user_id = "";
			for (int i = 0; i < fairy.getLength(); i++) {
				Node f = fairy.item(i).getFirstChild();
				do {
					if (f.getNodeName().equals("user_id")) {
						String str = f.getFirstChild().getNodeValue();
						if (!str.equals(Process.info.userId))
							user_id += f.getFirstChild().getNodeValue() + ",";
					}
					f = f.getNextSibling();
				} while (f != null);
			}
			user_id = user_id.substring(0, user_id.length() - 1);
			try {
				if (run2(serial_Id, user_id)) {
					Go.log(ErrorData.text);
					ErrorData.clear();
				}
			} catch (Exception ex) {
				if (ErrorData.currentErrorType == ErrorData.ErrorType.none)
					throw ex;
			}
			Process.info.SetTimeoutByAction(Name);

		} catch (Exception ex) {
			if (ErrorData.currentErrorType != ErrorData.ErrorType.none)
				throw ex;
			ErrorData.currentDataType = ErrorData.DataType.bytes;
			ErrorData.currentErrorType = ErrorData.ErrorType.FairyHistoryDataParseError;
			ErrorData.bytes = response;
			throw ex;
		}
		return true;
	}

	public static boolean run2(String serialId, String userId) throws Exception {
		Document doc;
		try {
			ArrayList<NameValuePair> al = new ArrayList<NameValuePair>();
			al.add(new BasicNameValuePair("dialog", "1"));
			al.add(new BasicNameValuePair("serial_id", serialId));
			al.add(new BasicNameValuePair("user_id", userId));
			response = Process.network.ConnectToServer(URL_PFB_GOOD, al, false);
		} catch (Exception ex) {
			ErrorData.currentDataType = ErrorData.DataType.text;
			ErrorData.currentErrorType = ErrorData.ErrorType.ConnectionError;
			ErrorData.text = ex.getMessage();
			throw ex;
		}

		try {
			doc = Process.ParseXMLBytes(response);
		} catch (Exception ex) {
			ErrorData.currentDataType = ErrorData.DataType.bytes;
			ErrorData.currentErrorType = ErrorData.ErrorType.PFB_GoodDataError;
			ErrorData.bytes = response;
			throw ex;
		}

		try {
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			if (!xpath.evaluate("/response/header/error/code", doc).equals(
					"1010")) {
				ErrorData.currentErrorType = ErrorData.ErrorType.PFB_GoodResponse;
				ErrorData.currentDataType = ErrorData.DataType.text;
				ErrorData.text = xpath.evaluate(
						"/response/header/error/message", doc);
				return false;
			} else {
				ErrorData.text = xpath.evaluate(
						"/response/header/error/message", doc);
				return true;
			}

		} catch (Exception ex) {
			if (ErrorData.currentErrorType == ErrorData.ErrorType.none) {
				throw ex;
			}
		}

		return false;
	}
}
