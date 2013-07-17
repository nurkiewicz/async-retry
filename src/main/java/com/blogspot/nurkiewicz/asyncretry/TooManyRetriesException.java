package com.blogspot.nurkiewicz.asyncretry;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/17/13, 7:52 PM
 */
public class TooManyRetriesException extends RuntimeException {

	private final int retries;

	public TooManyRetriesException(int retryCount, Throwable lastCause) {
		super("Too many retries: " + retryCount, lastCause);
		this.retries = retryCount;
	}

	public int getRetries() {
		return retries;
	}
}
