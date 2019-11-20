package digital.upgrade.replication.raft;

import java.io.IOException;

import static digital.upgrade.replication.raft.Raft.PersistentState;

/**
 * State manager interface which provides persistence used by the Raft
 * instances to manage reliable state persistence.
 *
 * This abstraction allows Raft instances to recover state on restart.
 */
public interface StateManager {

    /**
     * Initialise the state if the state manager reports that the state
     * does not current exist.
     *
     * This method is called by the Raft instance on first startup to
     * create expected initial state.
     */
    void initialiseState();

    /**
     * Report if the state manager has a pre-existing state.
     *
     * @return true if there is no prior persisted state.
     */
    boolean notExists();

    /**
     * Read the current state protocol buffer from underlying storage.
     *
     * @return the protocol buffer for the persisted state.
     * @throws IOException if the underlying manager encounters an IO error
     *         restoring state.
     */
    PersistentState read() throws IOException;

    /**
     * Write a value for the persistent state to underlying storage.
     *
     * @param state to write to persistent storage.
     */
    void write(PersistentState state);

    /**
     * Return the value for the highest committed index.
     *
     * @return highest committed commit index.
     */
    CommitIndex getHighestCommittedIndex();

    /**
     * Return the highest commit index which has been persisted to the underlying state machine.
     *
     * @return highest applied commit index.
     */
    CommitIndex getHighestAppliedIndex();
}
