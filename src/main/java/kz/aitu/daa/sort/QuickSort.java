package kz.aitu.daa.sort;

import java.util.Random;
import kz.aitu.daa.metrics.Metrics;

/**
 * Quick sort for {@code int[]} that is safe for very large arrays.
 *
 * <p>Three design decisions keep it out of trouble:
 * <ul>
 *   <li><b>Random pivot</b> - the pivot is drawn uniformly from the current range, so an already
 *       sorted input cannot trigger the quadratic worst case.</li>
 *   <li><b>Smaller side first</b> - only the smaller sub-range is recursed into, the larger one is
 *       handled by the surrounding loop. The recursion depth is therefore O(log n) even in the
 *       worst case, and a {@link StackOverflowError} is impossible for realistic inputs.</li>
 *   <li><b>Three-way partition</b> - values equal to the pivot are grouped in the middle and never
 *       looked at again, so an array of equal values costs Θ(n) instead of Θ(n²).</li>
 * </ul>
 *
 * <p>Average running time is Θ(n log n); the worst case is O(n²) and only occurs with adversarial
 * pivot choices, which the random pivot makes extremely unlikely.
 */
public final class QuickSort {

    /** Fixed seed so benchmarks and tests are reproducible. */
    public static final long DEFAULT_SEED = 20240920L;

    private QuickSort() {
    }

    public static void sort(int[] a, Metrics metrics) {
        sort(a, new Random(DEFAULT_SEED), metrics);
    }

    public static void sort(int[] a, Random random, Metrics metrics) {
        if (a == null) {
            throw new IllegalArgumentException("array must not be null");
        }
        Partition.Bounds bounds = new Partition.Bounds();
        sort(a, 0, a.length - 1, random, metrics, bounds);
    }

    private static void sort(int[] a, int lo, int hi, Random random, Metrics metrics, Partition.Bounds bounds) {
        while (lo < hi) {
            int pivotIndex = lo + random.nextInt(hi - lo + 1);
            Partition.threeWay(a, lo, hi, a[pivotIndex], metrics, bounds);
            int lt = bounds.lt;
            int gt = bounds.gt;
            if (lt - lo < hi - gt) {
                metrics.enterRecursion();
                sort(a, lo, lt - 1, random, metrics, bounds);
                metrics.exitRecursion();
                lo = gt + 1;
            } else {
                metrics.enterRecursion();
                sort(a, gt + 1, hi, random, metrics, bounds);
                metrics.exitRecursion();
                hi = lt - 1;
            }
        }
    }
}
