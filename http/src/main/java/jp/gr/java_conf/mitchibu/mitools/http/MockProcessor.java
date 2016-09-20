package jp.gr.java_conf.mitchibu.mitools.http;

import android.net.Uri;

import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

@SuppressWarnings("unused")
public class MockProcessor<E> implements MiHTTP.Processor<E> {
	@Override
	public E process(String url, MiHTTP.Method method, int maxRedirects, int connectTimeout, int readTimeout, Map<String, List<String>> headers, MiHTTP.Sender sender, MiHTTP.Receiver<E> receiver, HostnameVerifier verifier, SSLSocketFactory factory) throws Exception {
		String path = Uri.parse(url).getPath();
		return null;
	}
}
