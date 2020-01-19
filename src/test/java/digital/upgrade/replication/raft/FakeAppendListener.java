package digital.upgrade.replication.raft;

import digital.upgrade.replication.raft.Raft.AppendRequest;
import digital.upgrade.replication.raft.Raft.AppendResult;
import digital.upgrade.replication.raft.Raft.Peer;

public class FakeAppendListener implements AppendResponseListener {
  @Override
  public void handleResponse(Peer peer, AppendRequest request,
      AppendResult result) {

  }
}
