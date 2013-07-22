package com.blogspot.nurkiewicz.asyncretry.policy.exception;

import com.blogspot.nurkiewicz.asyncretry.RetryContext;
import com.blogspot.nurkiewicz.asyncretry.policy.RetryPolicy;
import com.blogspot.nurkiewicz.asyncretry.policy.RetryPolicyWrapper;
import com.google.common.collect.Sets;

import java.util.*;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/16/13, 8:47 PM
 */
public class ExceptionClassRetryPolicy extends RetryPolicyWrapper {

	private final Set<Class<? extends Throwable>> retryOn;
	private final Set<Class<? extends Throwable>> abortOn;

	public ExceptionClassRetryPolicy(RetryPolicy target) {
		this(target, Collections.<Class<? extends Throwable>>emptySet(), Collections.<Class<? extends Throwable>>emptySet());
	}

	public ExceptionClassRetryPolicy(RetryPolicy target, Set<Class<? extends Throwable>> retryOn, Set<Class<? extends Throwable>> abortOn) {
		super(target);
		this.retryOn = retryOn;
		this.abortOn = abortOn;
	}

	@SafeVarargs
	public static ExceptionClassRetryPolicy retryOn(RetryPolicy target, Class<? extends Throwable>... retryOnThrowable) {
		if (target instanceof ExceptionClassRetryPolicy) {
			return mergeRetryOnWithExisting((ExceptionClassRetryPolicy) target, Sets.newHashSet(retryOnThrowable));
		}
		return new ExceptionClassRetryPolicy(target, Sets.newHashSet(retryOnThrowable), Collections.<Class<? extends Throwable>>emptySet());
	}

	private static ExceptionClassRetryPolicy mergeRetryOnWithExisting(ExceptionClassRetryPolicy topTarget, Set<Class<? extends Throwable>> retryOnThrowables) {
		return new ExceptionClassRetryPolicy(
				topTarget.target,
				setPlusElems(topTarget.retryOn, retryOnThrowables),
				topTarget.abortOn
		);
	}

	@SafeVarargs
	public static ExceptionClassRetryPolicy abortOn(RetryPolicy target, Class<? extends Throwable>... abortOnThrowables) {
		if (target instanceof ExceptionClassRetryPolicy) {
			return mergeAbortOnWithExisting((ExceptionClassRetryPolicy) target, Sets.newHashSet(abortOnThrowables));
		}
		return new ExceptionClassRetryPolicy(target, Collections.<Class<? extends Throwable>>emptySet(), Sets.newHashSet(abortOnThrowables));
	}

	private static ExceptionClassRetryPolicy mergeAbortOnWithExisting(ExceptionClassRetryPolicy topTarget, Set<Class<? extends Throwable>> abortOnThrowables) {
		return new ExceptionClassRetryPolicy(
				topTarget.target,
				topTarget.retryOn,
				setPlusElems(topTarget.abortOn, abortOnThrowables)
		);
	}

	private static <T> Set<T> setPlusElems(Set<T> initial, Collection<T> newElements) {
		final HashSet<T> copy = new HashSet<>(initial);
		copy.addAll(Objects.requireNonNull(newElements));
		return Collections.unmodifiableSet(copy);
	}

	@Override
	public boolean shouldContinue(RetryContext context) {
		if (!target.shouldContinue(context)) {
			return false;
		}
		final Class<? extends Throwable> e = context.getLastThrowable().getClass();
		if (abortOn.isEmpty()) {
			return matches(e, retryOn);
		} else {
			return !matches(e, abortOn) && matches(e, retryOn);
		}
	}

	private static boolean matches(Class<? extends Throwable> throwable, Set<Class<? extends Throwable>> set) {
		if (set.isEmpty()) {
			return true;
		}
		for (Class<? extends Throwable> candidate : set) {
			if (candidate.isAssignableFrom(throwable)) {
				return true;
			}
		}
		return false;
	}
}
