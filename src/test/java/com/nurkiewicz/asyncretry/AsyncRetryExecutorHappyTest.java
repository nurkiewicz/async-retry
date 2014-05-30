package com.nurkiewicz.asyncretry;

import org.testng.annotations.Test;

import java.util.concurrent.CompletableFuture;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/16/13, 10:51 PM
 */
public class AsyncRetryExecutorHappyTest extends AbstractBaseTestCase {

	@Test
	public void shouldNotRetryIfCompletesAfterFirstExecution() throws Exception {
		//given
		final RetryExecutor executor = new AsyncRetryExecutor(schedulerMock);

		//when
		executor.doWithRetry(ctx -> serviceMock.alwaysSucceeds());

		//then
		verify(schedulerMock).schedule(notNullRunnable(), eq(0L), millis());
		verifyNoMoreInteractions(schedulerMock);
	}

	@Test
	public void shouldCallUserTaskOnlyOnceIfItDoesntFail() throws Exception {
		//given
		final RetryExecutor executor = new AsyncRetryExecutor(schedulerMock);

		//when
		executor.doWithRetry(ctx -> serviceMock.alwaysSucceeds());

		//then
		verify(serviceMock).alwaysSucceeds();
	}

	@Test
	public void shouldReturnResultOfFirstSuccessfulCall() throws Exception {
		//given
		final RetryExecutor executor = new AsyncRetryExecutor(schedulerMock);
		given(serviceMock.alwaysSucceeds()).willReturn(42);

		//when
		final CompletableFuture<Integer> future = executor.getWithRetry(serviceMock::alwaysSucceeds);

		//then
		assertThat(future.get()).isEqualTo(42);
	}

	@Test
	public void shouldReturnEvenIfNoRetryPolicy() throws Exception {
		//given
		final RetryExecutor executor = new AsyncRetryExecutor(schedulerMock).dontRetry();
		given(serviceMock.alwaysSucceeds()).willReturn(42);

		//when
		final CompletableFuture<Integer> future = executor.getWithRetry(serviceMock::alwaysSucceeds);

		//then
		assertThat(future.get()).isEqualTo(42);
	}

}
