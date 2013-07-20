package com.blogspot.nurkiewicz.asyncretry;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.*;

/**
 * @author Tomasz Nurkiewicz
 * @since 5/10/13, 9:56 PM
 */
public class AbstractBaseTestCase {

	public static final String DON_T_PANIC = "Don't panic!";

	@Mock
	protected ScheduledExecutorService schedulerMock;

	@Mock
	protected FaultyService serviceMock;

	@BeforeMethod(alwaysRun=true)
	public void injectMocks() {
		MockitoAnnotations.initMocks(this);
		setupMocks();
	}

	private void setupMocks() {
		given(schedulerMock.schedule(notNullRunnable(), anyLong(), eq(TimeUnit.MILLISECONDS))).willAnswer(invocation -> {
			((Runnable) invocation.getArguments()[0]).run();
			return null;
		});
	}

	protected Runnable notNullRunnable() {
		return (Runnable) notNull();
	}

	protected RetryContext notNullRetryContext() {
		return (RetryContext) notNull();
	}

	protected TimeUnit millis() {
		return eq(TimeUnit.MILLISECONDS);
	}
}
