package com.panagiotispetridis.day3;

import com.panagiotispetridis.common.Solver;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Main {

    record BatteryRow(List<Long> batteries) {}

    record Input(List<BatteryRow> batteryRows) {}

    record Output(Long joltage) {}

    static class Parser extends com.panagiotispetridis.common.Parser<Input> {

        public Parser(Scanner scanner) {
            super(scanner);
        }

        @Override
        public Input parse() {
            Input input = new Input(new ArrayList<>());
            while(scanner.hasNextLine()) {
                BatteryRow batteryRow = new BatteryRow(new ArrayList<>());
                Arrays.stream(scanner.nextLine()
                        .split(""))
                        .forEach(battery -> batteryRow.batteries().add(Long.parseLong(battery)));
                input.batteryRows().add(batteryRow);
            }

            return input;
        }

        static class Day3 implements Solver<Input, Output> {

            final private boolean part2;

            public Day3(boolean part2) {
                this.part2 = part2;
            }

            long pow(long base, long exp) {
                long result = 1;
                for (long i = 0; i < exp; i++) {
                    result *= base;
                }

                return result;
            }

            long maxJoltageForBatteryRow(BatteryRow batteryRow, int numberOfPicks) {
                List<Long> batteries = batteryRow.batteries();
                long[][] dp = new long[batteries.size() + 1][numberOfPicks + 1];

                for (int i = 0; i <= batteries.size(); i++) {
                    for (int j = 0; j <= numberOfPicks; j++) {
                        dp[i][j] = 0;
                    }
                }

                for (int i = 1; i <= batteries.size(); i++) {
                    for (int j = 1; j <= numberOfPicks; j++) {
                        // inverted because we go from largest to smallest number
                        long multiplier = pow(10L, numberOfPicks - j);
                        // answer = max(
                        //              if we do pick this number - ie. whatever max we can get by adding this number to whatever max we had before with 1 less pick (j-1)
                        //              if we don't pick this number - ie. whatever max we can get with same number of picks (j) but with the previous numbers
                        // );
                        dp[i][j] = Math.max(dp[i-1][j-1] + batteries.get(i-1) * multiplier, dp[i-1][j]);
                    }
                }

                return dp[batteries.size()][numberOfPicks];
            }

            @Override
            public Output solve(Input input) {
                long answer = 0;
                for (BatteryRow row : input.batteryRows()) {
                    answer += maxJoltageForBatteryRow(row, part2 ? 12 : 2);
                }

                return new Output(answer);
            }

        }

        public static void main(String[] args) {
            InputStream is = Main.class.getResourceAsStream("/day3/input.in");
            assert is != null;
            try (Scanner scanner = new Scanner(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                Parser parser = new Parser(scanner);
                Input input = parser.parse();
                Solver<Input, Output> solver = new Day3(true /* part2 */);
                Output output = solver.solve(input);

                System.out.printf("Answer: %s%n", output.joltage());
            }
        }

    }
}
