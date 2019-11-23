package digital.upgrade.replication.raft;

import java.io.IOException;
import java.util.UUID;

import static digital.upgrade.replication.raft.Raft.PersistentState;

public final class InMemoryStateManager implements StateManager {

    private static final long INITIAL_TERM_VALUE = 0;

    private final ClockSource clock;
    private PersistentState state;
    private long lastWriteTime = -1;

    InMemoryStateManager(ClockSource clock) {
        this.clock = clock;
    }

    @Override
    public void initialiseState() {
        write(PersistentState.newBuilder()
                .setTerm(Raft.Term.newBuilder()
                        .setNumber(INITIAL_TERM_VALUE)
                        .build())
                .setUuid(UUID.randomUUID().toString())
                .setCommitted(Raft.Index.newBuilder()
                        .setMostSignificant(0)
                        .setLeastSignificant(0)
                        .build())
                .setApplied(Raft.Index.newBuilder()
                        .setMostSignificant(0)
                        .setLeastSignificant(0)
                        .build())
                .build());
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
        return new CommitIndex(state.getCommitted());
    }

    @Override
    public CommitIndex getHighestAppliedIndex() {
        return new CommitIndex(state.getApplied());
    }

    long getLastWriteTime() {
        return lastWriteTime;
    }
}
