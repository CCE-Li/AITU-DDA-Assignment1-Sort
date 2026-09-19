package kz.aitu.daa.sort;

import kz.aitu.daa.metrics.Metrics;

/**
 * The three-way (Dutch national flag) partition shared by {@link QuickSort} and the selection
 * algorithms, so duplicates stay fast and no array allocation happens per call.
 *
 * <p>After {@link #threeWay} the range {@code [lo, hi]} looks like
 * {@code [< pivot][== pivot][> pivot]}; the boundaries of the equality region are reported through a
 * caller-owned {@link Bounds} instance.
 */
public final class Partition {

    private Partition() {
    }

    /** Reusable pair of indices {@code [lt, gt]} delimiting the "equal to pivot" region. */
    public static final class Bounds {
        public int lt;
        public int gt;
    }

    /**
     * Partitions {@code a[lo..hi]} around {@code pivotValue} in place.
     *
     * @param a          the array to partition
     * @param lo         first index of the range (inclusive)
     * @param hi         last index of the range (inclusive)
     * @param pivotValue the value to partition around; it must occur in {@code a[lo..hi]}
     * @param metrics    comparison counter
     * @param bounds     receives the indices such that {@code a[lt..gt] == pivotValue},
     *                   {@code a[lo..lt-1] < pivotValue} and {@code a[gt+1..hi] > pivotValue}
     */
    public static void threeWay(int[] a, int lo, int hi, int pivotValue, Metrics metrics, Bounds bounds) {
        int lt = lo;
        int gt = hi;
        int i = lo;
        while (i <= gt) {
            metrics.compare();
            int cmp = Integer.compare(a[i], pivotValue);
            if (cmp < 0) {
                swap(a, lt++, i++);
            } else if (cmp > 0) {
                swap(a, i, gt--);
            } else {
                i++;
            }
        }
        bounds.lt = lt;
        bounds.gt = gt;
    }

    public static void swap(int[] a, int i, int j) {
        int tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }
}
