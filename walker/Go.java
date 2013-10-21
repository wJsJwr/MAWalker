package walker;

import info.Card;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import walker.Info;
import net.Crypto;

public class Go {

	static String configFile;

	private static final String myCardDatafile = "myCard.xls";

	public static void main(String[] args) {
		if (args.length < 1) {
			printHelp();
			return;
		}
		try {
			GetConfig.parse(Process.ParseXMLBytes(ReadFileAll(args[0])));
			configFile = args[0];
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		if (args.length < 3) {
			System.out.println(Version.printVersion());
			Go.log(String.format("Read cards that can not be sold (%d).",
					Info.CanNotBeSold.size()), true);
		}
		if (args.length == 1) {
			Info.Debug = false;
			Info.Nolog = false;
			Process proc = new Process();
			proc.run();
		} else if (args.length == 2) {
			if (args[1].equals("-debug")) {
				Info.Debug = true;
				Info.Nolog = false;
				Process proc = new Process();
				proc.run();
			} else if (args[1].equals("-nolog")) {
				Info.Debug = false;
				Info.Nolog = true;
				Process proc = new Process();
				proc.run();
			}
		} else if (args.length == 3) {
			try {
				if (args[1].startsWith("-f")) {
					if (args[1].charAt(2) == '1') {
						System.out.println(new String(Crypto
								.DecryptNoKey(ReadFileAll(args[2]))));
					} else if (args[1].charAt(2) == '2') {
						System.out.println(new String(Crypto
								.DecryptWithKey(ReadFileAll(args[2]))));
					}
				} else if (args[1].equals("-t")) {
					// 用作测试使用
					try {
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				} else if (args[1].startsWith("-d")) {
					if (args[1].charAt(2) == '1') {
						System.out.println(new String(Crypto
								.DecryptBase64NoKey2Str(args[2])));
					} else if (args[1].charAt(2) == '2') {
						System.out.println(new String(Crypto
								.DecryptBase64WithKey2Str(args[2])));
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				printHelp();
			}
		} else {
			printHelp();
		}
	}

	public static void printHelp() {
		System.out.println(Version.printVersion());
		System.out
				.println("Usage: config_xml [-h][-f[1|2] file][-d[1|2] str][-m]");
	}

	public static void log(String message, boolean flag) {
		String LogString = "";
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
		if (message == null || message.isEmpty())
			return;
		if (!message.contains("\n")) {
			LogString = df.format(new Date()) + "> " + message;
			Log2File(LogString + "\r\n");
			if (flag)
				System.out.println(LogString);
			return;
		}
		for (String l : message.split("\n")) {
			LogString = df.format(new Date()) + "> " + l;
			Log2File(LogString + "\r\n");
			if (flag)
				System.out.println(LogString);
		}
	}

	private static void Log2File(String content) {
		try {
			if (createFolder()) {
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");// 设置日期格式
				String logfile = MD5(Info.LoginId) + "/" + "log-"
						+ df.format(new Date()) + ".txt";
				FileWriter writer = new FileWriter(logfile, true);
				writer.write(content);
				writer.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static byte[] ReadFileAll(String path) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		InputStream is = null;
		try {
			is = new FileInputStream(path);
			byte[] b = new byte[0x2800];
			int n;
			while ((n = is.read(b)) != -1)
				baos.write(b, 0, n);
		} catch (Exception ex) {
			throw ex;
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (Exception ex) {
				throw ex;
			}
		}
		// System.out.println(baos.toByteArray().length);
		return baos.toByteArray();
	}

	public static void saveSessionId(String sessionid) {
		Document doc = null;
		try {
			doc = Process.ParseXMLBytes(ReadFileAll(configFile));
			Node node = null;
			XPathFactory xpathFactory = XPathFactory.newInstance();
			XPath xpath = xpathFactory.newXPath();
			node = (Node) xpath.evaluate("/config/sessionId", doc,
					XPathConstants.NODE);
			node.setTextContent(sessionid);

			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();

			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(configFile));
			transformer.transform(source, result);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

	public static void saveDeck(int deckNumber, String cardList) {
		Document doc = null;
		try {

			doc = Process.ParseXMLBytes(ReadFileAll(configFile));
			Node node = null;
			XPathFactory xpathFactory = XPathFactory.newInstance();
			XPath xpath = xpathFactory.newXPath();

			node = (Node) xpath.evaluate(String.format(
					"/config/deck/deck_profile[no=%d]/card", deckNumber), doc,
					XPathConstants.NODE);
			node.setTextContent(cardList);

			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();

			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(configFile));
			transformer.transform(source, result);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

	public static void saveXMLFile(byte[] response, String folderName) {
		try {
			if (createFolder(folderName)) {
				SimpleDateFormat df = new SimpleDateFormat(
						"yyyy-MM-dd HH-mm-ss-SSS");// 设置日期格式
				File outputFile = new File(MD5(Info.LoginId) + "/" + folderName
						+ "/" + df.format(new Date()) + ".xml");
				FileOutputStream outputFileStream = new FileOutputStream(
						outputFile);
				outputFileStream.write(response);
				outputFileStream.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

	private static boolean createFolder() {
		try {
			File filePath = new File(MD5(Info.LoginId));
			if (!filePath.exists()) {
				filePath.mkdirs();
			}
			if (Process.info!=null && !Process.info.username.isEmpty()) {
				File fileName = new File(MD5(Info.LoginId) + "/" + Process.info.username);
				if (!fileName.exists()) {
					fileName.createNewFile();
				}
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	private static boolean createFolder(String subFolserName) {
		try {
			File filePath = new File(MD5(Info.LoginId) + "/" + subFolserName);
			if (!filePath.exists()) {
				filePath.mkdirs();
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	public static void saveCardData() {

		if (createFolder()) {
			WritableWorkbook wwb = null;
			try {
				wwb = Workbook.createWorkbook(new File(MD5(Info.LoginId) + "/"
						+ myCardDatafile));
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (wwb != null) {
				WritableSheet ws = wwb.createSheet("My cards", 0);
				for (int i = 0; i < Process.info.cardList.size() + 1; i++) {
					String singleCardData = "";
					if (i == 0) {
						singleCardData = "SerialID,CardID,CnName,JpName,Star,Cost,Holo,Level,Hp,Atk";
					} else {
						Card c = Process.info.cardList.get(i - 1);
						singleCardData = String.format(
								"%s,%s,%s,%s,%d,%d,%b,%d,%d,%d,", c.serialId,
								c.cardId, c.cardNameCn, c.cardNameJp, c.star,
								c.cost, c.holo, c.lv, c.hp, c.atk);
					}
					for (int j = 0; j < singleCardData.split(",").length; j++) {
						Label labelC = new Label(j, i,
								singleCardData.split(",")[j]);
						try {
							ws.addCell(labelC);
						} catch (RowsExceededException e) {
							e.printStackTrace();
						} catch (WriteException e) {
							e.printStackTrace();
						}
					}
				}
				try {
					wwb.write();
					wwb.close();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (WriteException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private final static String MD5(String pwd) {
		// 用于加密的字符
		char md5String[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'A', 'B', 'C', 'D', 'E', 'F' };
		try {
			// 使用平台的默认字符集将此 String 编码为 byte序列，并将结果存储到一个新的 byte数组中
			byte[] btInput = pwd.getBytes();

			// 获得指定摘要算法的 MessageDigest对象，此处为MD5
			// MessageDigest类为应用程序提供信息摘要算法的功能，如 MD5 或 SHA 算法。
			// 信息摘要是安全的单向哈希函数，它接收任意大小的数据，并输出固定长度的哈希值。
			MessageDigest mdInst = MessageDigest.getInstance("MD5");
			// System.out.println(mdInst);
			// MD5 Message Digest from SUN, <initialized>

			// MessageDigest对象通过使用 update方法处理数据， 使用指定的byte数组更新摘要
			mdInst.update(btInput);
			// System.out.println(mdInst);
			// MD5 Message Digest from SUN, <in progress>

			// 摘要更新之后，通过调用digest（）执行哈希计算，获得密文
			byte[] md = mdInst.digest();
			// System.out.println(md);

			// 把密文转换成十六进制的字符串形式
			int j = md.length;
			// System.out.println(j);
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) { // i = 0
				byte byte0 = md[i]; // 95
				str[k++] = md5String[byte0 >>> 4 & 0xf]; // 5
				str[k++] = md5String[byte0 & 0xf]; // F
			}

			// 返回经过加密后的字符串
			return new String(str);

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
