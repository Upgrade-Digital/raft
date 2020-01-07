package digital.upgrade.replication.raft;

import digital.upgrade.replication.raft.Raft.AppendRequest;
import digital.upgrade.replication.raft.Raft.AppendResult;
import digital.upgrade.replication.raft.Raft.Peer;
import digital.upgrade.replication.raft.Raft.VoteRequest;

import java.util.Collection;

/**
 * Functionality to allow sending messages between Raft instances.
 */
interface MessageTransport {

  /**
   * Set the vote handler which the transport uses to handle votes.
   *
   * @param handler to callback with request vote messages from peers
   */
  void setVoteHandler(RequestVoteHandler handler);

  /**
   * Set the append listener for callbacks from the transport to handle append entry requests.
   *
   * @param listener to route append entry requests to.
   */
  void setAppendHandler(AppendEntryHandler listener);

  /**
   * Send a request vote message to a peer
   *
   * @param peer to route the request vote to
   * @param request to be passed to the peer.
   * @param listener to callback on vote result
   */
  void sendRequestVote(Peer peer, VoteRequest request, RequestVoteResponseListener listener);

  /**
   * Append entries to the log and return the append result.
   *
   * @param peer to send request to
   * @param request to send to the peer
   * @return the append result sent by the peer
   */
  AppendResult sendAppend(Peer peer, AppendRequest request);

  /**
   * Get the collection of known peers of the local instance.
   *
   * @return a collection of peers
   */
  Collection<Peer> peers();
}
