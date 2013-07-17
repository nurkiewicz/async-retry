package com.blogspot.nurkiewicz.asyncretry.backoff;

import com.blogspot.nurkiewicz.asyncretry.RetryContext;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/16/13, 7:30 PM
 */
abstract public class RandomDelayBackoff extends BackoffWrapper {

	private final Supplier<Random> randomSource;

	protected RandomDelayBackoff(Backoff target) {
		this(target, ThreadLocalRandom::current);
	}

	protected RandomDelayBackoff(Backoff target, Random randomSource) {
		this(target, () -> randomSource);
	}

	private RandomDelayBackoff(Backoff target, Supplier<Random> randomSource) {
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
