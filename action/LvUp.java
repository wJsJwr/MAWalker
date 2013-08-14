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

public class LvUp {
	public static final Action Name = Action.LV_UP;
	
	private static final String URL_POINT_SETTING = "http://web.million-arthurs.com/connect/app/town/pointsetting?cyt=1";

	
	private static byte[] response;
	
	public static boolean run() throws Exception {
		ArrayList<NameValuePair> post = new ArrayList<NameValuePair>();
		post.add(new BasicNameValuePair("ap", String.valueOf(Process.info.apUp)));
		post.add(new BasicNameValuePair("bc", String.valueOf(Process.info.bcUp)));
		try {
			response = Process.network.ConnectToServer(URL_POINT_SETTING, post, false);
		} catch (Exception ex) {
			ErrorData.currentDataType = ErrorData.DataType.text;
			ErrorData.currentErrorType = ErrorData.ErrorType.ConnectionError;
			ErrorData.text = ex.getMessage();
			throw ex;
		}

		Document doc;
		try {
			doc = Process.ParseXMLBytes(response);
		} catch (Exception ex) {
			ErrorData.currentDataType = ErrorData.DataType.bytes;
			ErrorData.currentErrorType = ErrorData.ErrorType.LvUpDataError;
			ErrorData.bytes = response;
			throw ex;
		}
		
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		
		try {
			String code = xpath.evaluate("/response/header/error/code", doc);
			if (!code.equals("0")) {
				ErrorData.currentErrorType = ErrorData.ErrorType.LvUpResponse;
				ErrorData.currentDataType = ErrorData.DataType.text;
				ErrorData.text = xpath.evaluate("/response/header/error/message", doc);
				return false;
			}
			
			ParseUserDataInfo.parse(doc);
			
			Process.info.SetTimeoutByAction(Name);
			
		} catch (Exception ex) {
			if (ErrorData.currentErrorType != ErrorData.ErrorType.none) throw ex;
			ErrorData.currentDataType = ErrorData.DataType.bytes;
			ErrorData.currentErrorType = ErrorData.ErrorType.LvUpDataError;
			ErrorData.bytes = response;
			throw ex;
		}
		
		return true;
	}
	
}
