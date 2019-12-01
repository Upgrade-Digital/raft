package digital.upgrade.replication.raft;

import org.testng.annotations.Test;

import java.util.UUID;

import digital.upgrade.replication.raft.Raft.Index;
import digital.upgrade.replication.raft.Raft.Peer;
import digital.upgrade.replication.raft.Raft.Term;
import digital.upgrade.replication.raft.Raft.VoteRequest;
import digital.upgrade.replication.raft.Raft.VoteResult;

import static org.testng.Assert.assertNotNull;

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
}
