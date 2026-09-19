package kz.aitu.daa.select;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import kz.aitu.daa.TestArrays;
import kz.aitu.daa.metrics.Metrics;
import org.junit.jupiter.api.Test;

class QuickSelectTest {

    private static final int TRIALS = 100;

    @Test
    void matchesSortedArrayOn100RandomArrays() {
        Random random = new Random(53);
        for (int trial = 0; trial < TRIALS; trial++) {
            int n = 1 + random.nextInt(500);
            int[] a = trial % 2 == 0
                    ? TestArrays.randomValues(random, n)
                    : TestArrays.randomValues(random, n, 4);
            int k = random.nextInt(n);
            int[] expected = TestArrays.sorted(a);
            assertEquals(expected[k], QuickSelect.select(a, k, new Metrics()), "trial " + trial + " k=" + k);
        }
    }

    @Test
    void returnsTheMinimumForKZero() {
        int[] a = TestArrays.randomValues(new Random(59), 1_000);
        int[] expected = TestArrays.sorted(a);
        assertEquals(expected[0], QuickSelect.select(a, 0, new Metrics()));
    }

    @Test
    void returnsTheMaximumForTheLastPosition() {
        int[] a = TestArrays.randomValues(new Random(61), 1_000);
        int[] expected = TestArrays.sorted(a);
        assertEquals(expected[a.length - 1], QuickSelect.select(a, a.length - 1, new Metrics()));
    }

    @Test
    void handlesAllElementsEqual() {
        int[] a = TestArrays.filled(5_000, 12);
        assertEquals(12, QuickSelect.select(a, 2_500, new Metrics()));
    }

    @Test
    void handlesAlreadySortedArray() {
        int[] a = TestArrays.sorted(new Random(67), 100_000);
        int[] expected = a.clone();
        assertEquals(expected[50_000], QuickSelect.select(a, 50_000, new Metrics()));
    }

    @Test
    void handlesSingleElement() {
        assertEquals(9, QuickSelect.select(new int[] {9}, 0, new Metrics()));
    }

    @Test
    void rejectsEmptyArray() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> QuickSelect.select(new int[0], 0, new Metrics()));
        assertTrue(exception.getMessage().contains("empty"), exception.getMessage());
    }

    @Test
    void rejectsNullArray() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> QuickSelect.select(null, 0, new Metrics()));
        assertTrue(exception.getMessage().contains("null"), exception.getMessage());
    }

    @Test
    void rejectsNegativeK() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> QuickSelect.select(new int[] {1, 2, 3}, -1, new Metrics()));
        assertTrue(exception.getMessage().contains("k"), exception.getMessage());
    }

    @Test
    void rejectsKEqualToOnePastTheEnd() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> QuickSelect.select(new int[] {1, 2, 3}, 3, new Metrics()));
        assertTrue(exception.getMessage().contains("0"), exception.getMessage());
    }

    @Test
    void partitionRoundsStayLogarithmicOnSortedInput() {
        int n = 100_000;
        int[] a = TestArrays.sorted(new Random(71), n);
        Metrics metrics = new Metrics();
        QuickSelect.select(a, n / 2, metrics);
        double limit = 4 * TestArrays.log2(n);
        assertTrue(metrics.maxDepth() <= limit,
                "expected at most " + (int) limit + " partition rounds but used " + metrics.maxDepth());
    }

    @Test
    void staysWithinLinearComparisons() {
        int n = 100_000;
        int[] a = TestArrays.randomValues(new Random(73), n);
        Metrics metrics = new Metrics();
        QuickSelect.select(a, n / 2, metrics);
        assertTrue(metrics.comparisons() <= 10L * n,
                "expected linear comparisons but counted " + metrics.comparisons());
    }
}
