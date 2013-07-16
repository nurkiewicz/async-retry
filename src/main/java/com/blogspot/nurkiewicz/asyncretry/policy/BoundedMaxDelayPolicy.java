package com.blogspot.nurkiewicz.asyncretry.policy;

import com.blogspot.nurkiewicz.asyncretry.RetryContext;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/16/13, 7:04 PM
 */
public class BoundedMaxDelayPolicy extends RetryPolicyWrapper {

	public static final long DEFAULT_MAX_DELAY_MILLIS = 10_000;

	private final long maxDelayMillis;

	public BoundedMaxDelayPolicy(RetryPolicy target) {
		this(target, DEFAULT_MAX_DELAY_MILLIS);
	}

	public BoundedMaxDelayPolicy(RetryPolicy target, long maxDelayMillis) {
		super(target);
		this.maxDelayMillis = maxDelayMillis;
	}

	@Override
	public long delayMillis(RetryContext context) {
		return Math.min(target.delayMillis(context), maxDelayMillis);
	}
}
