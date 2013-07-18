package com.blogspot.nurkiewicz.asyncretry;

import com.blogspot.nurkiewicz.asyncretry.policy.RetryPolicy;

import java.util.Objects;

public class AsyncRetryContext implements RetryContext {

	private final RetryPolicy retryPolicy;
	private final int retry;
	private final Throwable lastThrowable;

	public AsyncRetryContext(RetryPolicy retryPolicy) {
		this(retryPolicy, 0, null);
	}

	public AsyncRetryContext(RetryPolicy retryPolicy, int retry, Throwable lastThrowable) {
		this.retryPolicy = Objects.requireNonNull(retryPolicy);
		this.retry = retry;
		this.lastThrowable = lastThrowable;
	}

	@Override
	public boolean willRetry() {
		return retryPolicy.shouldContinue(this.nextRetry(new Exception()));
	}

	@Override
	public int getRetryCount() {
		return retry;
	}

	@Override
	public Throwable getLastThrowable() {
		return lastThrowable;
	}

	public AsyncRetryContext nextRetry(Throwable cause) {
		return new AsyncRetryContext(retryPolicy, retry + 1, cause);
	}

}
