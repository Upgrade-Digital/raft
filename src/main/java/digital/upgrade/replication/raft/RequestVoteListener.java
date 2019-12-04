package digital.upgrade.replication.raft;

import digital.upgrade.replication.raft.Raft.VoteRequest;
import digital.upgrade.replication.raft.Raft.VoteResult;

interface RequestVoteListener {

  /**
   * Handle request vote calls from a transport.
   *
   * @param voteRequest from a peer
   * @return vote result for request
   */
  VoteResult requestVote(VoteRequest voteRequest);
}
