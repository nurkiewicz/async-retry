package com.nurkiewicz.asyncretry;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.nurkiewicz.asyncretry.function.RetryCallable;
import com.nurkiewicz.asyncretry.function.RetryRunnable;
import com.nurkiewicz.asyncretry.policy.RetryPolicy;

import java.util.concurrent.Callable;

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
	public ListenableFuture<Void> doWithRetry(RetryRunnable action) {
		try {
			action.run(RETRY_CONTEXT);
			return Futures.immediateFuture(null);
		} catch (Exception e) {
			return Futures.immediateFailedFuture(e);
		}
	}

	@Override
	public <V> ListenableFuture<V> getWithRetry(Callable<V> task) {
		try {
			return Futures.immediateFuture(task.call());
		} catch (Exception e) {
			return Futures.immediateFailedFuture(e);
		}
	}

	@Override
	public <V> ListenableFuture<V> getWithRetry(RetryCallable<V> task) {
		try {
			return Futures.immediateFuture(task.call(RETRY_CONTEXT));
		} catch (Exception e) {
			return Futures.immediateFailedFuture(e);
		}
	}

	@Override
	public <V> ListenableFuture<V> getFutureWithRetry(RetryCallable<ListenableFuture<V>> task) {
		try {
			return task.call(RETRY_CONTEXT);
		} catch (Exception e) {
			return Futures.immediateFailedFuture(e);
		}
	}

}
