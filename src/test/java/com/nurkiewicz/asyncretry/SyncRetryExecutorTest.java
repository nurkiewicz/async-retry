package com.nurkiewicz.asyncretry;

import com.nurkiewicz.asyncretry.function.RetryCallable;
import com.nurkiewicz.asyncretry.function.RetryRunnable;
import org.testng.annotations.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
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
		AtomicReference<String> poolThread = new AtomicReference<>();

		//when
		CompletableFuture<Void> result = executor.doWithRetry(ctx -> poolThread.set(Thread.currentThread().getName()));

		//then
		assertThat(poolThread.get()).isEqualTo(mainThread);
	}

	@Test
	void shouldWrapExceptionInFutureRatherThanThrowingIt() throws InterruptedException {
		//given
		RetryRunnable block = context -> {
			throw new IllegalArgumentException(DON_T_PANIC);
		};
		CompletableFuture<Void> future = executor.doWithRetry(block);

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
		CompletableFuture<String> result = executor.getWithRetry(() -> Thread.currentThread().getName());

		//then
		assertThat(result.get()).isEqualTo(mainThread);
	}

	@Test
	void shouldWrapExceptionInFutureRatherThanThrowingItInGetWithRetry() throws InterruptedException {
		//given
		Callable<Void> block = () -> {
			throw new IllegalArgumentException(DON_T_PANIC);
		};
		CompletableFuture<Void> future = executor.getWithRetry(block);

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
		CompletableFuture<String> result = executor.getWithRetry(ctx -> Thread.currentThread().getName());

		//then
		assertThat(result.get()).isEqualTo(mainThread);
	}

	@Test
	void shouldWrapExceptionInFutureRatherThanThrowingItInGetWithRetryContext() throws InterruptedException {
		//given
		RetryCallable<Void> block = ctx -> {
			throw new IllegalArgumentException(DON_T_PANIC);
		};
		CompletableFuture<Void> future = executor.getWithRetry(block);

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
		CompletableFuture<String> result = executor.getFutureWithRetry(ctx ->
				CompletableFuture.completedFuture(Thread.currentThread().getName()));

		//then
		assertThat(result.get()).isEqualTo(mainThread);
	}

	@Test
	void shouldWrapExceptionInFutureRatherThanThrowingItInGetWithRetryOnFuture() throws InterruptedException {
		//given
		RetryCallable<CompletableFuture<String>> block = ctx -> {
			final CompletableFuture<String> failedFuture = new CompletableFuture<>();
			failedFuture.completeExceptionally(new IllegalArgumentException(DON_T_PANIC));
			return failedFuture;
		};
		CompletableFuture<String> future = executor.getFutureWithRetry(block);

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
