package com.blogspot.nurkiewicz.asyncretry.backoff;

import com.blogspot.nurkiewicz.asyncretry.RetryContext;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/16/13, 6:17 PM
 */
public class ExponentialDelayRetryPolicy implements Backoff {

	private final long initialDelayMillis;
	private final double multiplier;

	public ExponentialDelayRetryPolicy(long initialDelayMillis, double multiplier) {
		this.initialDelayMillis = initialDelayMillis;
		this.multiplier = multiplier;
	}

	@Override
	public long delayMillis(RetryContext context) {
		return (long) (initialDelayMillis * Math.pow(multiplier, context.getRetryCount() - 1));
	}
}
