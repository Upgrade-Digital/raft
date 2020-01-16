package digital.upgrade.replication.raft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import digital.upgrade.replication.raft.Raft.Peer;
import digital.upgrade.replication.raft.Raft.Term;
import digital.upgrade.replication.raft.Raft.VoteRequest;
import digital.upgrade.replication.raft.Raft.VoteResult;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CandidateController implements Controller, RequestVoteResponseListener {

  private static final Duration ELECTION_TIMEOUT = Duration.ofMillis(100);
  private static final Logger LOG = LoggerFactory.getLogger(CandidateController.class);

  private final RaftReplicator replicator;
  private final ScheduledExecutorService executor;
  private final MessageTransport transport;
  private CountDownLatch votes;

  CandidateController(RaftReplicator replicator,
      ScheduledExecutorService executor,
      MessageTransport transport) {
    this.replicator = replicator;
    this.executor = executor;
    this.transport = transport;
  }

  @Override
  public void run() {
    Term nextTerm = replicator.incrementTerm();
    replicator.handleVoteRequest(voteRequest(nextTerm));
    Collection<Peer> peers = transport.peers();
    int peerCount = peers.size();
    votes = new CountDownLatch(peerCount);
    for (Peer peer : peers) {
      transport.sendRequestVote(peer, voteRequest(nextTerm), this);
    }
    boolean winner;
    try {
      winner = votes.await(ELECTION_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      LOG.info("Candidate election interrupted");
      winner = false;
    }
    if (winner || votes.getCount() <= (peerCount / 2)) {
      replicator.convertToLeader();
    } else {
      replicator.convertToCandidate();
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
  public void handleResponse(Peer peer, VoteRequest voteRequest, VoteResult voteResult) {
    if (voteResult.getVoteGranted()) {
      LOG.debug("Peer {} granted vote in term {}", peer, voteRequest.getCandidateTerm());
      votes.countDown();
    } else {
      LOG.debug("Peer {} rejected vote for {} in term {}", peer, voteRequest.getCandidate(),
          voteResult.getVoterTerm());
    }
  }
}
