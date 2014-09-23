package com.nurkiewicz.asyncretry;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/16/13, 6:36 PM
 */
public interface RetryContext {
	boolean willRetry();

	int getRetryCount();

	Throwable getLastThrowable();
}
