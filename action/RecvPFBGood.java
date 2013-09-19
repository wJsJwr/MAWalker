package action;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import walker.ErrorData;
import walker.Process;
import action.ActionRegistry.Action;

public class RecvPFBGood {
	public static final Action Name = Action.RECV_PFB_GOOD;

	private static final String URL_FAIRY_HISTORY = "http://web.million-arthurs.com/connect/app/private_fairy/private_fairy_history?cyt=1";
	public static boolean run() throws Exception {
			try {
				ArrayList<NameValuePair> al = new ArrayList<NameValuePair>();
				al.add(new BasicNameValuePair("serial_id", Process.info.fairy.SerialId));
				al.add(new BasicNameValuePair("user_id", Process.info.fairy.UserId));
				Process.network.ConnectToServer(URL_FAIRY_HISTORY,
						al, false);
				ErrorData.currentErrorType = ErrorData.ErrorType.FairyHistoryResponse;
				ErrorData.currentDataType = ErrorData.DataType.text;
				ErrorData.text = "收赞成功...";
			} catch (Exception ex) {
				ErrorData.currentDataType = ErrorData.DataType.text;
				ErrorData.currentErrorType = ErrorData.ErrorType.ConnectionError;
				ErrorData.text = ex.getMessage();
				throw ex;
			}
		return true;
	}
}
