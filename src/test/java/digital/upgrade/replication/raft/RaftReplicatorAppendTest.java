package digital.upgrade.replication.raft;

import digital.upgrade.replication.CommitReplicator;
import digital.upgrade.replication.Model.CommitMessage;
import digital.upgrade.replication.raft.Raft.AppendRequest;
import digital.upgrade.replication.raft.Raft.AppendResult;
import digital.upgrade.replication.raft.Raft.Entry;
import digital.upgrade.replication.raft.Raft.Index;
import digital.upgrade.replication.raft.Raft.Peer;
import digital.upgrade.replication.raft.Raft.Term;

import com.google.protobuf.ByteString;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertFalse;

public final class RaftReplicatorAppendTest {

  private static final Term FIRST_TERM = Term.newBuilder()
      .setNumber(0)
      .build();
  private static final Term TERM_777 = Term.newBuilder()
      .setNumber(777)
      .build();
  private static final Entry THIRD_ENTRY = entry(2, TERM_777);
  private static final Entry[] SINGLE_TERM_ENTRIES;

  static {
    SINGLE_TERM_ENTRIES = new Entry[10];
    for (int i = 0; i < SINGLE_TERM_ENTRIES.length; i++) {
      SINGLE_TERM_ENTRIES[i] = entry(i, FIRST_TERM);
    }
  }

  private static Entry entry(int index, Term term) {
    return Entry.newBuilder()
        .setCommit(Index.newBuilder()
            .setMostSignificant(0)
            .setLeastSignificant(index)
            .build())
        .setTerm(term)
        .setCommand(ByteString.EMPTY)
        .build();
  }

  @Test
  public void testCommitReturnsCommitState() {
    Clock clock = new SystemClock();
    CommitReplicator replicator = RaftReplicator.newBuilder()
        .setClockSource(clock)
        .setStateManager(new InMemoryStateManager(clock))
        .setCommitHandler(new InMemoryCommitHandler(clock))
        .setExecutor(new SynchronousExecutor(clock))
        .build();
    assertNotNull(replicator.commit(CommitMessage.newBuilder().build()));
  }

  @Test
  public void testAppendReturnsResult() {
    RaftReplicator replicator = RaftReplicatorStateTest.startedReplicator();
    AppendResult result = replicator.handleAppend(zeroRequest().build());
    assertNotNull(result);
  }

  @Test
  public void testReturnsFalseIfTermLessCurrentTerm() {
    RaftReplicator replicator = RaftReplicatorStateTest.startedReplicator();
    Term current = replicator.getCurrentTerm();
    AppendResult result = replicator.handleAppend(zeroRequest()
        .setLeaderTerm(replicator.getCurrentTerm()
            .toBuilder()
            .setNumber(current.getNumber() - 1))
        .build());
    assertFalse(result.getSuccess());
  }

  @Test
  public void testReturnTrueIfTermEqualCurrentTerm() {
    RaftReplicator replicator = RaftReplicatorStateTest.startedReplicator();
    AppendResult result = replicator.handleAppend(zeroRequest()
        .setLeaderTerm(replicator.getCurrentTerm())
        .build());
    assertTrue(result.getSuccess());
  }

  @Test
  public void testReturnFalseIfPreviousLogTermMismatch() {
    RaftReplicator replicator = RaftReplicatorStateTest.startedReplicator();
    CommitIndex previousIndex = zeroIndex();
    AppendResult result = replicator.handleAppend(zeroRequest()
        .addEntries(newEntry(replicator, previousIndex))
        .setLeaderTerm(replicator.getCurrentTerm())
        .build());
    CommitIndex nextIndex = previousIndex.nextIndex();
    assertTrue(result.getSuccess());
    Term wrongTerm = Term.newBuilder()
        .setNumber(999)
        .build();
    result = replicator.handleAppend(zeroRequest()
        .addEntries(newEntry(replicator, nextIndex))
        .setLeaderTerm(replicator.getCurrentTerm())
        .setPreviousIndex(previousIndex.indexValue())
        .setPreviousTerm(wrongTerm)
        .build());
    assertFalse(result.getSuccess());
  }

  @Test
  public void testOnConflictingTermDeleteEntryAndAllFollowing() {
    RaftReplicator replicator = RaftReplicatorStateTest.startedReplicator();
    replicator.handleAppend(zeroRequest()
        .addAllEntries(Arrays.asList(SINGLE_TERM_ENTRIES))
        .build());
    replicator.handleAppend(zeroRequest()
        .setPreviousTerm(replicator.getCurrentTerm())
        .setPreviousIndex(Index.newBuilder()
            .setMostSignificant(0)
            .setLeastSignificant(2)
            .build())
        .setLeaderTerm(replicator.getCurrentTerm())
        .addEntries(THIRD_ENTRY)
        .build());
    assertEquals(replicator.getCommittedIndex().indexValue(), THIRD_ENTRY.getCommit());
  }

  private Entry newEntry(RaftReplicator replicator, CommitIndex index) {
    return Entry.newBuilder()
        .setCommit(index.indexValue())
        .setTerm(replicator.getCurrentTerm())
        .setCommand(com.google.protobuf.ByteString.EMPTY)
        .build();
  }

  private CommitIndex zeroIndex() {
    return new CommitIndex(0);
  }


  private AppendRequest.Builder zeroRequest() {
    return AppendRequest.newBuilder()
        .setLeaderTerm(Term.newBuilder()
            .setNumber(0)
            .build())
        .setLeader(Peer.newBuilder()
            .setUuid(UUID.randomUUID().toString())
            .build())
        .setPreviousIndex(CommitIndex.ZERO)
        .setPreviousTerm(Term.newBuilder()
            .setNumber(0)
            .build())
        .setLeaderIndex(CommitIndex.ZERO);
  }
}
