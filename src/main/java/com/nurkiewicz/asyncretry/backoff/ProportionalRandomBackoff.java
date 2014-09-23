package com.nurkiewicz.asyncretry.backoff;

import java.util.Random;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/16/13, 7:49 PM
 */
public class ProportionalRandomBackoff extends RandomBackoff {

	/**
	 * Randomly up to +/- 10%
	 */
	public static final double DEFAULT_MULTIPLIER = 0.1;

	private final double multiplier;

	public ProportionalRandomBackoff(Backoff target) {
		this(target, DEFAULT_MULTIPLIER);
	}

	public ProportionalRandomBackoff(Backoff target, Random random) {
		this(target, DEFAULT_MULTIPLIER, random);
	}

	public ProportionalRandomBackoff(Backoff target, double multiplier) {
		super(target);
		this.multiplier = multiplier;
	}

	public ProportionalRandomBackoff(Backoff target, double multiplier, Random random) {
		super(target, random);
		this.multiplier = multiplier;
	}

	@Override
	long addRandomJitter(long initialDelay) {
		final double randomMultiplier = (1 - 2 * random().nextDouble()) * multiplier;
		return (long) (initialDelay * (1 + randomMultiplier));
	}
}
