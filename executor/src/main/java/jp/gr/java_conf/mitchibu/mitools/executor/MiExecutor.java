package jp.gr.java_conf.mitchibu.mitools.executor;

import android.content.Context;
import android.os.Handler;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

@SuppressWarnings("unused")
public class MiExecutor {
	private final static ExecutorService DEFAULT_EXECUTOR = Executors.newSingleThreadExecutor();

	private final ExecutorService executor;
	private final Handler handler;

	public MiExecutor(Context context) {
		this(context, DEFAULT_EXECUTOR);
	}

	public MiExecutor(Context context, ExecutorService executor) {
		this.executor = executor;
		handler = new Handler(context.getMainLooper());
	}

	public <E> Task<E> submit(Callable<E> callable, OnCompletedListener<E> onCompletedListener) {
		return submit(callable, onCompletedListener, null);
	}

	public <E> Task<E> submit(Callable<E> callable, OnCompletedListener<E> onCompletedListener, OnCancelledListener<E> onCancelledListener) {
		Task<E> task = new Task<>(callable, onCompletedListener, onCancelledListener);
		executor.submit(task);
		return task;
	}

	public void shutdown() {
		executor.shutdown();
	}

	public interface OnCompletedListener<E> {
		void onCompleted(Task<E> task);
	}

	public interface OnCancelledListener<E> {
		void onCancelled(Task<E> task);
	}

	public class Task<E> extends FutureTask<E> {
		private final OnCompletedListener<E> onCompletedListener;
		private final OnCancelledListener<E> onCancelledListener;

		public Task(Callable<E> callable, OnCompletedListener<E> onCompletedListener, OnCancelledListener<E> onCancelledListener) {
			super(callable);
			this.onCompletedListener = onCompletedListener;
			this.onCancelledListener = onCancelledListener;
		}

		@Override
		public void done() {
			handler.post(new Runnable() {
				@Override
				public void run() {
					if(isCancelled()) {
						if(onCancelledListener != null) onCancelledListener.onCancelled(Task.this);
					} else {
						if(onCompletedListener != null) onCompletedListener.onCompleted(Task.this);
					}
				}
			});
		}
	}
}
