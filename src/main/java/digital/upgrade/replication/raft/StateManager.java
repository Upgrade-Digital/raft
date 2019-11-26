package digital.upgrade.replication.raft;

import java.io.IOException;

import static digital.upgrade.replication.raft.Raft.Entry;
import static digital.upgrade.replication.raft.Raft.Index;
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

    /**
     * Write a commit to the persistent storage.
     *
     * @param entry to commit
     */
    void writeCommit(Entry entry);

    /**
     * Return the commit entry for a given index.
     *
     * @param index to return
     * @return the commit for the requested index
     * @throws IndexOutOfBoundsException if the index is not found.
     */
    Entry readCommit(Index index);

    /**
     * Test if the persistent state includes a commit with the given index.
     *
     * @param index of the commit entry
     * @return true if the index exists
     */
    boolean hasCommit(Index index);

    /**
     * Return true if the commit log messages is empty (on initialisation).
     *
     * @return when no log commit history
     */
    boolean isEmpty();
}
