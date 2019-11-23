package digital.upgrade.replication.raft;

import org.testng.annotations.Test;

import java.util.Objects;
import java.util.UUID;

import static org.testng.Assert.*;

public class CommitIndexTest {

    @Test
    public void testNextCommitIndexZeroOne() {
        CommitIndex index = new CommitIndex(0L, 0L);
        assertEquals(index.nextIndex(), new CommitIndex(0L, 1L));
    }

    @Test
    public void testHashCodeConsistent() {
        assertEquals(new CommitIndex(0L, 77L), new CommitIndex(0L, 77L));
    }

    @Test
    public void testMaxLeast() {
        assertEquals(new CommitIndex(0L, Long.MAX_VALUE).nextIndex(),
                new CommitIndex(1L, 0L));
    }

    @Test
    public void testLazyConstructor() {
        assertEquals(new CommitIndex(0L, 999L),
                new CommitIndex(999L));
    }

    @Test(expectedExceptions = ArithmeticException.class)
    public void testArithmeticOverflow() {
        CommitIndex result = new CommitIndex(Long.MAX_VALUE, Long.MAX_VALUE).nextIndex();
        System.out.print(result);
        System.out.println(Long.MIN_VALUE);
        System.out.println(result.getMostSignificantLong());
    }

    @Test
    public void testUuidRepresentation() {
        assertEquals(new UUID(77L, 99L), new CommitIndex(77L, 99L).toUuid());
    }

    @Test
    public void testNotEqualsNonCommit() {
        assertNotEquals(new Object(), new CommitIndex(CommitIndex.ZERO));
    }

    @Test
    public void testHashCodeValue() {
        assertEquals(new CommitIndex(CommitIndex.ZERO).hashCode(), Objects.hash(0L, 0L));
    }

    @Test
    public void testToString() {
        assertEquals(new CommitIndex(1L).toString(), "{0,1}");
    }

    @Test
    public void testMostSignificantLong() {
        assertEquals(new CommitIndex(99L, 0L).getMostSignificantLong(), 99L);
    }

    @Test
    public void testEquals() {
        assertEquals(new CommitIndex(777L), new CommitIndex(777L));
    }
}
