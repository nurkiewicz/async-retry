package com.blogspot.nurkiewicz.asyncretry.policy;

import com.blogspot.nurkiewicz.asyncretry.RetryContext;
import com.blogspot.nurkiewicz.asyncretry.policy.exception.AbortPredicateRetryPolicy;
import com.blogspot.nurkiewicz.asyncretry.policy.exception.ExceptionClassRetryPolicy;

import java.util.function.Predicate;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/16/13, 6:05 PM
 */
public interface RetryPolicy {

	public static final RetryPolicy DEFAULT = new RetryInfinitelyPolicy();

	boolean shouldContinue(RetryContext context);

	default RetryPolicy retryFor(Class<? extends Throwable> retryForThrowable) {
		return ExceptionClassRetryPolicy.retryFor(this, retryForThrowable);
	}

	default RetryPolicy abortFor(Class<? extends Throwable> retryForThrowable) {
		return ExceptionClassRetryPolicy.abortFor(this, retryForThrowable);
	}

	default RetryPolicy abortIf(Predicate<Throwable> retryPredicate) {
		return new AbortPredicateRetryPolicy(this, retryPredicate);
	}

	default RetryPolicy dontRetry() {
		return new MaxRetriesPolicy(this, 0);
	}

	default RetryPolicy withMaxRetries(int times) {
		return new MaxRetriesPolicy(this, times);
	}
}
