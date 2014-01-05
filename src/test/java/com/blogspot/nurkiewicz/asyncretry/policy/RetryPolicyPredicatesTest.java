package com.blogspot.nurkiewicz.asyncretry.policy;

import com.blogspot.nurkiewicz.asyncretry.AsyncRetryContext;
import com.blogspot.nurkiewicz.asyncretry.RetryContext;
import org.mockito.Mock;
import org.testng.annotations.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/20/13, 5:17 PM
 */
public class RetryPolicyPredicatesTest extends AbstractRetryPolicyTest {

	@Mock
	private RetryContext retryContextMock;

	@Test
	public void shouldAbortIfAbortPredicateTrue() throws Exception {
		//given
		final RetryPolicy retryPolicy = new RetryPolicy().abortIf(t -> true);

		//when
		final boolean shouldRetry = retryPolicy.shouldContinue(retryContextMock);

		//then
		assertThat(shouldRetry).isFalse();
	}

	@Test
	public void shouldRetryIfRetryPredicateTrue() throws Exception {
		//given
		final RetryPolicy retryPolicy = new RetryPolicy().retryIf(t -> true);

		//when
		final boolean shouldRetry = retryPolicy.shouldContinue(retryContextMock);

		//then
		assertThat(shouldRetry).isTrue();
	}

	@Test
	public void shouldRetryIfBothPredicatesAbstainButClassShouldRetry() throws Exception {
		//given
		final RetryPolicy retryPolicy = new RetryPolicy().
				retryIf(t -> false).
				abortIf(t -> false);
		given(retryContextMock.getLastThrowable()).willReturn(new RuntimeException());

		//when
		final boolean shouldRetry = retryPolicy.shouldContinue(retryContextMock);

		//then
		assertThat(shouldRetry).isTrue();
	}

	@Test
	public void shouldAbortIfBothPredicatesAbstainButClassShouldAbort() throws Exception {
		//given
		final RetryPolicy retryPolicy = new RetryPolicy().
				abortOn(NullPointerException.class).
				retryIf(t -> false).
				abortIf(t -> false);
		given(retryContextMock.getLastThrowable()).willReturn(new NullPointerException());

		//when
		final boolean shouldRetry = retryPolicy.shouldContinue(retryContextMock);

		//then
		assertThat(shouldRetry).isFalse();
	}

	@Test
	public void shouldRetryIfPredicateTrueEvenIfClassShouldAbort() throws Exception {
		//given
		final RetryPolicy retryPolicy = new RetryPolicy().
				abortOn(NullPointerException.class).
				retryIf(t -> true);
		given(retryContextMock.getLastThrowable()).willReturn(new NullPointerException());

		//when
		final boolean shouldRetry = retryPolicy.shouldContinue(retryContextMock);

		//then
		assertThat(shouldRetry).isTrue();
	}

	@Test
	public void shouldAbortIfPredicateTrueEvenIfClassShouldRetry() throws Exception {
		//given
		final RetryPolicy retryPolicy = new RetryPolicy().
				retryOn(NullPointerException.class).
				abortIf(t -> true);
		given(retryContextMock.getLastThrowable()).willReturn(new NullPointerException());

		//when
		final boolean shouldRetry = retryPolicy.shouldContinue(retryContextMock);

		//then
		assertThat(shouldRetry).isFalse();
	}

	@Test
	public void whenAbortAndRetryPredicatesBothYieldTrueThenAbortWins() throws Exception {
		//given
		final RetryPolicy retryPolicy = new RetryPolicy().
				retryOn(NullPointerException.class).
				retryIf(t -> t.getMessage().contains("Foo")).
				abortIf(t -> t.getMessage().contains("Foo"));
		given(retryContextMock.getLastThrowable()).willReturn(new NullPointerException("Foo"));

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
	public void shouldAbortIfPredicateTrueButShouldNotRetry() throws Exception {
		//given
		final RetryPolicy retryPolicy = new RetryPolicy().
				retryIf(t -> true).
				dontRetry();
		given(retryContextMock.getLastThrowable()).willReturn(new NullPointerException());
		given(retryContextMock.getRetryCount()).willReturn(1);

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
