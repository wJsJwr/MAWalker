package action;

import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;

import walker.ErrorData;
import walker.Process;
import action.ActionRegistry.Action;

public class RecvPFBGood {
	public static final Action Name = Action.RECV_PFB_GOOD;
	private static byte[] response;
	//private static final String URL_FAIRY_HISTORY = "http://web.million-arthurs.com/connect/app/private_fairy/private_fairy_history?cyt=1";
	private static final String URL_PRIVATE_BATTLE_TOP = "http://web.million-arthurs.com/connect/app/private_fairy/private_fairy_top?cyt=1";
	public static boolean run() throws Exception {
		Document doc;
			try {
				ArrayList<NameValuePair> al = new ArrayList<NameValuePair>();
				al.add(new BasicNameValuePair("serial_id", Process.info.fairy.SerialId));
				al.add(new BasicNameValuePair("user_id", Process.info.fairy.UserId));
				response = Process.network.ConnectToServer(URL_PRIVATE_BATTLE_TOP,
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
				ErrorData.currentErrorType = ErrorData.ErrorType.RecvPFBGoodDataError;
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
				ErrorData.currentErrorType = ErrorData.ErrorType.RecvPFBGoodResponse;
				ErrorData.currentDataType = ErrorData.DataType.text;
				ErrorData.text = xpath.evaluate(
						"/response/header/error/message", doc);
				return false;
			}

			String add, msg;
			if ((boolean)xpath.evaluate("count(/response/body/private_fairy_top/recover_by_like) > 0", doc, XPathConstants.BOOLEAN)) {
				
				msg = xpath.evaluate("/response/body/private_fairy_top/recover_by_like/message", doc);
				add = xpath.evaluate("/response/body/private_fairy_top/recover_by_like/recover_point", doc);

				ErrorData.currentErrorType = ErrorData.ErrorType.RecvPFBGoodResponse;
				ErrorData.currentDataType = ErrorData.DataType.text;
				ErrorData.text = String.format("%s\n收赞回复%s点BC...", msg, add);
			}
		} catch (Exception ex) {
			if (ErrorData.currentErrorType != ErrorData.ErrorType.none) throw ex;
			ErrorData.currentDataType = ErrorData.DataType.bytes;
			ErrorData.currentErrorType = ErrorData.ErrorType.RecvPFBGoodDataParseError;
			ErrorData.bytes = response;
			throw ex;
		}
		return true;
	}
}
