package com.blogspot.nurkiewicz.asyncretry;

class AsyncRetryContext implements RetryContext {

	private final int retry;
	private final Throwable lastThrowable;

	public AsyncRetryContext() {
		this(0, null);
	}

	public AsyncRetryContext(int retry, Throwable lastThrowable) {
		this.retry = retry;
		this.lastThrowable = lastThrowable;
	}

	@Override
	public boolean willRetry() {
		return false;
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
		return new AsyncRetryContext(retry + 1, cause);
	}

}
