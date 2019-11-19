package digital.upgrade.replication.raft;

import java.io.IOException;

import static digital.upgrade.replication.raft.Raft.PersistentState;

public final class InMemoryStateManager implements StateManager {

    private final ClockSource clock;
    private PersistentState state;
    private long lastWriteTime = -1;

    InMemoryStateManager(ClockSource clock) {
        this.clock = clock;
    }

    @Override
    public boolean notExists() {
        return null == state;
    }

    @Override
    public PersistentState read() throws IOException {
        if (notExists()) {
            throw new IOException("persistent state has not been saved");
        }
        return state;
    }

    @Override
    public void write(PersistentState state) {
        this.lastWriteTime = clock.currentTime();
        this.state = state;
    }

    @Override
    public CommitIndex getHighestCommittedIndex() {
        return new CommitIndex(state.getCommittedMost(), state.getCommittedLeast());
    }

    @Override
    public CommitIndex getHighestAppliedIndex() {
        return new CommitIndex(state.getAppliedMost(), state.getAppliedLeast());
    }

    long getLastWriteTime() {
        return lastWriteTime;
    }
}
