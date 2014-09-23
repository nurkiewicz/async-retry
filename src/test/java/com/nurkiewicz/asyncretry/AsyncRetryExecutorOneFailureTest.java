package com.nurkiewicz.asyncretry;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.nurkiewicz.asyncretry.policy.AbortRetryException;
import org.testng.annotations.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/16/13, 10:51 PM
 */
public class AsyncRetryExecutorOneFailureTest extends AbstractBaseTestCase {

	@Test
	public void shouldNotRetryIfAbortThrown() throws Exception {
		//given
		final RetryExecutor executor = new AsyncRetryExecutor(schedulerMock);
		given(serviceMock.sometimesFails()).
				willThrow(AbortRetryException.class);

		//when
		executor.getWithRetry(new Callable<String>() {
			@Override
			public String call() throws Exception {
				return serviceMock.sometimesFails();
			}
		});

		//then
		verify(serviceMock).sometimesFails();
	}

	@Test
	public void shouldRethrowAbortExceptionIfFirstIterationThrownIt() throws Exception {
		//given
		final RetryExecutor executor = new AsyncRetryExecutor(schedulerMock);
		given(serviceMock.sometimesFails()).
				willThrow(AbortRetryException.class);

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
			failBecauseExceptionWasNotThrown(ExecutionException.class);
		} catch (ExecutionException e) {
			assertThat(e.getCause()).isInstanceOf(AbortRetryException.class);
		}
	}

	@Test
	public void shouldCompleteWithExceptionIfFirstIterationThrownIt() throws Exception {
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
		final AtomicReference<Throwable> error = new AtomicReference<>();
		Futures.addCallback(future, new FutureCallback<String>() {
			@Override
			public void onSuccess(String result) {
			}

			@Override
			public void onFailure(Throwable t) {
				error.set(t);       //schedulerMock is synchronous anyway
			}
		});
		assertThat(error.get()).
				isNotNull().
				isInstanceOf(IllegalStateException.class).
				hasMessage(DON_T_PANIC);
	}

	@Test
	public void shouldRethrowLastThrownExceptionWhenAbortedInSubsequentIteration() throws Exception {
		//given
		final RetryExecutor executor = new AsyncRetryExecutor(schedulerMock);
		given(serviceMock.sometimesFails()).
				willThrow(
						new IllegalArgumentException("First"),
						new IllegalStateException("Second"),
						new AbortRetryException()
				);

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
			failBecauseExceptionWasNotThrown(ExecutionException.class);
		} catch (ExecutionException e) {
			assertThat(e.getCause()).isInstanceOf(IllegalStateException.class);
			assertThat(e.getCause().getMessage()).isEqualTo("Second");
		}
	}

}
