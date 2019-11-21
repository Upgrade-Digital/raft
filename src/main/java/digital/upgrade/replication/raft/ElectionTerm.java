package digital.upgrade.replication.raft;

/**
 * Election term representation.
 */
final class ElectionTerm {

    private static final long INITIAL_TERM = -1;
    private final long number;

    ElectionTerm(long number) {
        this.number = number;
    }

    ElectionTerm() {
        this(INITIAL_TERM);
    }

    @Override
    public boolean equals(Object other) {
        if(!(other instanceof ElectionTerm)) {
            return false;
        }
        ElectionTerm equal = (ElectionTerm)other;
        return number == equal.number;
    }

    @Override
    public int hashCode() {
        return Long.valueOf(number).hashCode();
    }
}
