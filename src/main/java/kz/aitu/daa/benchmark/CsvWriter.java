package kz.aitu.daa.benchmark;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Reads and writes {@code results.csv}. */
public final class CsvWriter {

    public static final String HEADER = "algorithm,input,n,time_ms,comparisons,max_depth";

    private CsvWriter() {
    }

    public static void write(Path path, List<ResultRow> rows) throws IOException {
        StringBuilder csv = new StringBuilder(HEADER).append('\n');
        for (ResultRow row : rows) {
            csv.append(row.algorithm()).append(',')
                    .append(row.input()).append(',')
                    .append(row.n()).append(',')
                    .append(String.format(Locale.ROOT, "%.4f", row.timeMs())).append(',')
                    .append(row.comparisons()).append(',')
                    .append(row.maxDepth()).append('\n');
        }
        Files.writeString(path, csv.toString(), StandardCharsets.UTF_8);
    }

    public static List<ResultRow> read(Path path) throws IOException {
        List<ResultRow> rows = new ArrayList<>();
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] cells = line.split(",");
            rows.add(new ResultRow(
                    cells[0].trim(),
                    cells[1].trim(),
                    Integer.parseInt(cells[2].trim()),
                    Double.parseDouble(cells[3].trim()),
                    Long.parseLong(cells[4].trim()),
                    Integer.parseInt(cells[5].trim())));
        }
        return rows;
    }
}
