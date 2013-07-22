package com.blogspot.nurkiewicz.asyncretry;

import com.blogspot.nurkiewicz.asyncretry.function.RetryCallable;
import com.blogspot.nurkiewicz.asyncretry.policy.exception.AbortRetryException;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.fest.assertions.api.Assertions;
import org.mockito.InOrder;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.ExecutionException;

import static com.blogspot.nurkiewicz.asyncretry.backoff.FixedIntervalBackoff.DEFAULT_PERIOD_MILLIS;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Fail.failBecauseExceptionWasNotThrown;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/21/13, 7:07 PM
 */
public class AsyncRetryJobTest extends AbstractBaseTestCase {

	@Test
	public void shouldUnwrapUserFutureAndReturnIt() throws Exception {
		//given
		final RetryExecutor executor = new AsyncRetryExecutor(schedulerMock);
		given(serviceMock.safeAsync()).willReturn(Futures.immediateFuture("42"));

		//when
		final ListenableFuture<String> future = executor.getFutureWithRetry(new RetryCallable<ListenableFuture<String>>() {
			@Override
			public ListenableFuture<String> call(RetryContext context) throws Exception {
				return serviceMock.safeAsync();
			}
		});

		//then
		assertThat(future.get()).isEqualTo("42");
	}

	@Test
	public void shouldSucceedAfterFewAsynchronousRetries() throws Exception {
		//given
		final RetryExecutor executor = new AsyncRetryExecutor(schedulerMock);
		given(serviceMock.safeAsync()).willReturn(
				failedAsync(new SocketException("First")),
				failedAsync(new IOException("Second")),
				Futures.immediateFuture("42")
		);

		//when
		final ListenableFuture<String> future = executor.getFutureWithRetry(new RetryCallable<ListenableFuture<String>>() {
			@Override
			public ListenableFuture<String> call(RetryContext context) throws Exception {
				return serviceMock.safeAsync();
			}
		});

		//then
		assertThat(future.get()).isEqualTo("42");
	}

	@Test
	public void shouldScheduleTwoTimesWhenRetries() throws Exception {
		//given
		final RetryExecutor executor = new AsyncRetryExecutor(schedulerMock);
		given(serviceMock.safeAsync()).willReturn(
				failedAsync(new SocketException("First")),
				failedAsync(new IOException("Second")),
				Futures.immediateFuture("42")
		);

		//when
		final ListenableFuture<String> future = executor.getFutureWithRetry(new RetryCallable<ListenableFuture<String>>() {
			@Override
			public ListenableFuture<String> call(RetryContext context) throws Exception {
				return serviceMock.safeAsync();
			}
		});

		//then
		future.get();

		final InOrder order = inOrder(schedulerMock);
		order.verify(schedulerMock).schedule(notNullRunnable(), eq(0L), millis());
		order.verify(schedulerMock, times(2)).schedule(notNullRunnable(), eq(DEFAULT_PERIOD_MILLIS), millis());
	}

	private ListenableFuture<String> failedAsync(Throwable throwable) {
		return Futures.immediateFailedFuture(throwable);
	}

	@Test
	public void shouldRethrowOriginalExceptionFromUserFutureCompletion() throws Exception {
		//given
		final RetryExecutor executor = new AsyncRetryExecutor(schedulerMock).
				abortOn(SocketException.class);
		given(serviceMock.safeAsync()).willReturn(
				failedAsync(new SocketException(DON_T_PANIC))
		);

		//when
		final ListenableFuture<String> future = executor.getFutureWithRetry(new RetryCallable<ListenableFuture<String>>() {
			@Override
			public ListenableFuture<String> call(RetryContext context) throws Exception {
				return serviceMock.safeAsync();
			}
		});

		//then
		try {
			future.get();
			failBecauseExceptionWasNotThrown(ExecutionException.class);
		} catch (ExecutionException e) {
			final Throwable cause = e.getCause();
			assertThat(cause).isInstanceOf(SocketException.class);
			assertThat(cause).hasMessage(DON_T_PANIC);
		}
	}

	@Test
	public void shouldAbortWhenTargetFutureWantsToAbort() throws Exception {
		//given
		final RetryExecutor executor = new AsyncRetryExecutor(schedulerMock);
		given(serviceMock.safeAsync()).willReturn(
				failedAsync(new AbortRetryException())
		);

		//when
		final ListenableFuture<String> future = executor.getFutureWithRetry(new RetryCallable<ListenableFuture<String>>() {
			@Override
			public ListenableFuture<String> call(RetryContext context) throws Exception {
				return serviceMock.safeAsync();
			}
		});

		//then
		try {
			future.get();
			failBecauseExceptionWasNotThrown(ExecutionException.class);
		} catch (ExecutionException e) {
			final Throwable cause = e.getCause();
			assertThat(cause).isInstanceOf(AbortRetryException.class);
		}
	}

	@Test
	public void shouldRethrowExceptionThatWasThrownFromUserTaskBeforeReturningFuture() throws Exception {
		//given
		final RetryExecutor executor = new AsyncRetryExecutor(schedulerMock).
				abortOn(IllegalArgumentException.class);
		given(serviceMock.safeAsync()).willThrow(new IllegalArgumentException(DON_T_PANIC));

		//when
		final ListenableFuture<String> future = executor.getFutureWithRetry(new RetryCallable<ListenableFuture<String>>() {
			@Override
			public ListenableFuture<String> call(RetryContext context) throws Exception {
				return serviceMock.safeAsync();
			}
		});

		//then
		try {
			future.get();
			Assertions.failBecauseExceptionWasNotThrown(ExecutionException.class);
		} catch (ExecutionException e) {
			assertThat(e.getCause()).isInstanceOf(IllegalArgumentException.class);
			assertThat(e.getCause()).hasMessage(DON_T_PANIC);
		}
	}

}
