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

	private final Set<Class<? extends Throwable>> retryFor;
	private final Set<Class<? extends Throwable>> abortFor;

	public ExceptionClassRetryPolicy(RetryPolicy target) {
		this(target, Collections.emptySet(), Collections.emptySet());
	}

	public ExceptionClassRetryPolicy(RetryPolicy target, Set<Class<? extends Throwable>> retryFor, Set<Class<? extends Throwable>> abortFor) {
		super(target);
		this.retryFor = retryFor;
		this.abortFor = abortFor;
	}

	public static ExceptionClassRetryPolicy retryFor(RetryPolicy target, Class<? extends Throwable> retryForThrowable) {
		if (target instanceof ExceptionClassRetryPolicy) {
			return mergeRetryForWithExisting((ExceptionClassRetryPolicy) target, retryForThrowable);
		}
		return new ExceptionClassRetryPolicy(target, Collections.<Class<? extends Throwable>>singleton(retryForThrowable), emptySet());
	}

	private static ExceptionClassRetryPolicy mergeRetryForWithExisting(ExceptionClassRetryPolicy topTarget, Class<? extends Throwable> retryForThrowable) {
		return new ExceptionClassRetryPolicy(
				topTarget.target,
				setPlusElem(topTarget.retryFor, retryForThrowable),
				topTarget.abortFor
		);
	}

	public static ExceptionClassRetryPolicy abortFor(RetryPolicy target, Class<? extends Throwable> abortForThrowable) {
		if (target instanceof ExceptionClassRetryPolicy) {
			return mergeAbortForWithExisting((ExceptionClassRetryPolicy) target, abortForThrowable);
		}
		return new ExceptionClassRetryPolicy(target, emptySet(), Collections.<Class<? extends Throwable>>singleton(abortForThrowable));
	}

	private static ExceptionClassRetryPolicy mergeAbortForWithExisting(ExceptionClassRetryPolicy topTarget, Class<? extends Throwable> abortForThrowable) {
		return new ExceptionClassRetryPolicy(
				topTarget.target,
				topTarget.retryFor,
				setPlusElem(topTarget.abortFor, abortForThrowable)
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
		if (abortFor.isEmpty()) {
			return matches(e, retryFor);
		} else {
			return !matches(e, abortFor) && matches(e, retryFor);
		}
	}

	private static boolean matches(Class<? extends Throwable> throwable, Set<Class<? extends Throwable>> set) {
		return set.isEmpty() || set.stream().anyMatch(c -> c.isAssignableFrom(throwable));
	}
}
