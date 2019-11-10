package digital.upgrade.replication.raft;

import digital.upgrade.replication.CommitReplicator;
import digital.upgrade.replication.Model.CommitMessage;

import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;

public class RaftReplicatorTest {

    @Test
    public void testCommitReturnsCommitState() {
        CommitReplicator replicator = new RaftReplicator();
        assertNotNull(replicator.commit(CommitMessage.newBuilder().build()));
    }
}
