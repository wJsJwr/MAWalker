package action;

import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.http.NameValuePair;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.w3c.dom.Document;

import walker.ErrorData;
import walker.Info;
import walker.Process;

public class CookieLogin {
	public static final ActionRegistry.Action Name = ActionRegistry.Action.LOGIN;
	// URLs
	private static final String URL_CHECK_INSPECTION = "http://web.million-arthurs.com/connect/app/check_inspection?cyt=1";
	private static final String URL_MAINMENU = "http://web.million-arthurs.com/connect/app/mainmenu?cyt=1";
	// error type
	public static final String ERR_CHECK_INSPECTION = "CoolieLogin/check_inspection";
	public static final String ERR_MAINMENU = "CookieLogin/mainmenu";

	private static byte[] result;

	public static int run() throws Exception {
		try {
			return run(false);
		} catch (Exception ex) {
			throw ex;
		}
	}

	public static int run(boolean jump) throws Exception {
		Document doc;
		if (!jump) {
			try {
				if (!Info.sessionId.isEmpty()) {
					BasicClientCookie c = new BasicClientCookie("S",
							Info.sessionId);
					c.setDomain("web.million-arthurs.com");
					c.setPath("/");
					Process.network.cookie.addCookie(c);
				}
				result = Process.network.ConnectToServer(URL_CHECK_INSPECTION,
						new ArrayList<NameValuePair>(), true);
			} catch (Exception ex) {
				ErrorData.currentDataType = ErrorData.DataType.text;
				ErrorData.currentErrorType = ErrorData.ErrorType.ConnectionError;
				ErrorData.text = ERR_CHECK_INSPECTION;
				throw ex;
			}
		}

		try {
			result = Process.network.ConnectToServer(URL_MAINMENU,
					new ArrayList<NameValuePair>(), true);
		} catch (Exception ex) {
			ErrorData.currentDataType = ErrorData.DataType.text;
			ErrorData.currentErrorType = ErrorData.ErrorType.ConnectionError;
			ErrorData.text = ex.getMessage();
			ex.printStackTrace();
			throw ex;
		}
		try {
			doc = Process.ParseXMLBytes(result); // 通过分析匿名类获得当前类名
		} catch (Exception ex) {
			ErrorData.currentDataType = ErrorData.DataType.text;
			ErrorData.currentErrorType = ErrorData.ErrorType.LoginDataError;
			ErrorData.text = ERR_MAINMENU;
			throw ex;
		}
		try {
			return parse(doc);
		} catch (Exception ex) {
			throw ex;
		}
	}

	private static int parse(Document doc) throws Exception {
		try {
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			if (!xpath.evaluate("/response/header/error/code", doc).equals("0")) {
				ErrorData.currentErrorType = ErrorData.ErrorType.LoginResponse;
				ErrorData.currentDataType = ErrorData.DataType.text;
				ErrorData.text = xpath.evaluate(
						"/response/header/error/message", doc);
				return 0;
			}

			if (GuildDefeat.judge(doc)) {
				return 2;
			}

			if (!xpath.evaluate("//fairy_appearance", doc).equals("0")) {
				Process.AddUrgentTask(Info.EventType.getFairyList);
			}

			Process.info.userId = xpath.evaluate("//login/user_id", doc);
			ParseUserDataInfo.parse(doc);
			// ParseCardList.parse(doc);

			Process.info.cardMax = Integer.parseInt(xpath.evaluate(
					"//your_data/max_card_num", doc));

		} catch (Exception ex) {
			if (ErrorData.currentErrorType != ErrorData.ErrorType.none)
				throw ex;
			ErrorData.currentDataType = ErrorData.DataType.bytes;
			ErrorData.currentErrorType = ErrorData.ErrorType.LoginDataParseError;
			ErrorData.bytes = result;
			throw ex;
		}
		return 1;
	}
}