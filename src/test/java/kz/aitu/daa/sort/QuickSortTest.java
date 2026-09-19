package kz.aitu.daa.sort;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import kz.aitu.daa.TestArrays;
import kz.aitu.daa.metrics.Metrics;
import org.junit.jupiter.api.Test;

class QuickSortTest {

    private static final int TRIALS = 100;

    @Test
    void matchesArraysSortOn100RandomArrays() {
        Random random = new Random(23);
        for (int trial = 0; trial < TRIALS; trial++) {
            int n = random.nextInt(600);
            int[] a = trial % 3 == 0
                    ? TestArrays.randomValues(random, n)
                    : TestArrays.randomValues(random, n, 5);
            int[] expected = TestArrays.sorted(a);
            QuickSort.sort(a, new Metrics());
            assertArrayEquals(expected, a, "trial " + trial);
        }
    }

    @Test
    void handlesEmptyArray() {
        int[] a = {};
        QuickSort.sort(a, new Metrics());
        assertArrayEquals(new int[0], a);
    }

    @Test
    void handlesSingleElement() {
        int[] a = {8};
        QuickSort.sort(a, new Metrics());
        assertArrayEquals(new int[] {8}, a);
    }

    @Test
    void handlesAllElementsEqual() {
        int[] a = TestArrays.filled(100_000, 4);
        Metrics metrics = new Metrics();
        QuickSort.sort(a, metrics);
        assertArrayEquals(TestArrays.filled(100_000, 4), a);
        assertTrue(metrics.comparisons() <= 4L * a.length,
                "three-way partitioning must stay linear on equal values, counted " + metrics.comparisons());
    }

    @Test
    void handlesManyDuplicatesInLinearithmicTime() {
        int n = 100_000;
        int[] a = TestArrays.randomValues(new Random(29), n, 10);
        Metrics metrics = new Metrics();
        QuickSort.sort(a, metrics);
        assertArrayEquals(TestArrays.sorted(a), a);
        double bound = 2.0 * n * TestArrays.log2(n);
        assertTrue(metrics.comparisons() <= bound,
                "expected at most " + (long) bound + " comparisons but counted " + metrics.comparisons());
    }

    @Test
    void sortsAlreadySortedArrayWithoutStackOverflow() {
        int n = 100_000;
        int[] a = TestArrays.sorted(new Random(31), n);
        int[] expected = a.clone();
        QuickSort.sort(a, new Metrics());
        assertArrayEquals(expected, a);
    }

    @Test
    void recursionDepthOnSortedInputStaysBelowTwiceLog2N() {
        int n = 100_000;
        int[] a = TestArrays.sorted(new Random(37), n);
        Metrics metrics = new Metrics();
        QuickSort.sort(a, metrics);
        double limit = 2 * TestArrays.log2(n);
        assertTrue(metrics.maxDepth() <= limit,
                "depth " + metrics.maxDepth() + " must stay below " + limit + " for n=" + n);
    }

    @Test
    void recursionDepthOnRandomInputStaysBelowTwiceLog2N() {
        int n = 100_000;
        int[] a = TestArrays.randomValues(new Random(41), n);
        Metrics metrics = new Metrics();
        QuickSort.sort(a, metrics);
        double limit = 2 * TestArrays.log2(n);
        assertTrue(metrics.maxDepth() <= limit,
                "depth " + metrics.maxDepth() + " must stay below " + limit + " for n=" + n);
    }

    @Test
    void sortsReverseSortedArray() {
        int[] a = TestArrays.sorted(new Random(43), 50_000);
        for (int i = 0; i < a.length / 2; i++) {
            int tmp = a[i];
            a[i] = a[a.length - 1 - i];
            a[a.length - 1 - i] = tmp;
        }
        int[] expected = TestArrays.sorted(a);
        QuickSort.sort(a, new Metrics());
        assertArrayEquals(expected, a);
    }

    @Test
    void handlesSizesAroundTheInsertionCutoff() {
        Random random = new Random(47);
        for (int n = 0; n <= MergeSort.CUTOFF * 2 + 2; n++) {
            int[] a = TestArrays.randomValues(random, n, 3);
            int[] expected = TestArrays.sorted(a);
            QuickSort.sort(a, new Metrics());
            assertArrayEquals(expected, a, "n=" + n);
        }
    }

    @Test
    void rejectsNullArray() {
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> QuickSort.sort(null, new Metrics()));
        assertTrue(exception.getMessage().contains("null"));
    }
}
