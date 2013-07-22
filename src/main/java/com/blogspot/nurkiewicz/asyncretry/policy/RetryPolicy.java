package com.blogspot.nurkiewicz.asyncretry.policy;

import com.blogspot.nurkiewicz.asyncretry.RetryContext;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/16/13, 6:05 PM
 */
public interface RetryPolicy {

	public static final RetryPolicy DEFAULT = new RetryInfinitelyPolicy();

	boolean shouldContinue(RetryContext context);

}
