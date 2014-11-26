package com.nurkiewicz.asyncretry;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.nurkiewicz.asyncretry.function.RetryCallable;
import com.nurkiewicz.asyncretry.function.RetryRunnable;
import org.testng.annotations.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

public class SyncRetryExecutorTest extends AbstractBaseTestCase {

	RetryExecutor executor = SyncRetryExecutor.INSTANCE;

	@Test
	void shouldReturnCompletedFutureWhenDoWithRetryCalled() throws ExecutionException, InterruptedException {
		//given
		String mainThread = Thread.currentThread().getName();
		final AtomicReference<String> poolThread = new AtomicReference<>();

		//when
		executor.doWithRetry(new RetryRunnable() {
			@Override
			public void run(RetryContext context) throws Exception {
				poolThread.set(Thread.currentThread().getName());
			}
		});

		//then
		assertThat(poolThread.get()).isEqualTo(mainThread);
	}

	@Test
	void shouldWrapExceptionInFutureRatherThanThrowingIt() throws InterruptedException {
		//given
		RetryRunnable block = new RetryRunnable() {
			@Override
			public void run(RetryContext context) throws Exception {
				throw new IllegalArgumentException(DON_T_PANIC);
			}
		};
		ListenableFuture<Void> future = executor.doWithRetry(block);

		try {
			//when
			future.get();
			failBecauseExceptionWasNotThrown(ExecutionException.class);
		} catch (ExecutionException e) {
			//then
			assertThat(e).hasCauseInstanceOf(IllegalArgumentException.class);
			assertThat(e.getCause()).hasMessage(DON_T_PANIC);
		}
	}

	@Test
	void shouldReturnCompletedFutureWhenGetWithRetryCalled() throws ExecutionException, InterruptedException {
		//given
		String mainThread = Thread.currentThread().getName();

		//when
		ListenableFuture<String> result = executor.getWithRetry(new Callable<String>() {
			@Override
			public String call() throws Exception {
				return Thread.currentThread().getName();
			}
		});

		//then
		assertThat(result.get()).isEqualTo(mainThread);
	}

	@Test
	void shouldWrapExceptionInFutureRatherThanThrowingItInGetWithRetry() throws InterruptedException {
		//given
		Callable<Void> block = new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				throw new IllegalArgumentException(DON_T_PANIC);
			}
		};
		ListenableFuture<Void> future = executor.getWithRetry(block);

		try {
			//when
			future.get();
			failBecauseExceptionWasNotThrown(ExecutionException.class);
		} catch (ExecutionException e) {
			//then
			assertThat(e).hasCauseInstanceOf(IllegalArgumentException.class);
			assertThat(e.getCause()).hasMessage(DON_T_PANIC);
		}
	}

	@Test
	void shouldReturnCompletedFutureWhenGetWithRetryCalledContext() throws ExecutionException, InterruptedException {
		//given
		String mainThread = Thread.currentThread().getName();

		//when
		ListenableFuture<String> result = executor.getWithRetry(new RetryCallable<String>() {
			@Override
			public String call(RetryContext context) throws Exception {
				return Thread.currentThread().getName();
			}
		});

		//then
		assertThat(result.get()).isEqualTo(mainThread);
	}

	@Test
	void shouldWrapExceptionInFutureRatherThanThrowingItInGetWithRetryContext() throws InterruptedException {
		//given
		RetryCallable<Void> block = new RetryCallable<Void>() {
			@Override
			public Void call(RetryContext context) throws Exception {
				throw new IllegalArgumentException(DON_T_PANIC);
			}
		};
		ListenableFuture<Void> future = executor.getWithRetry(block);

		try {
			//when
			future.get();
			failBecauseExceptionWasNotThrown(ExecutionException.class);
		} catch (ExecutionException e) {
			//then
			assertThat(e).hasCauseInstanceOf(IllegalArgumentException.class);
			assertThat(e.getCause()).hasMessage(DON_T_PANIC);
		}
	}

	@Test
	void shouldReturnCompletedFutureWhenGetWithRetryOnFutureCalled() throws ExecutionException, InterruptedException {
		//given
		String mainThread = Thread.currentThread().getName();

		//when
		ListenableFuture<String> result = executor.getFutureWithRetry(new RetryCallable<ListenableFuture<String>>() {
			@Override
			public ListenableFuture<String> call(RetryContext context) throws Exception {
				return Futures.immediateFuture(Thread.currentThread().getName());
			}
		});

		//then
		assertThat(result.get()).isEqualTo(mainThread);
	}

	@Test
	void shouldWrapExceptionInFutureRatherThanThrowingItInGetWithRetryOnFuture() throws InterruptedException {
		//given
		RetryCallable<ListenableFuture<String>> block = new RetryCallable<ListenableFuture<String>>() {
			@Override
			public ListenableFuture<String> call(RetryContext context) throws Exception {
				return Futures.immediateFailedFuture(new IllegalArgumentException(DON_T_PANIC));
			}
		};
		ListenableFuture<String> future = executor.getFutureWithRetry(block);

		try {
			//when
			future.get();
			failBecauseExceptionWasNotThrown(ExecutionException.class);
		} catch (ExecutionException e) {
			//then
			assertThat(e).hasCauseInstanceOf(IllegalArgumentException.class);
			assertThat(e.getCause()).hasMessage(DON_T_PANIC);
		}
	}

}
