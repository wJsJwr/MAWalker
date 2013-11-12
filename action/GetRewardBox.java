package action;

import info.Box;
import info.Card;

import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import walker.ErrorData;
import walker.Go;
import walker.Info;
import walker.Process;

public class GetRewardBox {
	private static final String URL_LIST_REWARD_BOX = "http://web.million-arthurs.com/connect/app/menu/rewardbox?cyt=1";
	private static final String URL_GET_REWARD_BOX = "http://web.million-arthurs.com/connect/app/menu/get_rewards?cyt=1";

	private static byte[] response;

	public static void list() throws Exception {
		Document doc;
		try {
			response = Process.network.ConnectToServer(URL_LIST_REWARD_BOX,
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
			ErrorData.currentErrorType = ErrorData.ErrorType.GetRewardBoxDataError;
			ErrorData.bytes = response;
			throw ex;
		}
		parse(doc);
	}

	public static void parse(Document doc) throws NumberFormatException,
			XPathExpressionException {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		int boxCount = ((NodeList) xpath.evaluate("//rewardbox_list/rewardbox",
				doc, XPathConstants.NODESET)).getLength();
		Process.info.boxList = new ArrayList<Box>();
		for (int i = 1; i < boxCount + 1; i++) {
			Box b = new Box();
			String p = String.format("//rewardbox_list/rewardbox[%d]", i);

			b.boxId = xpath.evaluate(p + "/id", doc);
			b.boxType = Integer.parseInt(xpath.evaluate(p + "/type", doc));
			b.exist = true;
			Process.info.boxList.add(b);
		}
	}

	public static void get() throws Exception {
		Document doc;
		int count = 0;
		String toGet = "";
		for (Box b : Process.info.boxList) {
			if (!b.exist)
				continue;
			if (toGet.isEmpty()) {
				toGet = b.boxId;
			} else {
				toGet += "," + b.boxId;
			}
			count++;
			b.exist = false;
			if (count >= 20)
				break;
		}

		Process.info.toGet = toGet;

		if (!toGet.isEmpty()) {
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			ArrayList<NameValuePair> post = new ArrayList<NameValuePair>();
			post.add(new BasicNameValuePair("notice_id", Process.info.toGet));
			try {
				response = Process.network.ConnectToServer(URL_GET_REWARD_BOX,
						post, false);
			} catch (Exception ex) {
				ErrorData.currentDataType = ErrorData.DataType.text;
				ErrorData.currentErrorType = ErrorData.ErrorType.ConnectionError;
				ErrorData.text = ex.getLocalizedMessage();
				throw ex;
			}

			try {
				doc = Process.ParseXMLBytes(response);
			} catch (Exception ex) {
				ErrorData.currentDataType = ErrorData.DataType.bytes;
				ErrorData.currentErrorType = ErrorData.ErrorType.GetRewardBoxDataError;
				ErrorData.bytes = response;
				throw ex;
			}

			try {
				if (!xpath.evaluate("/response/header/error/code", doc).equals(
						"0")) {
					ErrorData.currentErrorType = ErrorData.ErrorType.GetRewardBoxReponse;
					ErrorData.currentDataType = ErrorData.DataType.text;
					ErrorData.text = xpath.evaluate(
							"/response/header/error/message", doc);
					Go.log(ErrorData.text);
				}
			} catch (Exception ex) {
				ErrorData.currentDataType = ErrorData.DataType.bytes;
				ErrorData.currentErrorType = ErrorData.ErrorType.GetRewardBoxDataError;
				ErrorData.bytes = response;
				throw ex;
			}

			ParseCardList.parse(doc);
		}
	}

	public static void sell() throws Exception {
		int count = 0;
		String toSell = "";
		for (Card c : Process.info.cardList) {
			if (!c.exist)
				continue;
			if (c.holo && c.price >= 3300)
				continue; // 闪卡不卖，但是低等级的闪卡照样要卖
			if (c.hp > 6000)
				continue; // 防止不小心把贵重卡片卖了
			if (c.hp <= 2 && c.atk <= 2)
				continue;
			if ((c.cardId.equals(124) || c.cardId.equals(142) || c.cardId
					.equals(9)) && !Info.CanBeSold.contains(c.cardId))
				continue;
			if (c.price <= 3300 || Info.CanBeSold.contains(c.cardId)) {
				if (toSell.isEmpty()) {
					toSell = c.serialId;
				} else {
					toSell += "," + c.serialId;
				}
				count++;
				c.exist = false;
			}
			if (count >= 30)
				break;
		}

		Process.info.toSell = toSell;

		if (!toSell.isEmpty()) {
			SellCard.run();
			Go.log(ErrorData.text);
			ErrorData.text = null;
		}
	}
}