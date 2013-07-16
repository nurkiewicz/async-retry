package com.blogspot.nurkiewicz.asyncretry.policy;

import com.blogspot.nurkiewicz.asyncretry.RetryContext;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/16/13, 7:04 PM
 */
public class BoundedMinDelayPolicy extends RetryPolicyWrapper {

	public static final long DEFAULT_MIN_DELAY_MILLIS = 100;

	private final long minDelayMillis;

	public BoundedMinDelayPolicy(RetryPolicy target) {
		this(target, DEFAULT_MIN_DELAY_MILLIS);
	}

	public BoundedMinDelayPolicy(RetryPolicy target, long minDelayMillis) {
		super(target);
		this.minDelayMillis = minDelayMillis;
	}

	@Override
	public long delayMillis(RetryContext context) {
		return Math.max(target.delayMillis(context), minDelayMillis);
	}
}
