package digital.upgrade.replication.raft;

import org.testng.annotations.Test;

import java.util.UUID;

import digital.upgrade.replication.raft.Raft.Index;
import digital.upgrade.replication.raft.Raft.Peer;
import digital.upgrade.replication.raft.Raft.Term;
import digital.upgrade.replication.raft.Raft.VoteRequest;
import digital.upgrade.replication.raft.Raft.VoteResult;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class RaftReplicatorVoteTest {

  private static final Index FIRST_LOG_INDEX = CommitIndex.ZERO;
  private static final Term FIRST_TERM = Term.newBuilder()
      .setNumber(0)
      .build();

  @Test
  public void testVoteRequestReturnsNonNull() {
    RaftReplicator replicator = RaftReplicatorStateTest.startedReplicator();
    VoteResult result = replicator.requestVote(VoteRequest.newBuilder()
        .setCandidateTerm(Term.newBuilder()
            .setNumber(0)
            .build())
        .setCandidate(Peer.newBuilder()
            .setUuid(UUID.randomUUID().toString())
            .build())
        .setLastLogIndex(FIRST_LOG_INDEX)
        .setLastLogTerm(FIRST_TERM)
        .build());
    assertNotNull(result);
  }

  @Test
  public void testVoteTrueNoPrior() {
    RaftReplicator replicator = RaftReplicatorStateTest.startedReplicator();
    VoteResult result = replicator.requestVote(VoteRequest.newBuilder()
        .setCandidateTerm(Term.newBuilder()
            .setNumber(1)
            .build())
        .setCandidate(Peer.newBuilder()
            .setUuid(UUID.randomUUID().toString())
            .build())
        .setLastLogIndex(FIRST_LOG_INDEX)
        .setLastLogTerm(FIRST_TERM)
        .build());
    assertTrue(result.getVoteGranted());
  }

  @Test
  public void testRetryVoteGranted() {
    RaftReplicator replicator = RaftReplicatorStateTest.startedReplicator();
    VoteRequest request = VoteRequest.newBuilder()
        .setCandidateTerm(Term.newBuilder()
            .setNumber(1)
            .build())
        .setCandidate(Peer.newBuilder()
            .setUuid(UUID.randomUUID().toString())
            .build())
        .setLastLogIndex(FIRST_LOG_INDEX)
        .setLastLogTerm(FIRST_TERM)
        .build();
    replicator.requestVote(request);
    VoteResult result = replicator.requestVote(request);
    assertTrue(result.getVoteGranted());
  }

  @Test
  public void testFalseAlreadyVoted() {
    RaftReplicator replicator = RaftReplicatorStateTest.startedReplicator();
    replicator.requestVote(VoteRequest.newBuilder()
        .setCandidateTerm(Term.newBuilder()
            .setNumber(1)
            .build())
        .setCandidate(Peer.newBuilder()
            .setUuid(UUID.randomUUID().toString())
            .build())
        .setLastLogIndex(FIRST_LOG_INDEX)
        .setLastLogTerm(FIRST_TERM)
        .build());
    VoteResult result = replicator.requestVote(VoteRequest.newBuilder()
        .setCandidateTerm(Term.newBuilder()
            .setNumber(2)
            .build())
        .setCandidate(Peer.newBuilder()
            .setUuid(UUID.randomUUID().toString())
            .build())
        .setLastLogIndex(FIRST_LOG_INDEX)
        .setLastLogTerm(FIRST_TERM)
        .build());
    assertFalse(result.getVoteGranted());
  }
}
