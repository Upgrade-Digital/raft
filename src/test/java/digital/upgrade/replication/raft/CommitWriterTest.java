package digital.upgrade.replication.raft;

import com.google.common.base.Charsets;
import com.google.protobuf.ByteString;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import digital.upgrade.replication.CommitHandler;
import digital.upgrade.replication.Model.CommitMessage;
import digital.upgrade.replication.raft.Raft.AppendRequest;
import digital.upgrade.replication.raft.Raft.Entry;
import digital.upgrade.replication.raft.Raft.Peer;

import static org.testng.Assert.assertEquals;

public class CommitWriterTest {

  @Test
  public void testApplyFirstEntry() {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    ClockSource clock = new CallCountingClock();
    InMemoryCommitHandler handler = new InMemoryCommitHandler(clock);
    RaftReplicator replicator = runningReplicator(clock, executor, handler);
    replicator.append(AppendRequest.newBuilder()
        .setLeaderIndex(replicator.getCommittedIndex().indexValue())
        .setLeaderTerm(replicator.getCurrentTerm())
        .setLeader(replicator.getSelf())
        .addEntries(Entry.newBuilder()
            .setTerm(replicator.getCurrentTerm())
            .setCommit(replicator.getCommittedIndex().nextIndex().indexValue())
            .setCommand(CommitMessage.newBuilder()
                .setScope("world")
                .setData(ByteString.copyFrom("hello", Charsets.UTF_8))
                .build().toByteString()))
        .build());
    assertEquals(replicator.getCommittedIndex(), new CommitIndex(0, 1));
    Map<Long, CommitMessage> commits = handler.getCommits();
    assertEquals(replicator.getAppliedIndex(), replicator.getCommittedIndex());
    assertEquals(commits.size(), 1);
    assertEquals(commits.get(1L), CommitMessage.newBuilder()
        .setScope("world")
        .setData(ByteString.copyFrom("hello", Charsets.UTF_8))
        .build());
  }

  private RaftReplicator runningReplicator(ClockSource clock, ExecutorService executor, CommitHandler commitHandler) {
    InMemoryStateManager stateManager = new InMemoryStateManager(clock);
    RaftReplicator replicator = RaftReplicator.newBuilder()
        .setExecutor(executor)
        .setClockSource(clock)
        .setStateManager(stateManager)
        .setCommitHandler(commitHandler)
        .setSelf(Peer.newBuilder()
            .setUuid(UUID.randomUUID().toString())
            .build())
        .build();
    replicator.startup();
    return replicator;
  }
}
