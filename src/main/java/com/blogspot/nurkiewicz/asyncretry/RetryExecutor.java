package com.blogspot.nurkiewicz.asyncretry;

import com.blogspot.nurkiewicz.asyncretry.function.RetryCallable;
import com.blogspot.nurkiewicz.asyncretry.function.RetryRunnable;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Callable;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/17/13, 7:25 PM
 */
public interface RetryExecutor {

	ListenableFuture<Void> doWithRetry(RetryRunnable action);

	<V> ListenableFuture<V> getWithRetry(Callable<V> task);

	<V> ListenableFuture<V> getWithRetry(RetryCallable<V> task);

	<V> ListenableFuture<V> getFutureWithRetry(RetryCallable<ListenableFuture<V>> task);
}
