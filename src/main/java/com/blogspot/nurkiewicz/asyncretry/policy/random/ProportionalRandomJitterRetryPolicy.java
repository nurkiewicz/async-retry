package com.blogspot.nurkiewicz.asyncretry.policy.random;

import com.blogspot.nurkiewicz.asyncretry.policy.RetryPolicy;

import java.util.Random;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/16/13, 7:49 PM
 */
public class ProportionalRandomJitterRetryPolicy extends RandomDelayJitterRetryPolicy {

	/**
	 * Randomly up to +/- 10%
	 */
	public static final double DEFAULT_MULTIPLIER = 0.1;

	private final double multiplier;

	public ProportionalRandomJitterRetryPolicy(RetryPolicy target) {
		this(target, DEFAULT_MULTIPLIER);
	}

	public ProportionalRandomJitterRetryPolicy(RetryPolicy target, Random random) {
		this(target, DEFAULT_MULTIPLIER, random);
	}

	public ProportionalRandomJitterRetryPolicy(RetryPolicy target, double multiplier) {
		super(target);
		this.multiplier = multiplier;
	}

	public ProportionalRandomJitterRetryPolicy(RetryPolicy target, double multiplier, Random random) {
		super(target, random);
		this.multiplier = multiplier;
	}

	@Override
	long addRandomJitter(long initialDelay) {
		final double randomMultiplier = (1 - 2 * random().nextDouble()) * multiplier;
		return (long) (initialDelay * randomMultiplier);
	}
}
