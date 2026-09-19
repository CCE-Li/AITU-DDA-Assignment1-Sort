package kz.aitu.daa.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.ToDoubleFunction;
import javax.imageio.ImageIO;
import kz.aitu.daa.benchmark.Algorithm;
import kz.aitu.daa.benchmark.CsvWriter;
import kz.aitu.daa.benchmark.InputType;
import kz.aitu.daa.benchmark.ResultRow;

/**
 * Draws the plots of the report straight from {@code results.csv} using Java2D, so the whole report
 * can be reproduced without any extra tooling.
 *
 * <p>For every input type it writes four PNG files: time vs n, recursion depth vs n, comparisons
 * divided by {@code n log2 n} (the sorting algorithms) and comparisons divided by {@code n} (the
 * selection algorithms). The x axis is logarithmic because the sizes grow by a factor of ten.
 */
public final class PlotGenerator {

    static {
        System.setProperty("java.awt.headless", "true");
    }

    private static final int WIDTH = 960;
    private static final int HEIGHT = 600;
    private static final int MARGIN_LEFT = 110;
    private static final int MARGIN_RIGHT = 40;
    private static final int MARGIN_TOP = 70;
    private static final int MARGIN_BOTTOM = 80;
    private static final int Y_TICKS = 5;
    private static final int MARKER_RADIUS = 4;

    private static final Color GRID = new Color(0xE0E0E0);
    private static final Map<String, Color> COLORS = new LinkedHashMap<>();

    static {
        COLORS.put(Algorithm.MERGE_SORT.csvName(), new Color(0x1F77B4));
        COLORS.put(Algorithm.QUICK_SORT.csvName(), new Color(0xD62728));
        COLORS.put(Algorithm.QUICK_SELECT.csvName(), new Color(0x2CA02C));
        COLORS.put(Algorithm.MEDIAN_OF_MEDIANS.csvName(), new Color(0x9467BD));
        COLORS.put("insertionsort", new Color(0xFF7F0E));
    }

    private PlotGenerator() {
    }

    public static void main(String[] args) throws IOException {
        Path csv = Path.of(args.length > 0 ? args[0] : "results.csv");
        Path outputDir = Path.of(args.length > 1 ? args[1] : "plots");
        generate(csv, outputDir);
        System.out.println("wrote plots to " + outputDir.toAbsolutePath());
    }

    public static void generate(Path csvPath, Path outputDir) throws IOException {
        List<ResultRow> rows = CsvWriter.read(csvPath);
        Files.createDirectories(outputDir);
        for (InputType input : InputType.values()) {
            String suffix = input.csvName();
            List<ResultRow> ofInput = rows.stream()
                    .filter(row -> row.input().equals(suffix))
                    .toList();
            if (ofInput.isEmpty()) {
                continue;
            }
            List<ResultRow> sorts = ofInput.stream().filter(row -> !isSelection(row.algorithm())).toList();
            List<ResultRow> selections = ofInput.stream().filter(row -> isSelection(row.algorithm())).toList();

            draw(outputDir.resolve("time_vs_n_" + suffix + ".png"),
                    "Running time vs n (" + suffix + " input)", "n (log scale)", "time (ms)",
                    ofInput, ResultRow::timeMs);
            draw(outputDir.resolve("depth_vs_n_" + suffix + ".png"),
                    "Maximum recursion depth vs n (" + suffix + " input)", "n (log scale)", "max depth",
                    ofInput, row -> row.maxDepth());
            draw(outputDir.resolve("ratio_sorts_" + suffix + ".png"),
                    "Comparisons / (n * log2 n) vs n (" + suffix + " input, sorts)", "n (log scale)", "ratio",
                    sorts, row -> row.comparisons() / (row.n() * log2(row.n())));
            draw(outputDir.resolve("ratio_select_" + suffix + ".png"),
                    "Comparisons / n vs n (" + suffix + " input, selection)", "n (log scale)", "ratio",
                    selections, row -> row.comparisons() / (double) row.n());
        }
    }

    private static void draw(Path file, String title, String xLabel, String yLabel,
                             List<ResultRow> rows, ToDoubleFunction<ResultRow> value) throws IOException {
        if (rows.isEmpty()) {
            return;
        }
        Map<String, List<ResultRow>> series = groupByAlgorithm(rows);
        double xMin = log2(minN(rows));
        double xMax = log2(maxN(rows));
        if (xMax - xMin < 1e-9) {
            xMin -= 1;
            xMax += 1;
        }
        double yMax = 0;
        for (ResultRow row : rows) {
            yMax = Math.max(yMax, value.applyAsDouble(row));
        }
        yMax = yMax <= 0 ? 1 : yMax * 1.15;

        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, WIDTH, HEIGHT);
            g.setFont(new Font("SansSerif", Font.PLAIN, 12));

            int plotX = MARGIN_LEFT;
            int plotY = MARGIN_TOP;
            int plotW = WIDTH - MARGIN_LEFT - MARGIN_RIGHT;
            int plotH = HEIGHT - MARGIN_TOP - MARGIN_BOTTOM;

            for (int tick = 0; tick <= Y_TICKS; tick++) {
                double v = yMax * tick / Y_TICKS;
                int y = plotY + plotH - (int) Math.round(plotH * (v / yMax));
                g.setColor(GRID);
                g.drawLine(plotX, y, plotX + plotW, y);
                g.setColor(Color.DARK_GRAY);
                String label = formatValue(v);
                g.drawString(label, plotX - 10 - g.getFontMetrics().stringWidth(label), y + 4);
            }
            for (int n : distinctSizes(rows)) {
                int x = plotX + (int) Math.round(plotW * (log2(n) - xMin) / (xMax - xMin));
                g.setColor(GRID);
                g.drawLine(x, plotY, x, plotY + plotH);
                g.setColor(Color.DARK_GRAY);
                String label = Integer.toString(n);
                g.drawString(label, x - g.getFontMetrics().stringWidth(label) / 2, plotY + plotH + 20);
            }

            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(1.2f));
            g.drawRect(plotX, plotY, plotW, plotH);

            for (Map.Entry<String, List<ResultRow>> entry : series.entrySet()) {
                List<ResultRow> points = entry.getValue();
                int[] xs = new int[points.size()];
                int[] ys = new int[points.size()];
                for (int i = 0; i < points.size(); i++) {
                    ResultRow row = points.get(i);
                    xs[i] = plotX + (int) Math.round(plotW * (log2(row.n()) - xMin) / (xMax - xMin));
                    ys[i] = plotY + plotH - (int) Math.round(plotH * (value.applyAsDouble(row) / yMax));
                }
                g.setColor(colorOf(entry.getKey()));
                g.setStroke(new BasicStroke(2.2f));
                for (int i = 1; i < xs.length; i++) {
                    g.drawLine(xs[i - 1], ys[i - 1], xs[i], ys[i]);
                }
                g.setStroke(new BasicStroke(1f));
                for (int i = 0; i < xs.length; i++) {
                    g.fill(new Ellipse2D.Double(xs[i] - MARKER_RADIUS, ys[i] - MARKER_RADIUS,
                            2 * MARKER_RADIUS, 2 * MARKER_RADIUS));
                }
            }

            drawLegend(g, series.keySet(), plotX + plotW - 190, plotY + 14);
            drawTitles(g, title, xLabel, yLabel, plotX, plotY, plotW, plotH);
        } finally {
            g.dispose();
        }
        ImageIO.write(image, "png", file.toFile());
    }

    private static void drawLegend(Graphics2D g, Iterable<String> algorithms, int x, int y) {
        List<String> names = new ArrayList<>();
        algorithms.forEach(names::add);
        int height = names.size() * 20 + 12;
        g.setColor(new Color(255, 255, 255, 220));
        g.fillRect(x, y, 176, height);
        g.setColor(Color.GRAY);
        g.drawRect(x, y, 176, height);
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        int lineY = y + 20;
        for (String name : names) {
            g.setColor(colorOf(name));
            g.setStroke(new BasicStroke(2.4f));
            g.drawLine(x + 10, lineY - 4, x + 40, lineY - 4);
            g.setColor(Color.BLACK);
            g.drawString(displayName(name), x + 48, lineY);
            lineY += 20;
        }
    }

    private static void drawTitles(Graphics2D g, String title, String xLabel, String yLabel,
                                   int plotX, int plotY, int plotW, int plotH) {
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        g.drawString(title, (WIDTH - g.getFontMetrics().stringWidth(title)) / 2, 36);

        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        int centered = plotX + (plotW - g.getFontMetrics().stringWidth(xLabel)) / 2;
        g.drawString(xLabel, centered, HEIGHT - 24);

        Graphics2D rotated = (Graphics2D) g.create();
        try {
            rotated.rotate(-Math.PI / 2);
            int centeredY = plotY + (plotH + rotated.getFontMetrics().stringWidth(yLabel)) / 2;
            rotated.drawString(yLabel, -centeredY, 30);
        } finally {
            rotated.dispose();
        }
    }

    private static Map<String, List<ResultRow>> groupByAlgorithm(List<ResultRow> rows) {
        Map<String, List<ResultRow>> result = new LinkedHashMap<>();
        for (Algorithm algorithm : Algorithm.values()) {
            List<ResultRow> ofAlgorithm = rows.stream()
                    .filter(row -> row.algorithm().equals(algorithm.csvName()))
                    .sorted(Comparator.comparingInt(ResultRow::n))
                    .toList();
            if (!ofAlgorithm.isEmpty()) {
                result.put(algorithm.csvName(), ofAlgorithm);
            }
        }
        return result;
    }

    private static List<Integer> distinctSizes(List<ResultRow> rows) {
        List<Integer> sizes = new ArrayList<>();
        for (ResultRow row : rows) {
            if (!sizes.contains(row.n())) {
                sizes.add(row.n());
            }
        }
        sizes.sort(Integer::compareTo);
        return sizes;
    }

    private static int minN(List<ResultRow> rows) {
        int min = Integer.MAX_VALUE;
        for (ResultRow row : rows) {
            min = Math.min(min, row.n());
        }
        return min;
    }

    private static int maxN(List<ResultRow> rows) {
        int max = 1;
        for (ResultRow row : rows) {
            max = Math.max(max, row.n());
        }
        return max;
    }

    private static boolean isSelection(String algorithm) {
        return Algorithm.fromCsvName(algorithm).isSelection();
    }

    private static Color colorOf(String csvName) {
        return COLORS.getOrDefault(csvName, Color.GRAY);
    }

    private static String displayName(String csvName) {
        try {
            return Algorithm.fromCsvName(csvName).displayName();
        } catch (IllegalArgumentException e) {
            return csvName;
        }
    }

    private static double log2(double value) {
        return Math.log(value) / Math.log(2);
    }

    private static String formatValue(double value) {
        if (value == 0) {
            return "0";
        }
        double abs = Math.abs(value);
        if (abs >= 1000) {
            return String.format(Locale.ROOT, "%.0f", value);
        }
        if (abs >= 10) {
            return String.format(Locale.ROOT, "%.1f", value);
        }
        if (abs >= 1) {
            return String.format(Locale.ROOT, "%.2f", value);
        }
        if (abs >= 0.001) {
            return String.format(Locale.ROOT, "%.3f", value);
        }
        return String.format(Locale.ROOT, "%.1e", value);
    }
}
