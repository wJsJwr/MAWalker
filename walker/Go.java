package walker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import walker.Info;
import net.Crypto;

public class Go {

	static String configFile;

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
			Go.log(Version.printVersion(),true);
			Go.log(String.format("Read cards that can be sold (%d).",
					Info.CanBeSold.size()), true);
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
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");// 设置日期格式
			String logfile = "log-" + df.format(new Date()) + ".txt";
			FileWriter writer = new FileWriter(logfile, true);
			writer.write(content);
			;
			writer.close();
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

}
