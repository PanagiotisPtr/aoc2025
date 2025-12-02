package com.panagiotispetridis.day2;

import com.panagiotispetridis.common.Solver;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    record Range(BigInteger lowerBound, BigInteger upperBound) {}

    record Input(List<Range> ranges) {}

    record Output(BigInteger invalidIdSum) {}

    static class Parser extends com.panagiotispetridis.common.Parser<Input> {

        public Parser(Scanner scanner) {
            super(scanner);
        }

        public Input parse() {
            Input input = new Input(new ArrayList<>());

            Pattern pattern = Pattern.compile("(.+?)-(.+)");
            String line = scanner.nextLine();
            for (String range : line.split(",")) {
                Matcher matcher = pattern.matcher(range);
                boolean success = matcher.matches();
                assert success;
                BigInteger lowerBound = new BigInteger(matcher.group(1));
                BigInteger upperBound = new BigInteger(matcher.group(2));

                input.ranges().add(new Range(lowerBound, upperBound));
            }

            return input;
        }

    }

    static class Day2 implements Solver<Input, Output> {

        private static final char[] digits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

        final private boolean part2;

        public Day2(boolean part2) {
            this.part2 = part2;
        }

        boolean isInvalidId(String id, int splits) {
            if (id.length() % splits != 0) {
                return false;
            }
            for (int i = 0; i < id.length() / splits; i++) {
                for (int j = 1; j < splits; j++) {
                    if (id.charAt(i) != id.charAt(i + (id.length() / splits) * j)) {
                        return false;
                    }
                }
            }

            return true;
        }

        BigInteger invalidIdSumAnyTimes(Range range, int splitsLimit) {
            Stack<String> stack = new Stack<>();
            BigInteger sum = new BigInteger("0");
            for (int i = 1; i < 10; i++) {
                stack.add("" + i);
            }
            Set<String> seen = new HashSet<>();
            while (!stack.empty()) {
                String top = stack.pop();
                BigInteger id = new BigInteger(top + top);
                if (id.compareTo(range.upperBound) > 0) {
                    continue;
                }
                int splits = 2;
                while (splits <= splitsLimit && id.compareTo(range.upperBound) <= 0) {
                    if (!seen.contains(id.toString()) && id.compareTo(range.lowerBound) >= 0 && isInvalidId(id.toString(), splits)) {
                        sum = sum.add(id);
                        seen.add(id.toString());
                    }
                    id = new BigInteger(id + top);
                    splits++;
                }
                for (char digit : digits) {
                    stack.add(top + digit);
                }
            }

            return sum;
        }

        public Output solve(Input input) {
            BigInteger total = new BigInteger("0");
            for (Range range : input.ranges) {
                if (part2) {
                    total = total.add(invalidIdSumAnyTimes(range, 99999999));
                } else {
                    total = total.add(invalidIdSumAnyTimes(range, 2));
                }
            }

            return new Output(total);
        }

    }

    public static void main(String[] args) {
        InputStream is = Main.class.getResourceAsStream("/day2/input.in");
        assert is != null;
        try (Scanner scanner = new Scanner(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            Parser parser = new Parser(scanner);
            Input input = parser.parse();
            Solver<Input, Output> solver = new Day2(true /* part2 */);
            Output output = solver.solve(input);

            System.out.printf("Answer: %s%n", output.invalidIdSum());
        }
    }
}
