package jp.gr.java_conf.mitchibu.mitools;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import jp.gr.java_conf.mitchibu.mitools.executor.MiExecutor;
import jp.gr.java_conf.mitchibu.mitools.http.MiHTTP;
import jp.gr.java_conf.mitchibu.mitools.http.util.StringReceiver;

public class MainActivity extends AppCompatActivity {
	private MiExecutor executor;
	private MiExecutor.Task<String> task;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		executor = new MiExecutor(this);
		MiHTTP.VERBOSE = true;
		task = executor.submit(new Yahoo(), new MiExecutor.OnCompletedListener<String>() {
			@Override
			public void onCompleted(MiExecutor.Task<String> task) {
				MainActivity.this.task = null;

				try {
					String result = task.get();
					android.util.Log.v("test", "result: " + result);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(task != null) task.cancel(true);
		executor.shutdown();
	}

	class Yahoo extends Base {
		public Yahoo() {
			super("http://www.yahoo.co.jp");
		}
	}

	class Base extends MiHTTP<String> {
		public Base(String url) {
			super(url);
			receiver(new StringReceiver());
		}
	}
}
