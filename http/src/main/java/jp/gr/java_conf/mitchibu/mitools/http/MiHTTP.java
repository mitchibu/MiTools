package jp.gr.java_conf.mitchibu.mitools.http;

import android.net.Uri;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

@SuppressWarnings("unused")
public class MiHTTP<E> implements Callable<E> {
	public static boolean VERBOSE = false;

	private static void verbose(String text) {
		if(!VERBOSE) return;
		android.util.Log.v(MiHTTP.class.getSimpleName(), text);
	}

	public enum Method {
		GET,
		POST
	}

	private final String url;

	private Method method = Method.GET;
	private int maxRedirects = Integer.MAX_VALUE - 1;
	private int connectTimeout = 0;
	private int readTimeout = 0;
	private Map<String, List<String>> headers = null;
	private Sender sender = null;
	private Receiver<E> receiver = null;

	private Processor<E> processor = new DefaultProcessor<>();
	private SSLSocketFactory factory = null;
	private HostnameVerifier verifier = null;

	public MiHTTP(String url) {
		if(url == null) throw new IllegalArgumentException();
		this.url = url;
	}

	public MiHTTP<E> method(Method method) {
		if(method == null) throw new IllegalArgumentException();
		this.method = method;
		return this;
	}

	public MiHTTP<E> maxRedirects(int maxRedirects) {
		if(maxRedirects < 0 || maxRedirects == Integer.MAX_VALUE) throw new IllegalArgumentException();
		this.maxRedirects = maxRedirects;
		return this;
	}

	public MiHTTP<E> connectTimeout(int connectTimeout) {
		if(connectTimeout < 0) throw new IllegalArgumentException();
		this.connectTimeout = connectTimeout;
		return this;
	}

	public MiHTTP<E> readTimeout(int readTimeout) {
		if(readTimeout < 0) throw new IllegalArgumentException();
		this.readTimeout = readTimeout;
		return this;
	}

	public MiHTTP<E> addHeader(String name, String value) {
		if(name == null || value == null) throw new IllegalArgumentException();
		if(headers == null) headers = new TreeMap<>();
		List<String> entries = headers.get(name);
		if(entries == null) {
			entries = new ArrayList<>();
			headers.put(name, entries);
		}
		entries.add(value);
		return this;
	}

	public MiHTTP<E> sender(Sender sender) {
		this.sender = sender;
		return this;
	}

	public MiHTTP<E> receiver(Receiver<E> receiver) {
		this.receiver = receiver;
		return this;
	}

	public MiHTTP<E> processor(Processor<E> processor) {
		if(processor == null) throw new IllegalArgumentException();
		this.processor = processor;
		return this;
	}

	public MiHTTP<E> verifier(HostnameVerifier verifier) {
		this.verifier = verifier;
		return this;
	}

	public MiHTTP<E> factory(SSLSocketFactory factory) {
		this.factory = factory;
		return this;
	}

	@Override
	public E call() throws Exception {
		return processor.process(url, method, maxRedirects, connectTimeout, readTimeout, headers, sender, receiver, verifier, factory);
	}

	public interface Processor<E> {
		E process(String url, Method method, int maxRedirects, int connectTimeout, int readTimeout, Map<String, List<String>> headers, Sender sender, Receiver<E> receiver, HostnameVerifier verifier, SSLSocketFactory factory) throws Exception;
	}

	public interface Sender {
		String getContentType();
		int getContentLength();
		void processSend(OutputStream body) throws Exception;
	}

	public interface Receiver<E> {
		E processReceive(int code, String message, Map<String, List<String>> headers, InputStream body) throws Exception;
	}

	@SuppressWarnings("ConstantConditions")
	private static class DefaultProcessor<E> implements Processor<E> {
		@Override
		public E process(String url, Method method, int maxRedirects, int connectTimeout, int readTimeout, Map<String, List<String>> headers, Sender sender, Receiver<E> receiver, HostnameVerifier verifier, SSLSocketFactory factory) throws Exception {
			Thread thread = Thread.currentThread();
			for(int i = 0; i <= maxRedirects && !thread.isInterrupted(); ++ i) {
				verbose(url);
				HttpURLConnection http = null;

				try {
					http = (HttpURLConnection)new URL(url).openConnection();
					if(http instanceof HttpsURLConnection) {
						if(verifier != null) ((HttpsURLConnection)http).setHostnameVerifier(verifier);
						if(factory != null) ((HttpsURLConnection)http).setSSLSocketFactory(factory);
					}
					http.setRequestMethod(method.name());
					http.setConnectTimeout(connectTimeout);
					http.setReadTimeout(readTimeout);
					http.setInstanceFollowRedirects(false);
					if(headers != null) {
						Set<Map.Entry<String, List<String>>> entries = headers.entrySet();
						for(Map.Entry<String, List<String>> entry : entries) {
							for(String value : entry.getValue()) {
								http.addRequestProperty(entry.getKey(), value);
							}
						}
					}
					http.setDoOutput(sender != null);
					http.setDoInput(receiver != null);

					if(sender != null) {
						String contentType = sender.getContentType();
						if(contentType != null) http.addRequestProperty("Content-Type", contentType);
						int contentLength = sender.getContentLength();
						if(contentLength >= 0) http.setFixedLengthStreamingMode(contentLength);

						OutputStream body = null;
						try {
							body = http.getOutputStream();
							sender.processSend(body);
							body.flush();
						} finally {
							if(body != null) try { body.close(); } catch(Exception e) { e.printStackTrace(); }
						}
					}

					int code = http.getResponseCode();
					verbose(String.format("%s(%d)", url, code));
					if(code / 100 == 3 && i < maxRedirects) {
						String location = http.getHeaderField("Location");
						if(location.startsWith("/")) {
							location = Uri.parse(url).buildUpon().path(location).build().toString();
						}
						url = location;
						continue;
					}

					if(receiver != null) {
						InputStream body = null;
						try {
							body = getInputStream(http);
							return receiver.processReceive(code, http.getResponseMessage(), http.getHeaderFields(), body);
						} finally {
							if(body != null) try { body.close(); } catch(Exception e) { e.printStackTrace(); }
						}
					}
					return null;
				} finally {
					if(http != null) http.disconnect();
				}
			}
			if(thread.isInterrupted()) throw new CancellationException();
			return null;
		}

		private InputStream getInputStream(HttpURLConnection http) {
			try {
				return http.getInputStream();
			} catch(Exception e) {
				return http.getErrorStream();
			}
		}
	}
}
