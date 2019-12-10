package digital.upgrade.replication.raft;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import digital.upgrade.replication.raft.Raft.Entry;
import digital.upgrade.replication.raft.Raft.Index;

import static digital.upgrade.replication.raft.Raft.PersistentState;

public final class InMemoryStateManager implements StateManager {

  private static final long INITIAL_TERM_VALUE = 0;

  private final Clock clock;
  private PersistentState state;
  private long lastWriteTime = -1;
  private Map<Raft.Index, Entry> commits = new HashMap<>();

  InMemoryStateManager(Clock clock) {
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

  @Override
  public void writeCommit(Entry entry) {
    commits.put(entry.getCommit(), entry);
  }

  @Override
  public Entry readCommit(Raft.Index index) {
    if (!commits.containsKey(index)) {
      throw new IndexOutOfBoundsException("Unable to locate commit entry for index: " + index);
    }
    return commits.get(index);
  }

  @Override
  public boolean hasCommit(Raft.Index index) {
    return commits.containsKey(index);
  }

  @Override
  public boolean isEmpty() {
    return commits.isEmpty();
  }

  @Override
  public void removeCommit(Index index) {
    commits.remove(index);
  }

  long getLastWriteTime() {
    return lastWriteTime;
  }
}
