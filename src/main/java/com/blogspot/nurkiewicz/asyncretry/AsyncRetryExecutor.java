package com.blogspot.nurkiewicz.asyncretry;

import com.blogspot.nurkiewicz.asyncretry.policy.NeverRetryPolicy;
import com.blogspot.nurkiewicz.asyncretry.policy.RetryPolicy;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * @author Tomasz Nurkiewicz
 * @since 7/15/13, 11:06 PM
 */
public class AsyncRetryExecutor {

	private final ScheduledExecutorService scheduler;
	private final boolean fixedDelay;
	private final RetryPolicy retryPolicy;

	public AsyncRetryExecutor(ScheduledExecutorService scheduler) {
		this(scheduler, RetryPolicy.DEFAULT);
	}

	public AsyncRetryExecutor(ScheduledExecutorService scheduler, RetryPolicy retryPolicy) {
		this(scheduler, retryPolicy, false);
	}

	public AsyncRetryExecutor(ScheduledExecutorService scheduler, RetryPolicy retryPolicy, boolean fixedDelay) {
		this.scheduler = scheduler;
		this.retryPolicy = retryPolicy;
		this.fixedDelay = fixedDelay;
	}

	public <V> CompletableFuture<V> doWithRetry(Supplier<V> function) {
		return doWithRetry(r -> function.get());
	}

	public <V> CompletableFuture<V> doWithRetry(Function<RetryContext, V> function) {
		return scheduleImmediately(function);
	}

	private <V> CompletableFuture<V> scheduleImmediately(Function<RetryContext, V> function) {
		final RetryTask<V> task = new RetryTask<>(function, this);
		scheduler.schedule(task, 0, MILLISECONDS);
		return task.getFuture();
	}

	public ScheduledExecutorService getScheduler() {
		return scheduler;
	}

	public boolean isFixedDelay() {
		return fixedDelay;
	}

	public RetryPolicy getRetryPolicy() {
		return retryPolicy;
	}

	public AsyncRetryExecutor withScheduler(ScheduledExecutorService scheduler) {
		return new AsyncRetryExecutor(scheduler, retryPolicy, fixedDelay);
	}

	public AsyncRetryExecutor withRetryPolicy(RetryPolicy retryPolicy) {
		return new AsyncRetryExecutor(scheduler, retryPolicy, fixedDelay);
	}

	public AsyncRetryExecutor withFixedDelay() {
		return new AsyncRetryExecutor(scheduler, retryPolicy, true);
	}

	public AsyncRetryExecutor withFixedDelay(boolean fixedDelay) {
		return new AsyncRetryExecutor(scheduler, retryPolicy, fixedDelay);
	}

	public AsyncRetryExecutor retryFor(Class<Throwable> retryForThrowable) {
		return this.withRetryPolicy(retryPolicy.retryFor(retryForThrowable));
	}

	public AsyncRetryExecutor abortFor(Class<Throwable> abortForThrowable) {
		return this.withRetryPolicy(retryPolicy.abortFor(abortForThrowable));
	}

	public AsyncRetryExecutor abortIf(Predicate<Throwable> abortPredicate) {
		return this.withRetryPolicy(retryPolicy.abortIf(abortPredicate));
	}

	public AsyncRetryExecutor withUniformJitter() {
		return this.withRetryPolicy(this.retryPolicy.withUniformJitter());
	}

	public AsyncRetryExecutor withUniformJitter(long range) {
		return this.withRetryPolicy(this.retryPolicy.withUniformJitter(range));
	}

	public AsyncRetryExecutor withProportionalJitter() {
		return this.withRetryPolicy(this.retryPolicy.withProportionalJitter());
	}

	public AsyncRetryExecutor withProportionalJitter(double multiplier) {
		return this.withRetryPolicy(this.retryPolicy.withProportionalJitter(multiplier));
	}

	public AsyncRetryExecutor withMinDelay(long minDelayMillis) {
		return this.withRetryPolicy(this.retryPolicy.withMinDelay(minDelayMillis));
	}

	public AsyncRetryExecutor withMaxDelay(long maxDelayMillis) {
		return this.withRetryPolicy(this.retryPolicy.withMaxDelay(maxDelayMillis));
	}

	public AsyncRetryExecutor withMaxRetries(int times) {
		return this.withRetryPolicy(this.retryPolicy.withMaxRetries(times));
	}

	public AsyncRetryExecutor dontRetry() {
		return this.withRetryPolicy(new NeverRetryPolicy());
	}

}
