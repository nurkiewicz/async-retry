package com.blogspot.nurkiewicz.asyncretry.policy;

import com.blogspot.nurkiewicz.asyncretry.AbstractBaseTestCase;
import org.testng.annotations.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/20/13, 7:16 PM
 */
public class MaxRetriesPolicyTest extends AbstractBaseTestCase {

	@Test
	public void shouldStopAfterConfiguredNumberOfRetries() throws Exception {
		//given
		final RetryPolicy retryPolicy = new RetryPolicy().withMaxRetries(7);

		//when
		final boolean firstRetry = retryPolicy.shouldContinue(retry(1));
		final boolean lastRetry = retryPolicy.shouldContinue(retry(7));
		final boolean tooManyRetries = retryPolicy.shouldContinue(retry(8));

		//then
		assertThat(firstRetry).isTrue();
		assertThat(lastRetry).isTrue();
		assertThat(tooManyRetries).isFalse();
	}

}
