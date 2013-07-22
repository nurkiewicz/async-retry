package com.blogspot.nurkiewicz.asyncretry.backoff;

import com.blogspot.nurkiewicz.asyncretry.RetryContext;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/17/13, 11:15 PM
 */
public interface Backoff {

	Backoff DEFAULT = new FixedIntervalBackoff();

	long delayMillis(RetryContext context);

}
