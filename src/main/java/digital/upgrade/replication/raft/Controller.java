package digital.upgrade.replication.raft;

/**
 * State controller which provides a delegate for the Raft replicator to implement
 * different Raft server states e.g. follower, leader, candidate
 */
public interface Controller extends Runnable {

  /**
   * Shut down (gracefully) handling existing jobs and tasks.
   */
  void shutdown();
}
