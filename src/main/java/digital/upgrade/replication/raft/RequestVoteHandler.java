package digital.upgrade.replication.raft;

import digital.upgrade.replication.raft.Raft.VoteRequest;
import digital.upgrade.replication.raft.Raft.VoteResult;

/**
 * Interface for the local request vote handler used by the transport.
 */
public interface RequestVoteHandler {

  /**
   * Handle a vote request from the transport.
   *
   * @param voteRequest from a peer
   * @return response sent via the peer in response to the request
   */
  VoteResult handleVoteRequest(VoteRequest voteRequest);
}
