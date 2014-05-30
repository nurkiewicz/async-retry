package com.nurkiewicz.asyncretry.function;

import com.nurkiewicz.asyncretry.RetryContext;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/20/13, 9:36 PM
 */
@FunctionalInterface
public interface RetryRunnable {

	void run(RetryContext context) throws Exception;

}
