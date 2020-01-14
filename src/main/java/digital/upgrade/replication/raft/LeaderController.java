package digital.upgrade.replication.raft;

import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;

public class LeaderController implements Controller {

  private final RaftReplicator replicator;
  private final ScheduledExecutorService executor;
  private final Clock clock;
  private final MessageTransport transport;

  LeaderController(RaftReplicator replicator, ScheduledExecutorService executor,
                   Clock clock, MessageTransport transport) {
    this.replicator = replicator;
    this.executor = executor;
    this.clock = clock;
    this.transport = transport;
  }

  @Override
  public void run() {
    // TODO handle replication requests by shipping them to peers
  }
}
