package kz.aitu.daa.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MetricsTest {

    @Test
    void startsEmpty() {
        Metrics metrics = new Metrics();
        assertEquals(0, metrics.comparisons());
        assertEquals(0, metrics.maxDepth());
        assertEquals(0, metrics.elapsedNanos());
    }

    @Test
    void countsEveryComparison() {
        Metrics metrics = new Metrics();
        for (int i = 0; i < 25; i++) {
            metrics.compare();
        }
        assertEquals(25, metrics.comparisons());
    }

    @Test
    void keepsTheDeepestRecursionLevel() {
        Metrics metrics = new Metrics();
        metrics.enterRecursion();
        assertEquals(1, metrics.maxDepth());
        metrics.enterRecursion();
        metrics.enterRecursion();
        metrics.exitRecursion();
        metrics.enterRecursion();
        assertEquals(3, metrics.maxDepth());
    }

    @Test
    void measuresElapsedTime() {
        Metrics metrics = new Metrics();
        metrics.startTimer();
        long deadline = System.nanoTime() + 2_000_000L;
        while (System.nanoTime() < deadline) {
            // busy wait to get a measurable duration
        }
        metrics.stopTimer();
        assertTrue(metrics.elapsedMillis() >= 1.0, "expected at least 1 ms, got " + metrics.elapsedMillis());
    }

    @Test
    void accumulatesTimeAcrossSeveralRuns() {
        Metrics metrics = new Metrics();
        for (int run = 0; run < 3; run++) {
            metrics.startTimer();
            long deadline = System.nanoTime() + 1_000_000L;
            while (System.nanoTime() < deadline) {
                // busy wait
            }
            metrics.stopTimer();
        }
        assertTrue(metrics.elapsedMillis() >= 3.0, "expected the three runs to add up, got " + metrics.elapsedMillis());
    }

    @Test
    void resetClearsEverything() {
        Metrics metrics = new Metrics();
        metrics.compare();
        metrics.enterRecursion();
        metrics.startTimer();
        metrics.stopTimer();
        metrics.reset();
        assertEquals(0, metrics.comparisons());
        assertEquals(0, metrics.maxDepth());
        assertEquals(0, metrics.elapsedNanos());
    }
}
