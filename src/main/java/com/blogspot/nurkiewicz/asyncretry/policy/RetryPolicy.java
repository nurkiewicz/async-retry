package com.blogspot.nurkiewicz.asyncretry.policy;

import com.blogspot.nurkiewicz.asyncretry.RetryContext;
import com.blogspot.nurkiewicz.asyncretry.policy.exception.AbortPredicateRetryPolicy;
import com.blogspot.nurkiewicz.asyncretry.policy.exception.ExceptionClassRetryPolicy;
import com.blogspot.nurkiewicz.asyncretry.policy.random.ProportionalRandomJitterRetryPolicy;
import com.blogspot.nurkiewicz.asyncretry.policy.random.UniformRandomJitterRetryPolicy;

import java.util.function.Predicate;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/16/13, 6:05 PM
 */
public interface RetryPolicy {

	RetryPolicy DEFAULT = new FixedIntervalRetryPolicy();

	long delayMillis(RetryContext context);

	default boolean shouldContinue(RetryContext context) {
		return true;
	}

	default RetryPolicy retryFor(Class<Throwable> retryForThrowable) {
		return ExceptionClassRetryPolicy.retryFor(this, retryForThrowable);
	}

	default RetryPolicy abortFor(Class<Throwable> retryForThrowable) {
		return ExceptionClassRetryPolicy.abortFor(this, retryForThrowable);
	}

	default RetryPolicy abortIf(Predicate<Throwable> retryPredicate) {
		return new AbortPredicateRetryPolicy(this, retryPredicate);
	}

	default RetryPolicy withUniformJitter() {
		return new UniformRandomJitterRetryPolicy(this);
	}

	default RetryPolicy withUniformJitter(long range) {
		return new UniformRandomJitterRetryPolicy(this, range);
	}

	default RetryPolicy withProportionalJitter() {
		return new ProportionalRandomJitterRetryPolicy(this);
	}

	default RetryPolicy withProportionalJitter(double multiplier) {
		return new ProportionalRandomJitterRetryPolicy(this, multiplier);
	}

	default RetryPolicy withMinDelay(long minDelayMillis) {
		return new BoundedMinDelayPolicy(this, minDelayMillis);
	}

	default RetryPolicy withMaxDelay(long maxDelayMillis) {
		return new BoundedMaxDelayPolicy(this, maxDelayMillis);
	}

	default RetryPolicy dontRetry() {
		return withMaxRetries(0);
	}

	default RetryPolicy withMaxRetries(int times) {
		return new MaxRetriesPolicy(this, times);
	}
}
