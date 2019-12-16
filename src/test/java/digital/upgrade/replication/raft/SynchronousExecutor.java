package digital.upgrade.replication.raft;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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

  @Override @Nonnull
  public ScheduledFuture<?> schedule(@Nonnull Runnable runnable, long delay, @Nonnull TimeUnit timeUnit) {
    SynchronousScheduledFuture future = new SynchronousScheduledFuture(runnable, delay);
    Time at = clock.currentTime().plus(delay);
    schedule.put(at, future);
    return future;
  }

  @Override
  public void shutdown() {
    schedule.values().forEach(task -> task.cancel(false));
  }

  @Override @Nonnull
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
  public boolean awaitTermination(long l, @Nonnull TimeUnit timeUnit) {
    throw new UnsupportedOperationException();
  }

  @Override @Nonnull
  public <T> Future<T> submit(@Nonnull Callable<T> callable) {
    throw new UnsupportedOperationException();
  }

  @Override @Nonnull
  public <T> Future<T> submit(@Nonnull Runnable runnable, T t) {
    throw new UnsupportedOperationException();
  }

  @Override @Nonnull
  public Future<?> submit(@Nonnull Runnable runnable) {
    throw new UnsupportedOperationException();
  }

  @Override @Nonnull
  public <T> List<Future<T>> invokeAll(@Nonnull Collection<? extends Callable<T>> collection) {
    throw new UnsupportedOperationException();
  }

  @Override @Nonnull
  public <T> List<Future<T>> invokeAll(@Nonnull Collection<? extends Callable<T>> collection, long l, @Nonnull TimeUnit timeUnit) {
    throw new UnsupportedOperationException();
  }

  @Override @Nonnull
  public <T> T invokeAny(@Nonnull Collection<? extends Callable<T>> collection) {
    throw new UnsupportedOperationException();
  }

  @Override @Nonnull
  public <T> T invokeAny(@Nonnull Collection<? extends Callable<T>> collection, long l, @Nonnull TimeUnit timeUnit) {
    throw new UnsupportedOperationException();
  }

  @Override @Nonnull
  public <V> ScheduledFuture<V> schedule(@Nonnull Callable<V> callable, long l, @Nonnull TimeUnit timeUnit) {
    throw new UnsupportedOperationException();
  }

  @Override @Nonnull
  public ScheduledFuture<?> scheduleAtFixedRate(@Nonnull Runnable runnable, long l, long l1, @Nonnull TimeUnit timeUnit) {
    throw new UnsupportedOperationException();
  }

  @Override @Nonnull
  public ScheduledFuture<?> scheduleWithFixedDelay(@Nonnull Runnable runnable, long l, long l1, @Nonnull TimeUnit timeUnit) {
    throw new UnsupportedOperationException();
  }

  static class SynchronousScheduledFuture implements ScheduledFuture {

    private final Runnable delegate;
    private final long delay;
    private boolean cancelled = false;
    private boolean done = false;

    SynchronousScheduledFuture(Runnable run, long delay) {
      this.delegate = run;
      this.delay = delay;
    }

    @Override
    public long getDelay(@Nonnull TimeUnit timeUnit) {
      // TODO handle conversion units
      return delay;
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
      if (cancelled) {
        return null;
      }
      delegate.run();
      done = true;
      return null;
    }

    @Override
    public Object get(long l, @Nonnull TimeUnit timeUnit) {
      throw new UnsupportedOperationException();
    }

    @Override
    public int compareTo(Delayed delayed) {
      return Long.compare(delay, delayed.getDelay(TimeUnit.MILLISECONDS));
    }
  }
}
