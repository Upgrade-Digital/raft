package digital.upgrade.replication.raft;

import digital.upgrade.replication.raft.Raft.Peer;
import digital.upgrade.replication.raft.Raft.Term;
import digital.upgrade.replication.raft.Raft.VoteRequest;
import digital.upgrade.replication.raft.Raft.VoteResult;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;

public class CandidateController implements Controller, RequestVoteResponseListener {

  private static final Duration ELECTION_TIMEOUT = Duration.ofMillis(100);

  private final RaftReplicator replicator;
  private final ScheduledExecutorService executor;
  private final Clock clock;
  private final MessageTransport transport;
  private Time electionTimeout;

  CandidateController(RaftReplicator replicator,
      ScheduledExecutorService executor, Clock clock,
      MessageTransport transport) {
    this.replicator = replicator;
    this.executor = executor;
    this.clock = clock;
    this.transport = transport;
  }

  @Override
  public void run() {
    Term nextTerm = replicator.incrementTerm();
    replicator.handleVoteRequest(voteRequest(nextTerm));
    electionTimeout = clock.currentTime().plus(ELECTION_TIMEOUT);
    transport.setVoteHandler(this);
    for (Peer peer : transport.peers()) {
      // TODO refactor the vote request callback to pass any successful vote responses to the controller.
      transport.sendRequestVote(peer, voteRequest(nextTerm), this);
    }
  }

  private VoteRequest voteRequest(Term lastTerm) {
    return VoteRequest.newBuilder()
        .setCandidateTerm(replicator.getCurrentTerm())
        .setCandidate(replicator.getSelf())
        .setLastLogTerm(lastTerm)
        .setLastLogIndex(replicator.getCommittedIndex().indexValue())
        .build();
  }

  @Override
  public void handleResponse(VoteRequest voteRequest, VoteResult voteResult) {

  }
}
