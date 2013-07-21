package com.blogspot.nurkiewicz.asyncretry.function;

import com.blogspot.nurkiewicz.asyncretry.RetryContext;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/20/13, 9:34 PM
 */
@FunctionalInterface
public interface RetryCallable<V> {

	V call(RetryContext context) throws Exception;

}
