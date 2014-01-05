package com.blogspot.nurkiewicz.asyncretry.policy;

import org.testng.annotations.Test;

import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.TimeoutException;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/18/13, 10:56 PM
 */
public class RetryPolicyDefaultsTest extends AbstractRetryPolicyTest {

	@Test
	public void byDefaultShouldRetryOnAllExceptions() throws Exception {
		assertThat(shouldRetryOn(new RetryPolicy(), new Exception())).isTrue();
		assertThat(shouldRetryOn(new RetryPolicy(), new RuntimeException())).isTrue();
		assertThat(shouldRetryOn(new RetryPolicy(), new IOException())).isTrue();
		assertThat(shouldRetryOn(new RetryPolicy(), new ClassCastException())).isTrue();
		assertThat(shouldRetryOn(new RetryPolicy(), new NullPointerException())).isTrue();
		assertThat(shouldRetryOn(new RetryPolicy(), new IllegalArgumentException())).isTrue();
		assertThat(shouldRetryOn(new RetryPolicy(), new IllegalStateException())).isTrue();
		assertThat(shouldRetryOn(new RetryPolicy(), new TimeoutException())).isTrue();
		assertThat(shouldRetryOn(new RetryPolicy(), new SocketException())).isTrue();
	}

	@Test
	public void byDefaultShouldRetryOnAllThrowables() throws Exception {
		assertThat(shouldRetryOn(new RetryPolicy(), new OutOfMemoryError())).isTrue();
		assertThat(shouldRetryOn(new RetryPolicy(), new StackOverflowError())).isTrue();
		assertThat(shouldRetryOn(new RetryPolicy(), new NoClassDefFoundError())).isTrue();
	}
}

