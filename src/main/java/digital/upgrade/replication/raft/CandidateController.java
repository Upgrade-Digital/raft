package digital.upgrade.replication.raft;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;

import digital.upgrade.replication.raft.Raft.Peer;
import digital.upgrade.replication.raft.Raft.Term;
import digital.upgrade.replication.raft.Raft.VoteRequest;

public class CandidateController implements Controller, RequestVoteListener {

  private static final Duration ELECTION_TIMEOUT = Duration.ofMillis(100);

  private final RaftReplicator replicator;
  private final ScheduledExecutorService executor;
  private final Clock clock;
  private final MessageTransport transport;
  private Time electionTimeout;

  CandidateController(RaftReplicator replicator, ScheduledExecutorService executor, Clock clock,
                      MessageTransport transport) {
    this.replicator = replicator;
    this.executor = executor;
    this.clock = clock;
    this.transport = transport;
  }

  @Override
  public void run() {
    Term lastTerm = replicator.incrementTerm();
    replicator.requestVote(voteRequest(lastTerm));
    electionTimeout = clock.currentTime().plus(ELECTION_TIMEOUT);
    transport.setVoteListener(this);
    for (Peer peer : transport.peers()) {
      // TODO refactor the vote request callback to pass any successful vote responses to the controller.
      transport.sendRequestVote(peer, voteRequest(lastTerm));
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
}
