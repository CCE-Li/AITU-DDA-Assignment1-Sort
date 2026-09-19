package kz.aitu.daa.benchmark;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import kz.aitu.daa.metrics.Metrics;
import kz.aitu.daa.plot.PlotGenerator;
import kz.aitu.daa.select.MedianOfMedians;
import kz.aitu.daa.select.QuickSelect;
import kz.aitu.daa.sort.MergeSort;
import kz.aitu.daa.sort.QuickSort;

/**
 * Runs every algorithm on every input type and size, repeats each case {@value #RUNS} times and
 * keeps the median run, then writes {@code results.csv} and the plots.
 *
 * <p>Only the algorithm call itself is timed; cloning the input, comparing the result with the
 * reference and taking the median all happen outside the measured section.
 */
public final class Benchmark {

    public static final int[] SIZES = {1_000, 10_000, 100_000, 1_000_000};
    public static final int RUNS = 5;
    public static final long SEED = 42L;
    public static final Path CSV_PATH = Path.of("results.csv");
    public static final Path PLOTS_DIR = Path.of("plots");

    private Benchmark() {
    }

    public static void main(String[] args) throws IOException {
        System.out.println("AITU DAA Assignment 1 - sorting and selection benchmark");
        System.out.printf("sizes=%s runs=%d seed=%d%n%n", Arrays.toString(SIZES), RUNS, SEED);
        warmUp();
        List<ResultRow> rows = new ArrayList<>();
        for (InputType input : InputType.values()) {
            for (int n : SIZES) {
                int[] data = InputGenerator.generate(input, n, SEED);
                for (Algorithm algorithm : Algorithm.values()) {
                    ResultRow row = measure(algorithm, input, data);
                    rows.add(row);
                    System.out.printf(Locale.ROOT, "%-15s %-10s n=%,9d  time=%10.4f ms  comparisons=%,13d  depth=%4d%n",
                            row.algorithm(), row.input(), row.n(), row.timeMs(), row.comparisons(), row.maxDepth());
                }
            }
            System.out.println();
        }
        CsvWriter.write(CSV_PATH, rows);
        System.out.println("wrote " + CSV_PATH.toAbsolutePath());
        PlotGenerator.generate(CSV_PATH, PLOTS_DIR);
        System.out.println("wrote plots to " + PLOTS_DIR.toAbsolutePath());
    }

    /** Lets the JIT compiler warm up before anything is measured. */
    private static void warmUp() {
        int[] data = InputGenerator.generate(InputType.RANDOM, 20_000, SEED);
        for (int round = 0; round < 3; round++) {
            for (Algorithm algorithm : Algorithm.values()) {
                run(algorithm, data.clone(), data.length / 2, new Metrics());
            }
        }
    }

    private static ResultRow measure(Algorithm algorithm, InputType input, int[] data) {
        int k = data.length / 2;
        int expected = algorithm.isSelection() ? kthSmallest(data, k) : 0;
        List<Run> runs = new ArrayList<>(RUNS);
        for (int r = 0; r < RUNS; r++) {
            int[] work = data.clone();
            Metrics metrics = new Metrics();
            metrics.startTimer();
            int result = run(algorithm, work, k, metrics);
            metrics.stopTimer();
            verify(algorithm, input, work, result, expected);
            runs.add(new Run(metrics.elapsedNanos(), metrics.comparisons(), metrics.maxDepth()));
        }
        runs.sort(Comparator.comparingLong(Run::nanos));
        Run median = runs.get(RUNS / 2);
        return new ResultRow(algorithm.csvName(), input.csvName(), data.length,
                median.nanos / 1_000_000.0, median.comparisons, median.maxDepth);
    }

    /** Sorts return an unused placeholder; selection returns the k-th smallest element. */
    private static int run(Algorithm algorithm, int[] a, int k, Metrics metrics) {
        switch (algorithm) {
            case MERGE_SORT -> MergeSort.sort(a, metrics);
            case QUICK_SORT -> QuickSort.sort(a, metrics);
            case QUICK_SELECT -> {
                return QuickSelect.select(a, k, metrics);
            }
            case MEDIAN_OF_MEDIANS -> {
                return MedianOfMedians.select(a, k, metrics);
            }
        }
        return 0;
    }

    private static void verify(Algorithm algorithm, InputType input, int[] work, int result, int expected) {
        if (algorithm.isSelection()) {
            if (result != expected) {
                throw new IllegalStateException(algorithm + " returned " + result + " but the "
                        + (work.length / 2) + "-th smallest element of " + input + " input is " + expected);
            }
            return;
        }
        for (int i = 1; i < work.length; i++) {
            if (work[i - 1] > work[i]) {
                throw new IllegalStateException(algorithm + " did not sort " + input + " input at index " + i);
            }
        }
    }

    private static int kthSmallest(int[] data, int k) {
        int[] sorted = data.clone();
        Arrays.sort(sorted);
        return sorted[k];
    }

    private record Run(long nanos, long comparisons, int maxDepth) {
    }
}
