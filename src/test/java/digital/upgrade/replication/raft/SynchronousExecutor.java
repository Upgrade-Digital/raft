package digital.upgrade.replication.raft;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SynchronousExecutor implements ScheduledExecutorService {

  private final Clock clock;
  private final Map<Time, SynchronousScheduledFuture> schedule = new TreeMap<>();

  SynchronousExecutor(Clock clock) {
    this.clock = clock;
  }

  @Override
  public void execute(Runnable runnable) {
    runnable.run();
  }

  @Override
  public ScheduledFuture<?> schedule(Runnable runnable, long delay, TimeUnit timeUnit) {
    SynchronousScheduledFuture future = new SynchronousScheduledFuture(runnable, delay);
    Time at = clock.currentTime().plus(delay);
    schedule.put(at, future);
    return future;
  }

  @Override
  public void shutdown() {
    schedule.values().forEach(task -> task.cancel(false));
  }

  @Override
  public List<Runnable> shutdownNow() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isShutdown() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isTerminated() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean awaitTermination(long l, TimeUnit timeUnit) throws InterruptedException {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> Future<T> submit(Callable<T> callable) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> Future<T> submit(Runnable runnable, T t) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Future<?> submit(Runnable runnable) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> collection) throws InterruptedException {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> collection, long l, TimeUnit timeUnit) throws InterruptedException {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> collection) throws InterruptedException, ExecutionException {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> collection, long l, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> ScheduledFuture<V> schedule(Callable<V> callable, long l, TimeUnit timeUnit) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, long l, long l1, TimeUnit timeUnit) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ScheduledFuture<?> scheduleWithFixedDelay(Runnable runnable, long l, long l1, TimeUnit timeUnit) {
    throw new UnsupportedOperationException();
  }

  public static class SynchronousScheduledFuture implements ScheduledFuture {

    private final Runnable run;
    private final long delay;
    private boolean cancelled = false;

    SynchronousScheduledFuture(Runnable run, long delay) {
      this.run = run;
      this.delay = delay;
    }

    @Override
    public long getDelay(TimeUnit timeUnit) {
      // TODO handle conversion units
      return delay;
    }

    @Override
    public int compareTo(Delayed delayed) {
      return Long.compare(delay, delayed.getDelay(TimeUnit.MILLISECONDS));
    }

    @Override
    public boolean cancel(boolean b) {
      return !done;
    }

    @Override
    public boolean isCancelled() {
      return cancelled;
    }

    @Override
    public boolean isDone() {
      return done;
    }

    @Override
    public Object get() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Object get(long l, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
      throw new UnsupportedOperationException();
    }
  }
}
