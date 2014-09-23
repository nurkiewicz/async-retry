package com.nurkiewicz.asyncretry.policy;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.nurkiewicz.asyncretry.AsyncRetryContext;
import com.nurkiewicz.asyncretry.RetryContext;
import org.mockito.Mock;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
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
		final RetryPolicy retryPolicy = new RetryPolicy().abortIf(Predicates.<Throwable>alwaysTrue());

		//when
		final boolean shouldRetry = retryPolicy.shouldContinue(retryContextMock);

		//then
		assertThat(shouldRetry).isFalse();
	}

	@Test
	public void shouldRetryIfRetryPredicateTrue() throws Exception {
		//given
		final RetryPolicy retryPolicy = new RetryPolicy().retryIf(Predicates.<Throwable>alwaysTrue());

		//when
		final boolean shouldRetry = retryPolicy.shouldContinue(retryContextMock);

		//then
		assertThat(shouldRetry).isTrue();
	}

	@Test
	public void shouldRetryIfBothPredicatesAbstainButClassShouldRetry() throws Exception {
		//given
		final RetryPolicy retryPolicy = new RetryPolicy().
				retryIf(Predicates.<Throwable>alwaysFalse()).
				abortIf(Predicates.<Throwable>alwaysFalse());
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
				retryIf(Predicates.<Throwable>alwaysFalse()).
				abortIf(Predicates.<Throwable>alwaysFalse());
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
				retryIf(Predicates.<Throwable>alwaysTrue());
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
				abortIf(Predicates.<Throwable>alwaysTrue());
		given(retryContextMock.getLastThrowable()).willReturn(new NullPointerException());

		//when
		final boolean shouldRetry = retryPolicy.shouldContinue(retryContextMock);

		//then
		assertThat(shouldRetry).isFalse();
	}

	@Test
	public void whenAbortAndRetryPredicatesBothYieldTrueThenAbortWins() throws Exception {
		//given
		final Predicate<Throwable> predicate = new Predicate<Throwable>() {
			@Override
			public boolean apply(Throwable t) {
				return t.getMessage().contains("Foo");
			}
		};

		final RetryPolicy retryPolicy = new RetryPolicy().
				retryOn(NullPointerException.class).
				retryIf(predicate).
				abortIf(predicate);
		given(retryContextMock.getLastThrowable()).willReturn(new NullPointerException("Foo"));

		//when
		final boolean shouldRetry = retryPolicy.shouldContinue(retryContextMock);

		//then
		assertThat(shouldRetry).isFalse();
	}

	@Test
	public void shouldProceedIfPredicateFalseAndChildAccepts() throws Exception {
		//given
		final RetryPolicy retryPolicy = new RetryPolicy().abortIf(Predicates.<Throwable>alwaysFalse());
		given(retryContextMock.getLastThrowable()).willReturn(new RuntimeException());

		//when
		final boolean shouldRetry = retryPolicy.shouldContinue(retryContextMock);

		//then
		assertThat(shouldRetry).isTrue();
	}

	@Test
	public void shouldAbortIfPredicateFalseButShouldNotRetry() throws Exception {
		//given
		final RetryPolicy retryPolicy = new RetryPolicy().abortIf(Predicates.<Throwable>alwaysFalse()).dontRetry();

		//when
		final boolean shouldRetry = retryPolicy.shouldContinue(retryContextMock);

		//then
		assertThat(shouldRetry).isFalse();
	}

	@Test
	public void shouldAbortIfPredicateTrueButShouldNotRetry() throws Exception {
		//given
		final RetryPolicy retryPolicy = new RetryPolicy().
				retryIf(Predicates.<Throwable>alwaysTrue()).
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
		final RetryPolicy retryPolicy = new RetryPolicy().abortIf(new Predicate<Throwable>() {
			@Override
			public boolean apply(Throwable t) {
				return t.getMessage().contains("abort");
			}
		});

		//when
		final boolean abort = retryPolicy.shouldContinue(new AsyncRetryContext(retryPolicy, 1, new RuntimeException("abort")));
		final boolean retry = retryPolicy.shouldContinue(new AsyncRetryContext(retryPolicy, 1, new RuntimeException("normal")));

		//then
		assertThat(abort).isFalse();
		assertThat(retry).isTrue();
	}

}
