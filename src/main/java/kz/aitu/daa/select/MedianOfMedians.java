package kz.aitu.daa.select;

import kz.aitu.daa.metrics.Metrics;
import kz.aitu.daa.sort.InsertionSort;
import kz.aitu.daa.sort.Partition;

/**
 * Selection in guaranteed linear time using the median-of-medians pivot (Task A).
 *
 * <p>The range is split into groups of five, every group is sorted with insertion sort and its
 * median is moved into the prefix of the range. The exact median of those medians is then computed
 * with the same procedure and used as the pivot. Because that pivot can never be below the 30th or
 * above the 70th percentile, every partition removes at least 30% of the elements:
 *
 * <pre>T(n) = T(n/5) + T(7n/10) + Theta(n) = Theta(n)</pre>
 *
 * <p>The median of the medians has to be selected <em>exactly</em>. Approximating it by recursing
 * level after level without ever partitioning would let the bias of the tiny base cases be
 * multiplied by five on every level, which pushes the pivot far above the 70th percentile on sorted
 * input and destroys the linear worst case.
 *
 * <p>Compared with {@link QuickSelect} the worst case drops from O(n^2) to Theta(n), at the price of
 * a much larger constant factor.
 */
public final class MedianOfMedians {

    /** Size of the groups whose median is taken. */
    public static final int GROUP_SIZE = 5;

    private MedianOfMedians() {
    }

    /**
     * @param a       the array to select from; it is partially reordered in place
     * @param k       position of the wanted element, {@code 0 <= k < a.length}
     * @param metrics comparison and depth counters
     * @return the k-th smallest element of {@code a}
     * @throws IllegalArgumentException if the array is null/empty or k is out of range
     */
    public static int select(int[] a, int k, Metrics metrics) {
        if (a == null) {
            throw new IllegalArgumentException("array must not be null");
        }
        if (a.length == 0) {
            throw new IllegalArgumentException("array must not be empty");
        }
        if (k < 0 || k >= a.length) {
            throw new IllegalArgumentException("k must be in [0, " + (a.length - 1) + "] but was " + k);
        }
        return selectRange(a, 0, a.length - 1, k, metrics);
    }

    /**
     * Selects the element that belongs at index {@code target} inside {@code a[lo..hi]}.
     * The loop continues only in the part that contains {@code target}, so no stack is used for it.
     */
    private static int selectRange(int[] a, int lo, int hi, int target, Metrics metrics) {
        Partition.Bounds bounds = new Partition.Bounds();
        while (true) {
            if (hi - lo + 1 <= GROUP_SIZE) {
                InsertionSort.sort(a, lo, hi, metrics);
                return a[target];
            }
            int pivotValue = pivot(a, lo, hi, metrics);
            Partition.threeWay(a, lo, hi, pivotValue, metrics, bounds);
            if (target < bounds.lt) {
                hi = bounds.lt - 1;
            } else if (target > bounds.gt) {
                lo = bounds.gt + 1;
            } else {
                return a[target];
            }
        }
    }

    /** The exact median of the medians of {@code a[lo..hi]}, which is guaranteed to be a 30/70 pivot. */
    private static int pivot(int[] a, int lo, int hi, Metrics metrics) {
        int n = hi - lo + 1;
        int groups = (n + GROUP_SIZE - 1) / GROUP_SIZE;
        for (int g = 0; g < groups; g++) {
            int groupLo = lo + g * GROUP_SIZE;
            int groupHi = Math.min(groupLo + GROUP_SIZE - 1, hi);
            InsertionSort.sort(a, groupLo, groupHi, metrics);
            Partition.swap(a, lo + g, groupLo + (groupHi - groupLo) / 2);
        }
        int medianOfMedians = lo + (groups - 1) / 2;
        metrics.enterRecursion();
        int result = selectRange(a, lo, lo + groups - 1, medianOfMedians, metrics);
        metrics.exitRecursion();
        return result;
    }
}
