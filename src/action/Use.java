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

public class Use {
public static final Action Name = Action.USE;
	
	private static final String URL_USE = "http://web.million-arthurs.com/connect/app/item/use?cyt=1";
	private static byte[] response;
	
	public static boolean run() throws Exception {
		if (Process.info.toUse.isEmpty()) return false;
		ArrayList<NameValuePair> post = new ArrayList<NameValuePair>();
		post.add(new BasicNameValuePair("item_id", Process.info.toUse));
		try {
			response = Process.network.ConnectToServer(URL_USE, post, false);
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
			ErrorData.currentErrorType = ErrorData.ErrorType.UseDataError;
			ErrorData.bytes = response;
			throw ex;
		}
		
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		
		try {
			if (!xpath.evaluate("/response/header/error/code", doc).equals("1000")) {
				ErrorData.currentErrorType = ErrorData.ErrorType.UseResponse;
				ErrorData.currentDataType = ErrorData.DataType.text;
				ErrorData.text = xpath.evaluate("/response/header/error/message", doc);
				return false;
			} else {
				ErrorData.text = xpath.evaluate("/response/header/error/message", doc);
				Process.info.toUse = "";
				ParseUserDataInfo.parse(doc);
				return true;
			}
			
		} catch (Exception ex) {
			if (ErrorData.currentErrorType != ErrorData.ErrorType.none) throw ex;
			ErrorData.currentDataType = ErrorData.DataType.bytes;
			ErrorData.currentErrorType = ErrorData.ErrorType.UseDataError;
			ErrorData.bytes = response;
			throw ex;
		}
		

	}
	
}
