# AITU DAA Assignment 1 — Fast Sorting & Selection Engine

Divide-and-conquer sorting and selection for very large `int[]` arrays: **MergeSort**, **QuickSort**,
**QuickSelect**, plus the two bonus tasks **Median of Medians** and **Closest Pair of Points**.

Everything is measured with a reusable `Metrics` object (comparisons, maximum recursion depth,
`System.nanoTime()`), benchmarked over 4 sizes × 3 input shapes × 5 runs, and exported to
`results.csv` together with PNG plots.

## Requirements

* JDK 21 (the project compiles with `maven.compiler.release=21`)
* Maven 3.8+

> If Maven reports `The JAVA_HOME environment variable is not defined correctly`, point it at a real
> JDK, for example `set JAVA_HOME=C:\Program Files\Java\jdk-21.0.11` (PowerShell:
> `$env:JAVA_HOME="C:\Program Files\Java\jdk-21.0.11"`).

## Build, test, benchmark

```bash
# compile
mvn clean compile

# run the 69 JUnit 5 tests
mvn test

# run the benchmark: writes results.csv and the PNG files in plots/
mvn exec:java
```

`mvn exec:java` prints the median of five runs for every case and then writes:

* `results.csv` — `algorithm,input,n,time_ms,comparisons,max_depth`
* `plots/` — 12 PNG files (time, depth and ratio plots for each input type)

The plots can be regenerated on their own from an existing CSV:

```bash
mvn exec:java -Dexec.mainClass=kz.aitu.daa.plot.PlotGenerator
```

The benchmark checks its own work: every sort result is verified to be ordered and every selection
result is compared against `Arrays.sort`'s k-th element. A wrong answer aborts the run instead of
producing a nice-looking CSV.

## Project layout

```
src/main/java/kz/aitu/daa/
├── metrics/Metrics.java          comparisons, recursion depth, timer - passed in, never static
├── sort/InsertionSort.java       base case for MergeSort, median finder for Median of Medians
├── sort/Partition.java           three-way partition + reusable Bounds holder, shared with select
├── sort/MergeSort.java           one buffer, cutoff 15, linear merge
├── sort/QuickSort.java           random pivot, smaller side first, three-way partition
├── select/QuickSelect.java       reuses Partition, walks only the side containing k
├── select/MedianOfMedians.java   guaranteed Θ(n) worst case (bonus Task A)
├── geometry/Point.java           immutable 2D point
├── geometry/ClosestPair.java     O(n log n) divide and conquer (bonus Task B)
├── benchmark/…                   InputType, InputGenerator, Algorithm, CsvWriter, Benchmark
└── plot/PlotGenerator.java       Java2D plots, no external dependencies

src/test/java/kz/aitu/daa/…
```

## Design decisions

**MergeSort.**
* One helper array per top-level call, passed down the recursion — no `new int[...]` inside `merge`.
* Ranges of ≤ 15 elements are finished with insertion sort.
* `merge` copies the range once and walks two cursors, so it is Θ(n) and stable.

**QuickSort.**
* The pivot is drawn uniformly at random from the current range, so sorted input cannot trigger the
  quadratic case.
* Only the **smaller** side is recursed into, the larger side is handled by the enclosing loop, so
  the stack depth is O(log n) instead of O(n) on sorted input.
* A three-way partition puts all values equal to the pivot in the middle, so `duplicates` input
  costs Θ(n) instead of Θ(n²).

**QuickSelect.**
* Reuses `Partition.threeWay` and continues only in the part that contains position `k`.
* Iterative — selection needs no stack at all. Because of that, `max_depth` in `results.csv` reports
  the number of partition rounds for the selection algorithms (the depth a recursive version would
  have used), not a real stack depth.
* Empty/null arrays and out-of-range `k` throw `IllegalArgumentException` with a clear message.

**Reproducibility.** Pivot randomness uses a fixed seed (`QuickSort.DEFAULT_SEED`), the input
generator uses a fixed seed, and the median of five runs is reported. Re-running the benchmark
therefore produces the same comparison counts and the same plots.

## Results at a glance

Ratio of comparisons to the expected growth, `comparisons / (n·log2 n)` for the sorts and
`comparisons / n` for the selections (n = 10⁶, random input):

| algorithm | ratio | expected |
|---|---|---|
| MergeSort | 1.00 | Θ(n log n) |
| QuickSort | 1.25 | Θ(n log n) |
| QuickSelect | 2.80 | Θ(n) |
| MedianOfMedians | 8.52 | Θ(n) |

The ratios stop growing, which is the experimental confirmation of the Θ bounds. Full tables,
recurrences, the Master Theorem cases and the discussion are in [REPORT.md](REPORT.md).

## Git workflow

`main` holds only working code and is tagged `v1.0`. Work happened on
`feature/metrics`, `feature/mergesort`, `feature/quicksort`, `feature/select`,
`feature/closestpair`, `feature/benchmark` and `feature/report`, each merged back with a merge
commit: `feat(mergesort): …`, `test(quicksort): …`, `docs(report): …`.
