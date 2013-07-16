package com.blogspot.nurkiewicz.asyncretry.policy.random;

import com.blogspot.nurkiewicz.asyncretry.RetryContext;
import com.blogspot.nurkiewicz.asyncretry.policy.RetryPolicy;
import com.blogspot.nurkiewicz.asyncretry.policy.RetryPolicyWrapper;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/16/13, 7:30 PM
 */
abstract public class RandomDelayJitterRetryPolicy extends RetryPolicyWrapper {

	private final Supplier<Random> randomSource;

	protected RandomDelayJitterRetryPolicy(RetryPolicy target) {
		this(target, ThreadLocalRandom::current);
	}

	protected RandomDelayJitterRetryPolicy(RetryPolicy target, Random randomSource) {
		this(target, () -> randomSource);
	}

	private RandomDelayJitterRetryPolicy(RetryPolicy target, Supplier<Random> randomSource) {
		super(target);
		this.randomSource = randomSource;
	}

	@Override
	public long delayMillis(RetryContext context) {
		final long initialDelay = target.delayMillis(context);
		final long randomDelay = addRandomJitter(initialDelay);
		return Math.max(randomDelay, 0);
	}

	abstract long addRandomJitter(long initialDelay);

	protected Random random() {
		return randomSource.get();
	}
}
