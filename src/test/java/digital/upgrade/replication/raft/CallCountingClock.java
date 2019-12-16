package digital.upgrade.replication.raft;

public class CallCountingClock implements Clock {

  private Time time = Time.fromEpochMillis(0);

  @Override
  public Time currentTime() {
    return time.plus(1);
  }
}
