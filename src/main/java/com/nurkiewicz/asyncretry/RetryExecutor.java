package com.nurkiewicz.asyncretry;

import com.nurkiewicz.asyncretry.function.RetryCallable;
import com.nurkiewicz.asyncretry.function.RetryRunnable;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/17/13, 7:25 PM
 */
public interface RetryExecutor {

	CompletableFuture<Void> doWithRetry(RetryRunnable action);

	<V> CompletableFuture<V> getWithRetry(Callable<V> task);

	<V> CompletableFuture<V> getWithRetry(RetryCallable<V> task);

	<V> CompletableFuture<V> getFutureWithRetry(RetryCallable<CompletableFuture<V>> task);
}
