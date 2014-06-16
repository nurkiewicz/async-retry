package com.nurkiewicz.asyncretry.backoff;

import com.nurkiewicz.asyncretry.AbstractBaseTestCase;
import org.mockito.Mock;
import org.testng.annotations.Test;

import java.util.Random;

import static com.nurkiewicz.asyncretry.backoff.FixedIntervalBackoff.DEFAULT_PERIOD_MILLIS;
import static com.nurkiewicz.asyncretry.backoff.UniformRandomBackoff.DEFAULT_RANDOM_RANGE_MILLIS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/20/13, 6:57 PM
 */
public class RandomBackoffTest extends AbstractBaseTestCase {

	@Mock
	private Random randomMock;

	@Test
	public void shouldApplyRandomUniformDistributionWithDefaultRange() throws Exception {
		//given
		final Backoff backoff = new FixedIntervalBackoff().withUniformJitter();

		//when
		final long delay = backoff.delayMillis(anyRetry());

		//then
		assertThat(delay).
				isGreaterThanOrEqualTo(DEFAULT_PERIOD_MILLIS - DEFAULT_RANDOM_RANGE_MILLIS).
				isLessThanOrEqualTo(DEFAULT_PERIOD_MILLIS + DEFAULT_RANDOM_RANGE_MILLIS);
	}

	@Test
	public void shouldApplyRandomUniformDistribution() throws Exception {
		//given
		final int range = 300;
		final Backoff backoff = new FixedIntervalBackoff().withUniformJitter(range);

		//when
		final long delay = backoff.delayMillis(anyRetry());

		//then
		assertThat(delay).
				isGreaterThanOrEqualTo(DEFAULT_PERIOD_MILLIS - range).
				isLessThanOrEqualTo(DEFAULT_PERIOD_MILLIS + range);
	}

	@Test
	public void shouldApplyRandomUniformDistributionWithCustomRandomSource() throws Exception {
		//given
		final Backoff backoff = new UniformRandomBackoff(new FixedIntervalBackoff(), randomMock);
		given(randomMock.nextDouble()).willReturn(0.5);

		//when
		final long delay = backoff.delayMillis(anyRetry());

		//then
		assertThat(delay).isEqualTo(DEFAULT_PERIOD_MILLIS);
	}

	@Test
	public void shouldApplyRandomProportionalDistributionWithDefaultRange() throws Exception {
		//given
		final Backoff backoff = new FixedIntervalBackoff().withProportionalJitter();

		//when
		final long delay = backoff.delayMillis(anyRetry());

		//then
		assertThat(delay).
				isGreaterThanOrEqualTo((long) (DEFAULT_PERIOD_MILLIS * (1 - ProportionalRandomBackoff.DEFAULT_MULTIPLIER))).
				isLessThan((long) (DEFAULT_PERIOD_MILLIS * (1 + ProportionalRandomBackoff.DEFAULT_MULTIPLIER)));
	}

	@Test
	public void shouldApplyRandomProportionalDistribution() throws Exception {
		//given
		final double range = 0.3;
		final Backoff backoff = new FixedIntervalBackoff().withProportionalJitter(range);

		//when
		final long delay = backoff.delayMillis(anyRetry());

		//then
		assertThat(delay).
				isGreaterThanOrEqualTo((long) (DEFAULT_PERIOD_MILLIS * (1 - range))).
				isLessThan((long) (DEFAULT_PERIOD_MILLIS * (1 + range)));
	}

	@Test
	public void shouldApplyRandomProportionalDistributionWithCustomRandomSource() throws Exception {
		//given
		final Backoff backoff = new ProportionalRandomBackoff(new FixedIntervalBackoff(), randomMock);
		given(randomMock.nextDouble()).willReturn(0.5);

		//when
		final long delay = backoff.delayMillis(anyRetry());

		//then
		assertThat(delay).isEqualTo(DEFAULT_PERIOD_MILLIS);
	}

}
