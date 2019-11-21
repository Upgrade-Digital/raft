package digital.upgrade.replication.raft;

import com.google.protobuf.ByteString;
import digital.upgrade.replication.CommitState;
import org.testng.annotations.Test;

import java.util.Map;

import static digital.upgrade.replication.Model.CommitMessage;
import static org.testng.Assert.*;

public class RaftReplicatorStateTest {

    /**
     * From Section 5 Figure 2. Current term initialised to 0 on first boot.
     *
     * This occurs when there is no existing server state.
     */
    @Test
    public void currentTermInitialisedZeroOnFirstBoot() {
        RaftReplicator replicator = startedReplicator();
        assertEquals(replicator.getCurrentTerm(), new ElectionTerm(0));
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
        ClockSource clock = new CallCountingClock();
        InMemoryStateManager stateManager = new InMemoryStateManager(clock);
        InMemoryCommitHandler commitHandler = new InMemoryCommitHandler(clock);

        RaftReplicator replicator = RaftReplicator.newBuilder()
                .setClockSource(clock)
                .setStateManager(stateManager)
                .setCommitHandler(commitHandler)
                .build();
        replicator.startup();

        CommitMessage message = CommitMessage.newBuilder()
                .setScope("a")
                .setData(ByteString.EMPTY)
                .build();
        CommitState result = replicator.commit(message);
        assertNotNull(result);
        Map<Long, CommitMessage> commits = commitHandler.getCommits();
        assertEquals(stateManager.getLastWriteTime(), 0L, "Expected creating state to be first clock");
        assertEquals(commits.size(), 1, "Expected 1 commit");
        assertEquals(commits.get(1L), message, "Expected message to be at time 1");
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

    private RaftReplicator startedReplicator() {
        ClockSource clock = new CallCountingClock();
        InMemoryStateManager stateManager = new InMemoryStateManager(clock);
        InMemoryCommitHandler commitHandler = new InMemoryCommitHandler(clock);
        RaftReplicator replicator = RaftReplicator.newBuilder()
                .setClockSource(clock)
                .setStateManager(stateManager)
                .setCommitHandler(commitHandler)
                .build();
        replicator.startup();
        return replicator;
    }
}
