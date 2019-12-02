package digital.upgrade.replication;

/**
 * CommitState encapsulates information related to an attempt to commit a specific
 * commit message.
 */
public final class CommitState {

  private long time;

  /**
   * Return the time of commit execution.
   *
   * @return commit time in milliseconds.
   */
  public long getTime() {
    return time;
  }

  private CommitState() {
  }

  /**
   * Creates a new builder for a commit state response.
   *
   * @return new builder instance.
   */
  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {

    private CommitState result;

    private Builder() {
      result = new CommitState();
    }

    public CommitState build() {
      return result;
    }

    public Builder setTime(long time) {
      result.time = time;
      return this;
    }
  }
}
