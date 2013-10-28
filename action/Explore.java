package action;

import info.FairyBattleInfo;
import info.Floor;

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

public class Explore {
	public static final Action Name = Action.EXPLORE;
	
	private static final String URL_EXPLORE = "http://web.million-arthurs.com/connect/app/exploration/guild_explore?cyt=1";
	private static byte[] response;
	
	public static boolean run() throws Exception {
		ArrayList<NameValuePair> post = new ArrayList<NameValuePair>();
		post.add(new BasicNameValuePair("area_id", Process.info.front.areaId));
		post.add(new BasicNameValuePair("auto_build", "1"));
		post.add(new BasicNameValuePair("floor_id", Process.info.front.floorId));
		try {
			response = Process.network.ConnectToServer(URL_EXPLORE, post, false);
		} catch (Exception ex) {
			if (ex.getMessage().startsWith("302")) {
				Process.info.events.push(Info.EventType.innerMapJump);
				return false;
			}
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
			ErrorData.currentErrorType = ErrorData.ErrorType.ExploreDataError;
			ErrorData.bytes = response;
			throw ex;
		}
		
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		
		try {
			String code = xpath.evaluate("/response/header/error/code", doc);
			if (!code.equals("0")) {
				if (code.equals("8000")) {
					Process.info.events.push(Info.EventType.cardFull);
				}
				ErrorData.currentErrorType = ErrorData.ErrorType.ExploreResponse;
				ErrorData.currentDataType = ErrorData.DataType.text;
				ErrorData.text = xpath.evaluate("/response/header/error/message", doc);
				return false;
			}
			
			Process.info.username = xpath.evaluate("//your_data/name", doc);
			Process.info.lv = Integer.parseInt(xpath.evaluate("//town_level", doc));
			Process.info.ap = Integer.parseInt(xpath.evaluate("//ap/current", doc));
			Process.info.apMax = Integer.parseInt(xpath.evaluate("//ap/max", doc));
			Process.info.bc = Integer.parseInt(xpath.evaluate("//bc/current", doc));
			Process.info.bcMax = Integer.parseInt(xpath.evaluate("//bc/max", doc));
			Process.info.guildId = xpath.evaluate("//your_data/party_id", doc);
			Process.info.money = Long.parseLong(xpath.evaluate("//your_data/gold", doc));
			
			Process.info.SetTimeoutByAction(Name);
			
			// TODO: 添加升级事件
			Process.info.exp = Integer.parseInt(xpath.evaluate("//explore/next_exp", doc));
			
			Process.info.ExploreProgress = xpath.evaluate("//explore/progress", doc);
			Process.info.ExploreGold = xpath.evaluate("//explore/gold", doc);
			Process.info.ExploreExp = xpath.evaluate("//explore/get_exp", doc);
			
			int evt = Integer.parseInt(xpath.evaluate("//explore/event_type", doc));
			switch (evt) {
			case 22:
				// fairy battle
				Process.info.fairy = new FairyBattleInfo();
				Process.info.fairy.Type = FairyBattleInfo.PRIVATE | FairyBattleInfo.SELF;
				Process.info.fairy.FairyName = xpath.evaluate("//ex_fairy/fairy/name", doc);
				Process.info.fairy.FairyLevel = xpath.evaluate("//ex_fairy/fairy/lv", doc);
				Process.info.fairy.SerialId = xpath.evaluate("//ex_fairy/fairy/serial_id", doc);
				Process.info.fairy.UserId = xpath.evaluate("//ex_fairy/fairy/discoverer_id", doc);
				Process.info.fairy.fairyCurrHp = Integer.parseInt(xpath.evaluate("//ex_fairy/fairy/hp",doc));
				Process.info.fairy.fairyMaxHp = Integer.parseInt(xpath.evaluate("//ex_fairy/fairy/hp_max",doc));
				
				Process.info.events.push(Info.EventType.privateFairyAppear);
				Process.info.events.push(Info.EventType.recvPFBGood);
				Process.info.ExploreResult = "Fairy Appear";
				break;
			case 5:
				// floor or area clear
				if ((boolean)xpath.evaluate("count(//next_floor)>0", doc, XPathConstants.BOOLEAN)) {
					// floor clear
					Floor f = new Floor();
					f.areaId = xpath.evaluate("//next_floor/area_id", doc);
					f.floorId = xpath.evaluate("//next_floor/floor_info/id", doc);
					f.cost = Integer.parseInt(xpath.evaluate("//next_floor/floor_info/cost", doc));
					Process.info.front = f;
					Process.info.floor.put(f.cost, f);
					Process.info.ExploreResult = "Floor Clear";
				} else {
					Process.info.events.push(Info.EventType.areaComplete);
					Process.info.ExploreResult = "Area Clear";
				}
				break;
			case 12:
				// AP
				Process.info.ExploreResult = String.format("AP recover(%d)", 
						Integer.parseInt(xpath.evaluate("//explore/recover", doc)));
				break;
			case 13:
				// BC
				Process.info.ExploreResult = String.format("BC recover(%d)", 
						Integer.parseInt(xpath.evaluate("//explore/recover", doc)));
				break;
			case 19:
				int delta = Integer.parseInt(xpath.evaluate("//special_item/after_count", doc)) - 
							Integer.parseInt(xpath.evaluate("//special_item/before_count", doc));
				Process.info.ExploreResult = String.format("Gather(%d)", delta);
				break;
			case 2:
				Process.info.ExploreResult = "Meet People";
				break;
			case 3:
				Process.info.ExploreResult = "Get Card";
				break;
			case 0:
				Process.info.ExploreResult = "Nothing";
				break;
			default:
				Process.info.ExploreResult = String.format("Code: %d", evt);
				break;
			}
			
		} catch (Exception ex) {
			if (ErrorData.currentErrorType != ErrorData.ErrorType.none) throw ex;
			ErrorData.currentDataType = ErrorData.DataType.bytes;
			ErrorData.currentErrorType = ErrorData.ErrorType.ExploreDataParseError;
			ErrorData.bytes = response;
			throw ex;
		}
		return true;
	}
	
}
