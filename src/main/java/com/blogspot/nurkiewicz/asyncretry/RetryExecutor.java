package com.blogspot.nurkiewicz.asyncretry;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/17/13, 7:25 PM
 */
public interface RetryExecutor {
	CompletableFuture<Void> doWithRetry(Consumer<RetryContext> function);

	<V> CompletableFuture<V> getWithRetry(Supplier<V> function);

	<V> CompletableFuture<V> getWithRetry(Function<RetryContext, V> function);
}
