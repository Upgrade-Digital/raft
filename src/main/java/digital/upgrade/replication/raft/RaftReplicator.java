package digital.upgrade.replication.raft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;

import digital.upgrade.replication.CommitHandler;
import digital.upgrade.replication.CommitReplicator;
import digital.upgrade.replication.CommitState;
import digital.upgrade.replication.raft.Raft.AppendRequest;
import digital.upgrade.replication.raft.Raft.AppendResult;
import digital.upgrade.replication.raft.Raft.Entry;
import digital.upgrade.replication.raft.Raft.Index;
import digital.upgrade.replication.raft.Raft.Term;
import digital.upgrade.replication.raft.Raft.VoteRequest;
import digital.upgrade.replication.raft.Raft.VoteResult;

import static digital.upgrade.replication.Model.CommitMessage;
import static digital.upgrade.replication.raft.Raft.Peer;
import static digital.upgrade.replication.raft.Raft.PersistentState;

/**
 * Raft implementation of commit replicator which uses election to select a
 * leader which coordinates commits from clients.
 */
public final class RaftReplicator implements CommitReplicator,
    RequestVoteListener, AppendEntryListener, Closeable {

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

  /**
   * Handle requests to append entries to the underlying handler.
   *
   * @param request to process
   * @return append result which will be returned to the caller
   */
  public AppendResult append(AppendRequest request) {
    if (request.getLeaderTerm().getNumber() < getCurrentTerm().getNumber()) {
      LOG.debug("Append failure: request leader term < current term");
      return failureResponse();
    }
    if (lastIndexTermMismatch(request)) {
      LOG.debug("Append failure: state not empty and last index term mismatched");
      return failureResponse();
    }
    for (Entry commit : request.getEntriesList()) {
      if (indexMismatch(commit)) {
        removeFrom(commit.getCommit());
      }
      stateManager.writeCommit(commit);
      committed = new CommitIndex(commit.getCommit());
    }
    CommitIndex leaderIndex = new CommitIndex(request.getLeaderIndex());
    if (leaderIndex.greaterThan(committed)) {
      committed = leaderIndex;
    }
    LOG.debug("Append success: committed {} log entries", request.getEntriesCount());
    lastUpdatedTime = clock.currentTime();
    executor.execute(new CommitWriter(this, stateManager, commitHandler));
    return AppendResult.newBuilder()
        .setSuccess(true)
        .setTerm(getCurrentTerm())
        .build();
  }

  private void removeFrom(Index start) {
    CommitIndex remove = new CommitIndex(start);
    committed = remove.previousValue();
    while (stateManager.hasCommit(remove.indexValue())) {
      stateManager.removeCommit(remove.indexValue());
      remove = remove.nextIndex();
    }

  }

  private boolean indexMismatch(Entry commit) {
    if (!stateManager.hasCommit(commit.getCommit())) {
      return false;
    }
    Entry prior = stateManager.readCommit(commit.getCommit());
    return !prior.getTerm().equals(commit.getTerm());
  }

  private boolean lastIndexTermMismatch(AppendRequest request) {
    if (stateManager.isEmpty()) {
      return false;
    }
    Index lastIndex = request.getPreviousIndex();
    if (!stateManager.hasCommit(lastIndex)) {
      return true;
    }
    Entry commit = stateManager.readCommit(lastIndex);
    return !commit.getTerm().equals(request.getPreviousTerm());
  }

  private AppendResult failureResponse() {
    return AppendResult.newBuilder()
        .setSuccess(false)
        .setTerm(getCurrentTerm())
        .build();
  }

  /**
   * Handle a vote request and return vote if not already voted in this term and the index and term are up to date.
   *
   * @param voteRequest to consider for voting from a candidate
   * @return VoteResult with vote granted true if vote granted.
   */
  @Override
  public VoteResult requestVote(VoteRequest voteRequest) {
    CommitIndex candidateIndex = new CommitIndex(voteRequest.getLastLogIndex());
    if (null == votedFor ||
        (votedFor.equals(voteRequest.getCandidate()) && candidateIndex.greaterThanEqual(getCommittedIndex()))) {
        votedFor = voteRequest.getCandidate();
        return VoteResult.newBuilder()
            .setVoteGranted(true)
            .setVoterTerm(getCurrentTerm())
            .build();
    }
    return VoteResult.newBuilder()
        .setVoteGranted(false)
        .setVoterTerm(getCurrentTerm())
        .build();
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

  void convertToCandidate() {
    state = InstanceState.CANDIDATE;
    controller = new CandidateController();
  }

  Time getLastAppendTime() {
    return lastUpdatedTime;
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
      transport.setAppendListener(result);
      transport.setVoteListener(result);
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
