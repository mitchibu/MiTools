package jp.gr.java_conf.mitchibu.mitools.http.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import jp.gr.java_conf.mitchibu.mitools.http.MiHTTP;
import jp.gr.java_conf.mitchibu.mitools.http.ResponseException;

@SuppressWarnings("unused")
public class FileReceiver implements MiHTTP.Receiver<File> {
	private final File file;

	public FileReceiver(File file) {
		this.file = file;
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	@Override
	public File processReceive(int code, String message, Map<String, List<String>> headers, InputStream body) throws Exception {
		if(code / 100 != 2) throw new ResponseException(code, message);

		boolean result = false;
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);

			byte[] b = new byte[4096];
			int len;
			while((len = body.read(b)) > 0) out.write(b, 0, len);
			result = true;
			return file;
		} finally {
			if(out != null) {
				try { out.close(); } catch(Exception e) { e.printStackTrace(); }
				if(!result) file.delete();
			}
		}
	}
}
