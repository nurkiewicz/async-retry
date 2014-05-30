package com.nurkiewicz.asyncretry.backoff;

import com.nurkiewicz.asyncretry.AbstractBaseTestCase;
import org.testng.annotations.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/20/13, 6:32 PM
 */
public class BoundedMinBackoffTest extends AbstractBaseTestCase {

	@Test
	public void shouldReturnOriginalBackoffDelayIfAboveMin() throws Exception {
		final Backoff backoff = new ExponentialDelayBackoff(1000, 2.0).withMinDelay();

		assertThat(backoff.delayMillis(retry(1))).isEqualTo(1000);
		assertThat(backoff.delayMillis(retry(2))).isEqualTo(2000);
		assertThat(backoff.delayMillis(retry(3))).isEqualTo(4000);
		assertThat(backoff.delayMillis(retry(4))).isEqualTo(8000);
	}

	@Test
	public void shouldCapBackoffAtDefaultLevel() throws Exception {
		final Backoff backoff = new ExponentialDelayBackoff(1, 2.0).withMinDelay();

		assertThat(backoff.delayMillis(retry(1))).isEqualTo(BoundedMinBackoff.DEFAULT_MIN_DELAY_MILLIS);
	}

	@Test
	public void shouldCapBackoffAtGivenLevel() throws Exception {
		final Backoff backoff = new ExponentialDelayBackoff(1, 2.0).withMaxDelay(250);

		assertThat(backoff.delayMillis(retry(100))).isEqualTo(250);
	}

	@Test
	public void shouldApplyBothMinAndMaxBound() throws Exception {
		final Backoff backoff = new ExponentialDelayBackoff(1, 2.0).
				withMinDelay(5).
				withMaxDelay(10);

		assertThat(backoff.delayMillis(retry(2))).isEqualTo(5);
		assertThat(backoff.delayMillis(retry(3))).isEqualTo(5);
		assertThat(backoff.delayMillis(retry(4))).isEqualTo(8);
		assertThat(backoff.delayMillis(retry(5))).isEqualTo(10);
	}

}
