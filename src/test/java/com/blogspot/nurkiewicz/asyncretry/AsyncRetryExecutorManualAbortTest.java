package com.blogspot.nurkiewicz.asyncretry;

import com.blogspot.nurkiewicz.asyncretry.function.RetryCallable;
import com.google.common.util.concurrent.ListenableFuture;
import org.fest.assertions.api.Assertions;
import org.mockito.InOrder;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import static com.blogspot.nurkiewicz.asyncretry.backoff.FixedIntervalBackoff.DEFAULT_PERIOD_MILLIS;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/16/13, 10:51 PM
 */
public class AsyncRetryExecutorManualAbortTest extends AbstractBaseTestCase {

	@Test
	public void shouldRethrowIfFirstExecutionThrowsAnExceptionAndNoRetry() throws Exception {
		//given
		final RetryExecutor executor = new AsyncRetryExecutor(schedulerMock).dontRetry();
		given(serviceMock.sometimesFails()).
				willThrow(new IllegalStateException(DON_T_PANIC));

		//when
		final ListenableFuture<String> future = executor.getWithRetry(new Callable<String>() {
			@Override
			public String call() throws Exception {
				return serviceMock.sometimesFails();
			}
		});

		//then
		try {
			future.get();
			Assertions.failBecauseExceptionWasNotThrown(IllegalStateException.class);
		} catch (ExecutionException e) {
			final Throwable actualCause = e.getCause();
			assertThat(actualCause).isInstanceOf(IllegalStateException.class);
			assertThat(actualCause.getMessage()).isEqualToIgnoringCase(DON_T_PANIC);
		}
	}

	@Test
	public void shouldRetryAfterOneExceptionAndReturnValue() throws Exception {
		//given
		final RetryExecutor executor = new AsyncRetryExecutor(schedulerMock);
		given(serviceMock.sometimesFails()).
				willThrow(IllegalStateException.class).
				willReturn("Foo");

		//when
		final ListenableFuture<String> future = executor.getWithRetry(new Callable<String>() {
			@Override
			public String call() throws Exception {
				return serviceMock.sometimesFails();
			}
		});

		//then
		assertThat(future.get()).isEqualTo("Foo");
	}

	@Test
	public void shouldSucceedWhenOnlyOneRetryAllowed() throws Exception {
		//given
		final RetryExecutor executor = new AsyncRetryExecutor(schedulerMock).withMaxRetries(1);
		given(serviceMock.sometimesFails()).
				willThrow(IllegalStateException.class).
				willReturn("Foo");

		//when
		final ListenableFuture<String> future = executor.getWithRetry(new Callable<String>() {
			@Override
			public String call() throws Exception {
				return serviceMock.sometimesFails();
			}
		});

		//then
		assertThat(future.get()).isEqualTo("Foo");
	}

	@Test
	public void shouldRetryOnceIfFirstExecutionThrowsException() throws Exception {
		//given
		final RetryExecutor executor = new AsyncRetryExecutor(schedulerMock);
		given(serviceMock.sometimesFails()).
				willThrow(IllegalStateException.class).
				willReturn("Foo");

		//when
		executor.getWithRetry(new Callable<String>() {
			@Override
			public String call() throws Exception {
				return serviceMock.sometimesFails();
			}
		});

		//then
		verify(serviceMock, times(2)).sometimesFails();
	}

	@Test
	public void shouldScheduleRetryWithDefaultDelay() throws Exception {
		//given
		final RetryExecutor executor = new AsyncRetryExecutor(schedulerMock);
		given(serviceMock.sometimesFails()).
				willThrow(IllegalStateException.class).
				willReturn("Foo");

		//when
		executor.getWithRetry(new Callable<String>() {
			@Override
			public String call() throws Exception {
				return serviceMock.sometimesFails();
			}
		});

		//then
		final InOrder inOrder = inOrder(schedulerMock);
		inOrder.verify(schedulerMock).schedule(notNullRunnable(), eq(0L), millis());
		inOrder.verify(schedulerMock).schedule(notNullRunnable(), eq(DEFAULT_PERIOD_MILLIS), millis());
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void shouldPassCorrectRetryCountToEachInvocationInContext() throws Exception {
		//given
		final RetryExecutor executor = new AsyncRetryExecutor(schedulerMock);
		given(serviceMock.calculateSum(0)).willThrow(IllegalStateException.class);
		given(serviceMock.calculateSum(1)).willReturn(BigDecimal.ONE);

		//when
		executor.getWithRetry(new RetryCallable<BigDecimal>() {
			@Override
			public BigDecimal call(RetryContext context) throws Exception {
				return serviceMock.calculateSum(context.getRetryCount());
			}
		});

		//then
		final InOrder order = inOrder(serviceMock);
		order.verify(serviceMock).calculateSum(0);
		order.verify(serviceMock).calculateSum(1);
		order.verifyNoMoreInteractions();
	}

}
