package digital.upgrade.replication.raft;

import digital.upgrade.replication.raft.Raft.AppendRequest;
import digital.upgrade.replication.raft.Raft.AppendResult;
import digital.upgrade.replication.raft.Raft.Peer;

public interface AppendResponseListener {

  /**
   * Handle request vote calls from a transport.
   *
   * @param peer source of the vote request
   * @param request from a peer
   * @param result result for request
   */
  void handleResponse(Peer peer, AppendRequest request, AppendResult result);
}
