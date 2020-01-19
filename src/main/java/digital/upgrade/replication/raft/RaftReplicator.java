package digital.upgrade.replication.raft;

import digital.upgrade.replication.CommitHandler;
import digital.upgrade.replication.CommitReplicator;
import digital.upgrade.replication.CommitState;
import digital.upgrade.replication.raft.Raft.Entry;
import digital.upgrade.replication.raft.Raft.Index;
import digital.upgrade.replication.raft.Raft.Term;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;

import static digital.upgrade.replication.Model.CommitMessage;
import static digital.upgrade.replication.raft.Raft.Peer;
import static digital.upgrade.replication.raft.Raft.PersistentState;

/**
 * Raft implementation of commit replicator which uses election to select a
 * leader which coordinates commits from clients.
 */
public final class RaftReplicator implements CommitReplicator, Closeable {

  private static final Logger LOG = LoggerFactory.getLogger(RaftReplicator.class);

  private StateManager stateManager;
  private CommitHandler commitHandler;
  private Clock clock;
  private InstanceState state;
  private ScheduledExecutorService executor;

  private Term currentTerm = Term.newBuilder()
      .setNumber(-1)
      .build();
  private Peer votedFor;
  private CommitIndex committed;
  private CommitIndex applied;
  private Peer self;
  private MessageTransport transport;
  private Peer leader;
  private Controller controller;
  private Time lastUpdatedTime;

  private RaftReplicator() {
  }

  /**
   * Commit message to the replication state synchronously.
   *
   * @param message to commit to the state machine across the instances.
   * @return commit state result for the replication.
   */
  @Override
  public CommitState commit(CommitMessage message) {
    commitHandler.write(message);
    return CommitState.newBuilder()
        .setTime(clock.currentTime().toEpochMillis())
        .build();
  }

  /**
   * Runnable implementation for background thread for the raft replicator
   * which coordinates actions like elections and replication between
   * raft instances.
   */
  @Override
  public void run() {
    startup();
  }

  void startup() {
    try {
      restoreState();
      state = InstanceState.FOLLOWER;
      lastUpdatedTime = clock.currentTime();
      controller = new FollowerController(this, executor, clock);
      executor.execute(controller);
    } catch (IOException e) {
      LOG.error("Error restoring persistent state");
    }
  }

  private void restoreState() throws IOException {
    if (stateManager.notExists()) {
      stateManager.initialiseState();
    }
    PersistentState persistentState = stateManager.read();
    currentTerm = persistentState.getTerm();
    votedFor = persistentState.hasVotedFor() ? persistentState.getVotedFor() : null;
    committed = stateManager.getHighestCommittedIndex();
    applied = stateManager.getHighestAppliedIndex();
  }

  /**
   * Return the current election term.
   *
   * @return election term for the current election.
   */
  Term getCurrentTerm() {
    return currentTerm;
  }

  /**
   * Check if the raft instance has already voted in the current term.
   *
   * @return election term.
   */
  boolean hasVotedInTerm() {
    return null != votedFor;
  }

  /**
   * Update the candidate who has been voted for in the current term.
   *
   * TODO Update the candidate vote when the term increases.
   *
   * @param candidate update the vote cast
   */
  void castVote(Peer candidate) {
    votedFor = candidate;
  }

  /**
   * Return the highest commit index which is persisted for this instance.
   *
   * @return highest committed index.
   */
  CommitIndex getCommittedIndex() {
    return committed;
  }

  /**
   * Return the highest applied index which has been handled by the
   * underlying commit handler.
   *
   * @return highest applied commit to the underlying handler.
   */
  CommitIndex getAppliedIndex() {
    return applied;
  }

  /**
   * Create a new builder for the Raft replicator.
   *
   * @return builder for type.
   */
  static Builder newBuilder() {
    return new Builder();
  }

  /**
   * Return the current instance state.
   *
   * @return state of the instance as a leader, follower or election candidate.
   */
  InstanceState getState() {
    return state;
  }

  /**
   * Elect self to be the leader.
   */
  void elect() {
    leader = self;
    state = InstanceState.LEADER;
  }

  /**
   * Return the current value for the peer who is the leader.
   *
   * @return Peer leader
   */
  Peer getLeader() {
    return leader;
  }

  Peer getSelf() {
    return self;
  }

  Peer getVoteCast() {
    return votedFor;
  }

  void setAppliedIndex(CommitIndex applied) {
    this.applied = applied;
  }

  @Override
  public void close() {
    executor.shutdown();
  }

  void convertToLeader() {
    // TODO candidate election won start leader controller
    state = InstanceState.LEADER;
    controller = new LeaderController(this, executor, clock, transport);
  }

  void convertToCandidate() {
    state = InstanceState.CANDIDATE;
    controller = new CandidateController(this, executor, transport);
    executor.execute(controller);
  }

  void convertToFollower() {
    state = InstanceState.FOLLOWER;
    controller = new FollowerController(this, executor, clock);
  }

  Time getLastAppendTime() {
    return lastUpdatedTime;
  }

  /**
   * Increment the term of the replicator and return the prior value as a convenience.
   * @return prior term
   */
  Term incrementTerm() {
    Term prior = currentTerm;
    long nextTerm = currentTerm.getNumber() + 1;
    currentTerm = Term.newBuilder()
        .setNumber(nextTerm)
        .build();
    return prior;
  }

  void writeCommit(Entry commit) {
    stateManager.writeCommit(commit);
  }

  void setCommitted(CommitIndex commitIndex) {
    this.committed = commitIndex;
  }

  void setCurrentTerm(Term leaderTerm) {
    this.currentTerm = leaderTerm;
  }

  void setLeader(Peer leader) {
    this.leader = leader;
  }

  void refreshLastUpdated() {
    this.lastUpdatedTime = clock.currentTime();
  }

  void flushCommits() {
    executor.execute(new CommitWriter(this, stateManager, commitHandler));
  }

  boolean isEmpty() {
    return stateManager.isEmpty();
  }

  boolean hasCommit(Index index) {
    return stateManager.hasCommit(index);
  }

  Entry readCommit(Index index) {
    return stateManager.readCommit(index);
  }

  void removeCommit(Index index) {
    stateManager.removeCommit(index);
  }

  Controller getController() {
    return controller;
  }

  /**
   * Raft instance builder which handles the construction of Raft instances.
   */
  public static final class Builder {

    private Builder() {
    }

    private RaftReplicator result = new RaftReplicator();

    Builder setStateManager(StateManager stateManager) {
      result.stateManager = stateManager;
      return this;
    }

    Builder setCommitHandler(CommitHandler commitHandler) {
      result.commitHandler = commitHandler;
      return this;
    }

    Builder setClockSource(Clock clock) {
      result.clock = clock;
      return this;
    }

    Builder setSelf(Peer self) {
      result.self = self;
      return this;
    }

    Builder setTransport(MessageTransport transport) {
      result.transport = transport;
      return this;
    }

    Builder setExecutor(ScheduledExecutorService executor) {
      result.executor = executor;
      return this;
    }

    /**
     * Construct the RaftReplica.
     *
     * @return replica instance
     * @throws IllegalStateException when the configuration is missing required setup.
     */
    public RaftReplicator build() {
      if (null == result.stateManager) {
        throw new IllegalStateException("Can't construct RaftReplicator without state manager");
      }
      if (null == result.commitHandler) {
        throw new IllegalStateException("Can't construct RaftReplicator without commit handler");
      }
      if (null == result.clock) {
        throw new IllegalStateException("Can't construct RaftReplicator without clock source");
      }
      if (null == result.executor) {
        throw new IllegalStateException("Can't construct RaftReplicator without executor");
      }
      return result;
    }
  }
}
