package com.blogspot.nurkiewicz.asyncretry.policy.exception;

import com.blogspot.nurkiewicz.asyncretry.AbstractBaseTestCase;
import com.blogspot.nurkiewicz.asyncretry.AsyncRetryContext;
import com.blogspot.nurkiewicz.asyncretry.policy.RetryInfinitelyPolicy;
import com.blogspot.nurkiewicz.asyncretry.policy.RetryPolicy;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/18/13, 11:27 PM
 */
public class AbstractExceptionClassRetryPolicyTest extends AbstractBaseTestCase {

	private static final int ANY_RETRY = 7;

	protected final RetryPolicy always = new RetryInfinitelyPolicy();

	protected boolean shouldRetryOn(RetryPolicy policy, Throwable lastThrowable) {
		return policy.shouldContinue(new AsyncRetryContext(policy, ANY_RETRY, lastThrowable));
	}
}

class OptimisticLockException extends RuntimeException {}