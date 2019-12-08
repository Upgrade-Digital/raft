package digital.upgrade.replication.raft;

import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import digital.upgrade.replication.CommitHandler;
import digital.upgrade.replication.Model.CommitMessage;
import digital.upgrade.replication.raft.Raft.Entry;

/**
 * In order to support a number of commit write through modes the commit writer is used when
 * the replicator has pending commits to be applied.
 */
class CommitWriter implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(CommitWriter.class);

  private final RaftReplicator replicator;
  private final StateManager state;
  private final CommitHandler handler;

  CommitWriter(RaftReplicator replicator, StateManager state, CommitHandler handler) {
    this.replicator = replicator;
    this.state = state;
    this.handler = handler;
  }

  @Override
  public void run() {
    LOG.info("Commit Writer flushing non applied messages {} with applied at {}",
        replicator.getCommittedIndex(), replicator.getAppliedIndex());
    CommitIndex apply;
    while (replicator.getCommittedIndex().greaterThan(apply = replicator.getAppliedIndex())) {
      if (!state.hasCommit(apply.indexValue())) {
        LOG.info("Replicator reported non applied commit {} not found in store", apply);
        break;
      }
      Entry entry = state.readCommit(apply.indexValue());
      CommitMessage message;
      try {
        message = CommitMessage.parseFrom(entry.getCommand());
      } catch (InvalidProtocolBufferException e) {
        LOG.error("Invalid commit message at commit index {}", apply, e);
        break;
      }
      handler.write(message);
      replicator.setAppliedIndex(apply);
    }
  }
}
