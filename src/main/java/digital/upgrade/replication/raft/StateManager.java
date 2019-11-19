package digital.upgrade.replication.raft;

import java.io.IOException;

import static digital.upgrade.replication.raft.Raft.PersistentState;

public interface StateManager {

    boolean notExists();

    PersistentState read() throws IOException;

    void write(PersistentState state);

    CommitIndex getHighestCommittedIndex();

    CommitIndex getHighestAppliedIndex();
}
