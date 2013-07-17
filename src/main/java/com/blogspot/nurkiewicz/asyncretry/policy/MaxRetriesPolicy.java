package com.blogspot.nurkiewicz.asyncretry.policy;

import com.blogspot.nurkiewicz.asyncretry.RetryContext;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/16/13, 6:23 PM
 */
public class MaxRetriesPolicy extends RetryPolicyWrapper {

	public static final int DEFAULT_MAX_RETRIES = 10;

	private final int maxRetries;

	public MaxRetriesPolicy(RetryPolicy target) {
		this(target, DEFAULT_MAX_RETRIES);
	}

	public MaxRetriesPolicy(RetryPolicy target, int maxRetries) {
		super(target);
		this.maxRetries = maxRetries;
	}

	@Override
	public boolean shouldContinue(RetryContext context) {
		return target.shouldContinue(context) && context.getRetryCount() <= maxRetries;
	}
}
