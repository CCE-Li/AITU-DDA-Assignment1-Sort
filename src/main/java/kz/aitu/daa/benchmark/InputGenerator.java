package kz.aitu.daa.benchmark;

import java.util.Arrays;
import java.util.Random;

/** Builds the three input shapes used by the benchmark. */
public final class InputGenerator {

    /** Values of the {@link InputType#DUPLICATES} input are drawn from {@code [0, DUPLICATE_RANGE)}. */
    public static final int DUPLICATE_RANGE = 10;

    private InputGenerator() {
    }

    public static int[] generate(InputType type, int n, long seed) {
        Random random = new Random(seed);
        int[] a = new int[n];
        switch (type) {
            case RANDOM -> {
                for (int i = 0; i < n; i++) {
                    a[i] = random.nextInt();
                }
            }
            case SORTED -> {
                for (int i = 0; i < n; i++) {
                    a[i] = random.nextInt();
                }
                Arrays.sort(a);
            }
            case DUPLICATES -> {
                for (int i = 0; i < n; i++) {
                    a[i] = random.nextInt(DUPLICATE_RANGE);
                }
            }
        }
        return a;
    }
}
