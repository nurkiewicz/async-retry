package com.nurkiewicz.asyncretry;

import com.nurkiewicz.asyncretry.policy.AbortRetryException;
import org.assertj.core.api.Assertions;
import org.mockito.InOrder;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.nurkiewicz.asyncretry.backoff.FixedIntervalBackoff.DEFAULT_PERIOD_MILLIS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.failBecauseExceptionWasNotThrown;
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
		given(serviceMock.safeAsync()).willReturn(CompletableFuture.completedFuture("42"));

		//when
		final CompletableFuture<String> future = executor.getFutureWithRetry(ctx -> serviceMock.safeAsync());

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
				CompletableFuture.completedFuture("42")
		);

		//when
		final CompletableFuture<String> future = executor.getFutureWithRetry(ctx -> serviceMock.safeAsync());

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
				CompletableFuture.completedFuture("42")
		);

		//when
		final CompletableFuture<String> future = executor.getFutureWithRetry(ctx -> serviceMock.safeAsync());

		//then
		future.get();

		final InOrder order = inOrder(schedulerMock);
		order.verify(schedulerMock).schedule(notNullRunnable(), eq(0L), millis());
		order.verify(schedulerMock, times(2)).schedule(notNullRunnable(), eq(DEFAULT_PERIOD_MILLIS), millis());
	}

	private CompletableFuture<String> failedAsync(Throwable throwable) {
		final CompletableFuture<String> future = new CompletableFuture<>();
		future.completeExceptionally(throwable);
		return future;
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
		final CompletableFuture<String> future = executor.getFutureWithRetry(ctx -> serviceMock.safeAsync());

		//then
		assertThat(future.isCompletedExceptionally()).isTrue();

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
	public void shouldRethrowOriginalExceptionFromUserFutureCompletionAndAbortWhenTestFails() throws Exception {
		//given
		final RetryExecutor executor = new AsyncRetryExecutor(schedulerMock).
				abortIf(t -> { throw new RuntimeException("test invalid"); });

		given(serviceMock.safeAsync()).willReturn(
				failedAsync(new SocketException(DON_T_PANIC))
		);

		//when
		final CompletableFuture<String> future = executor.getFutureWithRetry(ctx -> serviceMock.safeAsync());

		//then
		assertThat(future.isCompletedExceptionally()).isTrue();

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
		final CompletableFuture<String> future = executor.getFutureWithRetry(ctx -> serviceMock.safeAsync());

		//then
		assertThat(future.isCompletedExceptionally()).isTrue();

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
		final CompletableFuture<String> future = executor.getFutureWithRetry(ctx -> serviceMock.safeAsync());

		//then
		assertThat(future.isCompletedExceptionally()).isTrue();

		try {
			future.get();
			Assertions.failBecauseExceptionWasNotThrown(ExecutionException.class);
		} catch (ExecutionException e) {
			assertThat(e.getCause()).isInstanceOf(IllegalArgumentException.class);
			assertThat(e.getCause()).hasMessage(DON_T_PANIC);
		}
	}

}
