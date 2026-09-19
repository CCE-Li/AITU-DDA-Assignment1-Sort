package kz.aitu.daa.select;

import java.util.Random;
import kz.aitu.daa.metrics.Metrics;
import kz.aitu.daa.sort.Partition;
import kz.aitu.daa.sort.QuickSort;

/**
 * Quickselect: returns the k-th smallest element (0-based) without sorting the whole array.
 *
 * <p>It reuses {@link Partition#threeWay} from quick sort and after every partition continues in the
 * single side that contains position k, so the expected number of comparisons is Θ(n).
 *
 * <p>The loop is iterative on purpose: selection needs no stack at all. Because there is no real
 * recursion, {@link Metrics#maxDepth()} reports the number of partition rounds instead - the depth a
 * naive recursive implementation would have used.
 */
public final class QuickSelect {

    private QuickSelect() {
    }

    /**
     * @param a       the array to select from; it is partially reordered in place
     * @param k       position of the wanted element, {@code 0 <= k < a.length}
     * @param metrics comparison and depth counters
     * @return the k-th smallest element of {@code a}
     * @throws IllegalArgumentException if the array is null/empty or k is out of range
     */
    public static int select(int[] a, int k, Metrics metrics) {
        return select(a, k, new Random(QuickSort.DEFAULT_SEED), metrics);
    }

    public static int select(int[] a, int k, Random random, Metrics metrics) {
        if (a == null) {
            throw new IllegalArgumentException("array must not be null");
        }
        if (a.length == 0) {
            throw new IllegalArgumentException("array must not be empty");
        }
        if (k < 0 || k >= a.length) {
            throw new IllegalArgumentException("k must be in [0, " + (a.length - 1) + "] but was " + k);
        }
        Partition.Bounds bounds = new Partition.Bounds();
        int lo = 0;
        int hi = a.length - 1;
        while (true) {
            metrics.enterRecursion();
            int pivotIndex = lo + random.nextInt(hi - lo + 1);
            Partition.threeWay(a, lo, hi, a[pivotIndex], metrics, bounds);
            if (k < bounds.lt) {
                hi = bounds.lt - 1;
            } else if (k > bounds.gt) {
                lo = bounds.gt + 1;
            } else {
                return a[k];
            }
        }
    }
}
