package com.blogspot.nurkiewicz.asyncretry;

import com.blogspot.nurkiewicz.asyncretry.policy.exception.AbortRetryException;
import org.testng.annotations.Test;

import java.util.concurrent.CompletableFuture;
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
		executor.getWithRetry(serviceMock::sometimesFails);

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
		final CompletableFuture<String> future = executor.getWithRetry(serviceMock::sometimesFails);

		//then
		assertThat(future.isCompletedExceptionally()).isTrue();
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
						new AbortRetryException("Ignored")
				);

		//when
		final CompletableFuture<String> future = executor.getWithRetry(serviceMock::sometimesFails);

		//then
		assertThat(future.isCompletedExceptionally()).isTrue();
		try {
			future.get();
			failBecauseExceptionWasNotThrown(ExecutionException.class);
		} catch (ExecutionException e) {
			assertThat(e.getCause()).isInstanceOf(IllegalStateException.class);
			assertThat(e.getCause().getMessage()).isEqualTo("Second");
		}
	}

}
