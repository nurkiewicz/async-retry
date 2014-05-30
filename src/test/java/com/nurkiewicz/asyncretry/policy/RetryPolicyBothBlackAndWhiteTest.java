package com.nurkiewicz.asyncretry.policy;

import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/18/13, 11:25 PM
 */
public class RetryPolicyBothBlackAndWhiteTest extends AbstractRetryPolicyTest {

	@Test
	public void shouldRetryOnGivenException() throws Exception {
		final RetryPolicy policy = new RetryPolicy().
				retryOn(IOException.class).
				abortOn(NullPointerException.class);

		assertThat(shouldRetryOn(policy, new NullPointerException())).isFalse();
		assertThat(shouldRetryOn(policy, new IOException())).isTrue();
		assertThat(shouldRetryOn(policy, new ConnectException())).isTrue();
	}

	@Test
	public void shouldAbortOnGivenException() throws Exception {
		final RetryPolicy policy = new RetryPolicy().
				abortOn(IOException.class).
				retryOn(NullPointerException.class);

		assertThat(shouldRetryOn(policy, new NullPointerException())).isTrue();
		assertThat(shouldRetryOn(policy, new IOException())).isFalse();
		assertThat(shouldRetryOn(policy, new ConnectException())).isFalse();
	}

	@Test
	public void shouldRetryUnlessGivenSubclass() throws Exception {
		final RetryPolicy policy = new RetryPolicy().
				retryOn(IOException.class).
				abortOn(FileNotFoundException.class);

		assertThat(shouldRetryOn(policy, new IOException())).isTrue();
		assertThat(shouldRetryOn(policy, new SocketException())).isTrue();
		assertThat(shouldRetryOn(policy, new ConnectException())).isTrue();
		assertThat(shouldRetryOn(policy, new FileNotFoundException())).isFalse();
	}

	@Test
	public void shouldRetryUnlessGivenSubclassWithReversedDeclarationOrder() throws Exception {
		final RetryPolicy policy = new RetryPolicy().
				abortOn(FileNotFoundException.class).
				retryOn(IOException.class);

		assertThat(shouldRetryOn(policy, new IOException())).isTrue();
		assertThat(shouldRetryOn(policy, new SocketException())).isTrue();
		assertThat(shouldRetryOn(policy, new ConnectException())).isTrue();
		assertThat(shouldRetryOn(policy, new FileNotFoundException())).isFalse();
	}

	@Test
	public void shouldUnderstandManyWhiteAndBlackListedExceptions() throws Exception {
		final RetryPolicy policy = new RetryPolicy().
				retryOn(Exception.class).
				retryOn(LinkageError.class).
				abortOn(IncompatibleClassChangeError.class).
				abortOn(ClassCastException.class).
				abortOn(ConnectException.class);

		assertThat(shouldRetryOn(policy, new Exception())).isTrue();
		assertThat(shouldRetryOn(policy, new IOException())).isTrue();
		assertThat(shouldRetryOn(policy, new IllegalStateException())).isTrue();
		assertThat(shouldRetryOn(policy, new NoClassDefFoundError())).isTrue();
		assertThat(shouldRetryOn(policy, new UnsupportedClassVersionError())).isTrue();

		assertThat(shouldRetryOn(policy, new NoSuchFieldError())).isFalse();
		assertThat(shouldRetryOn(policy, new OutOfMemoryError())).isFalse();
		assertThat(shouldRetryOn(policy, new ClassCastException())).isFalse();
		assertThat(shouldRetryOn(policy, new ConnectException())).isFalse();
	}

	@Test
	public void shouldUnderstandManyWhiteAndBlackListedExceptionsInOneList() throws Exception {
		final RetryPolicy policy = new RetryPolicy().
				retryOn(Exception.class, LinkageError.class).
				abortOn(IncompatibleClassChangeError.class, ClassCastException.class, ConnectException.class);

		assertThat(shouldRetryOn(policy, new Exception())).isTrue();
		assertThat(shouldRetryOn(policy, new IOException())).isTrue();
		assertThat(shouldRetryOn(policy, new IllegalStateException())).isTrue();
		assertThat(shouldRetryOn(policy, new NoClassDefFoundError())).isTrue();
		assertThat(shouldRetryOn(policy, new UnsupportedClassVersionError())).isTrue();

		assertThat(shouldRetryOn(policy, new NoSuchFieldError())).isFalse();
		assertThat(shouldRetryOn(policy, new OutOfMemoryError())).isFalse();
		assertThat(shouldRetryOn(policy, new ClassCastException())).isFalse();
		assertThat(shouldRetryOn(policy, new ConnectException())).isFalse();
	}

}
