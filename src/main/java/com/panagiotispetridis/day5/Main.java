package com.panagiotispetridis.day5;

import com.panagiotispetridis.common.Parser;
import com.panagiotispetridis.common.Solver;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.TreeSet;

public class Main {

    record Range(long start, long end) implements Comparable<Range> {
        @Override
        public int compareTo(Range other) {
            long compareStart = Long.compare(start, other.start());

            return safeCompare(compareStart != 0 ? compareStart : Long.compare(end, other.end()));
        }

        int safeCompare(long diff) {
            if (diff < 0) {
                return -1;
            } else if (diff > 0) {
                return 1;
            } else {
                return 0;
            }
        }

        public boolean contains(long item) {
            return start <= item && item <= end;
        }

        public long size() {
            return end - start + 1;
        }

        public boolean overlaps(Range other) {
            return other.contains(start)
                    || other.contains(end)
                    || this.contains(other.start())
                    || this.contains(other.end());
        }
    }

    record Input(List<Range> ranges, List<Long> queries) {}

    record Output(long numberOfFreshIngredients) {}

    static class Day5Parser extends Parser<Input> {
        public Day5Parser(Scanner scanner) {
            super(scanner);
        }

        @Override
        public Input parse() {
            Input input = new Input(new ArrayList<>(), new ArrayList<>());
            for (String line = scanner.nextLine(); line != ""; line = scanner.nextLine()) {
                String[] range = line.split("-");
                input.ranges().add(new Range(
                        Long.parseLong(range[0]),
                        Long.parseLong(range[1])
                        ));
            }

            while (scanner.hasNextLine()) {
                input.queries().add(Long.parseLong(scanner.nextLine()));
            }

            return input;
        }
    }

    static class RangeTree {
        final private TreeSet<Range> ranges;

        public RangeTree() {
            this.ranges = new TreeSet<>();
        }

        public void add(Range r) {
            Range merged = r;
            for (
                    Range existing = ranges.floor(merged);
                    existing != null && existing.overlaps(merged);
                    existing = ranges.floor(merged)
            ) {
                merged = new Range(
                        Math.min(existing.start(), merged.start()),
                        Math.max(existing.end(), merged.end())
                );
                ranges.remove(existing);
            }

            ranges.add(merged);
        }

        public boolean contains(long item) {
            Range match = ranges.floor(new Range(item, Long.MAX_VALUE));
            if (match == null) {
                return false;
            }

            return match.contains(item);
        }

        public long sum() {
            long sum = 0;
            for (Range r : ranges) {
                sum += r.size();
            }

            return sum;
        }
    }

    static class Day5 implements Solver<Input, Output> {
        final private boolean part2;
        final private RangeTree ranges;

        public Day5(boolean part2) {
            this.part2 = part2;
            this.ranges = new RangeTree();
        }

        @Override
        public Output solve(Input input) {
            input.ranges().sort(Range::compareTo);
            for (Range r : input.ranges()) {
                ranges.add(r);
            }

            if (part2) {
                return new Output(ranges.sum());
            }

            long numberOfFreshIngredients = 0;
            for (long item : input.queries()) {
                if (ranges.contains(item)) {
                    numberOfFreshIngredients++;
                }
            }

            return new Output(numberOfFreshIngredients);
        }
    }

    public static void main(String[] args) {
        InputStream is = Main.class.getResourceAsStream("/day5/input.in");
        assert is != null;
        try (Scanner scanner = new Scanner(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            try {
                Day5Parser parser = new Day5Parser(scanner);
                Input input = parser.parse();
                Solver<Input, Output> solver = new Day5(true /* part2 */);
                Output output = solver.solve(input);

                System.out.printf("Answer: %s", output.numberOfFreshIngredients());
            } catch (Exception e) {
                System.out.println(e);
            }

        }
    }
}
