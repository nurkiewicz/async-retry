package com.nurkiewicz.asyncretry;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/16/13, 6:36 PM
 */
public interface RetryContext {
	boolean willRetry();

	/**
	 * Which retry is being executed right now
	 * @return 1 means it's the first retry, i.e. action is executed for the second time
	 */
	int getRetryCount();

	Throwable getLastThrowable();

	default boolean isFirstRetry() {
		return getRetryCount() == 1;
	}

}
