package net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;


//import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

public final class HttpResponseHandler implements ResponseHandler<byte[]> {

	@Override
	public byte[] handleResponse(HttpResponse hr) throws ClientProtocolException, IOException {
		//String rslt = preProcessResponse(hr);
		//Network.c = !(rslt == "" || rslt.contains("2"));
		if (hr.getStatusLine().getStatusCode() == 200) {
			if (hr.getEntity().getContentEncoding() == null) return EntityUtils.toByteArray(hr.getEntity());
			if(hr.getEntity().getContentEncoding().getValue().contains("gzip")){
				GZIPInputStream gis = null;
				try {
					gis = new GZIPInputStream(hr.getEntity().getContent());
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					byte[] b = new byte[0x400];
					int n;
					while ((n = gis.read(b)) != -1) baos.write(b, 0, n);
					System.out.println("Gzipped");
					return baos.toByteArray();
				} catch (Exception e) {
					throw e;
				} finally {
					try {
						if (gis != null) gis.close();
					} catch (Exception ex) {
						throw ex;
					}
				}
			} else {
				return EntityUtils.toByteArray(hr.getEntity());
			}
		} else if (hr.getStatusLine().getStatusCode() == 302) {
			throw new IOException("302");
		}
		return null;
	}
	
	/*
	private String preProcessResponse(HttpResponse hr){
		if (hr.getAllHeaders().length > 0){
			for(Header h:hr.getAllHeaders()){
				if (h.getName().contains("Content-Spare-Item")){
					return h.getElements()[0].getName();
				}
			}
		}
		return "";
	}	
	*/
}
