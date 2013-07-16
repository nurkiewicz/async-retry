package com.blogspot.nurkiewicz.asyncretry.policy;

import com.blogspot.nurkiewicz.asyncretry.RetryContext;

import java.util.Objects;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/16/13, 7:22 PM
 */
abstract public class RetryPolicyWrapper implements RetryPolicy {

	protected final RetryPolicy target;

	protected RetryPolicyWrapper(RetryPolicy target) {
		this.target = Objects.requireNonNull(target);
	}

	@Override
	public long delayMillis(RetryContext context) {
		return target.delayMillis(context);
	}

	@Override
	public boolean shouldContinue(RetryContext context) {
		return target.shouldContinue(context);
	}
}
