package digital.upgrade.replication.raft;

import digital.upgrade.replication.CommitReplicator;
import digital.upgrade.replication.Model.CommitMessage;

import org.testng.annotations.Test;

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
}
