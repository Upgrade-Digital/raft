package digital.upgrade.replication.raft;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import digital.upgrade.replication.raft.Raft.AppendRequest;
import digital.upgrade.replication.raft.Raft.AppendResult;
import digital.upgrade.replication.raft.Raft.Peer;
import digital.upgrade.replication.raft.Raft.VoteRequest;
import digital.upgrade.replication.raft.Raft.VoteResult;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LeaderController implements Controller, AppendResponseListener {

  private static final long LEADER_REPLICATION_TIMEOUT = 300;
  private final RaftReplicator replicator;
  private final ScheduledExecutorService executor;
  private final Clock clock;
  private final MessageTransport transport;
  private final Lock lock = new ReentrantLock();
  private final Condition enforceLeadership = lock.newCondition();
  private boolean leader = true;

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
    // Handle timeout for replicating leadership state
    while (leader) {
      sendLeadershipState();
      try {
        enforceLeadership.await(LEADER_REPLICATION_TIMEOUT, TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  private void sendLeadershipState() {
    // TODO consider check whether timed out on previous send

    // Send a leader state replication (no append) heartbeat

    throw new NotImplementedException();
  }

  @Override
  public AppendResult handleAppend(AppendRequest request) {
    // TODO handle an append with a higher leader index

    // Send append requests to all followers
    for (Peer peer : transport.peers()) {
      transport.sendAppend(peer, request, this);
    }
    // reset the timeout for leader replication
    enforceLeadership.signal();
    // wait for append results
    return AppendResult.newBuilder()
        .build();
  }

  @Override
  public VoteResult handleVoteRequest(VoteRequest voteRequest) {
    throw new NotImplementedException();
  }

  @Override
  public void handleResponse(Peer peer, AppendRequest request, AppendResult result) {
    throw new NotImplementedException();
  }
}
