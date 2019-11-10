package digital.upgrade.replication.raft;

import org.testng.annotations.Test;

public class RaftReplicatorStateTest {

    /**
     * From Section 5 Figure 2. Persistent state is saved before responding to RPC.
     */
    @Test
    public void currentStatePersistedBeforeResponding() {
        RaftReplicator replicator = new RaftReplicator();

    }
}
