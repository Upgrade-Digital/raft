package digital.upgrade.raft;


import com.google.common.collect.Maps;
import digital.upgrade.raft.Model.AppendResult;
import digital.upgrade.raft.Model.Entry;
import digital.upgrade.raft.Model.NodeState;
import digital.upgrade.raft.Model.Term;
import digital.upgrade.raft.Model.Vote;
import digital.upgrade.raft.Model.VoteResult;

import java.util.Map;

/**
 * Entry point for a raft protocol server.
 *
 * @author damien@upgrade-digital.com
 */
public class Raft implements MessageHandler {

  private volatile NodeState state = NodeState.CANDIDATE;

  private volatile Map<String, Term> currentTerms = Maps.newConcurrentMap();

  private Raft() {}

  public static Builder newBuilder() {
    return new Builder();
  }

  @Override
  public AppendResult appendEntry(Entry entry) {
    AppendResult.Builder result = AppendResult.newBuilder();
    Term append = entry.getTerm();
    Term myTerm = currentTerms.get(append);
    if (appendIsStale(append, myTerm)) {
      return result.setSuccess(false)
          .build();
    }
    // Term previousLogTerm = entry.getLastLogTerm();
    // return false if the log term is not the same term at the same index

    // if an existing entry conflicts with a new one delete the existing entry
    // as well as all that follow it

    // append new entries not in the log

    // if the leader commit is greater than the local commit clock set the
    // local clock to be min(leader commit or index of last new entry here)
  }

  @Override
  public VoteResult requestVote(Vote voteRequest) {
    Term vote = voteRequest.getTerm();
    Term myTerm = currentTerm(vote.getResource());
    VoteResult.Builder result = VoteResult.newBuilder();
    if (vote.getClock() < myTerm.getClock()) {
      result.setVoteGranted(false);
    } else {
      if (nullOrSameNode(vote, myTerm) && candidateUpToDate(vote, myTerm)) {
        result.setVoteGranted(true);
        persistUpdatedVote(myTerm, vote);
      } else {
        result.setVoteGranted(false);
      }
    }
    return result.build();
  }

  boolean appendIsStale(Term append, Term myTerm) {
    return append.getClock() < myTerm.getClock();
  }

  void persistUpdatedVote(Term myTerm, Term vote) {

  }

  boolean candidateUpToDate(Term vote, Term myTerm) {
    return false;
  }

  boolean nullOrSameNode(Term vote, Term myTerm) {
    return false;
  }

  Term currentTerm(String resource) {
    Term term = currentTerms.get(resource);
    if (null == term) {
      term = Term.newBuilder()
          .setClock(0)
          .setResource(resource)
          .build();
      currentTerms.put(resource, term);
    }
    return term;
  }

  public static class Builder {

    private Raft raft;

    private Builder() {
      raft = new Raft();
    }

    public Raft build() {
      return raft;
    }
  }
}
