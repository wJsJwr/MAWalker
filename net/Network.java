package net;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

public class Network {
	private static final String Auth = "eWa25vrE";
	private static final String Key = "2DbcAh3G";

	public static String UserAgent = "";
	private CloseableHttpClient client;
	public CookieStore myCookie;
	HttpClientContext context;
	public static String myProxy = "";
	public static int myProxyPort = 8888;

	public Network() {
		// Use custom cookie store if necessary.
		myCookie = new BasicCookieStore();
		// Request configuration can be overridden at the request level.
		// They will take precedence over the one set at the client level.
		RequestConfig requestConfig;
		if (!myProxy.isEmpty()) {
			requestConfig = RequestConfig.custom().setSocketTimeout(0x7530)
					.setConnectTimeout(0x7530)
					// .setConnectionRequestTimeout(0x7530)
					.setProxy(new HttpHost(myProxy, myProxyPort)).build();
		} else {
			requestConfig = RequestConfig.custom().setSocketTimeout(0x7530)
					.setConnectTimeout(0x7530)
					// .setConnectionRequestTimeout(0x7530)
					// .setProxy(new HttpHost(myProxy, myProxyPort))
					.build();
		}
		// Create an HttpClient with the given custom dependencies and
		// configuration.
		client = HttpClients.custom().setDefaultCookieStore(myCookie)
				.setDefaultRequestConfig(requestConfig).build();
		// Use custom credentials provider if necessary.
		CredentialsProvider defaultcp = new BasicCredentialsProvider();
		// Execution context can be customized locally.
		context = HttpClientContext.create();
		// Contextual attributes set the local context level will take
		// precedence over those set at the client level.
		// context.setCookieStore(myCookie);
		context.setCredentialsProvider(defaultcp);
	}

	private List<NameValuePair> RequestProcess(List<NameValuePair> source,
			boolean UseDefaultKey) throws InvalidKeyException,
			NoSuchAlgorithmException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException {
		ArrayList<NameValuePair> result = new ArrayList<NameValuePair>();
		Iterator<NameValuePair> i = source.iterator();
		while (i.hasNext()) {
			NameValuePair n = i.next();
			if (UseDefaultKey) {
				result.add(new BasicNameValuePair(n.getName(), Crypto
						.Encrypt2Base64NoKey(n.getValue())));
			} else {
				result.add(new BasicNameValuePair(n.getName(), Crypto
						.Encrypt2Base64WithKey(n.getValue())));
			}
		}
		return result;
	}

	public byte[] ConnectToServer(String url, List<NameValuePair> content,
			boolean UseDefaultKey) throws InvalidKeyException,
			NoSuchAlgorithmException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException,
			ClientProtocolException, IOException {
		List<NameValuePair> post = RequestProcess(content, UseDefaultKey);

		HttpPost hp = new HttpPost(url);
		hp.setHeader("User-Agent", UserAgent);
		hp.setHeader("Accept-Encoding", "gzip, deflate");
		hp.setEntity(new UrlEncodedFormEntity(post, "UTF-8"));

		CredentialsProvider cp = new BasicCredentialsProvider();
		AuthScope as = new AuthScope(hp.getURI().getHost(), hp.getURI()
				.getPort());
		UsernamePasswordCredentials upc = new UsernamePasswordCredentials(Auth,
				Key);
		cp.setCredentials(as, upc);
		// if (!myCookie.getCookies().isEmpty())
		// context.setCookieStore(myCookie);
		context.setCredentialsProvider(cp);

		byte[] b = client.execute(hp, new HttpResponseHandler(), context);

		/* end */
		if (b != null) {
			if (url.contains("gp_verify_receipt?")) {
				// need to be decoded
				return null;
			}
			try {
				if (UseDefaultKey) {
					return Crypto.DecryptNoKey(b);
				} else {
					return Crypto.DecryptWithKey(b);
				}
			} catch (Exception ex) {
				if (!UseDefaultKey) {
					return Crypto.DecryptNoKey(b);
				} else {
					return Crypto.DecryptWithKey(b);
				}
			}
		}
		return null;
	}

}
