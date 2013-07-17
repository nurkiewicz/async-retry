package com.blogspot.nurkiewicz.asyncretry.policy;

import com.blogspot.nurkiewicz.asyncretry.RetryContext;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/17/13, 11:21 PM
 */
public class RetryInfinitelyPolicy implements RetryPolicy {

	@Override
	public boolean shouldContinue(RetryContext context) {
		return true;
	}

}
