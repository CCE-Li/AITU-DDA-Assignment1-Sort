package kz.aitu.daa.select;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import kz.aitu.daa.TestArrays;
import kz.aitu.daa.metrics.Metrics;
import org.junit.jupiter.api.Test;

class MedianOfMediansTest {

    private static final int TRIALS = 100;

    @Test
    void matchesSortedArrayOn100RandomArrays() {
        Random random = new Random(79);
        for (int trial = 0; trial < TRIALS; trial++) {
            int n = 1 + random.nextInt(500);
            int[] a = trial % 2 == 0
                    ? TestArrays.randomValues(random, n)
                    : TestArrays.randomValues(random, n, 4);
            int k = random.nextInt(n);
            int[] expected = TestArrays.sorted(a);
            assertEquals(expected[k], MedianOfMedians.select(a, k, new Metrics()), "trial " + trial + " k=" + k);
        }
    }

    @Test
    void handlesTheAdversarialSortedInput() {
        int n = 100_000;
        int[] a = TestArrays.sorted(new Random(83), n);
        int[] expected = a.clone();
        Metrics metrics = new Metrics();
        assertEquals(expected[12_345], MedianOfMedians.select(a, 12_345, metrics));
        assertTrue(metrics.comparisons() <= 60L * n,
                "median of medians must stay linear, counted " + metrics.comparisons());
    }

    @Test
    void handlesReverseSortedInput() {
        int[] a = TestArrays.sorted(new Random(89), 20_000);
        for (int i = 0; i < a.length / 2; i++) {
            int tmp = a[i];
            a[i] = a[a.length - 1 - i];
            a[a.length - 1 - i] = tmp;
        }
        int[] expected = TestArrays.sorted(a);
        assertEquals(expected[7_777], MedianOfMedians.select(a, 7_777, new Metrics()));
    }

    @Test
    void handlesManyDuplicates() {
        int[] a = TestArrays.randomValues(new Random(97), 50_000, 3);
        int[] expected = TestArrays.sorted(a);
        assertEquals(expected[25_000], MedianOfMedians.select(a, 25_000, new Metrics()));
    }

    @Test
    void handlesAllElementsEqual() {
        int[] a = TestArrays.filled(10_000, -5);
        assertEquals(-5, MedianOfMedians.select(a, 4_999, new Metrics()));
    }

    @Test
    void handlesSingleElement() {
        assertEquals(3, MedianOfMedians.select(new int[] {3}, 0, new Metrics()));
    }

    @Test
    void handlesBoundariesOfTheGroupSize() {
        Random random = new Random(101);
        for (int n = 1; n <= MedianOfMedians.GROUP_SIZE * 3 + 1; n++) {
            int[] a = TestArrays.randomValues(random, n);
            int[] expected = TestArrays.sorted(a);
            for (int k = 0; k < n; k++) {
                assertEquals(expected[k], MedianOfMedians.select(a.clone(), k, new Metrics()), "n=" + n + " k=" + k);
            }
        }
    }

    @Test
    void rejectsEmptyArray() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> MedianOfMedians.select(new int[0], 0, new Metrics()));
        assertTrue(exception.getMessage().contains("empty"), exception.getMessage());
    }

    @Test
    void rejectsNullArray() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> MedianOfMedians.select(null, 0, new Metrics()));
        assertTrue(exception.getMessage().contains("null"), exception.getMessage());
    }

    @Test
    void rejectsKOutOfRange() {
        assertThrows(IllegalArgumentException.class,
                () -> MedianOfMedians.select(new int[] {1, 2, 3}, -1, new Metrics()));
        assertThrows(IllegalArgumentException.class,
                () -> MedianOfMedians.select(new int[] {1, 2, 3}, 3, new Metrics()));
    }

    @Test
    void pivotStaysNearTheMedianForEverySize() {
        int[] sizes = {10_000, 20_000, 50_000, 100_000, 200_000};
        for (int n : sizes) {
            int[] a = TestArrays.sorted(new Random(151), n);
            Metrics metrics = new Metrics();
            MedianOfMedians.select(a, n / 2, metrics);
            assertTrue(metrics.comparisons() <= 12L * n,
                    "n=" + n + " needed " + metrics.comparisons() + " comparisons, the pivot is not a 30/70 split");
        }
    }

    @Test
    void comparisonsGrowLinearlyWithN() {
        int smallN = 50_000;
        int largeN = 200_000;
        Metrics small = new Metrics();
        Metrics large = new Metrics();
        MedianOfMedians.select(TestArrays.randomValues(new Random(103), smallN), smallN / 2, small);
        MedianOfMedians.select(TestArrays.randomValues(new Random(103), largeN), largeN / 2, large);
        double smallPerElement = small.comparisons() / (double) smallN;
        double largePerElement = large.comparisons() / (double) largeN;
        assertTrue(largePerElement <= 1.5 * smallPerElement,
                "the cost per element must stay almost constant for Theta(n): "
                        + smallPerElement + " -> " + largePerElement);
    }
}
