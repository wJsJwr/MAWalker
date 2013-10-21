package action;

import info.Area;
import info.Floor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;

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
			// if (ex.getMessage().equals("302"))
			// 上面的是为了截获里图跳转
			ErrorData.currentDataType = ErrorData.DataType.text;
			ErrorData.currentErrorType = ErrorData.ErrorType.ConnectionError;
			ErrorData.text = ex.getMessage();
			throw ex;
		}

		// Thread.sleep(Process.getRandom(1000, 2000));

		if (Info.Debug) {
			String clazzName = new Object() {
				public String getClassName() {
					String clazzName = this.getClass().getName();
					return clazzName.substring(0, clazzName.lastIndexOf('$'));
				}
			}.getClassName();
			walker.Go.saveXMLFile(response, clazzName);
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
				// Process.info.area = new Hashtable<Integer,Area>();
				Process.info.area.clear();
				Process.info.floor.clear();
				Process.info.allFloors.clear();
			}
			for (int i = 1; i <= areaCount; i++) {
				Area a = new Area();
				String p = String.format("//area_info_list/area_info[%d]/", i);
				a.areaId = Integer.parseInt(xpath.evaluate(p + "id", doc));
				a.areaName = xpath.evaluate(p + "name", doc);
				a.exploreProgress = Integer.parseInt(xpath.evaluate(p
						+ "prog_area", doc));
				if (Info.GoNoEventArea && a.areaId < 50000)
					Process.info.area.put(a.areaId, a);
				if (Info.GoDailyArea && a.areaId >= 50000 && a.areaId < 100000)
					Process.info.area.put(a.areaId, a);
				if (a.areaId >= 100000)
					Process.info.area.put(a.areaId, a);
			}
			Process.info.front = null;
			Iterator<Entry<Integer, Area>> itr = Process.info.area.entrySet()
					.iterator();
			while (itr.hasNext()) {
				Area tmpArea = itr.next().getValue();
				getFloor(tmpArea);
			}

			for (Floor tmpFloor : Process.info.allFloors) {
				if (Process.info.front == null) {
					Process.info.front = tmpFloor;
				} else {
					if (Integer.parseInt(Process.info.front.areaId) < Integer
							.parseInt(tmpFloor.areaId)) {
						Process.info.front = tmpFloor;
					} else if (Process.info.front.equals(tmpFloor.areaId)) {
						if (Integer.parseInt(Process.info.front.floorId) < Integer
								.parseInt(tmpFloor.floorId)) {
							Process.info.front = tmpFloor;
						}
					}
				}
			}

			Process.info.AllClear = true;
			if (Process.info.area.get(Integer
					.parseInt(Process.info.front.areaId)).exploreProgress != 100) {
				Process.info.AllClear = false;
			}
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
			// if (ex.getMessage().equals("302"))
			// 上面的是为了截获里图跳转
			ErrorData.currentDataType = ErrorData.DataType.text;
			ErrorData.currentErrorType = ErrorData.ErrorType.ConnectionError;
			ErrorData.text = ex.getLocalizedMessage();
			throw ex;
		}

		// Thread.sleep(Process.getRandom(1000, 2000));

		if (Info.Debug) {
			String clazzName = new Object() {
				public String getClassName() {
					String clazzName = this.getClass().getName();
					return clazzName.substring(0, clazzName.lastIndexOf('$'));
				}
			}.getClassName();
			walker.Go.saveXMLFile(response, clazzName);
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
			if (f.cost < 1)
				continue; // 跳过秘境守护者
			if (f.progress != 100) {
				Process.info.allFloors.add(f);
			}
			if (Process.info.floor.containsKey(f.cost)) {
				if (Integer.parseInt(Process.info.floor.get(f.cost).areaId) >= Integer
						.parseInt(f.areaId)) {
					continue;
				}
			}
			Process.info.floor.put(f.cost, f);
		}
	}

}
