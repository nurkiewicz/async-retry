package com.blogspot.nurkiewicz.asyncretry.policy.exception;

import com.blogspot.nurkiewicz.asyncretry.policy.RetryPolicy;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.util.concurrent.TimeoutException;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/18/13, 11:25 PM
 */
public class ExceptionClassRetryPolicyBlackListTest extends AbstractExceptionClassRetryPolicyTest {

	@Test
	public void shouldAbortForSpecifiedException() throws Exception {
		final RetryPolicy policy = new ExceptionClassRetryPolicy(always).
				abortFor(ConnectException.class);

		assertThat(shouldRetryOn(policy, new ConnectException())).isFalse();
	}

	@Test
	public void shouldRetryIfExceptionNotAborting() throws Exception {
		final RetryPolicy policy = new ExceptionClassRetryPolicy(always).
				abortFor(ConnectException.class);

		assertThat(shouldRetryOn(policy, new Exception())).isTrue();
		assertThat(shouldRetryOn(policy, new RuntimeException())).isTrue();
		assertThat(shouldRetryOn(policy, new IOException())).isTrue();
		assertThat(shouldRetryOn(policy, new SocketException())).isTrue();
		assertThat(shouldRetryOn(policy, new ClassCastException())).isTrue();
		assertThat(shouldRetryOn(policy, new NullPointerException())).isTrue();
		assertThat(shouldRetryOn(policy, new IllegalArgumentException())).isTrue();
		assertThat(shouldRetryOn(policy, new IllegalStateException())).isTrue();
		assertThat(shouldRetryOn(policy, new TimeoutException())).isTrue();
	}

	@Test
	public void shouldRetryIfErrorNotAborting() throws Exception {
		final RetryPolicy policy = always.abortFor(ConnectException.class);

		assertThat(shouldRetryOn(policy, new OutOfMemoryError())).isTrue();
		assertThat(shouldRetryOn(policy, new StackOverflowError())).isTrue();
		assertThat(shouldRetryOn(policy, new NoClassDefFoundError())).isTrue();
	}

	@Test
	public void shouldAbortIfBlackListedException() throws Exception {
		final RetryPolicy policy = new ExceptionClassRetryPolicy(always).
				abortFor(NullPointerException.class);

		assertThat(shouldRetryOn(policy, new NullPointerException())).isFalse();
	}

	@Test
	public void shouldAbortForSubclassesOfBlackListedException() throws Exception {
		final RetryPolicy policy = new ExceptionClassRetryPolicy(always).
				abortFor(IOException.class);

		assertThat(shouldRetryOn(policy, new FileNotFoundException())).isFalse();
		assertThat(shouldRetryOn(policy, new SocketException())).isFalse();
		assertThat(shouldRetryOn(policy, new ConnectException())).isFalse();
	}

	@Test
	public void shouldAbortForAnyBlackListedExceptions() throws Exception {
		final RetryPolicy policy = new ExceptionClassRetryPolicy(always).
				abortFor(NullPointerException.class).
				abortFor(OutOfMemoryError.class).
				abortFor(StackOverflowError.class);

		assertThat(shouldRetryOn(policy, new NullPointerException())).isFalse();
		assertThat(shouldRetryOn(policy, new OutOfMemoryError())).isFalse();
		assertThat(shouldRetryOn(policy, new StackOverflowError())).isFalse();
	}

	@Test
	public void shouldAbortForSubclassesOfAnyOfBlackListedExceptions() throws Exception {
		final RetryPolicy policy = new ExceptionClassRetryPolicy(always).
				abortFor(IOException.class).
				abortFor(RuntimeException.class);

		assertThat(shouldRetryOn(policy, new FileNotFoundException())).isFalse();
		assertThat(shouldRetryOn(policy, new ConnectException())).isFalse();
		assertThat(shouldRetryOn(policy, new NullPointerException())).isFalse();
	}

}
