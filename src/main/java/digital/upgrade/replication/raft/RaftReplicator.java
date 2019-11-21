package digital.upgrade.replication.raft;

import digital.upgrade.replication.CommitHandler;
import digital.upgrade.replication.CommitReplicator;
import digital.upgrade.replication.CommitState;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static digital.upgrade.replication.Model.CommitMessage;
import static digital.upgrade.replication.raft.Raft.Peer;
import static digital.upgrade.replication.raft.Raft.PersistentState;

/**
 * Raft implementation of commit replicator which uses election to select a
 * leader which coordinates commits from clients.
 */
public final class RaftReplicator implements CommitReplicator {

    private static final Logger LOG = LoggerFactory.getLogger(RaftReplicator.class);

    private StateManager stateManager;
    private CommitHandler commitHandler;
    private ClockSource clock;
    private InstanceState state;

    private ElectionTerm currentTerm = new ElectionTerm();
    private Peer votedFor;
    private CommitIndex committed;
    private CommitIndex applied;

    private RaftReplicator() {}

    /**
     * Commit message to the replication state synchronously.
     *
     * @param message to commit to the state machine across the instances.
     * @return commit state result for the replication.
     */
    @Override
    public CommitState commit(CommitMessage message) {
        commitHandler.write(message);
        return CommitState.newBuilder()
                .setTime(clock.currentTime())
                .build();
    }

    /**
     * Runnable implementation for background thread for the raft replicator
     * which coordinates actions like elections and replication between
     * raft instances.
     */
    @Override
    public void run() {
        startup();
    }

    void startup() {
        try {
            restoreState();
            state = InstanceState.FOLLOWER;
        } catch (IOException e) {
            LOG.error("Error restoring persistent state");
        }
    }

    private void restoreState() throws IOException {
        if (stateManager.notExists()) {
            stateManager.initialiseState();
        }
        PersistentState persistentState = stateManager.read();
        currentTerm = new ElectionTerm(persistentState.getTerm());
        votedFor = persistentState.hasVotedFor()? persistentState.getVotedFor() : null;
        committed = stateManager.getHighestCommittedIndex();
        applied = stateManager.getHighestAppliedIndex();
    }

    /**
     * Return the current election term.
     *
     * @return election term for the current election.
     */
    ElectionTerm getCurrentTerm() {
        return currentTerm;
    }

    /**
     * Check if the raft instance has already voted in the current term.
     *
     * @return election term.
     */
    boolean hasVotedInTerm() {
        return null != votedFor;
    }

    /**
     * Return the highest commit index which is persisted for this instance.
     *
     * @return highest committed index.
     */
    CommitIndex getCommittedIndex() {
        return committed;
    }

    /**
     * Return the highest applied index which has been handled by the
     * underlying commit handler.
     *
     * @return highest applied commit to the underlying handler.
     */
    CommitIndex getAppliedIndex() {
        return applied;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public InstanceState getState() {
        return state;
    }

    /**
     * Raft instance builder which handles the construction of Raft instances.
     */
    public static final class Builder {

        private Builder() {}

        private RaftReplicator result = new RaftReplicator();

        public Builder setStateManager(StateManager stateManager) {
            result.stateManager = stateManager;
            return this;
        }

        public Builder setCommitHandler(CommitHandler commitHandler) {
            result.commitHandler = commitHandler;
            return this;
        }

        public Builder setClockSource(ClockSource clock) {
            result.clock = clock;
            return this;
        }

        /**
         * Construct the RaftReplica.
         *
         * @return replica instance
         * @throws IllegalStateException when the configuration is missing required setup.
         */
        public RaftReplicator build() {
            if (null == result.stateManager) {
                throw new IllegalStateException("Can't construct RaftReplicator without state manager");
            }
            if (null == result.commitHandler) {
                throw new IllegalStateException("Can't construct RaftReplicator without commit handler");
            }
            if (null == result.clock) {
                throw new IllegalStateException("Can't construct RaftReplicator without clock source");
            }
            return result;
        }
    }
}
