package com.nurkiewicz.asyncretry.backoff;

import com.nurkiewicz.asyncretry.RetryContext;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/16/13, 7:04 PM
 */
public class BoundedMaxBackoff extends BackoffWrapper {

	public static final long DEFAULT_MAX_DELAY_MILLIS = 10_000;

	private final long maxDelayMillis;

	public BoundedMaxBackoff(Backoff target) {
		this(target, DEFAULT_MAX_DELAY_MILLIS);
	}

	public BoundedMaxBackoff(Backoff target, long maxDelayMillis) {
		super(target);
		this.maxDelayMillis = maxDelayMillis;
	}

	@Override
	public long delayMillis(RetryContext context) {
		return Math.min(target.delayMillis(context), maxDelayMillis);
	}
}
