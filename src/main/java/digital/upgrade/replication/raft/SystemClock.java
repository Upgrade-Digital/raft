package digital.upgrade.replication.raft;

import java.time.Clock;

public final class SystemClock implements ClockSource {

    @Override
    public long currentTime() {
        return Clock.systemUTC().millis();
    }
}
