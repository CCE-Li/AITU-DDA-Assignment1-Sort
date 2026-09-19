package kz.aitu.daa.metrics;

/**
 * Counters collected while an algorithm runs.
 *
 * <p>A {@code Metrics} instance is created by the caller and passed down into the algorithm, so the
 * algorithms stay free of static (global) state and can be run concurrently without interference.
 *
 * <p>{@code comparisons} counts element-to-element comparisons, {@code maxDepth} is the deepest
 * recursion level reached, and {@code elapsedNanos} accumulates the wall-clock time measured with
 * {@link System#nanoTime()}.
 */
public final class Metrics {

    private long comparisons;
    private int currentDepth;
    private int maxDepth;
    private long startNanos;
    private long elapsedNanos;

    /** Records one element-to-element comparison. */
    public void compare() {
        comparisons++;
    }

    /** Records entering a recursion level. */
    public void enterRecursion() {
        currentDepth++;
        if (currentDepth > maxDepth) {
            maxDepth = currentDepth;
        }
    }

    /** Records leaving a recursion level. */
    public void exitRecursion() {
        currentDepth--;
    }

    /** Starts (or restarts) the timer. */
    public void startTimer() {
        startNanos = System.nanoTime();
    }

    /** Accumulates the time elapsed since {@link #startTimer()}. */
    public void stopTimer() {
        elapsedNanos += System.nanoTime() - startNanos;
    }

    /** Clears every counter. */
    public void reset() {
        comparisons = 0;
        currentDepth = 0;
        maxDepth = 0;
        elapsedNanos = 0;
    }

    public long comparisons() {
        return comparisons;
    }

    public int maxDepth() {
        return maxDepth;
    }

    public long elapsedNanos() {
        return elapsedNanos;
    }

    public double elapsedMillis() {
        return elapsedNanos / 1_000_000.0;
    }
}
