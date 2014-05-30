package com.nurkiewicz.asyncretry;

import com.nurkiewicz.asyncretry.function.RetryCallable;

import java.util.concurrent.CompletableFuture;

class SyncRetryJob<V> extends RetryJob<V> {

	private final RetryCallable<V> userTask;

	public SyncRetryJob(RetryCallable<V> userTask, AsyncRetryExecutor parent) {
		this(userTask, parent, new AsyncRetryContext(parent.getRetryPolicy()), new CompletableFuture<>());
	}

	public SyncRetryJob(RetryCallable<V> userTask, AsyncRetryExecutor parent, AsyncRetryContext context, CompletableFuture<V> future) {
		super(context, parent, future);
		this.userTask = userTask;
	}

	@Override
	public void run(long startTime) {
		try {
			final V result = userTask.call(context);
			complete(result, System.currentTimeMillis() - startTime);
		} catch (Throwable t) {
			handleThrowable(t, System.currentTimeMillis() - startTime);
		}
	}

	protected RetryJob<V> nextTask(AsyncRetryContext nextRetryContext) {
		return new SyncRetryJob<>(userTask, parent, nextRetryContext, future);
	}

}