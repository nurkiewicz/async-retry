package com.blogspot.nurkiewicz.asyncretry.policy.exception;

import com.blogspot.nurkiewicz.asyncretry.AsyncRetryContext;
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

	@Test
	public void shouldAbortIfPredicateTrue() throws Exception {
		//given
		final RetryPolicy retryPolicy = new RetryPolicy().abortIf(t -> true);

		//when
		final boolean shouldRetry = retryPolicy.shouldContinue(retryContextMock);

		//then
		assertThat(shouldRetry).isFalse();
	}

	@Test
	public void shouldProceedIfPredicateFalseAndChildAccepts() throws Exception {
		//given
		final RetryPolicy retryPolicy = new RetryPolicy().abortIf(t -> false);
		given(retryContextMock.getLastThrowable()).willReturn(new RuntimeException());

		//when
		final boolean shouldRetry = retryPolicy.shouldContinue(retryContextMock);

		//then
		assertThat(shouldRetry).isTrue();
	}

	@Test
	public void shouldAbortIfPredicateFalseButShouldNotRetry() throws Exception {
		//given
		final RetryPolicy retryPolicy = new RetryPolicy().abortIf(t -> false).dontRetry();

		//when
		final boolean shouldRetry = retryPolicy.shouldContinue(retryContextMock);

		//then
		assertThat(shouldRetry).isFalse();
	}

	@Test
	public void shouldExamineExceptionAndDecide() throws Exception {
		//given
		final RetryPolicy retryPolicy = new RetryPolicy().abortIf(t -> t.getMessage().contains("abort"));

		//when
		final boolean abort = retryPolicy.shouldContinue(new AsyncRetryContext(retryPolicy, 1, new RuntimeException("abort")));
		final boolean retry = retryPolicy.shouldContinue(new AsyncRetryContext(retryPolicy, 1, new RuntimeException("normal")));

		//then
		assertThat(abort).isFalse();
		assertThat(retry).isTrue();
	}

}
