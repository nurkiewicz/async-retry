package com.blogspot.nurkiewicz.asyncretry;

import com.blogspot.nurkiewicz.asyncretry.function.RetryCallable;

import java.util.concurrent.CompletableFuture;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/21/13, 6:37 PM
 */
public class AsyncRetryJob<V> extends RetryJob<V> {

	private final RetryCallable<CompletableFuture<V>> userTask;

	public AsyncRetryJob(RetryCallable<CompletableFuture<V>> userTask, AsyncRetryExecutor parent) {
		this(userTask, parent, new AsyncRetryContext(parent.getRetryPolicy()), new CompletableFuture<>());
	}

	public AsyncRetryJob(RetryCallable<CompletableFuture<V>> userTask, AsyncRetryExecutor parent, AsyncRetryContext context, CompletableFuture<V> future) {
		super(context, parent, future);
		this.userTask = userTask;
	}

	@Override
	public void run(long startTime) {
		try {
			userTask.call(context).handle((result, throwable) -> {
				final long stopTime = System.currentTimeMillis() - startTime;
				if (throwable != null) {
					handleThrowable(throwable, stopTime);
				} else {
					complete(result, stopTime);
				}
				return null;
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
