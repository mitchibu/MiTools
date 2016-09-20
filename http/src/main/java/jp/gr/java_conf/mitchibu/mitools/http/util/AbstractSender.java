package jp.gr.java_conf.mitchibu.mitools.http.util;

import jp.gr.java_conf.mitchibu.mitools.http.MiHTTP;

@SuppressWarnings("unused")
public abstract class AbstractSender implements MiHTTP.Sender {
	private final String contentType;

	public AbstractSender() {
		this(null);
	}

	public AbstractSender(String contentType) {
		this.contentType = contentType;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public int getContentLength() {
		return 0;
	}
}
