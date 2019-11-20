package digital.upgrade.replication;

import static digital.upgrade.replication.Model.CommitMessage;

/**
 * Implementations of the CommitReplicator interface are responsible for distribution of commit entries across a
 * collection of instances of the class.
 */
public interface CommitReplicator extends Runnable {

    /**
     * Commit a specific state machine update to the underlying commit handler via the replicator.
     *
     * @param message to commit to the underlying commit handler.
     * @return state of the commit when done.
     */
    CommitState commit(CommitMessage message);
}
