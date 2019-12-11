package digital.upgrade.replication.raft;

public class CallCountingClock implements Clock {

  private Time time = new Time(0);

  @Override
  public Time currentTime() {
    return time.plus(1);
  }
}
