package digital.upgrade.raft;

import digital.upgrade.raft.Model.Term;
import digital.upgrade.raft.Model.Vote;
import digital.upgrade.raft.Model.VoteResult;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Test the initialisation state of Raft.
 *
 * @author damien@upgrade-digital.com
 */
public class RaftTest {

  @Test
  public void testInitialTerm() {
    Raft raft = newRaft();
    String resource = newResource();
    Term current = raft.currentTerm(resource);
    assertNotNull(current, "Current term should not be null");
    assertEquals(current.getResource(), resource, "Not same resource");
    assertEquals(current.getClock(), 0, "Initialise should be 0");
    assertFalse(current.hasNode(), "Should not have a candidate");
  }


  /**
   * Test the null candidate election succeeds.
   */
  @Test
  public void testVoteNullLocalCandidate() {
    Raft raft = newRaft();
    String resource = newResource();
    String candidate = newResource();
    Term testTerm = Term.newBuilder()
        .setClock(4)
        .setNode(candidate)
        .setResource(resource)
        .build();
    VoteResult result = raft.requestVote(Vote.newBuilder()
        .setCandidate(candidate)
        .setLastLogIndex(997)
        .setLastLogTerm(844)
        .setTerm(testTerm)
        .build());
    assertNotNull(result, "Vote result should not be null");
    assertEquals(result.getVoteGranted(), true, "Vote should be granted");
    assertEquals(result.getTerm(), testTerm, "Expected term update");
    assertEquals(raft.currentTerm(resource), testTerm,
        "Persisted term should " +
            "be same as returned term");
  }

  @Test
  public void testSameLocalCandidate() {
    Raft raft = newRaft();
    String resource = newResource();
    String candidate = newResource();
    Term firstTerm = Term.newBuilder()
        .setClock(7)
        .setNode(candidate)
        .setResource(resource)
        .build();
    VoteResult result = raft.requestVote(Vote.newBuilder()
        .setCandidate(candidate)
        .setLastLogIndex(1)
        .setLastLogTerm(7)
        .setTerm(firstTerm)
        .build());
    assertNotNull(result, "Vote requeste should not be null");
    assertTrue(result.getVoteGranted(), "Vote should be granted on vanilla request");
    // so the first one worked, let's push another, and see that it continues
    // (note that this does not take account of time yet)
    Term secondTerm = Term.newBuilder()
        .setClock(8)
        .setNode(candidate)
        .setResource(resource)
        .build();
    result = raft.requestVote(Vote.newBuilder()
        .setCandidate(candidate)
        .setLastLogIndex(2)
        .setLastLogTerm(999)
        .setTerm(secondTerm)
        .build());
    assertTrue(result.getVoteGranted(), "Vote should be granted");
    assertEquals(result.getTerm(), secondTerm, "Expected second term start");
    assertEquals(raft.currentTerm(resource), secondTerm, "Second term should be in effect");
  }

  @Test
  public void testRejectElectionOutOfDate() {
    fail();
  }

  @Test
  public void testRejectElectionAlreadyDecided() {
    fail();
  }

  @Test
  public void testCandidateUpToDate() {
    fail();
  }

  @Test
  public void testCandidateNotUpToDate() {
    fail();
  }

  @Test
  public void testGrantedVotePersisted() {
    fail();
  }

  @Test
  public void testRejectedVoteNotPersisted() {
    fail();
  }

  @Test
  public void testAppendIsStale() {
    fail();
  }

  @Test
  public void testAppendIsNotStale() {

  }

  private Raft newRaft() {
    return Raft.newBuilder()
        .build();
  }

  private String newResource() {
    return UUID.randomUUID().toString();
  }
}
