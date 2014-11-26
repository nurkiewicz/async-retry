[![Build Status](https://travis-ci.org/nurkiewicz/async-retry.svg?branch=master)](https://travis-ci.org/nurkiewicz/async-retry) [![Coverage Status](https://img.shields.io/coveralls/nurkiewicz/async-retry.svg)](https://coveralls.io/r/nurkiewicz/async-retry) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.nurkiewicz.asyncretry/asyncretry/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.nurkiewicz.asyncretry/asyncretry)

# Asynchronous retry pattern

When you have a piece of code that often fails and must be retried, this Java 7/8 library provides rich and unobtrusive API with fast and scalable solution to this problem:

```java
ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
RetryExecutor executor = new AsyncRetryExecutor(scheduler).
	retryOn(SocketException.class).
	withExponentialBackoff(500, 2).     //500ms times 2 after each retry
	withMaxDelay(10_000).               //10 seconds
	withUniformJitter().                //add between +/- 100 ms randomly
	withMaxRetries(20);
```

You can now run arbitrary block of code and the library will retry it for you in case it throws `SocketException`:

```java
final CompletableFuture<Socket> future = executor.getWithRetry(() ->
		new Socket("localhost", 8080)
);

future.thenAccept(socket ->
		System.out.println("Connected! " + socket)
);
```

Please look carefully! `getWithRetry()` does not block. It returns `CompletableFuture` immediately and invokes given function asynchronously. You can listen for that `Future` or even for multiple futures at once and do other work in the meantime. So what this code does is: trying to connect to `localhost:8080` and if it fails with `SocketException` it will retry after 500 milliseconds (with some random jitter), doubling delay after each retry, but not above 10 seconds.

Equivalent but more concise syntax:

```java
executor.
		getWithRetry(() -> new Socket("localhost", 8080)).
		thenAccept(socket -> System.out.println("Connected! " + socket));
```

This is a sample output that you might expect:

    TRACE | Retry 0 failed after 3ms, scheduled next retry in 508ms (Sun Jul 21 21:01:12 CEST 2013)
    java.net.ConnectException: Connection refused
    	at java.net.PlainSocketImpl.socketConnect(Native Method) ~[na:1.8.0-ea]
    	//...
    
    TRACE | Retry 1 failed after 0ms, scheduled next retry in 934ms (Sun Jul 21 21:01:13 CEST 2013)
    java.net.ConnectException: Connection refused
    	at java.net.PlainSocketImpl.socketConnect(Native Method) ~[na:1.8.0-ea]
    	//...
    
    TRACE | Retry 2 failed after 0ms, scheduled next retry in 1919ms (Sun Jul 21 21:01:15 CEST 2013)
    java.net.ConnectException: Connection refused
    	at java.net.PlainSocketImpl.socketConnect(Native Method) ~[na:1.8.0-ea]
    	//...
    
    TRACE | Successful after 2 retries, took 0ms and returned: Socket[addr=localhost/127.0.0.1,port=8080,localport=46332]

    Connected! Socket[addr=localhost/127.0.0.1,port=8080,localport=46332]
    
Imagine you connect to two different systems, one is *slow*, second *unreliable* and fails often:

```java
CompletableFuture<String> stringFuture = executor.getWithRetry(ctx -> unreliable());
CompletableFuture<Integer> intFuture = executor.getWithRetry(ctx -> slow());

stringFuture.thenAcceptBoth(intFuture, (String s, Integer i) -> {
	//both done after some retries
});
```

`thenAcceptBoth()` callback is executed asynchronously when both slow and unreliable systems finally reply without any failure. Similarly (using `CompletableFuture.acceptEither()`) you can call two or more unreliable servers asynchronously at the same time and be notified when the first one succeeds after some number of retries.

I can't emphasize this enough - retries are executed asynchronously and effectively use thread pool, rather than sleeping blindly.

## Rationale

Often we are forced to [retry](http://servicedesignpatterns.com/WebServiceInfrastructures/IdempotentRetry) given piece of code because it failed and we must try again, typically with a small delay to spare CPU. This requirement is quite common and there are few ready-made generic implementations with [retry support in Spring Batch](http://static.springsource.org/spring-batch/reference/html/retry.html) through [`RetryTemplate`](http://static.springsource.org/spring-batch/2.1.x/apidocs/org/springframework/batch/retry/support/RetryTemplate.html) class being best known. But there are few other, quite similar approaches ([[1]](http://fahdshariff.blogspot.no/2009/08/retrying-operations-in-java.html), [[2]](https://github.com/Ninja-Squad/ninja-core/tree/master/src/main/java/com/ninja_squad/core/retry)). All of these attempts (and I bet many of you implemented similar tool yourself!) suffer the same issue - they are blocking, thus wasting a lot of resources and not scaling well.

This is not bad *per se* because it makes programming model much simpler - the library takes care of retrying and you simply have to wait for return value longer than usual. But not only it creates leaky abstraction (method that is typically very fast suddenly becomes slow due to retries and delay), but also wastes valuable threads since such facility will spend most of the time sleeping between retries. Therefore [`Async-Retry`](https://github.com/nurkiewicz/async-retry) utility was created, targeting **Java 8** (with [Java 7 backport](https://github.com/nurkiewicz/async-retry/tree/java7) existing) and addressing issues above.

The main abstraction is [`RetryExecutor`](https://github.com/nurkiewicz/async-retry/blob/master/src/main/java/com/nurkiewicz/asyncretry/RetryExecutor.java) that provides simple API:

```java
public interface RetryExecutor {

	CompletableFuture<Void> doWithRetry(RetryRunnable action);

	<V> CompletableFuture<V> getWithRetry(Callable<V> task);

	<V> CompletableFuture<V> getWithRetry(RetryCallable<V> task);

	<V> CompletableFuture<V> getFutureWithRetry(RetryCallable<CompletableFuture<V>> task);
}
```

Don't worry about [`RetryRunnable`](https://github.com/nurkiewicz/async-retry/blob/master/src/main/java/com/nurkiewicz/asyncretry/function/RetryRunnable.java) and [`RetryCallable`](https://github.com/nurkiewicz/async-retry/blob/master/src/main/java/com/nurkiewicz/asyncretry/function/RetryCallable.java) - they allow checked exceptions for your convenience and most of the time we will use lambda expressions anyway.

Please note that it returns [`CompletableFuture`](http://nurkiewicz.blogspot.no/2013/05/java-8-definitive-guide-to.html). We no longer pretend that calling faulty method is fast. If the library encounters an exception it will retry our block of code with preconfigured backoff delays. The invocation time will sky-rocket from milliseconds to several seconds. `CompletableFuture` clearly indicates that. Moreover it's not a dumb [`java.util.concurrent.Future`](http://nurkiewicz.blogspot.no/2013/02/javautilconcurrentfuture-basics.html) we all know - [`CompletableFuture` in Java 8 is very powerful](http://nurkiewicz.blogspot.no/2013/05/java-8-completablefuture-in-action.html) and most importantly - non-blocking by default.

If you need blocking result after all, just call `.get()` on `Future` object.

## Basic API

The API is very simple. You provide a block of code and the library will run it multiple times until it returns normally rather than throwing an exception. It may also put configurable delays between retries:

```java
RetryExecutor executor = //see "Creating an instance of RetryExecutor" below

executor.getWithRetry(() -> new Socket("localhost", 8080));
```

Returned `CompletableFuture<Socket>` will be resolved once connecting to `localhost:8080` succeeds. Optionally we can consume [`RetryContext`](https://github.com/nurkiewicz/async-retry/blob/master/src/main/java/com/nurkiewicz/asyncretry/RetryContext.java) to get extra context like which retry is currently being executed:

```java
executor.
	getWithRetry(ctx -> new Socket("localhost", 8080 + ctx.getRetryCount())).
	thenAccept(System.out::println);
```

This code is more clever than it looks. During first execution `ctx.getRetryCount()` returns `0`, therefore we try to connect to `localhost:8080`. If this fails, next retry will try `localhost:8081` (`8080 + 1`) and so on. And if you realize that all of this happens asynchronously you can scan ports of several machines and be notified about first responding port on each host:

```java
Arrays.asList("host-one", "host-two", "host-three").
	stream().
	forEach(host ->
		executor.
			getWithRetry(ctx -> new Socket(host, 8080 + ctx.getRetryCount())).
			thenAccept(System.out::println)
	);
```

For each host `RetryExecutor` will attempt to connect to port 8080 and retry with higher ports. 

`getFutureWithRetry()` requires special attention. I you want to retry method that already returns `CompletableFuture<V>`: e.g. result of asynchronous HTTP call:

```java
private CompletableFuture<String> asyncHttp(URL url) { /*...*/}

//...

final CompletableFuture<CompletableFuture<String>> response =
	executor.getWithRetry(ctx ->
		asyncHttp(new URL("http://example.com")));
```

Passing `asyncHttp()` to `getWithRetry()` will yield `CompletableFuture<CompletableFuture<V>>`. Not only it's awkward to work with, but also broken. The library will barely call `asyncHttp()` and retry only if it fails, but not if returned `CompletableFuture<String>` fails. The solution is simple:

```java
final CompletableFuture<String> response =
	executor.getFutureWithRetry(ctx ->
		asyncHttp(new URL("http://example.com")));
```

In this case `RetryExecutor` will understand that whatever was returned from `asyncHttp()` is actually just a `Future` and will (asynchronously) wait for result or failure. Speaking of which, in some cases despite retrying `RetryExecutor` will fail to obtain successful result. In general there are three possible outcomes of returned `CompletableFuture`:

1. *successful* - (possibly after some number of retries) - when our function eventually returns rather than throws

2. *exceptional* due to excessive retries - if you configure finite number of retries, `RetryExecutor` will eventually give up. In that case `Future` is completed exceptionally, providing last encountered exception

3. *exceptional* due to exception that should not be retried (see e.g. `abortOn()` and `abortIf()`) - just as above last encountered exception completes `Future`.

You can handle all these cases with the following by using [`CompletableFuture.whenComplete()`](http://download.java.net/lambda/b102/docs/api/java/util/concurrent/CompletableFuture.html#whenComplete(java.util.function.BiConsumer)) or [`CompletableFuture.handle()`](http://download.java.net/lambda/b102/docs/api/java/util/concurrent/CompletableFuture.html#handle(java.util.function.BiFunction)):

```java
executor.
	getWithRetry(() -> new Socket("localhost", 8080)).
	whenComplete((socket, error) -> {
		if (socket != null) {
			//connected OK, proceed
		} else {
			log.error("Can't connect, last error:", error);
		}
	});
```

## Configuration options

In general there are two important factors you can configure: [`RetryPolicy`](https://github.com/nurkiewicz/async-retry/blob/master/src/main/java/com/nurkiewicz/asyncretry/policy/RetryPolicy.java) that controls whether next retry attempt should be made and [`Backoff`](https://github.com/nurkiewicz/async-retry/blob/master/src/main/java/com/nurkiewicz/asyncretry/backoff/Backoff.java) - that optionally adds delay between subsequent retry attempts.

By default `RetryExecutor` repeats user task infinitely on every `	Throwable` and adds 1 second delay between retry attempts.

### Creating an instance of `RetryExecutor`

Default implementation of `RetryExecutor` is [`AsyncRetryExecutor`](https://github.com/nurkiewicz/async-retry/blob/master/src/main/java/com/nurkiewicz/asyncretry/AsyncRetryExecutor.java) which you can create directly:

```java
ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

RetryExecutor executor = new AsyncRetryExecutor(scheduler);

//...

scheduler.shutdownNow();
```

The only required dependency is standard [`ScheduledExecutorService` from JDK](http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ScheduledExecutorService.html). One thread is enough in many cases but if you want to concurrently handle retries of hundreds or more tasks, consider increasing the pool size.

Notice that the `AsyncRetryExecutor` does not take care of shutting down the `ScheduledExecutorService`. This is a conscious design decision which will be explained later.

`AsyncRetryExecutor` has few other constructors but most of the time altering the behaviour of this class is most convenient with calling chained `with*()` methods. You will see plenty of examples written this way. Later on we will simply use `executor` reference without defining it. Assume it's of `RetryExecutor` type.

### Retrying policy

#### Exception classes

By default every `Throwable` (except special `AbortRetryException`) thrown from user task causes retry. Obviously this is configurable. For example in JPA you may want to retry a transaction that failed due to [`OptimisticLockException`](http://docs.oracle.com/javaee/6/api/javax/persistence/OptimisticLockException.html) - but every other exception should fail immediately:

```java
executor.
	retryOn(OptimisticLockException.class).
	withNoDelay().
	getWithRetry(ctx -> dao.optimistic());
```

Where `dao.optimistic()` may throw `OptimisticLockException`. In that case you probably don't want any delay between retries, more on that later. If you don't like the default of retrying on every `Throwable`, just restrict that using `retryOn()`:

```java
executor.retryOn(Exception.class)
```

Of course the opposite might also be desired - to abort retrying and fail immediately in case of certain exception being thrown rather than retrying. It's that simple:

```java
executor.
	abortOn(NullPointerException.class).
	abortOn(IllegalArgumentException.class).
	getWithRetry(ctx -> dao.optimistic());
```

Clearly you don't want to retry `NullPointerException` or `IllegalArgumentException` as they indicate programming bug rather than transient failure. And finally you can combine both retry and abort policies. User code will retry in case of any `retryOn()` exception (or subclass) unless it should `abortOn()` specified exception. For example we want to retry every `IOException` or `SQLException` but abort if `FileNotFoundException` or `java.sql.DataTruncation` is encountered (order of declarations is irrelevant):

```java
executor.
	retryOn(IOException.class).
	abortOn(FileNotFoundException.class).
	retryOn(SQLException.class).
	abortOn(DataTruncation.class).
	getWithRetry(ctx -> dao.load(42));
```
#### Exception predicates

If this is not enough you can provide custom predicate that will be invoked on each failure:

```java
executor.
	abortIf(throwable ->
		throwable instanceof SQLException &&
				throwable.getMessage().contains("ORA-00911")	
	).
	retryIf(t -> t.getCause() != null);
```

If any of `abortIf()` or `retryIf()` predicates return `true` task is aborted or retried respectively. Keep in mind that `abortIf()`/`retryIf()` take priority over `abortOn()`/`retryOn()` thus the following piece of code will retry on `FileNotFoundException("Access denied")`:

```java
executor.
		abortOn(FileNotFoundException.class).
		abortOn(NullPointerException.class).
		retryIf(e -> e.getMessage().contains("denied"))
``` 

If more than one `abortIf()` predicate passes as well as more than one `retryIf()` predicate then computation is aborted.

#### Max number of retries

Another way of interrupting retrying "loop" (remember that this process is asynchronous, there is no blocking *loop*) is by specifying maximum number of retries:

```java
executor.withMaxRetries(5)
```

In rare cases you may want to disable retries and barely take advantage from asynchronous execution. In that case try:

```java
executor.dontRetry()
```

Max number of retries takes precedence over `*On()` and `*If()` family of methods.

### Delays between retries (backoff)

Retrying immediately after failure is sometimes desired (see `OptimisticLockException` example) but in most cases it's a bad idea. If you can't connect to external system, waiting a little bit before next attempt sounds reasonably. You save CPU, bandwidth and other server's resources. But there are quite a few options to consider: 

* should we retry with constant intervals or [increase delay after each failure](http://en.wikipedia.org/wiki/Exponential_backoff)?

* should there be a lower and upper limit on waiting time?

* should we add random "jitter" to delay times to spread retries of many tasks in time?

This library answers all these questions.

#### Fixed interval between retries

By default each retry is preceded by 1 second waiting time. So if initial attempt fails, first retry will be executed after 1 second. Of course we can change that default, e.g. to 200 milliseconds:

```java
executor.withFixedBackoff(200)
```

If we are already here, by default backoff is applied after executing user task. If user task itself consumes some time, retries will be less frequent. For example with retry delay of 200ms and average time it takes before user task fails at about 50ms `RetryExecutor` will retry about 4 times per second (50ms + 200ms). However if you want to keep retry frequency at more predictable level you can use `fixedRate` flag:

```java
executor.
	withFixedBackoff(200).
	withFixedRate()
```

This is similar to "fixed rate" vs. "fixed delay" approaches in [`ScheduledExecutorService`](http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ScheduledExecutorService.html). BTW don't expect `RetryExecutor` to be very precise, it does it's best but it heavily depends on aforementioned `ScheduledExecutorService` accuracy.

#### Exponentially growing intervals between retries

It's probably an active research subject, but in general you may wish to expand retry delay over time, assuming that if the user task fails several times we should try less frequently. For example let's say we start with 100ms delay until first retry attempt is made but if that one fails as well, we should wait two times more (200ms). And later 400ms, 800ms... You get the idea:

```java
executor.withExponentialBackoff(100, 2)
```

This is an exponential function that can grow very fast. Thus it's useful to set maximum backoff time at some reasonable level, e.g. 10 seconds:

```java
executor.
	withExponentialBackoff(100, 2).
	withMaxDelay(10_000)      //10 seconds
```

#### Random jitter

One phenomena often observed during major outages is that systems tend to synchronize. Imagine a busy system that suddenly stops responding. Hundreds or thousands of requests fail and are retried. It depends on your backoff but by default all these requests will retry exactly after one second producing huge wave of traffic at one point in time. Finally such failures are propagated to other systems that, in turn, synchronize as well.

To avoid this problem it's useful to spread retries over time, flattening the load. A simple solution is to add random jitter to delay time so that not all request are scheduled for retry at the exact same time. You have choice between uniform jitter (random value from -100ms to 100ms):

```java
executor.withUniformJitter(100)     //ms
```

...and proportional jitter, multiplying delay time by random factor, by default between 0.9 and 1.1 (10%):

```java
executor.withProportionalJitter(0.1)        //10%
```

You may also put hard lower limit on delay time to avoid to short retry times being scheduled:

```java
executor.withMinDelay(50)   //ms
```

## Implementation details

This library was built with Java 8 in mind to take advantage of lambdas and new `CompletableFuture` abstraction (but [Java 7 port with Guava dependency exists](https://github.com/nurkiewicz/async-retry/tree/java7)). It uses `ScheduledExecutorService` underneath to run tasks and schedule retries in the future - which allows best thread utilization.

But what is really interesting is that the whole library is fully immutable, there is no single mutable field, at all. This might be counter-intuitive at first, take for example this trivial code sample:

```java
ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

AsyncRetryExecutor first = new AsyncRetryExecutor(scheduler).
	retryOn(Exception.class).
	withExponentialBackoff(500, 2);

AsyncRetryExecutor second = first.abortOn(FileNotFoundException.class);

AsyncRetryExecutor third = second.withMaxRetries(10);
```

It might seem that all `with*()` methods or `retryOn()`/`abortOn()` mutate existing executor. But that's not the case, each configuration change **creates new instance**, leaving the old one untouched. So for example while `first` executor will retry on `FileNotFoundException`, the `second` and `third` won't. However they all share the same `scheduler`. This is the reason why `AsyncRetryExecutor` does not shut down `ScheduledExecutorService` (it doesn't even have any `close()` method). Since we have no idea how many copies of `AsyncRetryExecutor` exist pointing to the same scheduler, we don't even try to manage its lifecycle. However this is typically not a problem (see *Spring integration* below).

You might be wondering, why such an awkward design decision? There are three reasons:

* when writing a concurrent code immutability can greatly reduce risk of multi-threading bugs. For example `RetryContext` holds number of retries. But instead of mutating it we simply create new instance (copy) with incremented but `final` counter. No race condition or visibility can ever occur.

* if you are given an existing `RetryExecutor` which is almost exactly what you want but you need one minor tweak, you simply call `executor.with...()` and get a fresh copy. You don't have to worry about other places where the same executor was used (see: *Spring integration* for further examples)

* functional programming and immutable data structures are *sexy* these days ;-).

N.B.: `AsyncRetryExecutor` is **not** marked `final`, does you can break immutability by subclassing it and adding mutable state. Please don't do this, subclassing is only permitted to alter behaviour.

## Dependencies

This library requires Java 8 and [SLF4J](http://www.slf4j.org/) for logging. Java 7 port additionally depends on [Guava](http://code.google.com/p/guava-libraries/).

## Spring integration

If you are just about to use `RetryExecutor` in Spring - feel free, but the configuration API might not work for you. Spring promotes (or used to promote) the convention of mutable services with plenty of setters. In XML you define bean and invoke setters (via `<property name="..."/>`) on it. This convention assumes the existence of mutating setters. But I found this approach error-prone and counter-intuitive under some circumstances.

Let's say we globally defined [`org.springframework.transaction.support.TransactionTemplate`](http://static.springsource.org/spring/docs/current/javadoc-api/org/springframework/transaction/support/TransactionTemplate.html) bean and injected it in multiple places. Great. Now there is this one single request that requires slightly different timeout:

```java
@Autowired
private TransactionTemplate template;
```

and later in the same class:
    
```java
final int oldTimeout = template.getTimeout();
template.setTimeout(10_000);
//do the work
template.setTimeout(oldTimeout);
```

This code is wrong on so many levels! First of all if something fails we never restore `oldTimeout`. OK, `finally` to the rescue. But also notice how we changed global, shared `TransactionTemplate` instance. Who knows how many other beans and threads are just about to use it, unaware of changed configuration?

And even if you do want to globally change the transaction timeout, fair enough, but it's still wrong way to do this. `private timeout` field is not `volatile` and thus changes made to it may or may not be visible to other threads. What a mess! The same problem appears with many other classes like [`JmsTemplate`](http://static.springsource.org/spring/docs/3.2.x/javadoc-api/org/springframework/jms/core/JmsTemplate.html).

You see where I'm going? Just create one, immutable service class and safely adjust it by creating copies whenever you need it. And using such services is equally simple these days:

```java
@Configuration
class Beans {

	@Bean
	public RetryExecutor retryExecutor() {
		return new AsyncRetryExecutor(scheduler()).
			retryOn(SocketException.class).
			withExponentialBackoff(500, 2);
	}

	@Bean(destroyMethod = "shutdownNow")
	public ScheduledExecutorService scheduler() {
		return Executors.newSingleThreadScheduledExecutor();
	}

}
```

Hey! It's 21st century, we don't need XML in Spring any more. Bootstrap is simple as well:

```java
final ApplicationContext context = new AnnotationConfigApplicationContext(Beans.class);
final RetryExecutor executor = context.getBean(RetryExecutor.class);
//...
context.close();
```

As you can see integrating modern, immutable services with Spring is just as simple. BTW if you are not prepared for such a big change when designing your own services, at least consider [constructor injection](http://nurkiewicz.blogspot.no/2011/09/evolution-of-spring-dependency.html).

## Maturity

This library is covered with a strong battery of unit tests ([![Build Status](https://travis-ci.org/nurkiewicz/async-retry.svg?branch=master)](https://travis-ci.org/nurkiewicz/async-retry)). However it wasn't yet used in any production code and the API is subject to change. Of course you are encouraged to submit [bugs, feature requests](https://github.com/nurkiewicz/async-retry/issues) and [pull requests](https://github.com/nurkiewicz/async-retry/pulls). It was developed with Java 8 in mind but [Java 7 backport](https://github.com/nurkiewicz/async-retry/tree/java7) exists with slightly more verbose API and mandatory Guava dependency ([`ListenableFuture`](http://nurkiewicz.blogspot.no/2013/02/listenablefuture-in-guava.html) instead of [`CompletableFuture` from Java 8](http://nurkiewicz.blogspot.no/2013/05/java-8-definitive-guide-to.html)).

## Using

### Maven

This library is available in [Maven Central Repository](http://search.maven.org):

```xml
<dependency>
    <groupId>com.nurkiewicz.asyncretry</groupId>
    <artifactId>asyncretry</artifactId>
    <version>0.0.6</version>
</dependency>
```


### Maven (Java 7)

Because backport to Java 7 has different API, it is maintained in a [separate branch](https://github.com/nurkiewicz/async-retry/tree/java7). It is also deployed to Maven Central under:

```xml
<dependency>
    <groupId>com.nurkiewicz.asyncretry</groupId>
    <artifactId>asyncretry-jdk7</artifactId>
    <version>0.0.6</version>
</dependency>
```

### Troubleshooting: `invalid target release: 1.8` during maven build

If you see this error message during maven build:

	[INFO] BUILD FAILURE
	...
	[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.1:compile (default-compile) on project lazyseq: 
	Fatal error compiling: invalid target release: 1.8 -> [Help 1]

it means you are not compiling using Java 8. [Download JDK 8 with lambda support](https://jdk8.java.net/lambda/) and let maven use it:

	$ export JAVA_HOME=/path/to/jdk8


## Version history

### 0.0.6 (26-11-2014)

* [`SyncRetryExecutor`](https://github.com/nurkiewicz/async-retry/blob/0.0.6/src/main/java/com/nurkiewicz/asyncretry/SyncRetryExecutor.java) convenience class.

### 0.0.5 (31-05-2014)

* Bringing back Java 7 support

### 0.0.4 (30-05-2014)

* First official release into [Maven Central repository](http://central.maven.org/maven2/com/nurkiewicz/asyncretry/asyncretry).

### 0.0.3 (05-01-2014)

* Fixed [#3 *RetryOn ignored due to wrong command order*](https://github.com/nurkiewicz/async-retry/issues/3)
* `AbortRetryException` class was moved from `com.nurkiewicz.asyncretry.policy.exception` to `com.nurkiewicz.asyncretry.policy`
* Java 7 backport is no longer maintained starting from this version

### 0.0.2 (28-07-2013)

* Ability to specify multiple exception classes in `retryOn()`/`abortON()` using varargs

### 0.0.1 (23-07-2013)

* Initial revision
