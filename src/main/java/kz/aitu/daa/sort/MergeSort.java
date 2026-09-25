package kz.aitu.daa.sort;

import kz.aitu.daa.metrics.Metrics;

public final class MergeSort {

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
