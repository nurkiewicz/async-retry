package com.blogspot.nurkiewicz.asyncretry.policy.exception;

import com.blogspot.nurkiewicz.asyncretry.policy.RetryPolicy;
import org.mockito.Mock;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.TimeoutException;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/18/13, 10:56 PM
 */
public class ExceptionClassRetryPolicyDefaultsTest extends AbstractExceptionClassRetryPolicyTest {

	@Mock
	private RetryPolicy retryPolicyMock;

	@Test
	public void byDefaultShouldRetryOnAllExceptions() throws Exception {
		final RetryPolicy policy = new ExceptionClassRetryPolicy(always);

		assertThat(shouldRetryOn(policy, new Exception())).isTrue();
		assertThat(shouldRetryOn(policy, new RuntimeException())).isTrue();
		assertThat(shouldRetryOn(policy, new IOException())).isTrue();
		assertThat(shouldRetryOn(policy, new ClassCastException())).isTrue();
		assertThat(shouldRetryOn(policy, new NullPointerException())).isTrue();
		assertThat(shouldRetryOn(policy, new IllegalArgumentException())).isTrue();
		assertThat(shouldRetryOn(policy, new IllegalStateException())).isTrue();
		assertThat(shouldRetryOn(policy, new TimeoutException())).isTrue();
		assertThat(shouldRetryOn(policy, new SocketException())).isTrue();
	}

	@Test
	public void byDefaultShouldRetryOnAllThrowables() throws Exception {
		final RetryPolicy policy = new ExceptionClassRetryPolicy(always);

		assertThat(shouldRetryOn(policy, new OutOfMemoryError())).isTrue();
		assertThat(shouldRetryOn(policy, new StackOverflowError())).isTrue();
		assertThat(shouldRetryOn(policy, new NoClassDefFoundError())).isTrue();
	}

	@Test
	public void shouldFirstAskChildPolicyAndAbortIfItAborts() throws Exception {
		//given
		final RetryPolicy policy = new ExceptionClassRetryPolicy(retryPolicyMock);
		given(retryPolicyMock.shouldContinue(notNullRetryContext())).willReturn(false);

		//when
		final boolean shouldRetry = shouldRetryOn(policy, new RuntimeException());

		//then
		assertThat(shouldRetry).isFalse();
		verify(retryPolicyMock).shouldContinue(notNullRetryContext());
	}
}

