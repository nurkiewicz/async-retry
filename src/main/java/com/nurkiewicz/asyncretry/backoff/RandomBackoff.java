package com.nurkiewicz.asyncretry.backoff;

import com.nurkiewicz.asyncretry.RetryContext;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/16/13, 7:30 PM
 */
abstract public class RandomBackoff extends BackoffWrapper {

	private final Supplier<Random> randomSource;

	protected RandomBackoff(Backoff target) {
		this(target, ThreadLocalRandom::current);
	}

	protected RandomBackoff(Backoff target, Random randomSource) {
		this(target, () -> randomSource);
	}

	private RandomBackoff(Backoff target, Supplier<Random> randomSource) {
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
