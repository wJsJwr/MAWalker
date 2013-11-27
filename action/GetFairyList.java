package action;

import info.FairyBattleInfo;
import info.FairySelectUser;

import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.http.NameValuePair;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import walker.ErrorData;
import walker.Go;
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
				ErrorData.text = xpath.evaluate(
						"/response/header/error/message", doc);
				return false;
			}

			if (!xpath.evaluate("//remaining_rewards", doc).equals("0")) {
				if (Info.receiveBattlePresent)
					Process.info.events.push(Info.EventType.fairyReward);
			}

			// 获取放妖的用户
			NodeList fairyuser = (NodeList) xpath.evaluate(
					"//fairy_select/user", doc, XPathConstants.NODESET);
			for (int i = 0; i < fairyuser.getLength(); i++) {
				Node f = fairyuser.item(i).getFirstChild();
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

			NodeList fairy = (NodeList) xpath.evaluate(
					"//fairy_select/fairy_event[put_down=1]/fairy", doc,
					XPathConstants.NODESET);
			Process.info.OwnFairyKilled = true;
			ArrayList<FairyBattleInfo> fbis = new ArrayList<FairyBattleInfo>();
			for (int i = 0; i < fairy.getLength(); i++) {
				Node f = fairy.item(i).getFirstChild();
				FairyBattleInfo fbi = new FairyBattleInfo();
				boolean attack_flag = false;
				do {
					if (f.getNodeName().equals("serial_id")) {
						fbi.SerialId = f.getFirstChild().getNodeValue();
					} else if (f.getNodeName().equals("discoverer_id")) {
						fbi.UserId = f.getFirstChild().getNodeValue();
					} else if (f.getNodeName().equals("lv")) {
						fbi.FairyLevel = f.getFirstChild().getNodeValue();
					} else if (f.getNodeName().equals("name")) {
						fbi.FairyName = f.getFirstChild().getNodeValue();
					} else if (f.getNodeName().equals("rare_flg")) {
						if (f.getFirstChild().getNodeValue().equals("1")) {
							if (fbi.UserId == Process.info.userId) {
								fbi.Type = FairyBattleInfo.PRIVATE
										| FairyBattleInfo.RARE
										| FairyBattleInfo.SELF;
							} else {
								fbi.Type = FairyBattleInfo.PRIVATE
										| FairyBattleInfo.RARE;
							}
						} else {
							fbi.Type = FairyBattleInfo.PRIVATE;
						}
					} else if (f.getNodeName().equals("hp")) {
						fbi.fairyCurrHp = Integer.parseInt(f.getFirstChild()
								.getNodeValue());
					} else if (f.getNodeName().equals("hp_max")) {
						fbi.fairyMaxHp = Integer.parseInt(f.getFirstChild()
								.getNodeValue());
					}
					f = f.getNextSibling();
				} while (f != null);

				if (Info.AllowAttackSameFairy) {
					fbis.add(fbi);
				} else {
					for (FairyBattleInfo bi : Process.info.LatestFairyList) {
						if (bi.equals(fbi)) {
							// 已经舔过
							attack_flag = true;
							break;
						}
					}
					if (!attack_flag)
						fbis.add(fbi);
				}
				if (Process.info.userId.equals(fbi.UserId)) {
					Process.info.OwnFairyKilled = false;
				}
			}// end for

			if (fbis.size() == 0) {
				// 活动妖，第一次0BC
				NodeList fairyEvent = (NodeList) xpath.evaluate(
						"//fairy_select/fairy_event[put_down=4]/fairy", doc,
						XPathConstants.NODESET);

				for (int i = 0; i < fairyEvent.getLength(); i++) {
					Node f = fairyEvent.item(i).getFirstChild();
					FairyBattleInfo fbi = new FairyBattleInfo();
					boolean attack_flag = false;
					do {
						if (f.getNodeName().equals("serial_id")) {
							fbi.SerialId = f.getFirstChild().getNodeValue();
						} else if (f.getNodeName().equals("discoverer_id")) {
							fbi.UserId = f.getFirstChild().getNodeValue();
						} else if (f.getNodeName().equals("lv")) {
							fbi.FairyLevel = f.getFirstChild().getNodeValue();
						} else if (f.getNodeName().equals("name")) {
							fbi.FairyName = f.getFirstChild().getNodeValue();
						} else if (f.getNodeName().equals("rare_flg")) {
							if (f.getFirstChild().getNodeValue().equals("1")) {
								if (fbi.UserId == Process.info.userId) {
									fbi.Type = FairyBattleInfo.PRIVATE
											| FairyBattleInfo.RARE
											| FairyBattleInfo.SELF;
								} else {
									fbi.Type = FairyBattleInfo.PRIVATE
											| FairyBattleInfo.RARE;
								}
							} else {
								fbi.Type = FairyBattleInfo.PRIVATE;
							}
						} else if (f.getNodeName().equals("hp")) {
							fbi.fairyCurrHp = Integer.parseInt(f
									.getFirstChild().getNodeValue());
						} else if (f.getNodeName().equals("hp_max")) {
							fbi.fairyMaxHp = Integer.parseInt(f.getFirstChild()
									.getNodeValue());
						}
						f = f.getNextSibling();
					} while (f != null);

					if (Info.AllowAttackSameFairy) {
						fbis.add(fbi);
					} else {
						for (FairyBattleInfo bi : Process.info.LatestFairyList) {
							if (bi.equals(fbi)) {
								// 已经舔过
								attack_flag = true;
								break;
							}
						}

						if (!attack_flag) {
							fbis.add(fbi);
						}
					}

					if (Process.info.userId.equals(fbi.UserId)) {
						Process.info.OwnFairyKilled = false;
					}
				}// end for

				if (fbis.size() > 0) {
					// 使用外敌卡组
					Process.info.fairy.No = Info.PublicFairyBattle.No;
				}
			}

			if (fbis.size() > 1)
				Process.info.events.push(Info.EventType.fairyAppear); // 以便再次寻找
			if (fbis.size() > 0) {
				if (!Process.info.events.contains("gotoFloor"))
					Process.info.events.push(Info.EventType.gotoFloor);
				Process.info.events.push(Info.EventType.recvPFBGood);
				Process.info.events.push(Info.EventType.fairyCanBattle);
				Process.info.fairy = new FairyBattleInfo(fbis.get(0));
			}

			NodeList fairy1 = (NodeList) xpath.evaluate(
					"//fairy_select/fairy_event[put_down=5]/fairy", doc,
					XPathConstants.NODESET);

			int aa = fairy1.getLength();

			Go.log("找到" + aa + "个可赞的PFB...");
			for (int i = 0; i < fairy1.getLength(); i++) {
				Node f = fairy1.item(i).getFirstChild();
				String serial_Id = "";
				String user_Id = "";
				do {
					if (f.getNodeName().equals("serial_id")) {
						serial_Id = f.getFirstChild().getNodeValue();
					} else if (f.getNodeName().equals("discoverer_id")) {
						user_Id = f.getFirstChild().getNodeValue();
					}
					f = f.getNextSibling();
				} while (f != null);
				Process.info.PFBGoodList.push(new info.PFBGood(serial_Id,
						user_Id));
			}
			if (!Process.info.PFBGoodList.isEmpty()) {
				Process.info.events.push(Info.EventType.PFBGood);
			}

			Process.info.SetTimeoutByAction(Name);

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
