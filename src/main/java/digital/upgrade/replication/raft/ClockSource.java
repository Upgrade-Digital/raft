package digital.upgrade.replication.raft;

/**
 * The clock interface used by replicators to determine things like timeout
 * and set alarms.
 */
public interface ClockSource {

  /**
   * Get the current time in milliseconds (from epoch)
   *
   * @return system clock time in milliseconds UTC.
   */
  long currentTime();
}
