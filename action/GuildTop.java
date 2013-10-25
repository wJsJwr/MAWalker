package action;

import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.http.NameValuePair;
import org.w3c.dom.Document;

import walker.ErrorData;
import walker.Info;
import walker.Process;
import action.ActionRegistry.Action;

public class GuildTop {
	public static final Action Name = Action.GUILD_TOP;
	private static final String URL_GUILD_TOP = "http://web.million-arthurs.com/connect/app/guild/guild_top?cyt=1";
	
	private static byte[] response;
	
	public static boolean run() throws Exception {
		ArrayList<NameValuePair> post = new ArrayList<NameValuePair>();
		try {
			response = Process.network.ConnectToServer(URL_GUILD_TOP, post, false);
		} catch (Exception ex) {
			//if (ex.getMessage().equals("302")) 
			// 上面的是为了截获里图跳转
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
			ErrorData.currentErrorType = ErrorData.ErrorType.GuildTopDataError;
			ErrorData.bytes = response;
			throw ex;
		}
		
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		
		
		try {
			if (!xpath.evaluate("/response/header/error/code", doc).equals("0")) {
				ErrorData.currentErrorType = ErrorData.ErrorType.GuildTopResponse;
				ErrorData.currentDataType = ErrorData.DataType.text;
				ErrorData.text = xpath.evaluate("/response/header/error/message", doc);
				return false;
			}

			if (GuildDefeat.judge(doc)) {
				Process.info.events.push(Info.EventType.guildTopRetry);
				return false;
			}
			
			if ((boolean)xpath.evaluate("count(//guild_top_no_fairy)>0", doc, XPathConstants.BOOLEAN)) {
				// 深夜没有外敌战
				Process.info.NoFairy = true;
				return false;
			} else {
				Process.info.NoFairy = false;
			}
			
			Process.info.gfairy.FairyName = xpath.evaluate("//fairy/name", doc);
			Process.info.gfairy.SerialId = xpath.evaluate("//fairy/serial_id", doc);
			Process.info.gfairy.GuildId = xpath.evaluate("//fairy/discoverer_id", doc);
			Process.info.gfairy.FairyLevel = xpath.evaluate("//fairy/lv", doc);
			Process.info.gfairy.fairyCurrHp = Integer.parseInt(xpath.evaluate("//fairy/hp", doc));
			Process.info.gfairy.fairyMaxHp = Integer.parseInt(xpath.evaluate("//fairy/hp_max", doc));
			
			Process.info.events.push(Info.EventType.guildBattle);
			
			return true;
		} catch (Exception ex) {
			if (ErrorData.currentErrorType != ErrorData.ErrorType.none) throw ex;
			ErrorData.currentDataType = ErrorData.DataType.bytes;
			ErrorData.currentErrorType = ErrorData.ErrorType.GuildTopDataParseError;
			ErrorData.bytes = response;
			throw ex;
		}
		
	}
	
}
