package digital.upgrade.replication.raft;

import digital.upgrade.replication.raft.Raft.Peer;
import digital.upgrade.replication.raft.Raft.VoteRequest;
import digital.upgrade.replication.raft.Raft.VoteResult;

public class FakeVoteListener implements RequestVoteResponseListener {
  @Override
  public void handleResponse(Peer peer, VoteRequest voteRequest,
      VoteResult voteResult) {
  }
}
