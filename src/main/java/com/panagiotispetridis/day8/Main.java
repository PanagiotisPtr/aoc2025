package com.panagiotispetridis.day8;

import com.panagiotispetridis.common.Parser;
import com.panagiotispetridis.common.Solver;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class Main {

    static class Point {
        static long count = 0;
        public final String alias;
        public final long x;
        public final long y;
        public final long z;
        public Point parent;

        Point(long x, long y, long z) {
            this.alias = String.format("P%s", count);
            this.x = x;
            this.y = y;
            this.z = z;
            this.parent = this;

            count++;
        }

        static Point fromString(String s) {
            List<Long> data = Arrays.stream(s.split(","))
                    .map(Long::parseLong)
                    .toList();
            assert data.size() == 3;

            return new Point(data.get(0), data.get(1), data.get(2));
        }

        Point rootParent() {
            if (parent == this) {
                return this;
            }
            parent = this.parent.rootParent();

            return parent;
        }

        long distance(Point other) {
            long dx = other.x - x;
            long dy = other.y - y;
            long dz = other.z - z;

            return dx*dx + dy*dy + dz*dz;
        }

        void setParent(Point p) {
            this.rootParent().parent = p;
            // update parents
            this.rootParent();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Point other) {
                return this.hashCode() == other.hashCode();
            }
            return false;
        }

        @Override
        public int hashCode() {
            return this.alias.hashCode();
        }

        @Override
        public String toString() {
            return String.format("%s(%s,%s,%s)", alias, x, y, z);
        }

        static int compareX(Point a, Point b) {
            if (a.x == b.x) {
                return 0;
            }

            return a.x < b.x ? 1 : -1;
        }

        static int compareY(Point a, Point b) {
            if (a.y == b.y) {
                return 0;
            }

            return a.y < b.y ? 1 : -1;
        }

        static int compareZ(Point a, Point b) {
            if (a.z == b.z) {
                return 0;
            }

            return a.z < b.z ? 1 : -1;
        }
    }

    record Input(List<Point> points) {}

    record Output(long answer) {}

    static class Day8Parser extends Parser<Input> {
        public Day8Parser(Scanner scanner) {
            super(scanner);
        }

        @Override
        public Input parse() {
            Input input = new Input(new ArrayList<>());
            while (scanner.hasNextLine()) {
                input.points().add(Point.fromString(scanner.nextLine()));
            }

            return input;
        }
    }

    static class Circuit {
        public long size;
        public Point root;

        Circuit(long size, Point root) {
            this.size = size;
            this.root = root;
        }

        @Override
        public String toString() {
            return String.format("[size=%s,root=%s]", size, root);
        }

        static List<Circuit> fromPoints(List<Point> points) {
            Map<Point, Circuit> circuits = new HashMap<>();
            for (Point p : points) {
                circuits.putIfAbsent(p.rootParent(), new Circuit(0, p.rootParent()));
                circuits.get(p.rootParent()).size++;
            }

            List<Circuit> result = new ArrayList<>(circuits.values().stream().toList());
            result.sort((a, b) -> {
                if (a.size == b.size) {
                    return 0;
                }
                return a.size < b.size ? 1 : -1;
            });

            return result;
        }
    }

    static class Day8 implements Solver<Input, Output> {
        final private boolean part2;

        Day8(boolean part2) {
            this.part2 = part2;
        }

        record Candidate(Point p1, Point p2, long distance) {
            static Candidate min(Candidate c1, Candidate c2) {
                if (c1 == null) {
                    return c2;
                }
                if (c2 == null) {
                    return c1;
                }
                return c1.distance() > c2.distance() ? c2 : c1;
            }
        }

        record PointPair(Point p1, Point p2) {
            @Override
            public boolean equals(Object obj) {
                if (obj instanceof PointPair(Point p3, Point p4)) {
                    return (p1.alias + p2.alias).equals(p3.alias + p4.alias) ||
                            (p2.alias + p1.alias).equals(p3.alias + p4.alias);
                }

                return false;
            }

            @Override
            public int hashCode() {
                return p1.hashCode() + p2.hashCode();
            }

            @Override
            public String toString() {
                return String.format("[%s, %s]", p1.alias, p2.alias);
            }
        }

        public boolean allSameParent(List<Point> points) {
            Point globalParent = points.getFirst().rootParent();
            for (Point p : points) {
                if (p.rootParent() != globalParent) {
                    return false;
                }
            }

            return true;
        }

        Candidate closestPair(int l, int r, List<Point> points, Set<PointPair> connected) {
            if (r - l <= 3) {
                Candidate c = new Candidate(null, null, Long.MAX_VALUE);
                for (int i = l; i < r; i++) {
                    for (int j = i + 1; j < r; j++) {
                        Point p1 = points.get(i);
                        Point p2 = points.get(j);
                        if (connected.contains(new PointPair(p1, p2))) {
                            continue;
                        }
                        c = Candidate.min(c, new Candidate(p1, p2, p1.distance(p2)));
                    }
                }
                return c;
            }

            int m = (l + r) / 2;
            long midx = points.get(m).x;
            Candidate left = closestPair(l, m, points, connected);
            Candidate right = closestPair(m, r, points, connected);
            Candidate best = Candidate.min(left, right);

            for (int i = l; i < m; i++) {
                Point p1 = points.get(i);
                long dist = Math.abs(p1.x - midx);
                if (dist*dist >= best.distance()) {
                    continue;
                }
                for (int j = m; j < r; j++) {
                    Point p2 = points.get(j);
                    dist = Math.abs(p2.x - midx);
                    if (dist*dist >= best.distance()) {
                        continue;
                    }
                    if (p1.distance(p2) > best.distance()) {
                        continue;
                    }
                    if (connected.contains(new PointPair(p1, p2))) {
                        continue;
                    }
                    best = Candidate.min(best, new Candidate(p1, p2, p1.distance(p2)));
                }
            }

            return best;
        }

        @Override
        public Output solve(Input input) {
            long count = 1000;
            long part2Ans = 0;
            input.points().sort(Point::compareX);
            Set<PointPair> connected = new HashSet<>();
            for (int i = 0; part2 || i < count; i++) {
                Candidate c = closestPair(0, input.points().size(), input.points(), connected);
                c.p2().setParent(c.p1().rootParent());
                if (allSameParent(input.points())) {
                    part2Ans = c.p1().x * c.p2().x;
                    break;
                }
                connected.add(new PointPair(c.p1(), c.p2()));
            }
            if (part2) {
                return new Output(part2Ans);
            }

            List<Circuit> circuits = Circuit.fromPoints(input.points());
            long answer = 1;
            for (int i = 0; i < 3; i++) {
                answer *= circuits.get(i).size;
            }
            return new Output(answer);
        }
    }

    public static void main(String[] args) {
        InputStream is = Main.class.getResourceAsStream("/day8/input.in");
        assert is != null;
        try (Scanner scanner = new Scanner(new InputStreamReader(is))) {
                Parser<Input> parser = new Day8Parser(scanner);
                Input input = parser.parse();
                Solver<Input, Output> solver = new Day8(true /* part2 */);
                Output output = solver.solve(input);

                System.out.printf("Answer: %s\n", output.answer());
        }
    }
}
