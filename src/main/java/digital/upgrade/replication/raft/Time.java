package digital.upgrade.replication.raft;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Helper class for dealing with time related concepts and units.
 */
class Time implements Comparable<Time> {

  private final Instant at;

  Time(long time) {
    at = Instant.ofEpochMilli(time);
  }

  Time(Instant instant) {
    at = instant;
  }

  boolean isOverdue(Clock clock) {
    return at.isBefore(clock.currentTime().at);
  }

  TimeUnit units() {
    return TimeUnit.MILLISECONDS;
  }

  Time plus(Duration duration) {
    return new Time(at.plusMillis(duration.toMillis()));
  }

  long toEpochMillis() {
    return at.toEpochMilli();
  }

  Time plus(long millis) {
    return new Time(at.toEpochMilli() + millis);
  }

  Time minus(long millis) {
    return new Time(at.toEpochMilli() - millis);
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Time)) {
      return false;
    }
    Time compare = (Time)other;
    return at.equals(compare.at);
  }

  @Override
  public int hashCode() {
    return at.hashCode();
  }

  @Override
  public int compareTo(Time time) {
    return at.compareTo(time.at);
  }
}
