package digital.upgrade.replication;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class CommitStateTest {

  @Test
  public void testCommitStateTime() {
    CommitState state = CommitState.newBuilder()
        .setTime(999L)
        .build();
    assertEquals(state.getTime(), 999L);
  }
}
