package com.blogspot.nurkiewicz.asyncretry.backoff;

import com.blogspot.nurkiewicz.asyncretry.AbstractBaseTestCase;
import org.testng.annotations.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/20/13, 5:44 PM
 */
public class BoundedMaxBackoffTest extends AbstractBaseTestCase {

	@Test
	public void shouldReturnOriginalBackoffDelayIfBelowMax() throws Exception {
		final Backoff backoff = new ExponentialDelayBackoff(1, 2.0).withMaxDelay();

		assertThat(backoff.delayMillis(retry(1))).isEqualTo(1);
		assertThat(backoff.delayMillis(retry(2))).isEqualTo(2);
		assertThat(backoff.delayMillis(retry(3))).isEqualTo(4);
		assertThat(backoff.delayMillis(retry(4))).isEqualTo(8);
	}

	@Test
	public void shouldCapBackoffAtDefaultLevel() throws Exception {
		final Backoff backoff = new ExponentialDelayBackoff(1, 2.0).withMaxDelay();

		assertThat(backoff.delayMillis(retry(100))).isEqualTo(BoundedMaxBackoff.DEFAULT_MAX_DELAY_MILLIS);
	}

}
