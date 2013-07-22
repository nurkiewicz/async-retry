package com.blogspot.nurkiewicz.asyncretry;

import com.blogspot.nurkiewicz.asyncretry.policy.exception.AbortRetryException;
import com.google.common.util.concurrent.ListenableFuture;
import org.testng.annotations.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;
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
