package digital.upgrade.replication.raft;

import java.util.Objects;
import java.util.UUID;

/**
 * Tiny type representation of a commit index which is represented as two long values in line with the inputs of
 * a UUID (128 bit) class.
 */
public class CommitIndex {

    private final long mostSignificant;
    private final long leastSignificant;

    public CommitIndex(long mostSignificant, long leastSignificant) {
        this.mostSignificant = mostSignificant;
        this.leastSignificant = leastSignificant;
    }

    public CommitIndex(long leastSignificant) {
        this(0L, leastSignificant);
    }

    public UUID toUuid() {
        return new UUID(mostSignificant, leastSignificant);
    }

    public CommitIndex nextIndex() {
        long least = leastSignificant;
        long most = mostSignificant;
        if (least == Long.MAX_VALUE) {
            most++;
            least = 0;
        } else {
            least++;
        }
        if (Long.MIN_VALUE == most) {
            throw new ArithmeticException("Commit index overflow");
        }
        return new CommitIndex(most, least);
    }

    @Override
    public boolean equals(Object right) {
        if (!(right instanceof CommitIndex)) {
            return false;
        }
        CommitIndex compare = (CommitIndex)right;
        return leastSignificant == compare.leastSignificant &&
                mostSignificant == compare.mostSignificant;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mostSignificant, leastSignificant);
    }

    @Override
    public String toString() {
        return String.format("{%d,%d}", mostSignificant, leastSignificant);
    }

    long getMostSignificantLong() {
        return mostSignificant;
    }
}
