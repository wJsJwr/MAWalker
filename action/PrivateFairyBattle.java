package action;

import info.FairyBattleInfo;

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
	
	public static boolean run() throws Exception {
		ArrayList<NameValuePair> post = new ArrayList<NameValuePair>();
		post.add(new BasicNameValuePair("no", Process.info.fairy.No));
		post.add(new BasicNameValuePair("serial_id", Process.info.fairy.SerialId));
		post.add(new BasicNameValuePair("user_id", Process.info.fairy.UserId));
		try {
			response = Process.network.ConnectToServer(URL_PRIVATE_BATTLE, post, false);
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
				ErrorData.text = xpath.evaluate("/response/header/error/message", doc);
				return false;
			}
			if (Process.info.LatestFairyList.size() > 1000) Process.info.LatestFairyList.poll();
			Process.info.LatestFairyList.offer(Process.info.fairy);
			
			if ((boolean)xpath.evaluate("count(//private_fairy_top) > 0", doc, XPathConstants.BOOLEAN)) {
				Process.info.events.push(Info.EventType.fairyBattleEnd);
				return true;
			}
			ParseUserDataInfo.parse(doc);
			ParseCardList.parse(doc);
			if (xpath.evaluate("//battle_result/winner", doc).equals("1")) {
				Process.info.events.push(Info.EventType.fairyBattleWin);
			} else {
				Process.info.events.push(Info.EventType.fairyBattleLose);
			}
			
			//Process.info.fairy.FairyName = xpath.evaluate("//battle_vs_info/player[last()]/name", doc);
			Process.info.SetTimeoutByAction(Name);
			
			String spec = xpath.evaluate("//private_fairy_reward_list/special_item/after_count", doc);
			if (spec.length() != 0) {
				Process.info.gather = Integer.parseInt(spec);
			} else {
				Process.info.gather = -1;
			}
			
			// 检查觉醒
			if ((boolean)xpath.evaluate("count(//ex_fairy/rare_fairy)>0", doc, XPathConstants.BOOLEAN)) {
				// Yes
				Process.info.fairy.Type = FairyBattleInfo.PRIVATE | FairyBattleInfo.SELF | FairyBattleInfo.RARE;
				Process.info.fairy.FairyLevel = xpath.evaluate("//ex_fairy/rare_fairy/lv", doc);
				Process.info.fairy.SerialId = xpath.evaluate("//ex_fairy/rare_fairy/serial_id", doc);
				Process.info.fairy.UserId = xpath.evaluate("//ex_fairy/rare_fairy/discoverer_id", doc);
				Process.info.fairy.fairyCurrHp = Integer.parseInt(xpath.evaluate("//ex_fairy/rare_fairy/hp", doc));
				Process.info.fairy.fairyMaxHp = Integer.parseInt(xpath.evaluate("//ex_fairy/rare_fairy/hp_max", doc));
				Process.info.events.push(Info.EventType.fairyTransform);
			}
			
			
		} catch (Exception ex) {
			if (ErrorData.currentErrorType != ErrorData.ErrorType.none) throw ex;
			ErrorData.currentDataType = ErrorData.DataType.bytes;
			ErrorData.currentErrorType = ErrorData.ErrorType.PrivateFairyBattleDataParseError;
			ErrorData.bytes = response;
			throw ex;
		}
		
		return true;
		
	}
}
