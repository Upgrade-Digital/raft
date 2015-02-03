package digital.upgrade.raft;

import digital.upgrade.raft.Model.AppendResult;
import digital.upgrade.raft.Model.Entry;
import digital.upgrade.raft.Model.Vote;
import digital.upgrade.raft.Model.VoteResult;

/**
 * Message handler needs to mutate it's state based on the different types of
 * messages is receives.
 *
 * @author damien@upgrade-digital.com
 */
public interface MessageHandler {

  public AppendResult appendEntry(Entry entry);

  public VoteResult requestVote(Vote voteRequest);
}
