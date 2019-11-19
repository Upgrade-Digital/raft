package digital.upgrade.replication.raft;

import java.io.IOException;

import static digital.upgrade.replication.raft.Raft.PersistentState;

public final class InMemoryStateManager implements StateManager {

    public static final CommitIndex INITIAL_INDEX = new CommitIndex(0L, 0L);
    private final ClockSource clock;
    private PersistentState state;
    private long lastWriteTime = -1;

    public InMemoryStateManager(ClockSource clock) {
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
        return INITIAL_INDEX;
    }

    @Override
    public CommitIndex getHighestAppliedIndex() {
        return INITIAL_INDEX;
    }

    public long getLastWriteTime() {
        return lastWriteTime;
    }
}
