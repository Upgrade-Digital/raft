package digital.upgrade.replication.raft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Followers are responsible for handling calls from candidates and leaders via the transport and tracking timeout
 * of append entries calls from the leader to trigger an election.
 */
public class FollowerController implements Controller {

  private static final Logger LOG = LoggerFactory.getLogger(FollowerController.class);
  private static final long DEFAULT_TIMEOUT = 100;

  private final RaftReplicator replicator;
  private final ScheduledExecutorService executor;
  private final Clock clock;
  private long timeout = DEFAULT_TIMEOUT;

  FollowerController(RaftReplicator replicator, ScheduledExecutorService executor, Clock clock) {
    this.replicator = replicator;
    this.executor = executor;
    this.clock = clock;
  }

  @Override
  public void run() {
    // check that the append haven't timed out
    Time lastAppend = replicator.getLastAppendTime();
    Time when = lastAppend.plus(timeout);
    if (when.isOverdue(clock)) {
      LOG.debug("Follower found append overdue. Converting to a candidate");
      replicator.convertToCandidate();
    } else {
      LOG.debug("Follower found recent append. Waiting until next overdue");
      executor.schedule(this, when.toEpochMillis(), when.units());
    }
  }
}
