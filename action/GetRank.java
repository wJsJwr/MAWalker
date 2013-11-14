package action;

import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;

import walker.ErrorData;
import walker.Process;

public class GetRank {
	private static final String URL_RANKING_NEXT = "http://web.million-arthurs.com/connect/app/ranking/ranking_next?cyt=1";
	private static byte[] response;

	public static int gatherrank(int iUser) throws Exception {
		Document doc;
		try {
			ArrayList<NameValuePair> al = new ArrayList<NameValuePair>();
			al.add(new BasicNameValuePair("from", String.valueOf(iUser)));
			al.add(new BasicNameValuePair("ranktype_id", "5"));

			response = Process.network.ConnectToServer(URL_RANKING_NEXT, al,
					false);
		} catch (Exception ex) {
			ErrorData.currentDataType = ErrorData.DataType.text;
			ErrorData.currentErrorType = ErrorData.ErrorType.ConnectionError;
			ErrorData.text = ex.getMessage();
			throw ex;
		}

		try {
			doc = Process.ParseXMLBytes(response);
		} catch (Exception ex) {
			ErrorData.currentDataType = ErrorData.DataType.bytes;
			ErrorData.currentErrorType = ErrorData.ErrorType.PFB_GoodDataError;
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

			return Integer.parseInt(xpath.evaluate("//user_list/user[1]/rank",
					doc)) - 1;

		} catch (Exception ex) {
			if (ErrorData.currentErrorType == ErrorData.ErrorType.none) {
				throw ex;
			}
		}

		return 0;
	}

}
