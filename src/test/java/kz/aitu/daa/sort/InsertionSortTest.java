package kz.aitu.daa.sort;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import kz.aitu.daa.TestArrays;
import kz.aitu.daa.metrics.Metrics;
import org.junit.jupiter.api.Test;

class InsertionSortTest {

    @Test
    void sortsRandomArrays() {
        Random random = new Random(3);
        for (int trial = 0; trial < 100; trial++) {
            int[] a = TestArrays.randomValues(random, random.nextInt(40));
            int[] expected = TestArrays.sorted(a);
            InsertionSort.sort(a, new Metrics());
            assertArrayEquals(expected, a, "trial " + trial);
        }
    }

    @Test
    void handlesEmptyArray() {
        int[] a = {};
        InsertionSort.sort(a, new Metrics());
        assertArrayEquals(new int[0], a);
    }

    @Test
    void handlesSingleElement() {
        int[] a = {42};
        InsertionSort.sort(a, new Metrics());
        assertArrayEquals(new int[] {42}, a);
    }

    @Test
    void handlesEqualElements() {
        int[] a = TestArrays.filled(20, 7);
        InsertionSort.sort(a, new Metrics());
        assertArrayEquals(TestArrays.filled(20, 7), a);
    }

    @Test
    void sortsOnlyTheRequestedRange() {
        int[] a = {9, 5, 3, 1, 7, 2, 8};
        InsertionSort.sort(a, 1, 4, new Metrics());
        assertArrayEquals(new int[] {9, 1, 3, 5, 7, 2, 8}, a);
    }

    @Test
    void countsComparisonsOfASortedRange() {
        int[] a = {1, 2, 3, 4, 5};
        Metrics metrics = new Metrics();
        InsertionSort.sort(a, metrics);
        assertTrue(metrics.comparisons() >= 4, "at least n-1 comparisons are needed");
    }
}
