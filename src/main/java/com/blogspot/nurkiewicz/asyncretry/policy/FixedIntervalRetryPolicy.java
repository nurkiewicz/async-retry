package com.blogspot.nurkiewicz.asyncretry.policy;

import com.blogspot.nurkiewicz.asyncretry.RetryContext;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/16/13, 6:14 PM
 */
public class FixedIntervalRetryPolicy implements RetryPolicy {

	public static final long DEFAULT_PERIOD_MILLIS = 1000;

	private final long intervalMillis;

	public FixedIntervalRetryPolicy() {
		this(DEFAULT_PERIOD_MILLIS);
	}

	public FixedIntervalRetryPolicy(long intervalMillis) {
		this.intervalMillis = intervalMillis;
	}

	@Override
	public long delayMillis(RetryContext context) {
		return intervalMillis;
	}

}
