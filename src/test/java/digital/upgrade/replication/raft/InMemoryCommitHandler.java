package digital.upgrade.replication.raft;

import com.google.common.collect.ImmutableMap;

import digital.upgrade.replication.CommitHandler;
import digital.upgrade.replication.Model;

import java.util.HashMap;
import java.util.Map;

public class InMemoryCommitHandler implements CommitHandler {

  private final ClockSource clock;
  private Map<Long, Model.CommitMessage> commits = new HashMap<>();

  public InMemoryCommitHandler(ClockSource clock) {
    this.clock = clock;
  }

  @Override
  public boolean write(Model.CommitMessage commit) {
    commits.put(clock.currentTime(), commit);
    return true;
  }

  public Map<Long, Model.CommitMessage> getCommits() {
    return ImmutableMap.copyOf(commits);
  }
}
