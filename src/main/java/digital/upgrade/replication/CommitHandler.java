package digital.upgrade.replication;

import static digital.upgrade.replication.Model.CommitMessage;

/**
 * CommitHandler is used to abstract the underlying writer implementation from the replicator
 * by encapsulating the writer semantics.
 */
public interface CommitHandler {

  /**
   * Write a commit message to underlying persistent storage.
   *
   * @param commit message to write.
   * @return true if the write was successful.
   */
  boolean write(CommitMessage commit);
}
