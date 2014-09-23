package com.nurkiewicz.asyncretry.function;

import com.nurkiewicz.asyncretry.RetryContext;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/20/13, 9:34 PM
 */
public interface RetryCallable<V> {

	V call(RetryContext context) throws Exception;

}
