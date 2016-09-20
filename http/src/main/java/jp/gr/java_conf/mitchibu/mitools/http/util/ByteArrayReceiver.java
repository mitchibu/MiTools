package jp.gr.java_conf.mitchibu.mitools.http.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

import jp.gr.java_conf.mitchibu.mitools.http.MiHTTP;
import jp.gr.java_conf.mitchibu.mitools.http.ResponseException;

@SuppressWarnings("unused")
public class ByteArrayReceiver implements MiHTTP.Receiver<byte[]> {
	@Override
	public byte[] processReceive(int code, String message, Map headers, InputStream body) throws Exception {
		if(code / 100 != 2) throw new ResponseException(code, message);

		ByteArrayOutputStream out = null;
		try {
			out = new ByteArrayOutputStream();
			byte[] b = new byte[4096];
			int n;
			while((n = body.read(b)) > 0) out.write(b, 0, n);
			return out.toByteArray();
		} finally {
			if(out != null) try { out.close(); } catch(Exception e) { e.printStackTrace(); }
		}
	}
}
