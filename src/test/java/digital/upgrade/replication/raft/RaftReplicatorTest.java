package digital.upgrade.replication.raft;

import digital.upgrade.replication.CommitReplicator;
import digital.upgrade.replication.Model.CommitMessage;

import org.testng.annotations.Test;

import java.util.UUID;

import static org.testng.Assert.assertNotNull;

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
        Raft.AppendResult result = replicator.append(Raft.AppendRequest.newBuilder()
                .setLeaderTerm(Raft.Term.newBuilder()
                        .setNumber(0)
                        .build())
                .setLeader(Raft.Peer.newBuilder()
                        .setUuid(UUID.randomUUID().toString())
                        .build())
                .setPreviousIndex(CommitIndex.ZERO)
                .setPreviousTerm(Raft.Term.newBuilder()
                        .setNumber(0)
                        .build())
                .setLeaderIndex(CommitIndex.ZERO)
                .build());
        assertNotNull(result);
    }
}
