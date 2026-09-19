package kz.aitu.daa.sort;

import kz.aitu.daa.metrics.Metrics;

/**
 * Insertion sort, used as the base case of {@link MergeSort} and by the median-of-medians pivot
 * selection. It sorts the half-open range {@code [lo, hi]} in place and is Θ(n²) in the worst case,
 * which is fine because it only ever runs on the constant-size cutoff range.
 */
public final class InsertionSort {

    private InsertionSort() {
    }

    public static void sort(int[] a, Metrics metrics) {
        sort(a, 0, a.length - 1, metrics);
    }

    public static void sort(int[] a, int lo, int hi, Metrics metrics) {
        for (int i = lo + 1; i <= hi; i++) {
            int key = a[i];
            int j = i - 1;
            while (j >= lo) {
                metrics.compare();
                if (a[j] <= key) {
                    break;
                }
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = key;
        }
    }
}
