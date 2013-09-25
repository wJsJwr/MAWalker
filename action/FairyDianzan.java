package action;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import walker.ErrorData;
import walker.Info;
import walker.Process;
import action.ActionRegistry.Action;

public class FairyDianzan {
	public static final Action Name = Action.FAIRY_DIANZAN;

	private static final String URL_PRIVATE_BATTLE_HISTORY = "http://web.million-arthurs.com/connect/app/private_fairy/private_fairy_history?cyt=1";
	private static final String URL_PRIVATE_BATTLE_DIANZAN = "http://web.million-arthurs.com/connect/app/private_fairy/private_fairy_battle_good?cyt=1";
	private static byte[] response;

	public static boolean run() throws Exception {

		while (!Process.info.FairyDianzanList.empty()) {
			info.FairyDianzanInfo fairy = Process.info.FairyDianzanList.pop();

			ArrayList<NameValuePair> post = new ArrayList<NameValuePair>();
			post.add(new BasicNameValuePair("serial_id", fairy.SerialId));
			post.add(new BasicNameValuePair("user_id", fairy.UserId));
			// 通讯
			try {
				response = Process.network.ConnectToServer(
						URL_PRIVATE_BATTLE_HISTORY, post, false);
			} catch (Exception ex) {
				ErrorData.currentDataType = ErrorData.DataType.text;
				ErrorData.currentErrorType = ErrorData.ErrorType.ConnectionError;
				ErrorData.text = ex.getLocalizedMessage();
				throw ex;
			}

			if (Info.Debug) {
				File outputFile = new File("PRIVATE_BATTLE_HISTORY.xml");
				FileOutputStream outputFileStream = new FileOutputStream(
						outputFile);
				outputFileStream.write(response);
				outputFileStream.close();
			}

			// 解析
			Document doc;
			try {
				doc = Process.ParseXMLBytes(response);
			} catch (Exception ex) {
				ErrorData.currentDataType = ErrorData.DataType.bytes;
				ErrorData.currentErrorType = ErrorData.ErrorType.FairyHistoryDataError;
				ErrorData.bytes = response;
				throw ex;
			}
			// 分析xml
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();

			try {
				// 检测是否有错误
				if (!xpath.evaluate("/response/header/error/code", doc).equals(
						"0")) {
					ErrorData.currentErrorType = ErrorData.ErrorType.FairyHistoryResponse;
					ErrorData.currentDataType = ErrorData.DataType.text;
					ErrorData.text = xpath.evaluate(
							"/response/header/error/message", doc);
					return false;
				}

				// 寻找帮助的好友
				NodeList attacker_list = (NodeList) xpath
						.evaluate(
								"/response/body/fairy_history/attacker_history/attacker[discoverer=0]",
								doc, XPathConstants.NODESET);

				String attacker_userid_list = "";
				String attacker_userid = "";
				for (int i = 0; i < attacker_list.getLength(); i++) {
					Node f = attacker_list.item(i).getFirstChild();
					do {
						if (f.getNodeName().equals("user_id")) {
							attacker_userid = f.getFirstChild().getNodeValue();
						}
						f = f.getNextSibling();
					} while (f != null);
					attacker_userid_list += attacker_userid;
					if (i != attacker_list.getLength() - 1) {
						attacker_userid_list += ",";
					}
				}

				fairy_dianzan(fairy.SerialId, attacker_userid_list);

			} catch (Exception ex) {
				if (ErrorData.currentErrorType != ErrorData.ErrorType.none)
					throw ex;
				ErrorData.currentDataType = ErrorData.DataType.bytes;
				ErrorData.currentErrorType = ErrorData.ErrorType.FairyHistoryDataParseError;
				ErrorData.bytes = response;
				throw ex;
			}
		}
		return true;

	}

	private static boolean fairy_dianzan(String SerialId,
			String Attacker_UserId_List) throws Exception {
		String dianzan_log;
		dianzan_log = String.format("Waiting for dianzan.");
		walker.Go.log(dianzan_log);

		ArrayList<NameValuePair> post = new ArrayList<NameValuePair>();
		post.add(new BasicNameValuePair("dialog", "1"));
		post.add(new BasicNameValuePair("serial_id", SerialId));
		post.add(new BasicNameValuePair("user_id", Attacker_UserId_List));
		// 通讯
		try {
			response = Process.network.ConnectToServer(
					URL_PRIVATE_BATTLE_DIANZAN, post, false);
		} catch (Exception ex) {
			ErrorData.currentDataType = ErrorData.DataType.text;
			ErrorData.currentErrorType = ErrorData.ErrorType.ConnectionError;
			ErrorData.text = ex.getLocalizedMessage();
			throw ex;
		}
		if (Info.Debug) {
			File outputFile = new File("PRIVATE_BATTLE_DIANZAN.xml");
			FileOutputStream outputFileStream = new FileOutputStream(outputFile);
			outputFileStream.write(response);
			outputFileStream.close();
		}

		// 解析
		Document doc;
		try {
			doc = Process.ParseXMLBytes(response);
		} catch (Exception ex) {
			ErrorData.currentDataType = ErrorData.DataType.bytes;
			ErrorData.currentErrorType = ErrorData.ErrorType.FairyDianzanDataError;
			ErrorData.bytes = response;
			throw ex;
		}

		// 分析xml
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		try {
			// 点赞的error code竟然是1010(汗)
			if (!xpath.evaluate("/response/header/error/code", doc).equals(
					"1010")) {
				ErrorData.currentErrorType = ErrorData.ErrorType.FairyDianzanResponse;
				ErrorData.currentDataType = ErrorData.DataType.text;
				ErrorData.text = xpath.evaluate(
						"/response/header/error/message", doc);
				return false;
			}

			if (xpath.evaluate("/response/body/friend_act_res/success", doc)
					.equals("1")) {
				String dianzan_message;
				dianzan_message = xpath.evaluate(
						"/response/body/friend_act_res/message", doc);
				dianzan_log = String.format("Succeed to dianzan, message: %s.",
						dianzan_message);
				walker.Go.log(dianzan_log);
			} else {
				dianzan_log = String.format("Fail to dianzan.");
				walker.Go.log(dianzan_log);
			}

		} catch (Exception ex) {
			if (ErrorData.currentErrorType != ErrorData.ErrorType.none)
				throw ex;
			ErrorData.currentDataType = ErrorData.DataType.bytes;
			ErrorData.currentErrorType = ErrorData.ErrorType.FairyDianzanDataParseError;
			ErrorData.bytes = response;
			throw ex;
		}
		return true;
	}

}
