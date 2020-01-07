package digital.upgrade.replication.raft;

import digital.upgrade.replication.raft.Raft.AppendRequest;
import digital.upgrade.replication.raft.Raft.AppendResult;

interface AppendEntryHandler {

  /**
   * Listener to handle append entry requests.
   *
   * @param request to process including new log entries and heartbeat
   * @return append result
   */
  AppendResult handleAppend(AppendRequest request);
}
