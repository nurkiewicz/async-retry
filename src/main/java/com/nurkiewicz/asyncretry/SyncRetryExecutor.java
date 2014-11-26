package com.nurkiewicz.asyncretry;

import com.nurkiewicz.asyncretry.function.RetryCallable;
import com.nurkiewicz.asyncretry.function.RetryRunnable;
import com.nurkiewicz.asyncretry.policy.RetryPolicy;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

/**
 * Singleton instance of {@link RetryExecutor} that executes tasks in the same thread and without retry.
 * Useful for testing or when no-op implementation of {@link RetryExecutor} is needed.
 * This implementation, while implements the API, runs all tasks synchronously so that returned futures are already completed.
 * Exceptions are not thrown but wrapped in Future as well.
 * @since 0.0.6
 */
public enum SyncRetryExecutor implements RetryExecutor {

	INSTANCE;

	private static final AsyncRetryContext RETRY_CONTEXT = new AsyncRetryContext(RetryPolicy.DEFAULT);

	@Override
	public CompletableFuture<Void> doWithRetry(RetryRunnable action) {
		try {
			action.run(RETRY_CONTEXT);
			return CompletableFuture.completedFuture(null);
		} catch (Exception e) {
			return failedFuture(e);
		}
	}

	@Override
	public <V> CompletableFuture<V> getWithRetry(Callable<V> task) {
		try {
			return CompletableFuture.completedFuture(task.call());
		} catch (Exception e) {
			return failedFuture(e);
		}
	}

	@Override
	public <V> CompletableFuture<V> getWithRetry(RetryCallable<V> task) {
		try {
			return CompletableFuture.completedFuture(task.call(RETRY_CONTEXT));
		} catch (Exception e) {
			return failedFuture(e);
		}
	}

	@Override
	public <V> CompletableFuture<V> getFutureWithRetry(RetryCallable<CompletableFuture<V>> task) {
		try {
			return task.call(RETRY_CONTEXT);
		} catch (Exception e) {
			return failedFuture(e);
		}
	}

	private static <T> CompletableFuture<T> failedFuture(Exception e) {
		final CompletableFuture<T> promise = new CompletableFuture<>();
		promise.completeExceptionally(e);
		return promise;
	}
}
