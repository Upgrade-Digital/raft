package digital.upgrade.replication.raft;

import digital.upgrade.replication.CommitReplicator;
import digital.upgrade.replication.CommitState;
import digital.upgrade.replication.Model;

public class RaftReplicator implements CommitReplicator {
    @Override
    public CommitState commit(Model.CommitMessage message) {
        return CommitState.newBuilder().build();
    }
}
