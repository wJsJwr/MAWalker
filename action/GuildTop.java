package action;

import info.GuildFairyBattleForce;

import java.io.File;
import java.io.FileOutputStream;
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
			response = Process.network.ConnectToServer(URL_GUILD_TOP, post,
					false);
		} catch (Exception ex) {
			// if (ex.getMessage().equals("302"))
			// 上面的是为了截获里图跳转
			ErrorData.currentDataType = ErrorData.DataType.text;
			ErrorData.currentErrorType = ErrorData.ErrorType.ConnectionError;
			ErrorData.text = ex.getMessage();
			throw ex;
		}

		if (Info.Debug) {
			File outputFile = new File("GUILD_TOP.xml");
			FileOutputStream outputFileStream = new FileOutputStream(outputFile);
			outputFileStream.write(response);
			outputFileStream.close();
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
				if (xpath.evaluate("/response/header/error/code", doc).equals(
						"9000")) {
					Process.AddUrgentTask(Info.EventType.cookieOutOfDate);
					ErrorData.currentErrorType = ErrorData.ErrorType.CookieOutOfDate;
				} else {
					ErrorData.currentErrorType = ErrorData.ErrorType.GuildTopResponse;
				}
				ErrorData.currentDataType = ErrorData.DataType.text;
				ErrorData.text = xpath.evaluate(
						"/response/header/error/message", doc);
				return false;
			}

			if (GuildDefeat.judge(doc)) {
				Process.AddUrgentTask(Info.EventType.guildTopRetry);
				Process.info.gfbforce = new GuildFairyBattleForce();
				return false;
			}

			if ((Boolean) xpath.evaluate("count(//guild_top_no_fairy)>0", doc,
					XPathConstants.BOOLEAN)) {
				// 深夜没有外敌战
				Process.info.NoFairy = true;
				return false;
			} else {
				Process.info.NoFairy = false;
			}

			Process.info.gfairy.FairyName = xpath.evaluate("//fairy/name", doc);
			Process.info.gfairy.SerialId = xpath.evaluate("//fairy/serial_id",
					doc);
			Process.info.gfairy.GuildId = xpath.evaluate(
					"//fairy/discoverer_id", doc);
			Process.info.gfairy.FairyLevel = Integer.parseInt(xpath.evaluate(
					"//fairy/lv", doc));
			if ((boolean) xpath.evaluate("count(//force_gauge)>0", doc,
					XPathConstants.BOOLEAN)) {// 第一次遇怪没有这些信息，需要先打一下
				Process.info.gfbforce.total = Long.parseLong(xpath.evaluate(
						"//force_gauge/total", doc));
				Process.info.gfbforce.own = Long.parseLong(xpath.evaluate(
						"//force_gauge/own", doc));
				Process.info.gfbforce.rival = Long.parseLong(xpath.evaluate(
						"//force_gauge/rival", doc));
				Process.info.gfbforce.chain_counter = Integer.parseInt(xpath
						.evaluate("//chain_counter", doc));
				Process.info.gfbforce.attack_compensation = Double
						.parseDouble(xpath.evaluate("//attack_compensation",
								doc));
				Process.info.gfbforce.ownscale = Process.info.gfbforce.own
						* 100 / Process.info.gfbforce.total;
				Process.info.gfbforce.rivalscale = Process.info.gfbforce.rival
						* 100 / Process.info.gfbforce.total;
				if (Process.info.ticket > 0) {
					if (Process.info.gfbforce.ownscale < 100 * Info.battlewinscale
							&& Process.info.gfbforce.rivalscale < 100 * Info.battlewinscale) {
						Process.AddUrgentTask(Info.EventType.guildBattle);
					} else if (Process.info.ticket >= Info.ticket_max) {
						Process.AddUrgentTask(Info.EventType.guildBattle);
					} else {
						while (Process.info.events
								.contains(Info.EventType.guildBattle))
							Process.info.events
									.remove(Info.EventType.guildBattle);
					}
				}
				return true;
			} else {
				if (Info.Nolog == false)
					walker.Go.log("Find a new Guild Fairy!");
				// Process.AddUrgentTask(Info.EventType.guildBattle);
				return false;
			}
		} catch (Exception ex) {
			if (ErrorData.currentErrorType != ErrorData.ErrorType.none)
				throw ex;
			ErrorData.currentDataType = ErrorData.DataType.bytes;
			ErrorData.currentErrorType = ErrorData.ErrorType.GuildTopDataParseError;
			ErrorData.bytes = response;
			throw ex;
		}

	}

}
