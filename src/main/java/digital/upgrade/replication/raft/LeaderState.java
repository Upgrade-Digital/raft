package digital.upgrade.replication.raft;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static digital.upgrade.replication.raft.Raft.Peer;

/**
 * Volatile state for the leader post election.
 */
final class LeaderState {

    private static final Raft.Index ZERO = Raft.Index.newBuilder()
            .setMostSignificant(0)
            .setLeastSignificant(0)
            .build();
    private Map<Peer, CommitIndex> nextIndex;
    private Map<Peer, CommitIndex> matchIndex;

    /**
     * Construct leader state given a collection of peers and the leaders last committed log index.
     *
     * @param peers Collection of peers which the leader will replicate state to.
     * @param leaderLastLogIndex the index of the last committed index on the leader.
     */
    LeaderState(Collection<Peer> peers, CommitIndex leaderLastLogIndex) {
        nextIndex = new HashMap<>(peers.size());
        matchIndex = new HashMap<>(peers.size());
        CommitIndex nextCommit = leaderLastLogIndex.nextIndex();
        CommitIndex initialIndex = new CommitIndex(ZERO);
        for (Peer peer : peers) {
            nextIndex.put(peer, nextCommit);
            matchIndex.put(peer, initialIndex);
        }
    }
}
