package jp.gr.java_conf.mitchibu.mitools.http;

public class ResponseException extends Exception {
	private final int code;

	public ResponseException(int code, String message) {
		super(message);
		this.code = code;
	}

	public int getCode() {
		return code;
	}
}
