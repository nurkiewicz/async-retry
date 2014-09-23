package com.nurkiewicz.asyncretry.backoff;

import com.google.common.base.Throwables;
import com.nurkiewicz.asyncretry.RetryContext;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/16/13, 7:30 PM
 */
abstract public class RandomBackoff extends BackoffWrapper {

	private final Callable<Random> randomSource;

	protected RandomBackoff(Backoff target) {
		this(target, new Callable<Random>() {
			@Override
			public Random call() throws Exception {
				return ThreadLocalRandom.current();
			}
		});
	}

	protected RandomBackoff(Backoff target, final Random randomSource) {
		this(target, new Callable<Random>() {
			@Override
			public Random call() throws Exception {
				return randomSource;
			}
		});
	}

	private RandomBackoff(Backoff target, Callable<Random> randomSource) {
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
		try {
			return randomSource.call();
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}
}
