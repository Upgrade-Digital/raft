package digital.upgrade.replication.raft;

import digital.upgrade.replication.raft.Raft.AppendRequest;
import digital.upgrade.replication.raft.Raft.AppendResult;
import digital.upgrade.replication.raft.Raft.Entry;
import digital.upgrade.replication.raft.Raft.Index;
import digital.upgrade.replication.raft.Raft.VoteRequest;
import digital.upgrade.replication.raft.Raft.VoteResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Followers are responsible for handling calls from candidates and leaders via the transport and tracking timeout
 * of append entries calls from the leader to trigger an election.
 */
public class FollowerController implements Controller {

  private static final Logger LOG = LoggerFactory.getLogger(FollowerController.class);
  private static final long DEFAULT_TIMEOUT = 100;

  private final RaftReplicator replicator;
  private final ScheduledExecutorService executor;
  private final Clock clock;

  FollowerController(RaftReplicator replicator, ScheduledExecutorService executor, Clock clock) {
    this.replicator = replicator;
    this.executor = executor;
    this.clock = clock;
  }

  @Override
  public void run() {
    // check that the append haven't timed out
    Time lastAppend = replicator.getLastAppendTime();
    Time when = lastAppend.plus(DEFAULT_TIMEOUT);
    if (when.isOverdue(clock)) {
      LOG.debug("Follower found append overdue. Converting to a candidate");
      replicator.convertToCandidate();
    } else {
      LOG.debug("Follower found recent append. Waiting until next overdue");
      executor.schedule(this, when.toEpochMillis(), when.units());
    }
  }

  /**
   * Handle a vote request from the transport synchronously.
   *
   * The transport for this replicator will use this method to request a vote
   * from a peer which will be returned to them accordingly.
   *
   * @param voteRequest to consider for voting from a candidate
   * @return VoteResult with vote granted true if vote granted.
   */
  @Override
  public VoteResult handleVoteRequest(VoteRequest voteRequest) {
    // TODO migrate the vote request logic to the delegated controller
    CommitIndex candidateIndex = new CommitIndex(voteRequest.getLastLogIndex());
    if (!replicator.hasVotedInTerm() ||
        (replicator.getVoteCast().equals(voteRequest.getCandidate()) &&
            candidateIndex.greaterThanEqual(replicator.getCommittedIndex()))) {
      replicator.castVote(voteRequest.getCandidate());
      return VoteResult.newBuilder()
          .setVoteGranted(true)
          .setVoterTerm(replicator.getCurrentTerm())
          .build();
    }
    return VoteResult.newBuilder()
        .setVoteGranted(false)
        .setVoterTerm(replicator.getCurrentTerm())
        .build();
  }

  /**
   * Handle requests to append entries to the underlying handler.
   *
   * @param request to process
   * @return append result which will be returned to the caller
   */
  public AppendResult handleAppend(AppendRequest request) {
    // TODO Migrate the append handler logic to the controller delegate
    if (request.getLeaderTerm().getNumber() < replicator.getCurrentTerm().getNumber()) {
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
      replicator.writeCommit(commit);
      replicator.setCommitted(new CommitIndex(commit.getCommit()));
    }
    if (request.getLeaderTerm().getNumber() > replicator.getCurrentTerm().getNumber()) {
      replicator.setCurrentTerm(request.getLeaderTerm());
      replicator.setLeader(request.getLeader());
      replicator.convertToFollower();
    }
    LOG.debug("Append success: committed {} log entries", request.getEntriesCount());
    replicator.refreshLastUpdated();
    replicator.flushCommits();

    return AppendResult.newBuilder()
        .setSuccess(true)
        .setTerm(replicator.getCurrentTerm())
        .build();
  }

  private boolean lastIndexTermMismatch(AppendRequest request) {
    if (replicator.isEmpty()) {
      return false;
    }
    Index lastIndex = request.getPreviousIndex();
    if (!replicator.hasCommit(lastIndex)) {
      return true;
    }
    Entry commit = replicator.readCommit(lastIndex);
    return !commit.getTerm().equals(request.getPreviousTerm());
  }

  private boolean indexMismatch(Entry commit) {
    if (!replicator.hasCommit(commit.getCommit())) {
      return false;
    }
    Entry prior = replicator.readCommit(commit.getCommit());
    return !prior.getTerm().equals(commit.getTerm());
  }

  private void removeFrom(Index start) {
    CommitIndex remove = new CommitIndex(start);
    replicator.setCommitted(remove.previousValue());
    while (replicator.hasCommit(remove.indexValue())) {
      replicator.removeCommit(remove.indexValue());
      remove = remove.nextIndex();
    }
  }

  private AppendResult failureResponse() {
    return AppendResult.newBuilder()
        .setSuccess(false)
        .setTerm(replicator.getCurrentTerm())
        .build();
  }
}
