package action;

import info.Area;

import java.util.ArrayList;
import java.util.Hashtable;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.http.NameValuePair;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import walker.ErrorData;
import walker.Process;
import action.ActionRegistry.Action;

public class AddArea {
	public static final Action Name = Action.ADD_AREA;
	
	private static final String URL_AREA = "http://web.million-arthurs.com/connect/app/exploration/area?cyt=1";	
	
	private static byte[] response;
	
	public static boolean run() throws Exception {
		response = null;
		Document doc;
		try {
			response = Process.network.ConnectToServer(URL_AREA, new ArrayList<NameValuePair>(), false);
		} catch (Exception ex) {
			//if (ex.getMessage().equals("302")) 
			// 上面的是为了截获里图跳转
			ErrorData.currentDataType = ErrorData.DataType.text;
			ErrorData.currentErrorType = ErrorData.ErrorType.ConnectionError;
			ErrorData.text = ex.getMessage();
			throw ex;
		}
		
		try {
			doc = Process.ParseXMLBytes(response);
		} catch (Exception ex) {
			ErrorData.currentDataType = ErrorData.DataType.bytes;
			ErrorData.currentErrorType = ErrorData.ErrorType.AreaDataError;
			ErrorData.bytes = response;
			throw ex;
		}
		
		try {
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			if (!xpath.evaluate("/response/header/error/code", doc).equals("0")) {
				ErrorData.currentErrorType = ErrorData.ErrorType.AreaResponse;
				ErrorData.currentDataType = ErrorData.DataType.text;
				ErrorData.text = xpath.evaluate("/response/header/error/message", doc);
				return false;
			}
			
			int areaCount = ((NodeList)xpath.evaluate("//area_info_list/area_info", doc, XPathConstants.NODESET)).getLength();
			if (areaCount > 0) Process.info.area = new Hashtable<Integer,Area>();
			Area newArea = new Area();
			newArea.areaId = -1;
			for (int i = areaCount; i > 0; i--){
				Area a = new Area();
				String p = String.format("//area_info_list/area_info[%d]/",i);
				a.areaId = Integer.parseInt(xpath.evaluate(p+"id", doc));
				if (Process.info.area.containsKey(a.areaId)) {
					continue;
				} else {
					newArea = a;
				}
				a.areaName = xpath.evaluate(p+"name", doc);
				a.exploreProgress = Integer.parseInt(xpath.evaluate(p+"prog_area", doc));
				if (a.areaId > 100000) Process.info.area.put(a.areaId, a);
			}
			Process.info.AllClear = true;
			
			if (newArea.areaId != -1) GetFloorInfo.getFloor(newArea);
			
		} catch (Exception ex) {
			if (ErrorData.currentErrorType == ErrorData.ErrorType.none) {
				throw ex;
			}
		}
		
		return true;
	}

}
