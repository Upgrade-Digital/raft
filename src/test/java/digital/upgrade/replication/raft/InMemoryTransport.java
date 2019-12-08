package digital.upgrade.replication.raft;

import java.util.HashMap;
import java.util.Map;

import digital.upgrade.replication.raft.Raft.AppendRequest;
import digital.upgrade.replication.raft.Raft.AppendResult;
import digital.upgrade.replication.raft.Raft.Peer;
import digital.upgrade.replication.raft.Raft.VoteRequest;
import digital.upgrade.replication.raft.Raft.VoteResult;

public class InMemoryTransport implements MessageTransport {

  private Map<Peer, RaftReplicator> peers = new HashMap<>();

  @Override
  public void setVoteListener(RequestVoteListener listener) {
  }

  @Override
  public VoteResult sendRequestVote(Peer peer, VoteRequest request) {
    return peers.get(peer).requestVote(request);
  }

  @Override
  public void setAppendListener(AppendEntryListener listener) {
  }

  @Override
  public AppendResult sendAppend(Peer peer, AppendRequest request) {
    return peers.get(peer).append(request);
  }

  void addPeer(Peer peer, RaftReplicator replicator) {
    peers.put(peer, replicator);
  }
}
