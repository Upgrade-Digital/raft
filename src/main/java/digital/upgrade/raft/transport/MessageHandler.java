package digital.upgrade.raft.transport;

import digital.upgrade.raft.Model.AppendEntries;
import digital.upgrade.raft.Model.AppendResponse;
import digital.upgrade.raft.Model.RequestVote;
import digital.upgrade.raft.Model.VoteResponse;

/**
 * Message handler needs to mutate it's state based on the different types of
 * messages is receives.
 *
 * @author damien@upgrade-digital.com
 */
public interface MessageHandler {

  public void handleAppendEntries(AppendEntries appendEntries);

  public void handleAppendResponse(AppendResponse appendResponse);

  public void handleRequestVote(RequestVote requestVote);

  public void handleVoteResponse(VoteResponse voteResponse);
}
