package jp.gr.java_conf.mitchibu.mitools.http.util;

@SuppressWarnings("unused")
public abstract class ByteArraySender extends AbstractSender {
	private final byte[] body;

	public ByteArraySender(byte[] body) {
		this(body, null);
	}

	public ByteArraySender(byte[] body, String contentType) {
		super(contentType);
		this.body = body;
	}

	@Override
	public int getContentLength() {
		return body == null ? 0 : body.length;
	}
}
