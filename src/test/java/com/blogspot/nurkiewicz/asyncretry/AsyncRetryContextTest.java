package com.blogspot.nurkiewicz.asyncretry;

import org.mockito.InOrder;
import org.testng.annotations.Test;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.*;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/17/13, 9:34 PM
 */
public class AsyncRetryContextTest extends AbstractBaseTestCase {

	@Test
	public void shouldNotRetryIfRetriesForbidden() throws Exception {
		//given
		final RetryExecutor executor = new AsyncRetryExecutor(schedulerMock).dontRetry();

		//when
		executor.doWithRetry(ctx -> serviceMock.withFlag(ctx.willRetry()));

		//then
		verify(serviceMock).withFlag(false);
	}

	@Test
	public void shouldSayItWillRetryIfUnlimitedNumberOfRetries() throws Exception {
		//given
		final RetryExecutor executor = new AsyncRetryExecutor(schedulerMock);

		//when
		executor.doWithRetry(ctx -> serviceMock.withFlag(ctx.willRetry()));

		//then
		verify(serviceMock).withFlag(true);
	}

	@Test
	public void shouldSayItWillRetryForFirstFewCases() throws Exception {
		//given
		final RetryExecutor executor = new AsyncRetryExecutor(schedulerMock).withMaxRetries(2);
		doThrow(IllegalStateException.class).when(serviceMock).withFlag(anyBoolean());

		//when
		executor.doWithRetry(ctx -> serviceMock.withFlag(ctx.willRetry()));

		//then
		final InOrder order = inOrder(serviceMock);
		order.verify(serviceMock, times(2)).withFlag(true);
		order.verify(serviceMock).withFlag(false);
		order.verifyNoMoreInteractions();
	}

}
