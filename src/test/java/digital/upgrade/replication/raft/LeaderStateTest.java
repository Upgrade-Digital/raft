package digital.upgrade.replication.raft;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import digital.upgrade.replication.raft.Raft.Peer;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class LeaderStateTest {

  private static final Peer PEER = Peer.newBuilder()
      .setUuid(UUID.randomUUID().toString())
      .build();

  private LeaderState state;

  @Test
  public void testInitialisation() {
    List<Peer> peers = new ArrayList<>();
    peers.add(PEER);
    state = new LeaderState(peers, new CommitIndex(0, 7));
    assertEquals(state.peerCount(), 1);
  }

  @Test(dependsOnMethods = "testInitialisation")
  public void testNextIndex() {
    assertEquals(state.nextIndex(PEER), new CommitIndex(0, 8));
  }

  @Test(dependsOnMethods = "testInitialisation")
  public void testMatchIndex() {
    assertEquals(state.appliedIndex(PEER), new CommitIndex(0, 0));
  }

  @Test(dependsOnMethods = "testInitialisation")
  public void testPeerSet() {
    assertEquals(state.peerCount(), 1);
    Set<Peer> peers = state.peerSet();
    assertEquals(peers.size(), 1);
    assertTrue(peers.contains(PEER));
  }

}
