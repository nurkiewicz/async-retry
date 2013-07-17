package com.blogspot.nurkiewicz.asyncretry;

import com.blogspot.nurkiewicz.asyncretry.policy.RetryPolicy;
import com.blogspot.nurkiewicz.asyncretry.policy.exception.AbortRetryException;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

class RetryTask<V> implements Runnable {

	private final CompletableFuture<V> future;
	private final Function<RetryContext, V> userTask;
	private final AsyncRetryContext context;
	private final AsyncRetryExecutor parent;

	public RetryTask(Function<RetryContext, V> userTask, AsyncRetryExecutor parent) {
		this(userTask, new AsyncRetryContext(), new CompletableFuture<>(), parent);
	}

	public RetryTask(Function<RetryContext, V> userTask, AsyncRetryContext context, CompletableFuture<V> future, AsyncRetryExecutor parent) {
		this.userTask = userTask;
		this.context = context;
		this.future = future;
		this.parent = parent;
	}

	@Override
	public void run() {
		final long startTime = System.currentTimeMillis();
		try {
			final V result = userTask.apply(context);
			future.complete(result);
		} catch(AbortRetryException abortEx) {
			handleManualAbort(abortEx);
		} catch(Throwable t) {
			handleThrowable(t, System.currentTimeMillis() - startTime);
		}
	}

	private void handleManualAbort(AbortRetryException abortEx) {
		if (context.getLastThrowable() != null) {
			future.completeExceptionally(context.getLastThrowable());
		} else {
			future.completeExceptionally(abortEx);
		}
	}

	private void handleThrowable(Throwable t, long taskDurationMillis) {
		final AsyncRetryContext nextRetryContext = context.nextRetry(t);
		final RetryPolicy retryPolicy = parent.getRetryPolicy();
		if (retryPolicy.shouldContinue(nextRetryContext)) {
			final long delay = calculateNextDelay(taskDurationMillis, nextRetryContext, retryPolicy);
			retryWithDelay(nextRetryContext, delay);
		} else {
			future.completeExceptionally(new TooManyRetriesException(context.getRetryCount(), t));
		}
	}

	private long calculateNextDelay(long taskDurationMillis, AsyncRetryContext nextRetryContext, RetryPolicy retryPolicy) {
		final long delay = retryPolicy.delayMillis(nextRetryContext);
		return delay - (parent.isFixedDelay()? taskDurationMillis : 0);
	}

	private void retryWithDelay(AsyncRetryContext nextRetryContext, long delay) {
		final RetryTask<V> nextRetryTask = new RetryTask<>(userTask, nextRetryContext, future, parent);
		parent.getScheduler().schedule(nextRetryTask, delay, MILLISECONDS);
	}

	public CompletableFuture<V> getFuture() {
		return future;
	}
}