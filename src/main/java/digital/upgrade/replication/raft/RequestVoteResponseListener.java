package digital.upgrade.replication.raft;

import digital.upgrade.replication.raft.Raft.Peer;
import digital.upgrade.replication.raft.Raft.VoteRequest;
import digital.upgrade.replication.raft.Raft.VoteResult;

interface RequestVoteResponseListener {

  /**
   * Handle request vote calls from a transport.
   *
   * @param peer
   * @param voteRequest from a peer
   * @param voteResult result for request
   */
  void handleResponse(Peer peer, VoteRequest voteRequest, VoteResult voteResult);
}
