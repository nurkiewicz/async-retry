package com.blogspot.nurkiewicz.asyncretry.policy.exception;

import com.blogspot.nurkiewicz.asyncretry.RetryContext;
import com.blogspot.nurkiewicz.asyncretry.policy.RetryPolicy;
import org.mockito.Mock;
import org.testng.annotations.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/20/13, 5:17 PM
 */
public class AbortPredicateRetryPolicyTest extends AbstractExceptionClassRetryPolicyTest {

	@Mock
	private RetryContext retryContextMock;

	@Mock
	private RetryPolicy retryPolicyMock;

	@Test
	public void shouldAbortIfPredicateTrue() throws Exception {
		//given
		final RetryPolicy retryPolicy = always.abortIf(t -> true);

		//when
		final boolean shouldRetry = retryPolicy.shouldContinue(retryContextMock);

		//then
		assertThat(shouldRetry).isFalse();
	}

	@Test
	public void shouldProceedIfPredicateFalseAndChildAccepts() throws Exception {
		//given
		final RetryPolicy retryPolicy = new AbortPredicateRetryPolicy(retryPolicyMock, t -> false);
		given(retryPolicyMock.shouldContinue(notNullRetryContext())).willReturn(true);


		//when
		final boolean shouldRetry = retryPolicy.shouldContinue(retryContextMock);

		//then
		assertThat(shouldRetry).isTrue();
	}

	@Test
	public void shouldAbortIfPredicateFalseAndChildAborts() throws Exception {
		//given
		final RetryPolicy retryPolicy = new AbortPredicateRetryPolicy(retryPolicyMock, t -> false);
		given(retryPolicyMock.shouldContinue(notNullRetryContext())).willReturn(false);


		//when
		final boolean shouldRetry = retryPolicy.shouldContinue(retryContextMock);

		//then
		assertThat(shouldRetry).isFalse();
	}

}
