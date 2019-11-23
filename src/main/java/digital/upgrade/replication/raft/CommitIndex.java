package digital.upgrade.replication.raft;

import java.util.Objects;
import java.util.UUID;

/**
 * Tiny type representation of a commit index which is represented as two long values in line with the inputs of
 * a UUID (128 bit) class.
 */
public class CommitIndex {

    static final Raft.Index ZERO = Raft.Index.newBuilder()
            .setMostSignificant(0)
            .setLeastSignificant(0)
            .build();

    private final long mostSignificant;
    private final long leastSignificant;

    /**
     * Construct a commit index based on two long value encoding of 128bit number
     * @param mostSignificant value for upper 64 bits.
     * @param leastSignificant value for lower 64 bits.
     */
    CommitIndex(long mostSignificant, long leastSignificant) {
        this.mostSignificant = mostSignificant;
        this.leastSignificant = leastSignificant;
    }

    /**
     * Convenience constructor for least significant long value.
     * @param index value for lower 64 bits.
     */
    CommitIndex(Raft.Index index) {
        this(index.getMostSignificant(), index.getLeastSignificant());
    }

    CommitIndex(long number) {
        this(0, number);
    }

    /**
     * Convert the 128 bits two long encoding into a UUID.
     * @return UUID representation of 128bit value.
     */
    UUID toUuid() {
        return new UUID(mostSignificant, leastSignificant);
    }

    /**
     * Calculate the next commit index from the current value.
     *
     * @return next index from the current value.
     * @throws ArithmeticException when the counter overflows
     */
    CommitIndex nextIndex() {
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
