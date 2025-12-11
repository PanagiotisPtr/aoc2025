package com.panagiotispetridis.day9;

import com.panagiotispetridis.common.Parser;
import com.panagiotispetridis.common.Solver;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {

    record Point(long x, long y) {
        static Point fromString(String s) {
            List<Long> data = Arrays.stream(s.split(",")).map(Long::parseLong).toList();
            assert data.size() == 2;

            return new Point(data.get(0), data.get(1));
        }
    }

    record Line(Point start, Point end) {
        boolean contains(Point p) {
            long vx = end.x() - start.x();
            long vy = end.y() - start.y();
            long ax = p.x() - start.x();
            long ay = p.y() - start.y();

            long cross = vx * ay - vy * ax;
            if (cross != 0) {
                return false;
            }

            long dot = ax * vx + ay * vy;
            if (dot < 0) {
                return false;
            }

            long len2 = vx * vx + vy * vy;
            if (dot > len2) {
                return false;
            }

            return true;
        }
    }

    record Input(List<Point> redTiles) {}

    record Output(long answer) {}

    static class Day9 implements Solver<Input, Output> {
        private final boolean part2;

        Day9(boolean part2) {
            this.part2 = part2;
        }

        long area(Point p1, Point p2) {
            long dx = Math.abs(p1.x() - p2.x());
            long dy = Math.abs(p1.y() - p2.y());

            return (dx+1) * (dy+1);
        }

        List<Point> opposideCorners(Point p1, Point p2) {
            return List.of(
                    new Point(p1.x(), p2.y()),
                    new Point(p2.x(), p1.y())
            );
        }

        boolean isInsidePolygon(Point p, List<Line> edges) {
            // if on any line then it's inside
            for (Line edge : edges) {
                if (edge.contains(p)) {
                    return true;
                }
            }

            boolean inside = false;
            // Even–odd rule with integer math (even intersections == outside else inside)
            for (Line edge : edges) {
                Point a = edge.start();
                Point b = edge.end();

                if ((a.y() > p.y()) != (b.y() > p.y())) {
                    long den = b.y() - a.y();
                    long num = (p.y() - a.y()) * (b.x() - a.x());
                    long rhs = (p.x() - a.x()) * den;

                    if (den > 0 ? num >= rhs : num <= rhs) {
                        inside = !inside;
                    }
                }
            }

            return inside;
        }

        boolean rectangleCrossedByEdge(List<Line> edges, long minX, long maxX, long minY, long maxY) {
            for (Line edge : edges) {
                Point a = edge.start();
                Point b = edge.end();

                // horizontal edge
                if (a.y() == b.y()) {
                    long y = a.y();
                    if (y > minY && y < maxY) {
                        long ex1 = Math.min(a.x(), b.x());
                        long ex2 = Math.max(a.x(), b.x());
                        if (ex2 > minX && ex1 < maxX) {
                            return true; // cuts through the interior
                        }
                    }
                }

                // vertical edge
                if (a.x() == b.x()) {
                    long x = a.x();
                    if (x > minX && x < maxX) {
                        long ey1 = Math.min(a.y(), b.y());
                        long ey2 = Math.max(a.y(), b.y());
                        if (ey2 > minY && ey1 < maxY) {
                            return true; // cuts through the interior
                        }
                    }
                }
            }

            return false;
        }

        @Override
        public Output solve(Input input) {
            long answer = 0;
            List<Line> lines = new ArrayList<>();
            for (int i = 0; i < input.redTiles().size()-1; i++) {
                Point p1 = input.redTiles().get(i);
                Point p2 = input.redTiles().get(i+1);

                lines.add(new Line(p1, p2));
            }
            lines.add(new Line(input.redTiles().getLast(), input.redTiles().getFirst()));

            for (Point p1 : input.redTiles()) {
                for (Point p2 : input.redTiles()) {
                    if (p1 == p2) {
                        continue;
                    }
                    List<Point> corners = opposideCorners(p1, p2);
                    boolean valid = true;
                    for (Point corner : corners) {
                        if (!isInsidePolygon(corner, lines)) {
                            valid = false;
                            break;
                        }
                    }
                    long minX = Math.min(p1.x(), p2.x());
                    long maxX = Math.max(p1.x(), p2.x());
                    long minY = Math.min(p1.y(), p2.y());
                    long maxY = Math.max(p1.y(), p2.y());
                    if (part2 && (!valid || rectangleCrossedByEdge(lines, minX, maxX, minY, maxY))) {
                        continue;
                    }
                    answer = Math.max(answer, area(p1, p2));
                }
            }

            return new Output(answer);
        }
    }

    static class Day9Parser extends Parser<Input> {
        public Day9Parser(Scanner scanner) {
            super(scanner);
        }

        @Override
        public Input parse() {
            Input input = new Input(new ArrayList<>());
            while (scanner.hasNextLine()) {
                input.redTiles().add(Point.fromString(scanner.nextLine()));
            }

            return input;
        }
    }

    public static void main(String[] args) {
        InputStream is = Main.class.getResourceAsStream("/day9/input.in");
        assert is != null;
        try (Scanner scanner = new Scanner(new InputStreamReader(is))) {
            Parser<Input> parser = new Day9Parser(scanner);
            Input input = parser.parse();
            Solver<Input, Output> solver = new Day9(true /* part2 */);
            Output output = solver.solve(input);

            System.out.printf("Answer: %s", output.answer());
        }
    }
}
