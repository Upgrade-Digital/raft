package digital.upgrade.replication.raft;

import digital.upgrade.replication.raft.Raft.AppendRequest;
import digital.upgrade.replication.raft.Raft.AppendResult;

interface AppendEntryListener {

  /**
   * Listener to handle append entry requests.
   *
   * @param request to process including new log entries and heartbeat
   * @return append result
   */
  AppendResult append(AppendRequest request);
}
