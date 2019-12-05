package digital.upgrade.replication.raft;

import com.google.common.base.Charsets;
import com.google.protobuf.ByteString;
import org.testng.annotations.Test;

import java.util.Map;

import digital.upgrade.replication.Model.CommitMessage;
import digital.upgrade.replication.raft.Raft.AppendRequest;
import digital.upgrade.replication.raft.Raft.Entry;

import static org.testng.Assert.assertEquals;

public class CommitWriterTest {

  @Test
  public void testApplyFirstIndex() {
    ClockSource clock = new CallCountingClock();
    InMemoryCommitHandler handler = new InMemoryCommitHandler(clock);
    RaftReplicator replicator = RaftReplicatorStateTest.startedReplicator();
    replicator.append(AppendRequest.newBuilder()
        .setLeaderIndex(replicator.getCommittedIndex().indexValue())
        .setLeaderTerm(replicator.getCurrentTerm())
        .setLeader(replicator.getSelf())
        .setPreviousTerm(replicator.getCurrentTerm())
        .setPreviousIndex(replicator.getCommittedIndex().previousValue().indexValue())
        .addEntries(Entry.newBuilder()
            .setTerm(replicator.getCurrentTerm())
            .setCommit(replicator.getCommittedIndex().nextIndex().indexValue())
            .setCommand(CommitMessage.newBuilder()
                .setScope("world")
                .setData(ByteString.copyFrom("hello", Charsets.UTF_8))
                .build().toByteString()))
        .build());
    CommitWriter writer = new CommitWriter(replicator, replicator.getStateManager(), handler);
    writer.run();
    Map<Long, CommitMessage> commits = handler.getCommits();
    assertEquals(commits.size(), 1);
    assertEquals(commits.get(777L), CommitMessage.newBuilder()
        .setScope("world")
        .setData(ByteString.copyFrom("hello", Charsets.UTF_8))
        .build());
  }
}
