package digital.upgrade.replication.raft;

import digital.upgrade.replication.raft.Raft.Peer;
import digital.upgrade.replication.raft.Raft.VoteRequest;
import digital.upgrade.replication.raft.Raft.VoteResult;

public interface AppendResponseListener {

  /**
   * Handle request vote calls from a transport.
   *
   * @param peer source of the vote request
   * @param voteRequest from a peer
   * @param voteResult result for request
   */
  void handleResponse(Peer peer, VoteRequest voteRequest, VoteResult voteResult);
}
