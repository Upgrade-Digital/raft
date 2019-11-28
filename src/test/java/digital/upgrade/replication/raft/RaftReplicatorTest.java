package digital.upgrade.replication.raft;

import digital.upgrade.replication.CommitReplicator;
import digital.upgrade.replication.Model.CommitMessage;
import digital.upgrade.replication.raft.Raft.AppendResult;
import digital.upgrade.replication.raft.Raft.Entry;
import digital.upgrade.replication.raft.Raft.Term;

import org.testng.annotations.Test;

import java.util.UUID;

import static org.testng.Assert.*;

public final class RaftReplicatorTest {

    @Test
    public void testCommitReturnsCommitState() {
        ClockSource clock = new SystemClock();
        CommitReplicator replicator = RaftReplicator.newBuilder()
                .setClockSource(clock)
                .setStateManager(new InMemoryStateManager(clock))
                .setCommitHandler(new InMemoryCommitHandler(clock))
                .build();
        assertNotNull(replicator.commit(CommitMessage.newBuilder().build()));
    }

    @Test
    public void testAppendReturnsResult() {
        RaftReplicator replicator = RaftReplicatorStateTest.startedReplicator();
        AppendResult result = replicator.append(zeroRequest().build());
        assertNotNull(result);
    }

    @Test
    public void testReturnsFalseIfTermLessCurrentTerm() {
        RaftReplicator replicator = RaftReplicatorStateTest.startedReplicator();
        Term current = replicator.getCurrentTerm();
        AppendResult result = replicator.append(zeroRequest()
                .setLeaderTerm(replicator.getCurrentTerm()
                        .toBuilder()
                        .setNumber(current.getNumber() - 1))
                .build());
        assertFalse(result.getSuccess());
    }

    @Test
    public void testReturnTrueIfTermEqualCurrentTerm() {
        RaftReplicator replicator = RaftReplicatorStateTest.startedReplicator();
        AppendResult result = replicator.append(zeroRequest()
                .setLeaderTerm(replicator.getCurrentTerm())
                .build());
        assertTrue(result.getSuccess());
    }

    @Test
    public void testReturnFalseIfPreviousLogTermMismatch() {
        RaftReplicator replicator = RaftReplicatorStateTest.startedReplicator();
        CommitIndex previousIndex = zeroIndex();
        AppendResult result = replicator.append(zeroRequest()
                .addEntries(newEntry(replicator, previousIndex))
                .setLeaderTerm(replicator.getCurrentTerm())
                .build());
        CommitIndex nextIndex = previousIndex.nextIndex();
        assertTrue(result.getSuccess());
        Term wrongTerm = Term.newBuilder()
                .setNumber(999)
                .build();
        result = replicator.append(zeroRequest()
                .addEntries(newEntry(replicator, nextIndex))
                .setLeaderTerm(replicator.getCurrentTerm())
                .setPreviousIndex(previousIndex.indexValue())
                .setPreviousTerm(wrongTerm)
                .build());
        assertFalse(result.getSuccess());
    }

    private Entry newEntry(RaftReplicator replicator, CommitIndex index) {
        return Entry.newBuilder()
                .setCommit(index.indexValue())
                .setTerm(replicator.getCurrentTerm())
                .setCommand(com.google.protobuf.ByteString.EMPTY)
                .build();
    }

    private CommitIndex zeroIndex() {
        return new CommitIndex(0);
    }


    private Raft.AppendRequest.Builder zeroRequest() {
        return Raft.AppendRequest.newBuilder()
                .setLeaderTerm(Term.newBuilder()
                        .setNumber(0)
                        .build())
                .setLeader(Raft.Peer.newBuilder()
                        .setUuid(UUID.randomUUID().toString())
                        .build())
                .setPreviousIndex(CommitIndex.ZERO)
                .setPreviousTerm(Term.newBuilder()
                        .setNumber(0)
                        .build())
                .setLeaderIndex(CommitIndex.ZERO);
    }
}
