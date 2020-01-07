package digital.upgrade.replication.raft;

import digital.upgrade.replication.raft.Raft.VoteRequest;
import digital.upgrade.replication.raft.Raft.VoteResult;

interface RequestVoteResponseListener {

  /**
   * Handle request vote calls from a transport.
   *
   * @param voteRequest from a peer
   * @param voteResult result for request
   */
  void handleResponse(VoteRequest voteRequest, VoteResult voteResult);
}
