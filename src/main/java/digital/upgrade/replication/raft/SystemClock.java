package digital.upgrade.replication.raft;

import java.time.Clock;

/**
 * Implementation of ClockSource which uses the underlying system clock UTC milliseconds since Epoch.
 */
public final class SystemClock implements ClockSource {

    @Override
    public long currentTime() {
        return Clock.systemUTC().millis();
    }
}
