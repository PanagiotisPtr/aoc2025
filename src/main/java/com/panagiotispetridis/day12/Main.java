package com.panagiotispetridis.day12;

import com.panagiotispetridis.common.Parser;
import com.panagiotispetridis.common.Solver;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.*;

public class Main {

    static class Shape {
        record Cell(int x, int y) {}

        int w, h;
        List<Cell> cells;
        long mask;
        String key;

        Shape(int w, int h, List<Cell> cells, long mask, String key) {
            this.w = w;
            this.h = h;
            this.cells = cells;
            this.mask = mask;
            this.key = key;
        }

        public static Shape fromString(String ascii) {
            List<Cell> raw = new ArrayList<>();
            List<String> lines = ascii.lines().filter(s -> !s.isBlank()).toList();

            for (int y = 1; y < lines.size(); y++) {
                String line = lines.get(y).stripTrailing();
                for (int x = 0; x < line.length(); x++) {
                    char c = line.charAt(x);
                    if (c == '#') raw.add(new Cell(x, y));
                }
            }

            return normalize(raw);
        }

        static Shape normalize(List<Cell> raw) {
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;
            for (Cell c : raw) {
                minX = Math.min(minX, c.x);
                minY = Math.min(minY, c.y);
                maxX = Math.max(maxX, c.x);
                maxY = Math.max(maxY, c.y);
            }
            int w = maxX - minX + 1;
            int h = maxY - minY + 1;

            ArrayList<Cell> cells = new ArrayList<>(raw.size());
            for (Cell c : raw) {
                cells.add(new Cell(c.x - minX, c.y - minY));
            }
            cells.sort(Comparator.comparingInt(Cell::y).thenComparingInt(Cell::x));

            long mask = 0L;
            for (Cell c : cells) {
                mask |= 1L << (c.y * w + c.x);
            }

            StringBuilder sb = new StringBuilder();
            sb.append(w).append('x').append(h).append(':');
            for (Cell c : cells) {
                sb.append(c.x).append(',').append(c.y).append(';');
            }

            return new Shape(w, h, cells, mask, sb.toString());
        }

        public Shape rot90() {
            List<Cell> rotated = new ArrayList<>(cells.size());
            for (Cell c : cells) {
                rotated.add(new Cell(c.y, (w - 1) - c.x));
            }

            return normalize(rotated);
        }

        public Shape flipH() {
            List<Cell> flipped = new ArrayList<>(cells.size());
            for (Cell c : cells) {
                flipped.add(new Cell((w - 1) - c.x, c.y));
            }

            return normalize(flipped);
        }

        public List<Shape> orientations() {
            HashMap<String, Shape> map = new HashMap<>();

            Shape a = this;
            for (int k = 0; k < 4; k++) {
                map.putIfAbsent(a.key, a);
                a = a.rot90();
            }

            Shape b = this.flipH();
            for (int k = 0; k < 4; k++) {
                map.putIfAbsent(b.key, b);
                b = b.rot90();
            }

            return new ArrayList<>(map.values());
        }
    }

    static class Grid {
        int w;
        int h;
        int cells;

        Grid(int w, int h) {
            this.w = w;
            this.h = h;
            this.cells = w * h;
        }
    }

    record Query(Grid grid, List<Integer> counts) {}

    record Input(List<Shape> shapes, List<Query> queries) {}

    record Output(int answer) {}

    static class Day12Parser extends Parser<Input> {
        public Day12Parser(Scanner scanner) {
            super(scanner);
        }

        List<Query> parseQueries(String input) {
            List<String> lines = input.lines().toList();
            List<Query> queries = new ArrayList<>();
            for (String line : lines) {
                List<Integer> counts = new ArrayList<>();
                Grid grid;
                String[] parts = line.split(":");
                String[] gridParts = parts[0].split("x");
                int width = Integer.parseInt(gridParts[0]);
                int height = Integer.parseInt(gridParts[1]);
                grid = new Grid(width, height);
                String[] countParts = parts[1].split(" ");
                for (String countStr : countParts) {
                    if (countStr.isBlank()) {
                        continue;
                    }
                    counts.add(Integer.parseInt(countStr.trim()));
                }
                queries.add(new Query(grid, counts));
            }

            return queries;
        }

        @Override
        public Input parse() {
            List<Shape> shapes = new ArrayList<>();
            List<Query> queries = new ArrayList<>();
            while (scanner.hasNextLine()) {
                // read until blank line or end of file
                StringBuilder shapeStr = new StringBuilder();
                while (true) {
                    if (!scanner.hasNextLine()) {
                        break;
                    }
                    String line = scanner.nextLine();
                    if (line.isBlank()) {
                        break;
                    }
                    shapeStr.append(line).append("\n");
                }
                if (!scanner.hasNextLine()) {
                    // parse queries
                    queries = parseQueries(shapeStr.toString());
                } else {
                    // parse shape
                    shapes.add(Shape.fromString(shapeStr.toString()));
                }
            }

            return new Input(shapes, queries);
        }
    }

    static class Day12 implements Solver<Input, Output> {

        static class Piece {
            Shape shape;
            List<long[]> placements; // every possible placement bitmask
            int count;

            Piece(Shape shape, List<long[]> placements, int count) {
                this.shape = shape;
                this.placements = placements;
                this.count = count;
            }
        }

        @Override
        public Output solve(Input input) {
            int total = input.queries().size();
            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                CompletionService<Boolean> cs =
                        new ExecutorCompletionService<>(executor);
                for (Query query : input.queries()) {
                    cs.submit(() -> canFitDLX(input.shapes(), query));
                }

                int answer = 0;
                int completed = 0;
                for (int i = 0; i < total; i++) {
                    Future<Boolean> future = cs.take();
                    boolean ok = future.get();
                    if (ok) {
                        answer++;
                    }

                    completed++;
                    System.out.println("Completed " + completed + "/" + total);
                }

                return new Output(answer);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private boolean canFitDLX(List<Shape> shapes, Query q) {
            int W = q.grid().w;
            int H = q.grid().h;
            int gridCells = W * H;
            List<Integer> counts = q.counts();

            // Columns = grid cells + one shape column per shape
            int shapeCount = shapes.size();
            int COLS = gridCells + shapeCount;

            // Count how many rows will exist (upper bound for DLX row allocation)
            int estimatedRows = 0;
            for (int si = 0; si < shapes.size(); si++) {
                int cnt = counts.get(si);
                if (cnt == 0) continue;
                Shape base = shapes.get(si);
                List<Shape> orientations = base.orientations();
                for (Shape o : orientations)
                    estimatedRows += (H - o.h + 1) * (W - o.w + 1);
            }

            DLX dlx = new DLX(COLS, estimatedRows);

            // Mark each shape column with required count
            for (int si = 0; si < shapes.size(); si++) {
                dlx.setWeightedRequirement(gridCells + si, counts.get(si));
            }

            int nextRowId = 0;

            // Generate each placement row (ONE row per placement)
            for (int si = 0; si < shapes.size(); si++) {

                if (counts.get(si) == 0) continue;

                Shape base = shapes.get(si);
                List<Shape> orientations = base.orientations();

                int shapeColumn = gridCells + si;

                for (Shape o : orientations) {
                    for (int y = 0; y <= H - o.h; y++) {
                        for (int x = 0; x <= W - o.w; x++) {

                            int[] columns = new int[o.cells.size() + 1];
                            int k = 0;

                            // Grid cell constraints
                            for (Shape.Cell c : o.cells) {
                                int gx = x + c.x();
                                int gy = y + c.y();
                                columns[k++] = gy * W + gx;
                            }

                            // Shape column constraint
                            columns[k] = shapeColumn;

                            dlx.addRow(columns, si);
                            nextRowId++;
                        }
                    }
                }
            }

            return dlx.solveExists();
        }

        boolean canFitAll(List<Shape> shapes, Query query) {
            Grid grid = query.grid();
            int numberOfCells = grid.w * grid.h;

            int totalRequiredCells = 0;
            for (int i = 0; i < query.counts().size(); i++) {
                if (query.counts().get(i) > 0) {
                    totalRequiredCells += shapes.get(i).cells.size() * query.counts().get(i);
                }
            }
            if (totalRequiredCells > numberOfCells) {
                return false;
            }

            // generate all combinations of shapes by flipping and rotating them
            List<Piece> pieces = new ArrayList<>();
            for (int i = 0; i < query.counts().size(); i++) {
                int count = query.counts().get(i);
                if (count == 0) {
                    continue;
                }

                Shape s = shapes.get(i);
                List<Shape> orientations = s.orientations();

                // keep rotating shape, flip and rotate
                List<long[]> placements = new ArrayList<>();
                for (Shape o : orientations) {
                    for (int y = 0; y <= grid.h - o.h; y++) {
                        for (int x = 0; x <= grid.w - o.w; x++) {
                            long mask = 0L;
                            for (Shape.Cell c : o.cells) {
                                int gridX = x + c.x();
                                int gridY = y + c.y();
                                int bit = gridY * grid.w + gridX;
                                mask |= (1L << bit);
                            }

                            placements.add(new long[]{mask});
                        }
                    }
                }

                pieces.add(new Piece(s, placements, count));
            }

            // place bigger pieces first
            pieces.sort(Comparator.comparingInt(
                    (Piece p) -> -p.shape.cells.size()
            ));

            return part1(0, pieces, 0L);
        }

        boolean part1(int idx, List<Piece> pieces, long usedMask) {
            if (idx == pieces.size()) {
                return true;
            }

            Piece piece = pieces.get(idx);
            if (piece.count == 0) {
                return part1(idx + 1, pieces, usedMask);
            }

            for (long[] placement : piece.placements) {
                long mask = placement[0];
                if ((usedMask & mask) != 0) {
                    // overlaps with existing blocks
                    continue;
                }

                piece.count--;
                if (part1(idx, pieces, usedMask | mask)) {
                    return true;
                }
                piece.count++;
            }

            return false;
        }
    }

    public static void main(String[] args) {
        InputStream is = Main.class.getResourceAsStream("/day12/input.in");
        assert is != null;
        try (Scanner scanner = new Scanner(new InputStreamReader(is))) {
            Day12Parser parser = new Day12Parser(scanner);
            Input input = parser.parse();
            Day12 solver = new Day12();
            Output output = solver.solve(input);

            System.out.printf("Answer: %s\n", output.answer());
        }
    }
}