package action;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.http.NameValuePair;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import walker.ErrorData;
import walker.Info;
import walker.Process;
import action.ActionRegistry.Action;

public class RewardBox {
	public static final Action Name = Action.REWARD_BOX;

	private static final String URL_REWARD_BOX = "http://web.million-arthurs.com/connect/app/menu/rewardbox?cyt=1";
	private static byte[] response;

	public static boolean run() throws Exception {
		ArrayList<NameValuePair> post = new ArrayList<NameValuePair>();
		try {
			response = Process.network.ConnectToServer(URL_REWARD_BOX, post,
					false);
		} catch (Exception ex) {
			ErrorData.currentDataType = ErrorData.DataType.text;
			ErrorData.currentErrorType = ErrorData.ErrorType.ConnectionError;
			ErrorData.text = ex.getLocalizedMessage();
			throw ex;
		}

		// Thread.sleep(Process.getRandom(1000, 2000));

		if (Info.Debug) {
			File outputFile = new File("REWARD_BOX.xml");
			FileOutputStream outputFileStream = new FileOutputStream(outputFile);
			outputFileStream.write(response);
			outputFileStream.close();
		}

		Document doc;
		try {
			doc = Process.ParseXMLBytes(response);
		} catch (Exception ex) {
			ErrorData.currentDataType = ErrorData.DataType.bytes;
			ErrorData.currentErrorType = ErrorData.ErrorType.RewardBoxDataError;
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
			if (!xpath.evaluate("/response/header/error/code", doc).equals("0")) {
				ErrorData.currentErrorType = ErrorData.ErrorType.RewardBoxResponse;
				ErrorData.currentDataType = ErrorData.DataType.text;
				ErrorData.text = xpath.evaluate(
						"/response/header/error/message", doc);
				return false;
			}

			NodeList rewardbox_list = (NodeList) xpath.evaluate(
					"//rewardbox_list/rewardbox", doc, XPathConstants.NODESET);

			if (rewardbox_list.getLength() < 10)// 少于10个reward box没必要收
				return true;
			String rewardbox_result = String.format("Find %d reward box(es).",
					rewardbox_list.getLength());
			walker.Go.log(rewardbox_result, !Info.Nolog);

			Process.info.rewardBoxList = "";
			for (int i = 0; i < rewardbox_list.getLength(); i++) {
				Node f = rewardbox_list.item(i).getFirstChild();
				do {
					if (f.getNodeName().equals("id")) {
						if (Process.info.rewardBoxList.isEmpty()) {
							Process.info.rewardBoxList = f.getFirstChild()
									.getNodeValue();
						} else {
							Process.info.rewardBoxList += ","
									+ f.getFirstChild().getNodeValue();
						}
					}
					f = f.getNextSibling();
				} while (f != null);
			}
			if (!Process.info.rewardBoxList.isEmpty()) {
				Process.AddUrgentTask(Info.EventType.getRewards);
			}
			return true;

		} catch (Exception ex) {
			if (ErrorData.currentErrorType != ErrorData.ErrorType.none)
				throw ex;
			ErrorData.currentDataType = ErrorData.DataType.bytes;
			ErrorData.currentErrorType = ErrorData.ErrorType.RewardBoxDataParseError;
			ErrorData.bytes = response;
			throw ex;
		}
	}
}