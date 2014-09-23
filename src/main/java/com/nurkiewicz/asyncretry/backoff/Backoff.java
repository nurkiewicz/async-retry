package com.nurkiewicz.asyncretry.backoff;

import com.nurkiewicz.asyncretry.RetryContext;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/17/13, 11:15 PM
 */
public interface Backoff {

	Backoff DEFAULT = new FixedIntervalBackoff();

	long delayMillis(RetryContext context);

}
