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

	public static int run() throws Exception {
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
				return 0;
			}

			if (!(Boolean) xpath.evaluate("count(/response/body/guild_top)>0",
					doc, XPathConstants.BOOLEAN)) {
				if ((Boolean) xpath.evaluate("count(//guild_top_no_fairy)>0",
						doc, XPathConstants.BOOLEAN)) {
					Process.info.NoFairy = true;// 深夜没有外敌战
					return 0;
				} else {
					Process.info.NoFairy = false;
					return 3;// 需要重新获取
				}
			}

			if (GuildDefeat.judge(doc)) {
				return 3;// 需要重新获取
			}

			Process.info.gfairy.FairyName = xpath.evaluate("//fairy/name", doc);
			Process.info.gfairy.SerialId = xpath.evaluate("//fairy/serial_id",
					doc);
			Process.info.gfairy.GuildId = xpath.evaluate(
					"//fairy/discoverer_id", doc);
			Process.info.gfairy.FairyLevel = Integer.parseInt(xpath.evaluate(
					"//fairy/lv", doc));
			Process.info.gfbforce.chain_counter = Integer.parseInt(xpath
					.evaluate("//chain_counter", doc));
			Process.info.gfbforce.attack_compensation = Double
					.parseDouble(xpath.evaluate("//attack_compensation", doc));

			if (Info.OnlyBcBuff && Process.info.ticket < Info.ticket_max) {
				if ((boolean) xpath.evaluate("count(//spp_skill_effect)>0",
						doc, XPathConstants.BOOLEAN)) {
					String tmp = xpath.evaluate("//spp_skill_effect", doc);
					walker.Go.log(String.format("Guild Fairy Buff: %s.", tmp),
							!Info.Nolog);
					if (tmp.indexOf("BC") == -1)
						return 0;
				} else {
					walker.Go.log("Guild Fairy Buff: None.", !Info.Nolog);
					return 0;
				}
			}

			if ((boolean) xpath.evaluate("count(//force_gauge)>0", doc,
					XPathConstants.BOOLEAN)) {
				Process.info.gfbforce.total = Long.parseLong(xpath.evaluate(
						"//force_gauge/total", doc));
				Process.info.gfbforce.own = Long.parseLong(xpath.evaluate(
						"//force_gauge/own", doc));
				Process.info.gfbforce.rival = Long.parseLong(xpath.evaluate(
						"//force_gauge/rival", doc));
				Process.info.gfbforce.ownscale = Process.info.gfbforce.own
						* 100 / Process.info.gfbforce.total;
				Process.info.gfbforce.rivalscale = Process.info.gfbforce.rival
						* 100 / Process.info.gfbforce.total;

				if (Process.info.ticket == 0)
					return 1;// 没票不打

				if (Process.info.ticket >= Info.ticket_max)
					return 2;// 挑战书太多要打

				if (Process.info.gfbforce.ownscale > 100 * Info.battle_win_scale
						|| Process.info.gfbforce.rivalscale > 100 * Info.battle_win_scale)
					return 1;// 已经分出胜负不打

				return 2;// 其他情况都要打
			} else {
				return 0;
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
