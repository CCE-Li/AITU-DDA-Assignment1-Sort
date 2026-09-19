package kz.aitu.daa.sort;

import kz.aitu.daa.metrics.Metrics;

/**
 * Top-down merge sort for {@code int[]}.
 *
 * <p>Guarantees that matter for the analytics platform:
 * <ul>
 *   <li>exactly one helper array is allocated per top-level call and passed down the recursion;</li>
 *   <li>ranges of {@value #CUTOFF} elements or fewer are finished with insertion sort;</li>
 *   <li>merging two sorted halves is linear, so the recurrence is T(n) = 2T(n/2) + Θ(n) = Θ(n log n).</li>
 * </ul>
 *
 * <p>The recursion depth is Θ(log n), which is far below any JVM stack limit even for n = 10^8.
 */
public final class MergeSort {

    /** Ranges of this size or smaller are sorted with insertion sort instead of recursing further. */
    public static final int CUTOFF = 15;

    private MergeSort() {
    }

    public static void sort(int[] a, Metrics metrics) {
        if (a == null) {
            throw new IllegalArgumentException("array must not be null");
        }
        int[] buffer = new int[a.length];
        sort(a, buffer, 0, a.length - 1, metrics);
    }

    private static void sort(int[] a, int[] buffer, int lo, int hi, Metrics metrics) {
        if (hi - lo + 1 <= CUTOFF) {
            InsertionSort.sort(a, lo, hi, metrics);
            return;
        }
        int mid = lo + (hi - lo) / 2;
        metrics.enterRecursion();
        sort(a, buffer, lo, mid, metrics);
        sort(a, buffer, mid + 1, hi, metrics);
        metrics.exitRecursion();
        merge(a, buffer, lo, mid, hi, metrics);
    }

    /** Merges the sorted ranges {@code [lo, mid]} and {@code [mid+1, hi]} in linear time. */
    private static void merge(int[] a, int[] buffer, int lo, int mid, int hi, Metrics metrics) {
        System.arraycopy(a, lo, buffer, lo, hi - lo + 1);
        int i = lo;
        int j = mid + 1;
        for (int k = lo; k <= hi; k++) {
            if (i > mid) {
                a[k] = buffer[j++];
            } else if (j > hi) {
                a[k] = buffer[i++];
            } else {
                metrics.compare();
                if (buffer[i] <= buffer[j]) {
                    a[k] = buffer[i++];
                } else {
                    a[k] = buffer[j++];
                }
            }
        }
    }
}
