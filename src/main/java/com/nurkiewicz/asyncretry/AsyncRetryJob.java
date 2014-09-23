package com.nurkiewicz.asyncretry;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.nurkiewicz.asyncretry.function.RetryCallable;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/21/13, 6:37 PM
 */
public class AsyncRetryJob<V> extends RetryJob<V> {

	private final RetryCallable<ListenableFuture<V>> userTask;

	public AsyncRetryJob(RetryCallable<ListenableFuture<V>> userTask, AsyncRetryExecutor parent) {
		this(userTask, parent, new AsyncRetryContext(parent.getRetryPolicy()), SettableFuture.<V>create());
	}

	public AsyncRetryJob(RetryCallable<ListenableFuture<V>> userTask, AsyncRetryExecutor parent, AsyncRetryContext context, SettableFuture<V> future) {
		super(context, parent, future);
		this.userTask = userTask;
	}

	@Override
	public void run(final long startTime) {
		try {
			Futures.addCallback(userTask.call(context), new FutureCallback<V>() {
				@Override
				public void onSuccess(V result) {
					complete(result, System.currentTimeMillis() - startTime);
				}

				@Override
				public void onFailure(Throwable throwable) {
					handleThrowable(throwable, System.currentTimeMillis() - startTime);
				}
			});
		} catch (Throwable t) {
			handleThrowable(t, System.currentTimeMillis() - startTime);
		}
	}

	@Override
	protected RetryJob<V> nextTask(AsyncRetryContext nextRetryContext) {
		return new AsyncRetryJob<>(userTask, parent, nextRetryContext, future);
	}
}
