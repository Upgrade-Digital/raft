package digital.upgrade.replication.raft;

import com.google.protobuf.ByteString;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import digital.upgrade.replication.raft.Raft.AppendRequest;
import digital.upgrade.replication.raft.Raft.Entry;
import digital.upgrade.replication.raft.Raft.Index;
import digital.upgrade.replication.raft.Raft.Peer;
import digital.upgrade.replication.raft.Raft.Term;
import digital.upgrade.replication.raft.Raft.VoteRequest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class MessageTransportTest {

  private static final Peer NARUTO = Peer.newBuilder()
      .setUuid(randomUuid())
      .build();
  private static final Peer SASUKE = Peer.newBuilder()
      .setUuid(randomUuid())
      .build();
  private static final Index FIRST_LOG_INDEX = Index.newBuilder()
      .setMostSignificant(0)
      .setLeastSignificant(0)
      .build();
  private static final Term FIRST_LOG_TERM = Term.newBuilder()
      .setNumber(1)
      .build();
  private InMemoryTransport transport;
  private RaftReplicator naruto;
  private RaftReplicator sasuke;

  private static String randomUuid() {
    return UUID.randomUUID().toString();
  }

  @BeforeClass
  public void setup() {
    transport = new InMemoryTransport();
    naruto = newReplicator(NARUTO, transport);
    transport.addPeer(NARUTO, naruto);
    sasuke = newReplicator(SASUKE, transport);
    transport.addPeer(SASUKE, sasuke);
  }

  @Test
  public void testSelf() {
    assertEquals(naruto.getSelf(), NARUTO);
    assertEquals(sasuke.getSelf(), SASUKE);
  }

  @Test
  public void testTransportReturnsVote() {
    transport.sendRequestVote(SASUKE, VoteRequest.newBuilder()
        .setLastLogIndex(FIRST_LOG_INDEX)
        .setLastLogTerm(FIRST_LOG_TERM)
        .setCandidate(NARUTO)
        .setCandidateTerm(FIRST_LOG_TERM)
        .build());
    assertTrue(sasuke.hasVotedInTerm());
    assertEquals(sasuke.getVoteCast(), NARUTO);
  }

  @Test
  public void testTransportCommitsEntry() {
    transport.sendAppend(SASUKE, AppendRequest.newBuilder()
        .setLeaderTerm(FIRST_LOG_TERM)
        .setPreviousIndex(FIRST_LOG_INDEX)
        .setLeader(NARUTO)
        .addEntries(Entry.newBuilder()
            .setTerm(FIRST_LOG_TERM)
            .setCommit(FIRST_LOG_INDEX)
            .setCommand(ByteString.copyFrom("Sakura", StandardCharsets.UTF_8)))
        .setPreviousTerm(FIRST_LOG_TERM)
        .setLeaderIndex(FIRST_LOG_INDEX)
        .build());
    assertEquals(sasuke.getAppliedIndex(), new CommitIndex(FIRST_LOG_INDEX));
  }

  private static RaftReplicator newReplicator(Peer self, MessageTransport transport) {
    ClockSource clock = new CallCountingClock();
    InMemoryStateManager stateManager = new InMemoryStateManager(clock);
    InMemoryCommitHandler commitHandler = new InMemoryCommitHandler(clock);
    RaftReplicator replicator = RaftReplicator.newBuilder()
        .setSelf(self)
        .setClockSource(clock)
        .setStateManager(stateManager)
        .setCommitHandler(commitHandler)
        .setTransport(transport)
        .build();
    replicator.startup();
    return replicator;
  }
}
