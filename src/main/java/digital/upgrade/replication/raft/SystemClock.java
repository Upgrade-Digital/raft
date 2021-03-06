package digital.upgrade.replication.raft;

/**
 * Implementation of ClockSource which uses the underlying system clock UTC milliseconds since Epoch.
 */
public final class SystemClock implements Clock {

  @Override
  public Time currentTime() {
    return Time.fromEpochMillis(java.time.Clock.systemUTC().millis());
  }
}
