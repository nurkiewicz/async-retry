package com.blogspot.nurkiewicz.asyncretry.policy;

import com.blogspot.nurkiewicz.asyncretry.AbstractBaseTestCase;
import com.blogspot.nurkiewicz.asyncretry.AsyncRetryContext;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/18/13, 11:27 PM
 */
public class AbstractRetryPolicyTest extends AbstractBaseTestCase {

	private static final int ANY_RETRY = 7;

	protected boolean shouldRetryOn(RetryPolicy policy, Throwable lastThrowable) {
		return policy.shouldContinue(new AsyncRetryContext(policy, ANY_RETRY, lastThrowable));
	}
}

class OptimisticLockException extends RuntimeException {}