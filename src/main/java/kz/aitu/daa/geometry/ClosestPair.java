package kz.aitu.daa.geometry;

import java.util.Arrays;
import java.util.Comparator;
import kz.aitu.daa.metrics.Metrics;

/**
 * Closest pair of 2D points in O(n log n) (Task B).
 *
 * <p>The points are sorted by x once. Every recursive call splits its range in half at the median
 * line, solves both halves, keeps the smaller distance delta, and then checks only the points that
 * are within delta of the median line, scanned in y order. The packing argument behind the classic
 * algorithm says that at most seven of those strip points can be closer than delta, so the strip
 * scan is linear and
 *
 * <pre>T(n) = 2T(n/2) + Theta(n) = Theta(n log n)</pre>
 *
 * <p>Each call also merges the two y-sorted halves in linear time instead of re-sorting them, which
 * keeps the total cost at O(n log n) rather than O(n log^2 n).
 */
public final class ClosestPair {

    /** Ranges of this size or smaller are solved with the O(n^2) brute force scan. */
    public static final int BRUTE_FORCE_LIMIT = 3;

    /** Number of following strip points that have to be examined for every point. */
    private static final int STRIP_WINDOW = 7;

    private static final Comparator<Point> BY_X =
            Comparator.comparingDouble(Point::x).thenComparingDouble(Point::y);
    private static final Comparator<Point> BY_Y = Comparator.comparingDouble(Point::y);

    private ClosestPair() {
    }

    /**
     * @param points  the input points, not modified
     * @param metrics comparison, distance and depth counters
     * @return the smallest distance between two points, or {@link Double#POSITIVE_INFINITY} when
     *         fewer than two points were given
     */
    public static double closestDistance(Point[] points, Metrics metrics) {
        if (points == null) {
            throw new IllegalArgumentException("points must not be null");
        }
        if (points.length < 2) {
            return Double.POSITIVE_INFINITY;
        }
        Point[] byX = points.clone();
        Arrays.sort(byX, BY_X);
        return solve(byX, metrics).distance;
    }

    /** Smallest distance of a sub-problem together with its points sorted by y. */
    private static final class SubProblem {
        final double distance;
        final Point[] byY;

        SubProblem(double distance, Point[] byY) {
            this.distance = distance;
            this.byY = byY;
        }
    }

    private static SubProblem solve(Point[] byX, Metrics metrics) {
        int n = byX.length;
        if (n <= BRUTE_FORCE_LIMIT) {
            Point[] byY = byX.clone();
            Arrays.sort(byY, BY_Y);
            return new SubProblem(bruteForce(byX, metrics), byY);
        }
        int mid = n / 2;
        double midX = byX[mid].x();
        Point[] leftX = Arrays.copyOfRange(byX, 0, mid);
        Point[] rightX = Arrays.copyOfRange(byX, mid, n);

        metrics.enterRecursion();
        SubProblem left = solve(leftX, metrics);
        SubProblem right = solve(rightX, metrics);
        metrics.exitRecursion();

        double delta = Math.min(left.distance, right.distance);
        Point[] byY = mergeByY(left.byY, right.byY, metrics);
        double strip = stripMinimum(byY, midX, delta, metrics);
        return new SubProblem(Math.min(delta, strip), byY);
    }

    private static double bruteForce(Point[] points, Metrics metrics) {
        double best = Double.POSITIVE_INFINITY;
        for (int i = 0; i < points.length; i++) {
            for (int j = i + 1; j < points.length; j++) {
                metrics.compare();
                best = Math.min(best, distance(points[i], points[j]));
            }
        }
        return best;
    }

    /** Scans the strip around the median line in y order; only the next seven points can be closer. */
    private static double stripMinimum(Point[] byY, double midX, double delta, Metrics metrics) {
        Point[] strip = new Point[byY.length];
        int size = 0;
        for (Point p : byY) {
            if (Math.abs(p.x() - midX) <= delta) {
                strip[size++] = p;
            }
        }
        double best = delta;
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size && j <= i + STRIP_WINDOW; j++) {
                if (strip[j].y() - strip[i].y() >= best) {
                    break;
                }
                metrics.compare();
                best = Math.min(best, distance(strip[i], strip[j]));
            }
        }
        return best;
    }

    private static Point[] mergeByY(Point[] left, Point[] right, Metrics metrics) {
        Point[] merged = new Point[left.length + right.length];
        int i = 0;
        int j = 0;
        int k = 0;
        while (i < left.length && j < right.length) {
            metrics.compare();
            merged[k++] = left[i].y() <= right[j].y() ? left[i++] : right[j++];
        }
        while (i < left.length) {
            merged[k++] = left[i++];
        }
        while (j < right.length) {
            merged[k++] = right[j++];
        }
        return merged;
    }

    private static double distance(Point a, Point b) {
        double dx = a.x() - b.x();
        double dy = a.y() - b.y();
        return Math.sqrt(dx * dx + dy * dy);
    }
}
