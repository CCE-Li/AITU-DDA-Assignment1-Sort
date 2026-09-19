package kz.aitu.daa.sort;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import kz.aitu.daa.TestArrays;
import kz.aitu.daa.metrics.Metrics;
import org.junit.jupiter.api.Test;

class MergeSortTest {

    private static final int TRIALS = 100;

    @Test
    void matchesArraysSortOn100RandomArrays() {
        Random random = new Random(11);
        for (int trial = 0; trial < TRIALS; trial++) {
            int n = random.nextInt(600);
            int[] a = trial % 2 == 0
                    ? TestArrays.randomValues(random, n)
                    : TestArrays.randomValues(random, n, 10);
            int[] expected = TestArrays.sorted(a);
            MergeSort.sort(a, new Metrics());
            assertArrayEquals(expected, a, "trial " + trial);
        }
    }

    @Test
    void handlesEmptyArray() {
        int[] a = {};
        MergeSort.sort(a, new Metrics());
        assertArrayEquals(new int[0], a);
    }

    @Test
    void handlesSingleElement() {
        int[] a = {5};
        MergeSort.sort(a, new Metrics());
        assertArrayEquals(new int[] {5}, a);
    }

    @Test
    void handlesAllElementsEqual() {
        int[] a = TestArrays.filled(5_000, 3);
        MergeSort.sort(a, new Metrics());
        assertArrayEquals(TestArrays.filled(5_000, 3), a);
    }

    @Test
    void handlesAlreadySortedArray() {
        int[] a = TestArrays.sorted(new Random(5), 5_000);
        int[] expected = a.clone();
        MergeSort.sort(a, new Metrics());
        assertArrayEquals(expected, a);
    }

    @Test
    void handlesReverseSortedArray() {
        Random random = new Random(6);
        int[] a = TestArrays.sorted(random, 5_000);
        for (int i = 0; i < a.length / 2; i++) {
            int tmp = a[i];
            a[i] = a[a.length - 1 - i];
            a[a.length - 1 - i] = tmp;
        }
        int[] expected = TestArrays.sorted(a);
        MergeSort.sort(a, new Metrics());
        assertArrayEquals(expected, a);
    }

    @Test
    void handlesSizesAroundTheInsertionCutoff() {
        Random random = new Random(7);
        for (int n = 0; n <= MergeSort.CUTOFF * 2 + 2; n++) {
            for (int pattern = 0; pattern < 3; pattern++) {
                int[] a = switch (pattern) {
                    case 0 -> TestArrays.randomValues(random, n);
                    case 1 -> TestArrays.sorted(random, n);
                    default -> TestArrays.filled(n, 1);
                };
                int[] expected = TestArrays.sorted(a);
                MergeSort.sort(a, new Metrics());
                assertArrayEquals(expected, a, "n=" + n + " pattern=" + pattern);
            }
        }
    }

    @Test
    void rejectsNullArray() {
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> MergeSort.sort(null, new Metrics()));
        assertTrue(exception.getMessage().contains("null"));
    }

    @Test
    void staysWithinLinearithmicComparisons() {
        int n = 100_000;
        int[] a = TestArrays.randomValues(new Random(13), n);
        Metrics metrics = new Metrics();
        MergeSort.sort(a, metrics);
        double bound = 2.0 * n * TestArrays.log2(n);
        assertTrue(metrics.comparisons() <= bound,
                "expected at most " + (long) bound + " comparisons but counted " + metrics.comparisons());
    }

    @Test
    void recursionDepthIsLogarithmic() {
        int n = 100_000;
        int[] a = TestArrays.randomValues(new Random(17), n);
        Metrics metrics = new Metrics();
        MergeSort.sort(a, metrics);
        assertTrue(metrics.maxDepth() <= Math.ceil(TestArrays.log2(n)),
                "depth " + metrics.maxDepth() + " should stay below log2(n) for n=" + n);
    }

    @Test
    void doesNotChangeTheArrayForOneElementRanges() {
        int[] a = {2, 1};
        Metrics metrics = new Metrics();
        MergeSort.sort(a, metrics);
        assertArrayEquals(new int[] {1, 2}, a);
        assertEquals(1, metrics.comparisons());
    }
}
