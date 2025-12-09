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

        Candidate closestPair(List<Point> pointsByX, List<Point> pointsByY, Set<PointPair> connected) {
            return closestPairRec(pointsByX, pointsByY, connected);
        }

        // Shamos-Hoey 1975
        private Candidate closestPairRec(List<Point> px, List<Point> py, Set<PointPair> connected) {
            int n = px.size();
            if (n <= 3) {
                return bruteForceClosest(px, connected);
            }

            int mid = n / 2;
            Point midPoint = px.get(mid);
            long midX = midPoint.x;

            List<Point> pxL = px.subList(0, mid);
            List<Point> pxR = px.subList(mid, n);

            Set<Point> leftSet = new HashSet<>(pxL);

            List<Point> pyL = new ArrayList<>(mid);
            List<Point> pyR = new ArrayList<>(n - mid);
            for (Point p : py) {
                if (leftSet.contains(p)) {
                    pyL.add(p);
                } else {
                    pyR.add(p);
                }
            }

            Candidate left = closestPairRec(pxL, pyL, connected);
            Candidate right = closestPairRec(pxR, pyR, connected);
            Candidate best = Candidate.min(left, right);
            long bestDist = (best == null) ? Long.MAX_VALUE : best.distance();

            List<Point> strip = new ArrayList<>();
            for (Point p : py) {
                long dx = p.x - midX;
                if (dx * dx < bestDist) {
                    strip.add(p);
                }
            }

            for (int i = 0; i < strip.size(); i++) {
                Point p = strip.get(i);
                for (int j = i + 1; j < strip.size(); j++) {
                    Point q = strip.get(j);

                    long dy = q.y - p.y;
                    if (dy * dy >= bestDist) {
                        break;
                    }

                    long dz = q.z - p.z;
                    if (dz * dz >= bestDist) {
                        continue;
                    }

                    PointPair pair = new PointPair(p, q);
                    if (connected.contains(pair)) {
                        continue;
                    }

                    long d2 = p.distance(q);
                    if (d2 < bestDist) {
                        bestDist = d2;
                        best = new Candidate(p, q, d2);
                    }
                }
            }

            return best;
        }

        private Candidate bruteForceClosest(List<Point> pts, Set<PointPair> connected) {
            Candidate best = null;
            long bestDist = Long.MAX_VALUE;
            int n = pts.size();
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    Point p = pts.get(i);
                    Point q = pts.get(j);

                    PointPair pair = new PointPair(p, q);
                    if (connected.contains(pair)) {
                        continue;
                    }

                    long d2 = p.distance(q);
                    if (d2 < bestDist) {
                        bestDist = d2;
                        best = new Candidate(p, q, d2);
                    }
                }
            }
            return best;
        }

        @Override
        public Output solve(Input input) {
            long count = 1000;
            long part2Ans = 0;

            List<Point> pointsByX = new ArrayList<>(input.points());
            pointsByX.sort(Point::compareX);

            List<Point> pointsByY = new ArrayList<>(input.points());
            pointsByY.sort(Point::compareY);

            Set<PointPair> connected = new HashSet<>();

            for (int i = 0; part2 || i < count; i++) {
                Candidate c = closestPair(pointsByX, pointsByY, connected);
                if (c == null) {
                    break;
                }

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
            for (int i = 0; i < 3 && i < circuits.size(); i++) {
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
