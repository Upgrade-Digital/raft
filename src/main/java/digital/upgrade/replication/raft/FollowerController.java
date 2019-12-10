package digital.upgrade.replication.raft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import digital.upgrade.replication.raft.Raft.AppendRequest;

/**
 * Followers are responsible for handling calls from candidates and leaders via the transport and tracking timeout
 * of append entries calls from the leader to trigger an election.
 */
public class FollowerController implements Controller {

  private static final Logger LOG = LoggerFactory.getLogger(FollowerController.class);
  private static final long DEFAULT_TIMEOUT = 100;
  private static final int DEFAULT_QUEUE_CAPACITY = 1024;

  private final RaftReplicator replicator;
  private final Clock clock;
  private boolean run = true;
  private long timeout = DEFAULT_TIMEOUT;

  public FollowerController(RaftReplicator replicator, Clock clock) {
    this.replicator = replicator;
    this.clock = clock;
  }

  @Override
  public void run() {
    while (run) {

    }
  }

  @Override
  public void shutdown() {
    run = false;
  }
}
