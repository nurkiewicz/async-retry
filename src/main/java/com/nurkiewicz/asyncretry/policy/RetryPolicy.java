package com.nurkiewicz.asyncretry.policy;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.nurkiewicz.asyncretry.RetryContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/16/13, 6:05 PM
 */
public class RetryPolicy {

	public static final RetryPolicy DEFAULT = new RetryPolicy();

	private final int maxRetries;
	private final Set<Class<? extends Throwable>> retryOn;
	private final Set<Class<? extends Throwable>> abortOn;
	private final Predicate<Throwable> retryPredicate;
	private final Predicate<Throwable> abortPredicate;

	public RetryPolicy retryOn(Class<? extends Throwable>... retryOnThrowables) {
		return new RetryPolicy(maxRetries, setPlusElems(retryOn, retryOnThrowables), abortOn, retryPredicate, abortPredicate);
	}

	public RetryPolicy abortOn(Class<? extends Throwable>... abortOnThrowables) {
		return new RetryPolicy(maxRetries, retryOn, setPlusElems(abortOn, abortOnThrowables), retryPredicate, abortPredicate);
	}

	public RetryPolicy abortIf(Predicate<Throwable> abortPredicate) {
		return new RetryPolicy(maxRetries, retryOn, abortOn, retryPredicate, Predicates.or(this.abortPredicate, abortPredicate));
	}

	public RetryPolicy retryIf(Predicate<Throwable> retryPredicate) {
		return new RetryPolicy(maxRetries, retryOn, abortOn, Predicates.or(this.retryPredicate, retryPredicate), abortPredicate);
	}

	public RetryPolicy dontRetry() {
		return new RetryPolicy(0, retryOn, abortOn, retryPredicate, abortPredicate);
	}

	public RetryPolicy withMaxRetries(int times) {
		return new RetryPolicy(times, retryOn, abortOn, retryPredicate, abortPredicate);
	}

	public RetryPolicy(int maxRetries, Set<Class<? extends Throwable>> retryOn, Set<Class<? extends Throwable>> abortOn, Predicate<Throwable> retryPredicate, Predicate<Throwable> abortPredicate) {
		this.maxRetries = maxRetries;
		this.retryOn = retryOn;
		this.abortOn = abortOn;
		this.retryPredicate = retryPredicate;
		this.abortPredicate = abortPredicate;
	}

	public RetryPolicy() {
		this(
				Integer.MAX_VALUE,
				Collections.<Class<? extends Throwable>>emptySet(),
				Collections.<Class<? extends Throwable>>emptySet(),
				Predicates.<Throwable>alwaysFalse(),
				Predicates.<Throwable>alwaysFalse());
	}

	public boolean shouldContinue(RetryContext context) {
		if (tooManyRetries(context)) {
			return false;
		}
		if (abortPredicate.apply(context.getLastThrowable())) {
			return false;
		}
		if (retryPredicate.apply(context.getLastThrowable())) {
			return true;
		}
		return exceptionClassRetryable(context);
	}

	private boolean tooManyRetries(RetryContext context) {
		return context.getRetryCount() > maxRetries;
	}

	private boolean exceptionClassRetryable(RetryContext context) {
		if (context.getLastThrowable() == null) {
			return false;
		}
		final Class<? extends Throwable> e = context.getLastThrowable().getClass();
		if (abortOn.isEmpty()) {
			return matches(e, retryOn);
		} else {
			return !matches(e, abortOn) && matches(e, retryOn);
		}
	}

	private static boolean matches(Class<? extends Throwable> throwable, Set<Class<? extends Throwable>> classSet) {
		if (classSet.isEmpty()) {
			return true;
		}
		for (Class<? extends Throwable> c : classSet) {
			if (c.isAssignableFrom(throwable)) {
				return true;
			}
		}
		return false;
	}

	private static <T> Set<T> setPlusElems(Set<T> initial, T... newElement) {
		final HashSet<T> copy = new HashSet<>(initial);
		copy.addAll(Arrays.asList(newElement));
		return Collections.unmodifiableSet(copy);
	}

}
