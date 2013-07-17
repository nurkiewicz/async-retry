package com.blogspot.nurkiewicz.asyncretry;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.notNull;

/**
 * @author Tomasz Nurkiewicz
 * @since 5/10/13, 9:56 PM
 */
public class AbstractBaseTestCase {

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
		given(schedulerMock.schedule((Runnable)notNull(), anyLong(), eq(TimeUnit.MILLISECONDS))).willAnswer(invocation -> {
			((Runnable) invocation.getArguments()[0]).run();
			return null;
		});
	}

}
