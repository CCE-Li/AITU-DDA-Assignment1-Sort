package kz.aitu.daa.sort;

import java.util.Random;
import kz.aitu.daa.metrics.Metrics;
public final class QuickSort {

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
