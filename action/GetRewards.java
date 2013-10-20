package action;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;

import walker.ErrorData;
import walker.Info;
import walker.Process;
import action.ActionRegistry.Action;

public class GetRewards {
	public static final Action Name = Action.GET_REWARDS;

	private static final String URL_GET_REWARDS = "http://web.million-arthurs.com/connect/app/menu/get_rewards?cyt=1";
	private static byte[] response;

	public static boolean run() throws Exception {
		ArrayList<NameValuePair> post = new ArrayList<NameValuePair>();
		post.add(new BasicNameValuePair("notice_id", Process.info.rewardBoxList));
		Process.info.rewardBoxList = "";
		try {
			response = Process.network.ConnectToServer(URL_GET_REWARDS, post,
					false);
		} catch (Exception ex) {
			ErrorData.currentDataType = ErrorData.DataType.text;
			ErrorData.currentErrorType = ErrorData.ErrorType.ConnectionError;
			ErrorData.text = ex.getLocalizedMessage();
			throw ex;
		}

		// Thread.sleep(Process.getRandom(1000, 2000));

		if (Info.Debug) {
			File outputFile = new File("GET_REWARDS.xml");
			FileOutputStream outputFileStream = new FileOutputStream(outputFile);
			outputFileStream.write(response);
			outputFileStream.close();
		}

		Document doc;
		try {
			doc = Process.ParseXMLBytes(response);
		} catch (Exception ex) {
			ErrorData.currentDataType = ErrorData.DataType.bytes;
			ErrorData.currentErrorType = ErrorData.ErrorType.GetRewardsDataError;
			ErrorData.bytes = response;
			throw ex;
		}

		try {
			return parse(doc);
		} catch (Exception ex) {
			throw ex;
		}
	}

	private static boolean parse(Document doc) throws Exception {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		try {
			if (!(xpath.evaluate("/response/header/error/code", doc).equals(
					"1000") || xpath.evaluate("/response/header/error/code",
					doc).equals("1010"))) {
				ErrorData.currentErrorType = ErrorData.ErrorType.GetRewardsResponse;
				ErrorData.currentDataType = ErrorData.DataType.text;
				ErrorData.text = xpath.evaluate(
						"/response/header/error/message", doc);
				return false;
			} else {
				ErrorData.text = xpath.evaluate(
						"/response/header/error/message", doc);
				ParseUserDataInfo.parse(doc);
				ParseCardList.parse(doc);
				return true;
			}

		} catch (Exception ex) {
			if (ErrorData.currentErrorType != ErrorData.ErrorType.none)
				throw ex;
			ErrorData.currentDataType = ErrorData.DataType.bytes;
			ErrorData.currentErrorType = ErrorData.ErrorType.GetRewardsDataParseError;
			ErrorData.bytes = response;
			throw ex;
		}
	}
}