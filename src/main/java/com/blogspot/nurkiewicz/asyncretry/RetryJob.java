package com.blogspot.nurkiewicz.asyncretry;

import com.blogspot.nurkiewicz.asyncretry.backoff.Backoff;
import com.blogspot.nurkiewicz.asyncretry.policy.exception.AbortRetryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/21/13, 6:10 PM
 */
public abstract class RetryJob<V> implements Runnable {
	private static final Logger log = LoggerFactory.getLogger(RetryJob.class);
	protected final CompletableFuture<V> future;
	protected final AsyncRetryContext context;
	protected final AsyncRetryExecutor parent;

	public RetryJob(AsyncRetryContext context, AsyncRetryExecutor parent, CompletableFuture<V> future) {
		this.context = context;
		this.parent = parent;
		this.future = future;
	}

	protected void logSuccess(RetryContext context, V result, long duration) {
		log.trace("Successful after {} retries, took {}ms and returned: {}", (context.getRetryCount() - 1), duration, result);
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
		if (t instanceof AbortRetryException) {
			handleManualAbort((AbortRetryException) t);
		} else {
			handleUserThrowable(t, duration);
		}
	}

	protected void handleUserThrowable(Throwable t, long duration) {
		final AsyncRetryContext nextRetryContext = context.nextRetry(t);
		if (parent.getRetryPolicy().shouldContinue(nextRetryContext)) {
			final long delay = calculateNextDelay(duration, nextRetryContext, parent.getBackoff());
			retryWithDelay(nextRetryContext, delay, duration);
		} else {
			logFailure(nextRetryContext, duration);
			future.completeExceptionally(t);
		}
	}

	protected void logFailure(AsyncRetryContext nextRetryContext, long duration) {
		log.trace("Giving up after {} retries, last run took: {}ms, last exception: ",
				context.getRetryCount(),
				duration,
				nextRetryContext.getLastThrowable());
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

	protected void logRetry(AsyncRetryContext nextRetryContext, long delay, long duration) {
		final Date nextRunDate = new Date(System.currentTimeMillis() + delay);
		log.trace("Retry {} failed after {}ms, scheduled next retry in {}ms ({})",
				context.getRetryCount(),
				duration,
				delay,
				nextRunDate,
				nextRetryContext.getLastThrowable());
	}

	@Override
	public void run() {
		run(System.currentTimeMillis());
	}

	protected abstract void run(long startTime);

	protected abstract RetryJob<V> nextTask(AsyncRetryContext nextRetryContext);

	protected void complete(V result, long duration) {
		logSuccess(context, result, duration);
		future.complete(result);
	}

	public CompletableFuture<V> getFuture() {
		return future;
	}
}
