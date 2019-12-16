package digital.upgrade.replication.raft;

import com.google.protobuf.ByteString;

import digital.upgrade.replication.CommitState;
import digital.upgrade.replication.raft.Raft.Peer;

import org.testng.annotations.Test;

import java.util.Map;
import java.util.UUID;

import static digital.upgrade.replication.Model.CommitMessage;
import static org.testng.Assert.*;

public class RaftReplicatorStateTest {

  /**
   * From Section 5 Figure 2. Current term initialised to 0 on first boot.
   * <p>
   * This occurs when there is no existing server state.
   */
  @Test
  public void currentTermInitialisedZeroOnFirstBoot() {
    RaftReplicator replicator = startedReplicator();
    assertEquals(replicator.getCurrentTerm(),
        Raft.Term.newBuilder()
            .setNumber(0)
            .build());
  }

  @Test
  public void votedForInitiallyNotSet() {
    RaftReplicator replicator = startedReplicator();
    assertFalse(replicator.hasVotedInTerm());
  }

  /**
   * From Section 5 Figure 2. Persistent state is saved before responding to RPC.
   */
  @Test
  public void currentStatePersistedBeforeResponding() {
    Clock clock = new CallCountingClock();
    InMemoryStateManager stateManager = new InMemoryStateManager(clock);
    InMemoryCommitHandler commitHandler = new InMemoryCommitHandler(clock);

    RaftReplicator replicator = RaftReplicator.newBuilder()
        .setClockSource(clock)
        .setStateManager(stateManager)
        .setCommitHandler(commitHandler)
        .setExecutor(new SynchronousExecutor(clock))
        .build();
    replicator.startup();

    CommitMessage message = CommitMessage.newBuilder()
        .setScope("a")
        .setData(ByteString.EMPTY)
        .build();
    CommitState result = replicator.commit(message);
    assertNotNull(result);
    Map<Time, CommitMessage> commits = commitHandler.getCommits();
    assertEquals(stateManager.getLastWriteTime().toEpochMillis(), 0L, "Expected creating state to be first clock");
    assertEquals(commits.size(), 1, "Expected 1 commit");
    assertEquals(commits.get(Time.fromEpochMillis(1L)), message, "Expected message to be at time 1");
    assertEquals(result.getTime(), 2, "Commit should be created after handler write");
  }

  @Test
  public void testRaftInitialCommitZero() {
    RaftReplicator replicator = startedReplicator();
    assertEquals(replicator.getCommittedIndex(), new CommitIndex(0L));
  }

  @Test
  public void testRaftInitialAppliedIndex() {
    RaftReplicator replicator = startedReplicator();
    assertEquals(replicator.getAppliedIndex(), new CommitIndex(0L));
  }

  @Test
  public void testInitiallyFollower() {
    RaftReplicator replicator = startedReplicator();
    assertEquals(replicator.getState(), InstanceState.FOLLOWER);
  }

  static RaftReplicator startedReplicator() {
    Clock clock = new CallCountingClock();
    InMemoryStateManager stateManager = new InMemoryStateManager(clock);
    InMemoryCommitHandler commitHandler = new InMemoryCommitHandler(clock);
    RaftReplicator replicator = RaftReplicator.newBuilder()
        .setClockSource(clock)
        .setStateManager(stateManager)
        .setCommitHandler(commitHandler)
        .setExecutor(new SynchronousExecutor(clock))
        .setSelf(Peer.newBuilder()
            .setUuid(UUID.randomUUID().toString())
            .build())
        .build();
    replicator.startup();
    return replicator;
  }
}
