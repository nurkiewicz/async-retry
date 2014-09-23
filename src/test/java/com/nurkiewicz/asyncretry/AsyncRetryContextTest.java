package com.nurkiewicz.asyncretry;

import com.nurkiewicz.asyncretry.function.RetryRunnable;
import org.mockito.InOrder;
import org.testng.annotations.Test;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
		executor.doWithRetry(new RetryRunnable() {
			@Override
			public void run(RetryContext context) throws Exception {
				serviceMock.withFlag(context.willRetry());
			}
		});

		//then
		verify(serviceMock).withFlag(false);
	}

	@Test
	public void shouldSayItWillRetryIfUnlimitedNumberOfRetries() throws Exception {
		//given
		final RetryExecutor executor = new AsyncRetryExecutor(schedulerMock);

		//when
		executor.doWithRetry(new RetryRunnable() {
			@Override
			public void run(RetryContext context) throws Exception {
				serviceMock.withFlag(context.willRetry());
			}
		});

		//then
		verify(serviceMock).withFlag(true);
	}

	@Test
	public void shouldSayItWillRetryOnFirstFewCases() throws Exception {
		//given
		final RetryExecutor executor = new AsyncRetryExecutor(schedulerMock).withMaxRetries(2);
		doThrow(IllegalStateException.class).when(serviceMock).withFlag(anyBoolean());

		//when
		executor.doWithRetry(new RetryRunnable() {
			@Override
			public void run(RetryContext context) throws Exception {
				serviceMock.withFlag(context.willRetry());
			}
		});

		//then
		final InOrder order = inOrder(serviceMock);
		order.verify(serviceMock, times(2)).withFlag(true);
		order.verify(serviceMock).withFlag(false);
		order.verifyNoMoreInteractions();
	}

}
