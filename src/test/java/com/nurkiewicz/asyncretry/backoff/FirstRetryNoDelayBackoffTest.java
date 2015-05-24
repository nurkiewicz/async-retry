package com.nurkiewicz.asyncretry.backoff;

import com.nurkiewicz.asyncretry.AbstractBaseTestCase;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FirstRetryNoDelayBackoffTest extends AbstractBaseTestCase {

	@Test
	public void firstRetryShouldHaveNoDelay() {
		//given
		final Backoff backoff = new FixedIntervalBackoff(1_000).withFirstRetryNoDelay();

		//when
		final long first = backoff.delayMillis(retry(1));
		final long second = backoff.delayMillis(retry(2));
		final long third = backoff.delayMillis(retry(3));

		//then
		assertThat(first).isEqualTo(0);
		assertThat(second).isEqualTo(1_000);
		assertThat(third).isEqualTo(1_000);
	}

	@Test
	public void secondRetryShouldCalculateDelayAsIfItWasFirst() {
		//given
		final Backoff backoff = new ExponentialDelayBackoff(100, 2).withFirstRetryNoDelay();

		//when
		final long first = backoff.delayMillis(retry(1));
		final long second = backoff.delayMillis(retry(2));
		final long third = backoff.delayMillis(retry(3));

		//then
		assertThat(first).isEqualTo(0);
		assertThat(second).isEqualTo(100);
		assertThat(third).isEqualTo(200);
	}

}