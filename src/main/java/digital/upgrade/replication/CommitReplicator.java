package digital.upgrade.replication;

/**
 * Implementations of the CommitReplicator interface are responsible for distribution of commit entries across a
 * collection of instances of the class.
 */
public interface CommitReplicator {

    CommitState commit(Model.CommitMessage message);
}
