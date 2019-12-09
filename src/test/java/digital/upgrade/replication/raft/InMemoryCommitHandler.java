package digital.upgrade.replication.raft;

import com.google.common.collect.ImmutableMap;

import digital.upgrade.replication.CommitHandler;
import digital.upgrade.replication.Model;

import java.util.HashMap;
import java.util.Map;

/**
 * In memory commit handler stores commits according to their commit time. This can be useful when creating tests for
 * commit write through as the handler stores the clock commit time, when used with counting clock source can create
 * reproducible commit replication.
 */
public class InMemoryCommitHandler implements CommitHandler {

  private final ClockSource clock;
  private Map<Long, Model.CommitMessage> commits = new HashMap<>();

  InMemoryCommitHandler(ClockSource clock) {
    this.clock = clock;
  }

  @Override
  public boolean write(Model.CommitMessage commit) {
    commits.put(clock.currentTime(), commit);
    return true;
  }

  Map<Long, Model.CommitMessage> getCommits() {
    return ImmutableMap.copyOf(commits);
  }
}
