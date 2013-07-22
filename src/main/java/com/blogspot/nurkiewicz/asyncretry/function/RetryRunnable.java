package com.blogspot.nurkiewicz.asyncretry.function;

import com.blogspot.nurkiewicz.asyncretry.RetryContext;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/20/13, 9:36 PM
 */
public interface RetryRunnable {

	void run(RetryContext context) throws Exception;

}
