package digital.upgrade.replication.raft;

import digital.upgrade.replication.CommitHandler;
import digital.upgrade.replication.CommitReplicator;
import digital.upgrade.replication.CommitState;
import digital.upgrade.replication.Model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;

import static digital.upgrade.replication.raft.Raft.Peer;
import static digital.upgrade.replication.raft.Raft.PersistentState;

public final class RaftReplicator implements CommitReplicator {

    private static final long INITIAL_TERM_VALUE = 0;
    private static final Logger LOG = LoggerFactory.getLogger(RaftReplicator.class);

    private StateManager stateManager;
    private CommitHandler commitHandler;
    private ClockSource clock;

    private long currentTerm = -1;
    private Peer votedFor;
    private CommitIndex committed;
    private CommitIndex applied;

    private RaftReplicator() {}

    @Override
    public CommitState commit(Model.CommitMessage message) {
        commitHandler.write(message);
        return CommitState.newBuilder()
                .setTime(clock.currentTime())
                .build();
    }

    @Override
    public void run() {
        startup();

    }

    void startup() {
        try {
            restoreState();
        } catch (IOException e) {
            LOG.error("Error restoring persistent state");
        }
    }

    private void restoreState() throws IOException {
        if (stateManager.notExists()) {
            stateManager.write(PersistentState.newBuilder()
                    .setTerm(INITIAL_TERM_VALUE)
                    .setUuid(UUID.randomUUID().toString())
                    .build());
        }
        PersistentState persistentState = stateManager.read();
        currentTerm = persistentState.getTerm();
        votedFor = persistentState.hasVotedFor()? persistentState.getVotedFor() : null;
        committed = stateManager.getHighestCommittedIndex();
        applied = stateManager.getHighestAppliedIndex();
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public long getCurrentTerm() {
        return currentTerm;
    }

    public boolean hasVotedInTerm() {
        return null != votedFor;
    }


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
