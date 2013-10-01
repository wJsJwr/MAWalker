package action;

import info.FairyBattleInfo;
import info.FairyDianzanInfo;
import info.FairySelectUser;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;

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
			response = Process.network.ConnectToServer(URL_FAIRY_LIST,
					new ArrayList<NameValuePair>(), false);
		} catch (Exception ex) {
			ErrorData.currentDataType = ErrorData.DataType.text;
			ErrorData.currentErrorType = ErrorData.ErrorType.ConnectionError;
			ErrorData.text = ex.getMessage();
			throw ex;
		}

		Thread.sleep(Process.getRandom(2000, 3000));

		if (Info.Debug) {
			File outputFile = new File("FAIRY_LIST.xml");
			FileOutputStream outputFileStream = new FileOutputStream(outputFile);
			outputFileStream.write(response);
			outputFileStream.close();
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
				if (xpath.evaluate("/response/header/error/code", doc).equals(
						"9000")) {
					Process.AddUrgentTask(Info.EventType.cookieOutOfDate);
					ErrorData.currentErrorType = ErrorData.ErrorType.CookieOutOfDate;
				} else {
					ErrorData.currentErrorType = ErrorData.ErrorType.FairyListResponse;
				}
				ErrorData.currentDataType = ErrorData.DataType.text;
				ErrorData.text = xpath.evaluate(
						"/response/header/error/message", doc);
				return false;
			}

			// 寻找可以点赞的妖
			NodeList fairy_dianzan = (NodeList) xpath.evaluate(
					"//fairy_select/fairy_event[put_down=5]/fairy", doc,
					XPathConstants.NODESET);

			if (fairy_dianzan.getLength() > 0) {
				String dianzan_result = String.format(
						"Find %d fairy(s) that we can send dianzan.",
						fairy_dianzan.getLength());
				walker.Go.log(dianzan_result);
			}

			for (int i = 0; i < fairy_dianzan.getLength(); i++) {
				Node f = fairy_dianzan.item(i).getFirstChild();
				FairyDianzanInfo fbi_dianzan = new FairyDianzanInfo();
				boolean dianzan_flag = false;
				do {
					if (f.getNodeName().equals("serial_id")) {
						fbi_dianzan.SerialId = f.getFirstChild().getNodeValue();
					} else if (f.getNodeName().equals("discoverer_id")) {
						fbi_dianzan.UserId = f.getFirstChild().getNodeValue();
					}
					f = f.getNextSibling();
				} while (f != null);
				FairyDianzanInfo fdz = new FairyDianzanInfo(
						fbi_dianzan.SerialId, fbi_dianzan.UserId);
				for (FairyDianzanInfo fdzi : Process.info.FairyDianzanList) {
					if (fdz.equals(fdzi)) {
						dianzan_flag = true;
						break;
					}
				}
				if (!dianzan_flag)
					Process.info.FairyDianzanList.push(fdz);
			}

			if (!Process.info.FairyDianzanList.empty()) {
				Process.AddTask(Info.EventType.fairyDianzan);
			}

			// 获取奖励
			if (!xpath.evaluate("//remaining_rewards", doc).equals("0")) {
				Process.AddTask(Info.EventType.fairyReward);
			}

			// 获取放妖的用户
			NodeList fairy_finder = (NodeList) xpath.evaluate(
					"//fairy_select/user", doc, XPathConstants.NODESET);
			for (int i = 0; i < fairy_finder.getLength(); i++) {
				Node f = fairy_finder.item(i).getFirstChild();
				FairySelectUser fsu = new FairySelectUser();
				do {
					if (f.getNodeName().equals("id")) {
						fsu.userID = f.getFirstChild().getNodeValue();
					} else if (f.getNodeName().equals("name")) {
						fsu.userName = f.getFirstChild().getNodeValue();
					}
					f = f.getNextSibling();
				} while (f != null);
				if (!Process.info.FairySelectUserList.containsKey(fsu.userID)) {
					Process.info.FairySelectUserList.put(fsu.userID, fsu);
				}
			}

			// TODO: 这两周先是只寻找0BC的，之后再扩展
			Process.info.myFairyStillAlive = false;
			// NodeList fairy =
			// (NodeList)xpath.evaluate("//fairy_select/fairy_event[put_down=4]/fairy",
			// doc, XPathConstants.NODESET);
			NodeList fairy = (NodeList) xpath.evaluate(
					"//fairy_select/fairy_event[put_down=1]/fairy", doc,
					XPathConstants.NODESET);

			Process.info.PrivateFairyList = new LinkedList<FairyBattleInfo>();
			for (int i = 0; i < fairy.getLength(); i++) {
				Node f = fairy.item(i).getFirstChild();
				FairyBattleInfo fbi = new FairyBattleInfo();
				fbi.ForceKill = false;
				boolean attack_flag = false;
				do {
					if (f.getNodeName().equals("serial_id")) {
						fbi.SerialId = f.getFirstChild().getNodeValue();
					} else if (f.getNodeName().equals("discoverer_id")) {
						fbi.UserId = f.getFirstChild().getNodeValue();
						fbi.Type = FairyBattleInfo.PRIVATE;
						if (Process.info.userId.equals(fbi.UserId)) {
							fbi.Type |= FairyBattleInfo.SELF;
							Process.info.myFairyStillAlive = true;
						}
					} else if (f.getNodeName().equals("lv")) {
						fbi.FairyLevel = Integer.parseInt(f.getFirstChild()
								.getNodeValue());
					} else if (f.getNodeName().equals("name")) {
						fbi.FairyName = f.getFirstChild().getNodeValue();
					} else if (f.getNodeName().equals("rare_flg")) {
						if (f.getFirstChild().getNodeValue().equals("1")) {
							fbi.Type |= FairyBattleInfo.RARE;
						}
					} else if (f.getNodeName().equals("hp")) {
						fbi.FairyHp = Integer.parseInt(f.getFirstChild()
								.getNodeValue());
					} else if (f.getNodeName().equals("hp_max")) {
						fbi.FairyHpMax = Integer.parseInt(f.getFirstChild()
								.getNodeValue());
					}
					f = f.getNextSibling();
				} while (f != null);
				if (Info.AllowAttackSameFairy) {
					Process.info.PrivateFairyList.offer(fbi);
				} else {
					if (fbi.ForceKill) {
						Process.info.PrivateFairyList.offer(fbi);
					} else {
						for (FairyBattleInfo bi : Process.info.LatestFairyList) {
							if (bi.equals(fbi)) {
								// 已经舔过
								attack_flag = true;
								if (fbi.FairyHp < Info.killFairyHp)
									fbi.ForceKill = true;
							}
						}
						if (!attack_flag || fbi.ForceKill)
							Process.info.PrivateFairyList.offer(fbi);
					}
				}
			}

			if (!Process.info.PrivateFairyList.isEmpty()) {
				Process.AddUrgentTask(Info.EventType.fairyCanBattle);
			}

		} catch (Exception ex) {
			if (ErrorData.currentErrorType != ErrorData.ErrorType.none)
				throw ex;
			ErrorData.currentDataType = ErrorData.DataType.bytes;
			ErrorData.currentErrorType = ErrorData.ErrorType.FairyListDataParseError;
			ErrorData.bytes = response;
			throw ex;
		}

		return true;
	}
}
