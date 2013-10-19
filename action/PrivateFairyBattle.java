package action;

//import info.FairyBattleInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;

import walker.ErrorData;
import walker.Info;
import walker.Process;
import action.ActionRegistry.Action;

public class PrivateFairyBattle {
	public static final Action Name = Action.PRIVATE_FAIRY_BATTLE;

	private static final String URL_PRIVATE_BATTLE = "http://web.million-arthurs.com/connect/app/private_fairy/private_fairy_battle?cyt=1";

	private static byte[] response;

	public enum PrivateFairyBattleResult {
		win, lose, escape, unknown
	};

	public static PrivateFairyBattleResult FairyBattleResult = PrivateFairyBattleResult.unknown;

	public static boolean run() throws Exception {
		FairyBattleResult = PrivateFairyBattleResult.unknown;

		boolean flag_deck = false;

		if (Integer.parseInt(Process.info.CurrentDeck.No) > 200) {
			if (ChangeCardDeck.run()) {
				Process.info.pfairy.No = "2";
				flag_deck = true;
				walker.Go.log(String.format(
						"Succeed to change card deck to Deck %s",
						Process.info.CurrentDeck.No), true);
			} else {
				flag_deck = false;
				walker.Go.log("Fail to change card deck.", true);
			}
		} else {
			Process.info.pfairy.No = Process.info.CurrentDeck.No;
			flag_deck = true;
		}
		if (!flag_deck)
			return false;

		ArrayList<NameValuePair> post = new ArrayList<NameValuePair>();
		post.add(new BasicNameValuePair("no", Process.info.pfairy.No));
		post.add(new BasicNameValuePair("serial_id",
				Process.info.pfairy.SerialId));
		post.add(new BasicNameValuePair("user_id", Process.info.pfairy.UserId));

		try {
			response = Process.network.ConnectToServer(URL_PRIVATE_BATTLE,
					post, false);
		} catch (Exception ex) {
			ErrorData.currentDataType = ErrorData.DataType.text;
			ErrorData.currentErrorType = ErrorData.ErrorType.ConnectionError;
			ErrorData.text = ex.getLocalizedMessage();
			throw ex;
		}

		//Thread.sleep(Process.getRandom(1000, 2000));

		if (Info.Debug) {
			File outputFile = new File("PRIVATE_BATTLE.xml");
			FileOutputStream outputFileStream = new FileOutputStream(outputFile);
			outputFileStream.write(response);
			outputFileStream.close();
		}

		Document doc;
		try {
			doc = Process.ParseXMLBytes(response);
		} catch (Exception ex) {
			ErrorData.currentDataType = ErrorData.DataType.bytes;
			ErrorData.currentErrorType = ErrorData.ErrorType.PrivateFairyBattleDataError;
			ErrorData.bytes = response;
			throw ex;
		}

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		try {
			if (!xpath.evaluate("/response/header/error/code", doc).equals("0")) {
				ErrorData.currentErrorType = ErrorData.ErrorType.PrivateFairyBattleResponse;
				ErrorData.currentDataType = ErrorData.DataType.text;
				ErrorData.text = xpath.evaluate(
						"/response/header/error/message", doc);
				return false;
			}
			if (Process.info.LatestFairyList.size() > 1000)
				Process.info.LatestFairyList.poll();
			Process.info.LatestFairyList.offer(Process.info.pfairy);

			if ((boolean) xpath.evaluate("count(//private_fairy_top) > 0", doc,
					XPathConstants.BOOLEAN)) {
				FairyBattleResult = PrivateFairyBattleResult.escape;
				return true;
			}
			ParseUserDataInfo.parse(doc);
			ParseCardList.parse(doc);
			if (xpath.evaluate("//battle_result/winner", doc).equals("1"))
				FairyBattleResult = PrivateFairyBattleResult.win;
			else
				FairyBattleResult = PrivateFairyBattleResult.lose;

			String spec = xpath
					.evaluate(
							"//private_fairy_reward_list/special_item/after_count",
							doc);
			if (spec.length() != 0) {
				Process.info.gather = Integer.parseInt(spec);
			} else {
				Process.info.gather = -1;
			}

			if (!Process.info.PrivateFairyList.isEmpty()) {
				Process.AddUrgentTask(Info.EventType.fairyCanBattle);
			}

		} catch (Exception ex) {
			Process.AddUrgentTask(Info.EventType.autoMedicine);
			if (ErrorData.currentErrorType != ErrorData.ErrorType.none)
				throw ex;
			ErrorData.currentDataType = ErrorData.DataType.bytes;
			ErrorData.currentErrorType = ErrorData.ErrorType.PrivateFairyBattleDataParseError;
			ErrorData.bytes = response;
			throw ex;
		}

		return true;

	}
}
