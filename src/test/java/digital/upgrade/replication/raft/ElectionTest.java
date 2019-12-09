package digital.upgrade.replication.raft;

import org.testng.annotations.Test;

import java.util.UUID;

import digital.upgrade.replication.raft.Raft.AppendRequest;
import digital.upgrade.replication.raft.Raft.Peer;
import digital.upgrade.replication.raft.Raft.Term;

import static org.testng.Assert.assertEquals;

public class ElectionTest {

  @Test
  public void testLeaderConvertsToFollowerOnHigherTerm() {
    RaftReplicator replicator = RaftReplicatorStateTest.startedReplicator();
    replicator.elect();
    Term nextTerm = nextTerm(replicator);
    Peer otherPeer = randomPeer();
    assertEquals(replicator.getState(), InstanceState.LEADER);
    replicator.append(AppendRequest.newBuilder()
        .setLeader(otherPeer)
        .setLeaderTerm(nextTerm)
        .setLeaderIndex(replicator.getCommittedIndex().indexValue())
        .build());
    assertEquals(replicator.getState(), InstanceState.FOLLOWER);
    assertEquals(replicator.getCurrentTerm(), nextTerm);
    assertEquals(replicator.getLeader(), otherPeer);
  }

  private Peer randomPeer() {
    return Peer.newBuilder()
        .setUuid(randomUuid())
        .build();
  }

  private String randomUuid() {
    return UUID.randomUUID().toString();
  }

  private Term nextTerm(RaftReplicator replicator) {
    Term current = replicator.getCurrentTerm();
    return Term.newBuilder()
        .setNumber(current.getNumber() + 1)
        .build();
  }
}
