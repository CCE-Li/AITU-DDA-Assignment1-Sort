package kz.aitu.daa.benchmark;

/** The algorithms that take part in the benchmark. */
public enum Algorithm {

    MERGE_SORT("mergesort", "MergeSort", false),
    QUICK_SORT("quicksort", "QuickSort", false),
    QUICK_SELECT("quickselect", "QuickSelect", true),
    MEDIAN_OF_MEDIANS("medianofmedians", "MedianOfMedians", true);

    private final String csvName;
    private final String displayName;
    private final boolean selection;

    Algorithm(String csvName, String displayName, boolean selection) {
        this.csvName = csvName;
        this.displayName = displayName;
        this.selection = selection;
    }

    public String csvName() {
        return csvName;
    }

    public String displayName() {
        return displayName;
    }

    /** {@code true} for the algorithms that only select the k-th element instead of sorting. */
    public boolean isSelection() {
        return selection;
    }

    public static Algorithm fromCsvName(String csvName) {
        for (Algorithm algorithm : values()) {
            if (algorithm.csvName.equals(csvName)) {
                return algorithm;
            }
        }
        throw new IllegalArgumentException("unknown algorithm: " + csvName);
    }
}
