package com.blogspot.nurkiewicz.asyncretry.policy.exception;

import com.blogspot.nurkiewicz.asyncretry.RetryContext;
import com.blogspot.nurkiewicz.asyncretry.policy.RetryPolicy;
import com.blogspot.nurkiewicz.asyncretry.policy.RetryPolicyWrapper;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static java.util.Collections.emptySet;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/16/13, 8:47 PM
 */
public class ExceptionClassRetryPolicy extends RetryPolicyWrapper {

	private final Set<Class<? extends Throwable>> retryOn;
	private final Set<Class<? extends Throwable>> abortOn;

	public ExceptionClassRetryPolicy(RetryPolicy target) {
		this(target, Collections.emptySet(), Collections.emptySet());
	}

	public ExceptionClassRetryPolicy(RetryPolicy target, Set<Class<? extends Throwable>> retryOn, Set<Class<? extends Throwable>> abortOn) {
		super(target);
		this.retryOn = retryOn;
		this.abortOn = abortOn;
	}

	public static ExceptionClassRetryPolicy retryOn(RetryPolicy target, Class<? extends Throwable> retryOnThrowable) {
		if (target instanceof ExceptionClassRetryPolicy) {
			return mergeRetryOnWithExisting((ExceptionClassRetryPolicy) target, retryOnThrowable);
		}
		return new ExceptionClassRetryPolicy(target, Collections.<Class<? extends Throwable>>singleton(retryOnThrowable), emptySet());
	}

	private static ExceptionClassRetryPolicy mergeRetryOnWithExisting(ExceptionClassRetryPolicy topTarget, Class<? extends Throwable> retryOnThrowable) {
		return new ExceptionClassRetryPolicy(
				topTarget.target,
				setPlusElem(topTarget.retryOn, retryOnThrowable),
				topTarget.abortOn
		);
	}

	public static ExceptionClassRetryPolicy abortOn(RetryPolicy target, Class<? extends Throwable> abortOnThrowable) {
		if (target instanceof ExceptionClassRetryPolicy) {
			return mergeAbortOnWithExisting((ExceptionClassRetryPolicy) target, abortOnThrowable);
		}
		return new ExceptionClassRetryPolicy(target, emptySet(), Collections.<Class<? extends Throwable>>singleton(abortOnThrowable));
	}

	private static ExceptionClassRetryPolicy mergeAbortOnWithExisting(ExceptionClassRetryPolicy topTarget, Class<? extends Throwable> abortOnThrowable) {
		return new ExceptionClassRetryPolicy(
				topTarget.target,
				topTarget.retryOn,
				setPlusElem(topTarget.abortOn, abortOnThrowable)
		);
	}

	private static <T> Set<T> setPlusElem(Set<T> initial, T newElement) {
		final HashSet<T> copy = new HashSet<>(initial);
		copy.add(Objects.requireNonNull(newElement));
		return Collections.unmodifiableSet(copy);
	}

	@Override
	public boolean shouldContinue(RetryContext context) {
		if(!target.shouldContinue(context)) {
			return false;
		}
		final Class<? extends Throwable > e = context.getLastThrowable().getClass();
		if (abortOn.isEmpty()) {
			return matches(e, retryOn);
		} else {
			return !matches(e, abortOn) && matches(e, retryOn);
		}
	}

	private static boolean matches(Class<? extends Throwable> throwable, Set<Class<? extends Throwable>> set) {
		return set.isEmpty() || set.stream().anyMatch(c -> c.isAssignableFrom(throwable));
	}
}
