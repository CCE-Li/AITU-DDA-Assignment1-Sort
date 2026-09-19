package kz.aitu.daa.benchmark;

/** One measured benchmark case. */
public record ResultRow(String algorithm, String input, int n, double timeMs, long comparisons, int maxDepth) {
}
