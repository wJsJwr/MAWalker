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

public class GotoFloor {
	public static final Action Name = Action.GOTO_FLOOR;
	
	private static final String URL_GET_FLOOR = "http://web.million-arthurs.com/connect/app/exploration/get_floor?cyt=1";
	
	private static byte[] response;
	
	public static boolean run() throws Exception {
		ArrayList<NameValuePair> post = new ArrayList<NameValuePair>();
		post.add(new BasicNameValuePair("area_id", Process.info.front.areaId));
		post.add(new BasicNameValuePair("check","1"));
		post.add(new BasicNameValuePair("floor_id",Process.info.front.floorId));
		try {
			response = Process.network.ConnectToServer(URL_GET_FLOOR, post, false);
		} catch (Exception ex) {
			//if (ex.getMessage().equals("302")) 
			// 上面的是为了截获里图跳转
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
			ErrorData.currentErrorType = ErrorData.ErrorType.GotoFloorDataError;
			ErrorData.bytes = response;
			throw ex;
		}
		
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		
		
		try {
			if (!xpath.evaluate("/response/header/error/code", doc).equals("0")) {
				ErrorData.currentErrorType = ErrorData.ErrorType.GotoFloorResponse;
				ErrorData.currentDataType = ErrorData.DataType.text;
				ErrorData.text = xpath.evaluate("/response/header/error/message", doc);
				return false;
			}
			Process.info.username = xpath.evaluate("//your_data/name", doc);
			Process.info.lv = Integer.parseInt(xpath.evaluate("//town_level", doc));
			Process.info.ap = Integer.parseInt(xpath.evaluate("//ap/current", doc));
			Process.info.apMax = Integer.parseInt(xpath.evaluate("//ap/max", doc));
			Process.info.bc = Integer.parseInt(xpath.evaluate("//bc/current", doc));
			Process.info.bcMax = Integer.parseInt(xpath.evaluate("//bc/max", doc));
			Process.info.guildId = xpath.evaluate("//your_data/party_id", doc);
			
			Process.info.SetTimeoutByAction(Name);
			
			Process.info.exp = Integer.parseInt(xpath.evaluate("//get_floor/next_exp", doc));
			String spec = xpath.evaluate("//get_floor/special_item/before_count", doc);
			if (spec.length() != 0) {
				Process.info.gather = Integer.parseInt(spec);
			} else {
				Process.info.gather = -1;
			}
		} catch (Exception ex) {
			if (ErrorData.currentErrorType != ErrorData.ErrorType.none) throw ex;
			ErrorData.currentDataType = ErrorData.DataType.bytes;
			ErrorData.currentErrorType = ErrorData.ErrorType.GotoFloorDataParseError;
			ErrorData.bytes = response;
			throw ex;
		}
		
		return true;
	}
}
