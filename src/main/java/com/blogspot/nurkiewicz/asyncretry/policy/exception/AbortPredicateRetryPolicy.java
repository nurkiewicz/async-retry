package com.blogspot.nurkiewicz.asyncretry.policy.exception;

import com.blogspot.nurkiewicz.asyncretry.RetryContext;
import com.blogspot.nurkiewicz.asyncretry.policy.RetryPolicy;
import com.blogspot.nurkiewicz.asyncretry.policy.RetryPolicyWrapper;
import com.google.common.base.Predicate;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/16/13, 9:08 PM
 */
public class AbortPredicateRetryPolicy extends RetryPolicyWrapper {

	private final Predicate<Throwable> abortPredicate;

	public AbortPredicateRetryPolicy(RetryPolicy target, Predicate<Throwable> abortPredicate) {
		super(target);
		this.abortPredicate = abortPredicate;
	}

	@Override
	public boolean shouldContinue(RetryContext context) {
		return !abortPredicate.apply(context.getLastThrowable()) &&
				target.shouldContinue(context);
	}

}
