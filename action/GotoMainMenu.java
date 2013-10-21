package action;

import org.apache.http.NameValuePair;
import org.w3c.dom.Document;

import walker.ErrorData;
import walker.Info;
import walker.Process;

import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

public class GotoMainMenu {
	public static final ActionRegistry.Action Name = ActionRegistry.Action.GOTO_MAIN_MENU;

	private static final String URL_MAIN_MENU = "http://web.million-arthurs.com/connect/app/mainmenu?cyt=1";
	private static byte[] response;

	public static int run() throws Exception {
		Document doc;
		try {
			response = walker.Process.network.ConnectToServer(URL_MAIN_MENU,
					new ArrayList<NameValuePair>(), false);
		} catch (Exception ex) {
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

		try {
			doc = Process.ParseXMLBytes(response);
		} catch (Exception ex) {
			ErrorData.currentDataType = ErrorData.DataType.bytes;
			ErrorData.currentErrorType = ErrorData.ErrorType.MainMenuDataError;
			ErrorData.bytes = response;
			throw ex;
		}
		try {
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();

			if (!xpath.evaluate("/response/header/error/code", doc).equals("0")) {
				ErrorData.currentErrorType = ErrorData.ErrorType.FairyHistoryResponse;
				ErrorData.currentDataType = ErrorData.DataType.text;
				ErrorData.text = xpath.evaluate(
						"/response/header/error/message", doc);
				return 0;
			}

			if (GuildDefeat.judge(doc)) {
				return 2;
			}

			ParseUserDataInfo.parse(doc);

		} catch (Exception ex) {
			if (ErrorData.currentErrorType != ErrorData.ErrorType.none)
				throw ex;
			ErrorData.currentDataType = ErrorData.DataType.bytes;
			ErrorData.currentErrorType = ErrorData.ErrorType.GotoMainMenuDataParseError;
			ErrorData.bytes = response;
			throw ex;
		}

		return 1;
	}
}
