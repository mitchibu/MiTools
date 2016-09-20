package jp.gr.java_conf.mitchibu.mitools.http.util;

import java.io.InputStream;
import java.util.Map;

import jp.gr.java_conf.mitchibu.mitools.http.MiHTTP;
import jp.gr.java_conf.mitchibu.mitools.http.ResponseException;

@SuppressWarnings("unused")
public class StringReceiver implements MiHTTP.Receiver<String> {
	@Override
	public String processReceive(int code, String message, Map headers, InputStream body) throws Exception {
		if(code / 100 != 2) throw new ResponseException(code, message);

		StringBuilder sb = new StringBuilder();
		byte[] b = new byte[4096];
		int n;
		while((n = body.read(b)) > 0) sb.append(new String(b, 0, n));
		return sb.toString();
	}
}
