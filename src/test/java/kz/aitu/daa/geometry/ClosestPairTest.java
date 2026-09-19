package kz.aitu.daa.geometry;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import kz.aitu.daa.TestArrays;
import kz.aitu.daa.metrics.Metrics;
import org.junit.jupiter.api.Test;

class ClosestPairTest {

    private static final double EPSILON = 1e-9;

    @Test
    void matchesBruteForceOn100RandomPointSets() {
        Random random = new Random(107);
        for (int trial = 0; trial < 100; trial++) {
            int n = 2 + random.nextInt(200);
            Point[] points = randomPoints(random, n, 1_000);
            assertEquals(bruteForce(points), ClosestPair.closestDistance(points, new Metrics()), EPSILON,
                    "trial " + trial);
        }
    }

    @Test
    void matchesBruteForceForTwoThousandPoints() {
        Random random = new Random(109);
        Point[] points = randomPoints(random, 2_000, 100_000);
        assertEquals(bruteForce(points), ClosestPair.closestDistance(points, new Metrics()), EPSILON);
    }

    @Test
    void matchesBruteForceForClusteredPoints() {
        Random random = new Random(113);
        for (int trial = 0; trial < 20; trial++) {
            int n = 100 + random.nextInt(400);
            Point[] points = new Point[n];
            for (int i = 0; i < n; i++) {
                double clusterX = random.nextInt(3) * 1e6;
                double clusterY = random.nextInt(3) * 1e6;
                points[i] = new Point(clusterX + random.nextDouble(), clusterY + random.nextDouble());
            }
            assertEquals(bruteForce(points), ClosestPair.closestDistance(points, new Metrics()), EPSILON,
                    "trial " + trial);
        }
    }

    @Test
    void returnsInfinityWhenFewerThanTwoPoints() {
        assertEquals(Double.POSITIVE_INFINITY, ClosestPair.closestDistance(new Point[0], new Metrics()));
        assertEquals(Double.POSITIVE_INFINITY,
                ClosestPair.closestDistance(new Point[] {new Point(1, 2)}, new Metrics()));
    }

    @Test
    void findsZeroForRepeatedPoints() {
        Point[] points = {new Point(0, 0), new Point(5, 5), new Point(5, 5), new Point(9, 1)};
        assertEquals(0.0, ClosestPair.closestDistance(points, new Metrics()), EPSILON);
    }

    @Test
    void handlesPointsOnAVerticalLine() {
        Point[] points = new Point[50];
        for (int i = 0; i < points.length; i++) {
            points[i] = new Point(7.0, i * 3.0);
        }
        assertEquals(3.0, ClosestPair.closestDistance(points, new Metrics()), EPSILON);
    }

    @Test
    void handlesPointsOnAHorizontalLine() {
        Random random = new Random(127);
        Point[] points = new Point[200];
        for (int i = 0; i < points.length; i++) {
            points[i] = new Point(random.nextDouble() * 1_000, 42.0);
        }
        assertEquals(bruteForce(points), ClosestPair.closestDistance(points, new Metrics()), EPSILON);
    }

    @Test
    void handlesPointsWithEqualXCoordinates() {
        Point[] points = new Point[300];
        Random random = new Random(131);
        for (int i = 0; i < points.length; i++) {
            points[i] = new Point(random.nextInt(10), random.nextDouble() * 1_000);
        }
        assertEquals(bruteForce(points), ClosestPair.closestDistance(points, new Metrics()), EPSILON);
    }

    @Test
    void doesNotModifyTheInputArray() {
        Random random = new Random(137);
        Point[] points = randomPoints(random, 500, 10_000);
        Point[] copy = points.clone();
        ClosestPair.closestDistance(points, new Metrics());
        assertArrayEquals(copy, points);
    }

    @Test
    void recursionDepthIsLogarithmic() {
        Random random = new Random(139);
        Point[] points = randomPoints(random, 100_000, 1_000_000);
        Metrics metrics = new Metrics();
        ClosestPair.closestDistance(points, metrics);
        double limit = 2 * TestArrays.log2(points.length);
        assertTrue(metrics.maxDepth() <= limit,
                "depth " + metrics.maxDepth() + " must stay below " + limit);
    }

    @Test
    void rejectsNullInput() {
        assertThrows(IllegalArgumentException.class, () -> ClosestPair.closestDistance(null, new Metrics()));
    }

    private static Point[] randomPoints(Random random, int n, double bound) {
        Point[] points = new Point[n];
        for (int i = 0; i < n; i++) {
            points[i] = new Point(random.nextDouble() * bound, random.nextDouble() * bound);
        }
        return points;
    }

    private static double bruteForce(Point[] points) {
        double best = Double.POSITIVE_INFINITY;
        for (int i = 0; i < points.length; i++) {
            for (int j = i + 1; j < points.length; j++) {
                double dx = points[i].x() - points[j].x();
                double dy = points[i].y() - points[j].y();
                best = Math.min(best, Math.sqrt(dx * dx + dy * dy));
            }
        }
        return best;
    }
}
