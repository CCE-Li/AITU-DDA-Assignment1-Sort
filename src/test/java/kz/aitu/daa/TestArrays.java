package kz.aitu.daa;

import java.util.Arrays;
import java.util.Random;

/** Small helpers shared by the test classes. */
public final class TestArrays {

    private TestArrays() {
    }

    /** Array of {@code n} uniformly random ints from the full int range. */
    public static int[] randomValues(Random random, int n) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = random.nextInt();
        }
        return a;
    }

    /** Array of {@code n} random ints from {@code [0, bound)}. */
    public static int[] randomValues(Random random, int n, int bound) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = random.nextInt(bound);
        }
        return a;
    }

    public static int[] sorted(int[] a) {
        int[] copy = a.clone();
        Arrays.sort(copy);
        return copy;
    }

    public static int[] sorted(Random random, int n) {
        return sorted(randomValues(random, n));
    }

    public static int[] filled(int n, int value) {
        int[] a = new int[n];
        Arrays.fill(a, value);
        return a;
    }

    public static double log2(double value) {
        return Math.log(value) / Math.log(2);
    }
}
