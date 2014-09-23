package com.nurkiewicz.asyncretry;

import com.google.common.util.concurrent.ListenableFuture;
import com.nurkiewicz.asyncretry.function.RetryCallable;
import com.nurkiewicz.asyncretry.function.RetryRunnable;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
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
		executor.doWithRetry(new RetryRunnable() {
			@Override
			public void run(RetryContext context) throws Exception {
				serviceMock.alwaysSucceeds();
			}
		});

		//then
		verify(schedulerMock).schedule(notNullRunnable(), eq(0L), millis());
		verifyNoMoreInteractions(schedulerMock);
	}

	@Test
	public void shouldCallUserTaskOnlyOnceIfItDoesntFail() throws Exception {
		//given
		final RetryExecutor executor = new AsyncRetryExecutor(schedulerMock);

		//when
		executor.doWithRetry(new RetryRunnable() {
			@Override
			public void run(RetryContext context) throws Exception {
				serviceMock.alwaysSucceeds();
			}
		});

		//then
		verify(serviceMock).alwaysSucceeds();
	}

	@Test
	public void shouldReturnResultOfFirstSuccessfulCall() throws Exception {
		//given
		final RetryExecutor executor = new AsyncRetryExecutor(schedulerMock);
		given(serviceMock.alwaysSucceeds()).willReturn(42);

		//when
		final ListenableFuture<Integer> future = executor.getWithRetry(new RetryCallable<Integer>() {
			@Override
			public Integer call(RetryContext context) throws Exception {
				return serviceMock.alwaysSucceeds();
			}
		});

		//then
		assertThat(future.get()).isEqualTo(42);
	}

	@Test
	public void shouldReturnEvenIfNoRetryPolicy() throws Exception {
		//given
		final RetryExecutor executor = new AsyncRetryExecutor(schedulerMock).dontRetry();
		given(serviceMock.alwaysSucceeds()).willReturn(42);

		//when
		final ListenableFuture<Integer> future = executor.getWithRetry(new RetryCallable<Integer>() {
			@Override
			public Integer call(RetryContext context) throws Exception {
				return serviceMock.alwaysSucceeds();
			}
		});

		//then
		assertThat(future.get()).isEqualTo(42);
	}

}
