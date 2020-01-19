package digital.upgrade.replication.raft;

import digital.upgrade.replication.raft.Raft.AppendRequest;
import digital.upgrade.replication.raft.Raft.AppendResult;
import digital.upgrade.replication.raft.Raft.Peer;
import digital.upgrade.replication.raft.Raft.VoteRequest;
import digital.upgrade.replication.raft.Raft.VoteResult;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class InMemoryTransport implements MessageTransport {

  private Map<Peer, RaftReplicator> peers = new HashMap<>();

  void addPeer(Peer peer, RaftReplicator replicator) {
    peers.put(peer, replicator);
  }

  @Override
  public void sendRequestVote(Peer peer, VoteRequest request,
      RequestVoteResponseListener listener) {
    VoteResult response = peers.get(peer).getController()
        .handleVoteRequest(request);
    listener.handleResponse(peer, request, response);
  }

  @Override
  public void sendAppend(Peer peer, AppendRequest request,
      AppendResponseListener listener) {
    AppendResult result = peers.get(peer).getController().handleAppend(request);
    listener.handleResponse(peer, request, result);
  }

  @Override
  public Collection<Peer> peers() {
    return peers.keySet();
  }
}
