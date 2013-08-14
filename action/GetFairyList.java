package action;

import info.FairyBattleInfo;

import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.http.NameValuePair;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import walker.ErrorData;
import walker.Info;
import walker.Process;
import action.ActionRegistry.Action;

public class GetFairyList {
	public static final Action Name = Action.GET_FAIRY_LIST;

	private static final String URL_FAIRY_LIST = "http://web.million-arthurs.com/connect/app/private_fairy/private_fairy_select?cyt=1";
	
	private static byte[] response;
	
	public static boolean run() throws Exception {
		try {
			response = Process.network.ConnectToServer(URL_FAIRY_LIST, new ArrayList<NameValuePair>(), false);
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
			ErrorData.currentErrorType = ErrorData.ErrorType.FairyListDataError;
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
				ErrorData.currentErrorType = ErrorData.ErrorType.FairyListResponse;
				ErrorData.currentDataType = ErrorData.DataType.text;
				ErrorData.text = xpath.evaluate("/response/header/error/message", doc);
				return false;
			}
			
			if (!xpath.evaluate("//remaining_rewards", doc).equals("0")) {
				Process.info.events.push(Info.EventType.fairyReward);
			}
			
			// TODO: 这两周先是只寻找0BC的，之后再扩展
			//NodeList fairy = (NodeList)xpath.evaluate("//fairy_select/fairy_event[put_down=4]/fairy", doc, XPathConstants.NODESET);
			NodeList fairy = (NodeList)xpath.evaluate("//fairy_select/fairy_event[put_down=1]/fairy", doc, XPathConstants.NODESET);
			
			if (fairy.getLength() > 1) Process.info.events.push(Info.EventType.fairyAppear); // 以便再次寻找
			if (fairy.getLength() > 0) Process.info.events.push(Info.EventType.fairyCanBattle);
			for (int i = 0; i < fairy.getLength(); i++) {
				Node f = fairy.item(i).getFirstChild();
				do {
					if (f.getNodeName().equals("serial_id")) {
						Process.info.fairy.SerialId = f.getFirstChild().getNodeValue();
					} else if (f.getNodeName().equals("discoverer_id")) {
						Process.info.fairy.UserId = f.getFirstChild().getNodeValue();
						Process.info.fairy.Type = FairyBattleInfo.PRIVATE | FairyBattleInfo.RARE;
					} else if (f.getNodeName().equals("lv")) {
						Process.info.fairy.FairyLevel = f.getFirstChild().getNodeValue();
					} else if (f.getNodeName().equals("name")) {
						Process.info.fairy.FairyName = f.getFirstChild().getNodeValue();
					}
					f = f.getNextSibling();
				} while (f != null);
			}
			
			Process.info.SetTimeoutByAction(Name);
			
		} catch (Exception ex) {
			if (ErrorData.currentErrorType != ErrorData.ErrorType.none) throw ex;
			ErrorData.currentDataType = ErrorData.DataType.bytes;
			ErrorData.currentErrorType = ErrorData.ErrorType.FairyListDataParseError;
			ErrorData.bytes = response;
			throw ex;
		}
		
		return true;

	}
}
