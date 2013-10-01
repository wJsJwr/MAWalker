package action;

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

public class RecFairyDianzan {
	private static byte[] response;
	private static final String URL_PRIVATE_BATTLE_TOP = "http://web.million-arthurs.com/connect/app/private_fairy/private_fairy_top?cyt=1";

	public static boolean run() throws Exception {

		ArrayList<NameValuePair> post = new ArrayList<NameValuePair>();
		post.add(new BasicNameValuePair("serial_id",
				Process.info.pfairy.SerialId));
		post.add(new BasicNameValuePair("user_id", Process.info.pfairy.UserId));

		try {
			response = Process.network.ConnectToServer(URL_PRIVATE_BATTLE_TOP,
					post, false);
		} catch (Exception ex) {
			ErrorData.currentDataType = ErrorData.DataType.text;
			ErrorData.currentErrorType = ErrorData.ErrorType.ConnectionError;
			ErrorData.text = ex.getLocalizedMessage();
			throw ex;
		}

		Thread.sleep(Process.getRandom(2000, 3000));

		if (Info.Debug) {
			File outputFile = new File("PRIVATE_BATTLE_TOP.xml");
			FileOutputStream outputFileStream = new FileOutputStream(outputFile);
			outputFileStream.write(response);
			outputFileStream.close();
		}

		Document doc;
		try {
			doc = Process.ParseXMLBytes(response);
		} catch (Exception ex) {
			ErrorData.currentDataType = ErrorData.DataType.bytes;
			ErrorData.currentErrorType = ErrorData.ErrorType.RecFairyDianzanDataError;
			ErrorData.bytes = response;
			throw ex;
		}

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		try {
			// 检测是否有错误
			if (!xpath.evaluate("/response/header/error/code", doc).equals("0")) {
				ErrorData.currentErrorType = ErrorData.ErrorType.RecFairyDianzanResponse;
				ErrorData.currentDataType = ErrorData.DataType.text;
				ErrorData.text = xpath.evaluate(
						"/response/header/error/message", doc);
				return false;
			}

			// 检测是否有点赞
			if ((boolean) xpath
					.evaluate(
							"count(/response/body/private_fairy_top/recover_by_like) > 0",
							doc, XPathConstants.BOOLEAN)) {
				String bc_result, bc_message;
				int bc_add, bc_before, bc_after = 0;
				bc_message = xpath
						.evaluate(
								"/response/body/private_fairy_top/recover_by_like/message",
								doc);
				bc_add = Integer
						.parseInt(xpath
								.evaluate(
										"/response/body/private_fairy_top/recover_by_like/recover_point",
										doc));
				bc_before = Integer
						.parseInt(xpath
								.evaluate(
										"/response/body/private_fairy_top/recover_by_like/before_point",
										doc));
				bc_after = bc_before + bc_add;
				if (bc_after > Process.info.bcMax)
					bc_after = Process.info.bcMax;
				bc_result = String
						.format("Receive Dianzan Message: %s, BC recovery: %d, BC before: %d, BC after: %d.",
								bc_message, bc_add, bc_before, bc_after);
				walker.Go.log(bc_result);
			}
		} catch (Exception ex) {
			if (ErrorData.currentErrorType != ErrorData.ErrorType.none)
				throw ex;
			ErrorData.currentDataType = ErrorData.DataType.bytes;
			ErrorData.currentErrorType = ErrorData.ErrorType.RecFairyDianzanDataParseError;
			ErrorData.bytes = response;
			throw ex;
		}

		return true;
	}
}
