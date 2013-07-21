package com.blogspot.nurkiewicz.asyncretry;

import com.blogspot.nurkiewicz.asyncretry.backoff.Backoff;
import com.blogspot.nurkiewicz.asyncretry.function.RetryCallable;
import com.blogspot.nurkiewicz.asyncretry.policy.exception.AbortRetryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

class RetryJob<V> implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(RetryJob.class);

	private final CompletableFuture<V> future;
	private final RetryCallable<V> userTask;
	private final AsyncRetryContext context;
	private final AsyncRetryExecutor parent;

	public RetryJob(RetryCallable<V> userTask, AsyncRetryExecutor parent) {
		this(userTask, new AsyncRetryContext(parent.getRetryPolicy()), new CompletableFuture<>(), parent);
	}

	public RetryJob(RetryCallable<V> userTask, AsyncRetryContext context, CompletableFuture<V> future, AsyncRetryExecutor parent) {
		this.userTask = userTask;
		this.context = context;
		this.future = future;
		this.parent = parent;
	}

	@Override
	public void run() {
		final long startTime = System.currentTimeMillis();
		try {
			final V result = userTask.call(context);
			logSuccess(context, result, System.currentTimeMillis() - startTime);
			future.complete(result);
		} catch(AbortRetryException abortEx) {
			handleManualAbort(abortEx);
		} catch(Throwable t) {
			handleThrowable(t, System.currentTimeMillis() - startTime);
		}
	}

	protected void logSuccess(RetryContext context, V result, long duration) {
		log.trace("Successful after {} retries, took {}ms and returned: {}", context.getRetryCount(), duration, result);
	}

	protected void handleManualAbort(AbortRetryException abortEx) {
		logAbort(context);
		if (context.getLastThrowable() != null) {
			future.completeExceptionally(context.getLastThrowable());
		} else {
			future.completeExceptionally(abortEx);
		}
	}

	protected void logAbort(RetryContext context) {
		log.trace("Aborted by user after {} retries", context.getRetryCount() + 1);
	}

	protected void handleThrowable(Throwable t, long duration) {
		final AsyncRetryContext nextRetryContext = context.nextRetry(t);
		if (parent.getRetryPolicy().shouldContinue(nextRetryContext)) {
			final long delay = calculateNextDelay(duration, nextRetryContext, parent.getBackoff());
			retryWithDelay(nextRetryContext, delay, duration);
		} else {
			logFailure(nextRetryContext, duration);
			future.completeExceptionally(new TooManyRetriesException(context.getRetryCount(), t));
		}
	}

	protected void logFailure(AsyncRetryContext context, long duration) {
		log.trace("Giving up after {} retries, last run took: {}, last exception: ", context.getRetryCount(), duration, context.getLastThrowable());
	}

	private long calculateNextDelay(long taskDurationMillis, AsyncRetryContext nextRetryContext, Backoff backoff) {
		final long delay = backoff.delayMillis(nextRetryContext);
		return delay - (parent.isFixedDelay()? taskDurationMillis : 0);
	}

	private void retryWithDelay(AsyncRetryContext nextRetryContext, long delay, long duration) {
		final RetryJob<V> nextTask = nextTask(nextRetryContext);
		parent.getScheduler().schedule(nextTask, delay, MILLISECONDS);
		logRetry(nextRetryContext, delay, duration);
	}

	protected void logRetry(AsyncRetryContext context, long delay, long duration) {
		final Date nextRunDate = new Date(System.currentTimeMillis() + delay);
		log.trace("Retry {} failed after {}ms, scheduled next retry in {}ms ({})",
				context.getRetryCount(), duration, delay, nextRunDate, context.getLastThrowable());
	}

	protected RetryJob<V> nextTask(AsyncRetryContext nextRetryContext) {
		return new RetryJob<>(userTask, nextRetryContext, future, parent);
	}

	public CompletableFuture<V> getFuture() {
		return future;
	}
}