package action;

import info.Area;
import info.Floor;

import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import walker.ErrorData;
import walker.Info;
import walker.Process;
import action.ActionRegistry.Action;

public class GetFloorInfo {
	public static final Action Name = Action.GET_FLOOR_INFO;

	private static final String URL_AREA = "http://web.million-arthurs.com/connect/app/exploration/area?cyt=1";
	private static final String URL_FLOOR = "http://web.million-arthurs.com/connect/app/exploration/floor?cyt=1";

	private static byte[] response;

	public static boolean run() throws Exception {
		response = null;
		Document doc;
		try {
			response = Process.network.ConnectToServer(URL_AREA,
					new ArrayList<NameValuePair>(), false);
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
				ErrorData.text = xpath.evaluate(
						"/response/header/error/message", doc);
				return false;
			}

			int areaCount = ((NodeList) xpath.evaluate(
					"//area_info_list/area_info", doc, XPathConstants.NODESET))
					.getLength();
			if (areaCount > 0) {
				Process.info.area.clear();
				Process.info.floor.clear();
			}

			for (int i = 1; i <= areaCount; i++) {
				Area a = new Area();
				String p = String.format("//area_info_list/area_info[%d]/", i);
				a.areaId = Integer.parseInt(xpath.evaluate(p + "id", doc));
				a.areaName = xpath.evaluate(p + "name", doc);
				a.exploreProgress = Integer.parseInt(xpath.evaluate(p
						+ "prog_area", doc));
				a.areaType = (a.areaId >= Info.InnerMapNo) ? 1 : 0;
				if (a.areaType == 1) {
					if (Info.InnerInstance)
						Process.info.area.put(a.areaId, a);
				} else if (a.areaId > 100000)
					Process.info.area.put(a.areaId, a);
				if (Info.SpecilInstance && a.areaId >= 50000
						&& a.areaId <= 50010)
					Process.info.area.put(a.areaId, a);
			}

			Process.info.AllClear = true;
			Process.info.front = null;

			// 日常图逻辑
			boolean flag = false;
			for (int i = 50000; i <= 50010; i++) {
				if (Process.info.area.containsKey(i)
						&& Process.info.area.get(i).exploreProgress < 100) {
					getFloor(Process.info.area.get(i));
					flag = true;
					break;
				}
			}

			if (flag == false) {
				for (int i : Process.info.area.keySet()) {
					getFloor(Process.info.area.get(i));
				} // end of area iterator
			}

			int floor = Process.info.floor.firstKey();
			if (Info.MinAPOnly || Process.info.AllClear) {
				if (Info.InnerInstance) {
					for (int i : Process.info.floor.keySet()) {
						if (Process.info.floor.get(i).innerFlag) {
							Process.info.front = Process.info.floor.get(i);
							break;
						}
					}
				} else {
					for (int i : Process.info.floor.keySet()) {
						if (!Process.info.floor.get(i).innerFlag) {
							Process.info.front = Process.info.floor.get(i);
							break;
						}
					}
				}
			} else {
				for (int i : Process.info.floor.keySet()) {
					if (!Info.InnerInstance) {
						if (Process.info.floor.get(i).innerFlag) {
							Process.info.floor.remove(i);
						}
					}
				}
				floor = Process.info.floor.lastKey();
			}

			if (Process.info.front == null)
				Process.info.front = Process.info.floor.get(floor);
			Process.info.SetTimeoutByAction(Name);
		} catch (Exception ex) {
			if (ErrorData.currentErrorType == ErrorData.ErrorType.none) {
				throw ex;
			}
		}

		return true;
	}

	public static void getFloor(Area a) throws Exception {
		ArrayList<NameValuePair> post = new ArrayList<NameValuePair>();
		post.add(new BasicNameValuePair("area_id", String.valueOf(a.areaId)));
		try {
			response = Process.network.ConnectToServer(URL_FLOOR, post, false);
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
			ErrorData.currentErrorType = ErrorData.ErrorType.AreaDataError;
			ErrorData.bytes = response;
			throw ex;
		}

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		int floorCount = ((NodeList) xpath.evaluate(
				"//floor_info_list/floor_info", doc, XPathConstants.NODESET))
				.getLength();
		String aid = xpath.evaluate("//exploration_floor/area_id", doc);

		for (int j = floorCount; j > 0; j--) {
			Floor f = new Floor();
			String p = String.format("//floor_info_list/floor_info[%d]/", j);
			f.areaId = aid;
			f.floorId = xpath.evaluate(p + "id", doc);
			f.cost = Integer.parseInt(xpath.evaluate(p + "cost", doc));
			f.progress = Integer.parseInt(xpath.evaluate(p + "progress", doc));
			f.type = xpath.evaluate(p + "type", doc);
			f.innerFlag = (a.areaType == 1) ? true : false;

			int areaId = Integer.parseInt(f.areaId);
			if (areaId < 100000 && f.cost < 1
					&& (areaId < 50000 || areaId > 50010)) {
				continue; // 跳过秘境守护者
			}

			if (Process.info.floor.containsKey(f.cost)) {
				if (Integer.parseInt(Process.info.floor.get(f.cost).areaId) > Integer
						.parseInt(f.areaId)) {
					continue;
				}
			}

			Process.info.floor.put(f.cost, f);
			if (f.progress != 100 && a.exploreProgress != 100
					&& Process.info.AllClear) {
				Process.info.front = f;
				Process.info.AllClear = false;
			}
		}
	}

}
