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
public class ExceptionClassRetryPolicyWhiteListTest extends AbstractExceptionClassRetryPolicyTest {

	@Test
	public void retryOnExceptionExplicitly() throws Exception {
		final RetryPolicy policy = always.retryOn(Exception.class);

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
	public void retryOnExceptionShouldNotRetryOnError() throws Exception {
		final RetryPolicy policy = new ExceptionClassRetryPolicy(always).
				retryOn(Exception.class);

		assertThat(shouldRetryOn(policy, new OutOfMemoryError())).isFalse();
		assertThat(shouldRetryOn(policy, new StackOverflowError())).isFalse();
		assertThat(shouldRetryOn(policy, new NoClassDefFoundError())).isFalse();
	}

	@Test
	public void shouldRetryOnOnlyOneSpecificException() throws Exception {
		final RetryPolicy policy = new ExceptionClassRetryPolicy(always).
				retryOn(OptimisticLockException.class);

		assertThat(shouldRetryOn(policy, new OptimisticLockException())).isTrue();
	}

	@Test
	public void shouldNotRetryOnOtherExceptionsIfOneGivenExplicitly() throws Exception {
		final RetryPolicy policy = new ExceptionClassRetryPolicy(always).
				retryOn(OptimisticLockException.class);

		assertThat(shouldRetryOn(policy, new Exception())).isFalse();
		assertThat(shouldRetryOn(policy, new RuntimeException())).isFalse();
		assertThat(shouldRetryOn(policy, new IOException())).isFalse();
		assertThat(shouldRetryOn(policy, new ClassCastException())).isFalse();
		assertThat(shouldRetryOn(policy, new NullPointerException())).isFalse();
		assertThat(shouldRetryOn(policy, new IllegalArgumentException())).isFalse();
		assertThat(shouldRetryOn(policy, new IllegalStateException())).isFalse();
		assertThat(shouldRetryOn(policy, new TimeoutException())).isFalse();
		assertThat(shouldRetryOn(policy, new SocketException())).isFalse();
	}

	@Test
	public void shouldNotRetryOnErrorsIfExceptionGivenExplicitly() throws Exception {
		final RetryPolicy policy = new ExceptionClassRetryPolicy(always).
				retryOn(OptimisticLockException.class);

		assertThat(shouldRetryOn(policy, new OutOfMemoryError())).isFalse();
		assertThat(shouldRetryOn(policy, new StackOverflowError())).isFalse();
		assertThat(shouldRetryOn(policy, new NoClassDefFoundError())).isFalse();
	}

	@Test
	public void shouldRetryOnAnyOfProvidedExceptions() throws Exception {
		final RetryPolicy policy = new ExceptionClassRetryPolicy(always).
				retryOn(OptimisticLockException.class).
				retryOn(IOException.class);

		assertThat(shouldRetryOn(policy, new OptimisticLockException())).isTrue();
		assertThat(shouldRetryOn(policy, new IOException())).isTrue();
	}

	@Test
	public void shouldRetryOnAnyOfProvidedExceptionsInOneList() throws Exception {
		final RetryPolicy policy = new ExceptionClassRetryPolicy(always).
				retryOn(OptimisticLockException.class, IOException.class);

		assertThat(shouldRetryOn(policy, new OptimisticLockException())).isTrue();
		assertThat(shouldRetryOn(policy, new IOException())).isTrue();
	}

	@Test
	public void shouldNotRetryOnOtherExceptionsIfFewGivenExplicitly() throws Exception {
		final RetryPolicy policy = new ExceptionClassRetryPolicy(always).
				retryOn(OptimisticLockException.class).
				retryOn(IOException.class);

		assertThat(shouldRetryOn(policy, new Exception())).isFalse();
		assertThat(shouldRetryOn(policy, new RuntimeException())).isFalse();
		assertThat(shouldRetryOn(policy, new ClassCastException())).isFalse();
		assertThat(shouldRetryOn(policy, new NullPointerException())).isFalse();
		assertThat(shouldRetryOn(policy, new IllegalArgumentException())).isFalse();
		assertThat(shouldRetryOn(policy, new IllegalStateException())).isFalse();
		assertThat(shouldRetryOn(policy, new TimeoutException())).isFalse();
	}

	@Test
	public void shouldNotRetryOnOtherExceptionsIfFewGivenExplicitlyInOneList() throws Exception {
		final RetryPolicy policy = new ExceptionClassRetryPolicy(always).
				retryOn(OptimisticLockException.class, IOException.class);

		assertThat(shouldRetryOn(policy, new Exception())).isFalse();
		assertThat(shouldRetryOn(policy, new RuntimeException())).isFalse();
		assertThat(shouldRetryOn(policy, new ClassCastException())).isFalse();
		assertThat(shouldRetryOn(policy, new NullPointerException())).isFalse();
		assertThat(shouldRetryOn(policy, new IllegalArgumentException())).isFalse();
		assertThat(shouldRetryOn(policy, new IllegalStateException())).isFalse();
		assertThat(shouldRetryOn(policy, new TimeoutException())).isFalse();
	}

	@Test
	public void shouldNotRetryOnErrorsIfFewExceptionsGivenExplicitly() throws Exception {
		final RetryPolicy policy = new ExceptionClassRetryPolicy(always).
				retryOn(OptimisticLockException.class).
				retryOn(IOException.class);

		assertThat(shouldRetryOn(policy, new OutOfMemoryError())).isFalse();
		assertThat(shouldRetryOn(policy, new StackOverflowError())).isFalse();
		assertThat(shouldRetryOn(policy, new NoClassDefFoundError())).isFalse();
	}

	@Test
	public void shouldNotRetryOnErrorsIfFewExceptionsGivenExplicitlyInOneList() throws Exception {
		final RetryPolicy policy = new ExceptionClassRetryPolicy(always).
				retryOn(OptimisticLockException.class, IOException.class);

		assertThat(shouldRetryOn(policy, new OutOfMemoryError())).isFalse();
		assertThat(shouldRetryOn(policy, new StackOverflowError())).isFalse();
		assertThat(shouldRetryOn(policy, new NoClassDefFoundError())).isFalse();
	}

	@Test
	public void shouldRetryWhenSubclassOfGivenExceptionThrown() throws Exception {
		final RetryPolicy policy = new ExceptionClassRetryPolicy(always).
				retryOn(IOException.class);

		assertThat(shouldRetryOn(policy, new FileNotFoundException())).isTrue();
		assertThat(shouldRetryOn(policy, new SocketException())).isTrue();
		assertThat(shouldRetryOn(policy, new ConnectException())).isTrue();
	}

	@Test
	public void shouldNotRetryOnSiblilngExceptions() throws Exception {
		final RetryPolicy policy = new ExceptionClassRetryPolicy(always).
				retryOn(FileNotFoundException.class);

		assertThat(shouldRetryOn(policy, new SocketException())).isFalse();
	}

	@Test
	public void shouldNotRetryOnSuperClassesOfGivenClass() throws Exception {
		final RetryPolicy policy = new ExceptionClassRetryPolicy(always).
				retryOn(FileNotFoundException.class);

		assertThat(shouldRetryOn(policy, new IOException())).isFalse();
	}

}
