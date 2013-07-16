package com.blogspot.nurkiewicz.asyncretry.policy;

import com.blogspot.nurkiewicz.asyncretry.RetryContext;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/16/13, 7:25 PM
 */
public class NeverRetryPolicy implements RetryPolicy {
	@Override
	public long delayMillis(RetryContext context) {
		return 0;
	}

	@Override
	public boolean shouldContinue(RetryContext context) {
		return false;
	}
}
