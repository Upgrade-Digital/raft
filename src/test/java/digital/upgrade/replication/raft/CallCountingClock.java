package digital.upgrade.replication.raft;

public class CallCountingClock implements ClockSource {

  private long time = 0;

  @Override
  public long currentTime() {
    return time++;
  }
}
