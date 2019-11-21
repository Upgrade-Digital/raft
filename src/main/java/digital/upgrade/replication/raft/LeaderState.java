package digital.upgrade.replication.raft;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static digital.upgrade.replication.raft.Raft.Peer;

/**
 * Volatile state for the leader.
 */
class LeaderState {

    private Map<Peer, CommitIndex> nextIndex;
    private Map<Peer, CommitIndex> matchIndex;

    LeaderState(Collection<Peer> peers, CommitIndex leaderLastLogIndex) {
        nextIndex = new HashMap<>(peers.size());
        matchIndex = new HashMap<>(peers.size());
        CommitIndex nextCommit = leaderLastLogIndex.nextIndex();
        CommitIndex initialIndex = new CommitIndex(0L);
        for (Peer peer : peers) {
            nextIndex.put(peer, nextCommit);
            matchIndex.put(peer, initialIndex);
        }
    }
}
