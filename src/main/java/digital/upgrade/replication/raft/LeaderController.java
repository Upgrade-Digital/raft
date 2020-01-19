package digital.upgrade.replication.raft;

import digital.upgrade.replication.raft.Raft.AppendRequest;
import digital.upgrade.replication.raft.Raft.AppendResult;
import digital.upgrade.replication.raft.Raft.VoteRequest;
import digital.upgrade.replication.raft.Raft.VoteResult;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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

  @Override
  public AppendResult handleAppend(AppendRequest request) {
    throw new NotImplementedException();
  }

  @Override
  public VoteResult handleVoteRequest(VoteRequest voteRequest) {
    throw new NotImplementedException();
  }
}
