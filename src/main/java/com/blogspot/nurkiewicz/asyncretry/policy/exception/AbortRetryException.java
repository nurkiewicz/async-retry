package com.blogspot.nurkiewicz.asyncretry.policy.exception;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/16/13, 10:23 PM
 */
public class AbortRetryException extends RuntimeException {

	public AbortRetryException() {
	}

	public AbortRetryException(String message) {
		super(message);
	}
}
