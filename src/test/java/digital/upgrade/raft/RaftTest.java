package digital.upgrade.raft;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

import digital.upgrade.raft.Model.Term;
import org.testng.annotations.Test;

import java.util.UUID;

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

  @Test
  public void testNullLocalCandidate() {
    fail();
  }

  @Test
  public void testSameLocalCandidate() {
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

  private Raft newRaft() {
    return Raft.newBuilder()
        .build();
  }

  private String newResource() {
    return UUID.randomUUID().toString();
  }
}
