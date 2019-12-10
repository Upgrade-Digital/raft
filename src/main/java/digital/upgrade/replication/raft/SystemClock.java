package digital.upgrade.replication.raft;

/**
 * Implementation of ClockSource which uses the underlying system clock UTC milliseconds since Epoch.
 */
public final class SystemClock implements Clock {

  @Override
  public long currentTime() {
    return java.time.Clock.systemUTC().millis();
  }
}
