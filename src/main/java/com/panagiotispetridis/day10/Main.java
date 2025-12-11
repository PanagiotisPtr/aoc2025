package com.panagiotispetridis.day10;

import com.panagiotispetridis.common.Parser;
import com.panagiotispetridis.common.Solver;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Function;

import com.google.ortools.Loader;
import com.google.ortools.sat.*;

public class Main {

    static class Button {
        private int mask;
        public int[] counts;

        Button(List<Integer> links, int size) {
            this.mask = 0;
            this.counts = new int[size];
            for (Integer link : links) {
                mask ^= 1 << link;
                counts[link] = 1;
            }
        }

        Button(int[] counts) {
            this.mask = 0;
            this.counts = counts;
        }

        int trigger(int value) {
            return value ^ mask;
        }

        static Button combine(Button a, Button b) {
            int[] c = new int[a.counts.length];
            for (int i = 0; i < a.counts.length; i++) {
                c[i] = a.counts[i] + b.counts[i];
            }

            return new Button(c);
        }

        int sum() {
            int sum = 0;
            for (int i = 0; i < counts.length; i++) {
                sum += counts[i];
            }

            return sum;
        }

        int[] apply(int[] joltages) {
            int[] rv = new int[joltages.length];
            for (int i = 0; i < joltages.length; i++) {
                rv[i] = joltages[i] + counts[i];
            }

            return rv;
        }
    }

    record Machine(List<Boolean> lights, List<Button> buttons, List<Integer> joltages) {}

    record Input(List<Machine> machines) {}

    record Output(int answer) {}

    static class Day10Parser extends Parser<Input> {
        public Day10Parser(Scanner scanner) {
            super(scanner);
        }

        @Override
        public Input parse() {
            Input input = new Input(new ArrayList<>());
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                List<String> parts = Arrays.stream(line.split(" ")).toList();
                String rawLights = parts.getFirst();
                List<String> rawButtons = parts.subList(1, parts.size()-1);
                String rawJoltages = parts.getLast();

                List<Boolean> lights = Arrays.stream(rawLights.substring(1, rawLights.length()-1).split(""))
                        .map(s -> s.equals("#"))
                        .toList();

                Function<String, List<Integer>> parseLinks = s ->
                    Arrays.stream(s.substring(1, s.length()-1).split(","))
                            .map(Integer::parseInt)
                            .toList();
                List<Button> buttons = new ArrayList<>(rawButtons.stream()
                        .map(parseLinks)
                        .map(links -> new Button(links, lights.size()))
                        .sorted((a, b) -> b.sum() - a.sum())
                        .toList());
                buttons.sort((a, b) -> b.sum() - a.sum());
                List<Integer> joltages = Arrays.stream(rawJoltages.substring(1, rawJoltages.length()-1).split(","))
                        .map(Integer::parseInt)
                        .toList();

                input.machines().add(new Machine(lights, buttons, joltages));
            }

            return input;
        }
    }

    static class Day10 implements Solver<Input, Output> {
        private final boolean part2;
        private final Set<String> seen;

        private static int gcd(int a, int b) {
            while (b != 0) {
                int temp = b;
                b = a % b;
                a = temp;
            }
            return Math.abs(a);
        }

        public static int gcd(int[] nums) {
            int result = nums[0];

            for (int i = 1; i < nums.length; i++) {
                result = gcd(result, nums[i]);
                if (result == 1) {
                    return 1;
                }
            }

            return result;
        }

        String serializeState(int[] state) {
            StringBuilder builder = new StringBuilder(state.length);
            for (int i = 0; i < state.length; i++) {
                builder.append(state[i]);
            }
            return builder.toString();
        }

        Day10(boolean part2) {
            this.part2 = part2;
            this.seen = new HashSet<>();
        }

        int configure(Machine machine) {
            int end = 0;
            for (int i = 0; i < machine.lights().size(); i++) {
                if (machine.lights().get(i).equals(true)) {
                    end ^= 1 << i;
                }
            }
            Queue<int[]> queue = new ArrayDeque<>();
            queue.add(new int[]{0, 0});
            while (!queue.isEmpty()) {
                int[] top = queue.poll();
                if (top[0] == end) {
                    return top[1];
                }
                for (Button button : machine.buttons()) {
                    queue.add(new int[]{button.trigger(top[0]), top[1]+1});
                }
            }

            return Integer.MAX_VALUE;
        }

        int solveMachineWithORTools(Machine machine) {
            Loader.loadNativeLibraries();

            int d = machine.joltages().size();
            int k = machine.buttons().size();

            int[] target = new int[d];
            for (int i = 0; i < d; i++) {
                target[i] = machine.joltages().get(i);
            }

            int maxPress = 0;
            for (int t : target) maxPress = Math.max(maxPress, t);

            CpModel model = new CpModel();

            IntVar[] x = new IntVar[k];
            for (int j = 0; j < k; j++) {
                x[j] = model.newIntVar(0, maxPress, "x_" + j);
            }

            for (int i = 0; i < d; i++) {
                List<LinearExpr> terms = new ArrayList<>();

                for (int j = 0; j < k; j++) {
                    int coeff = machine.buttons().get(j).counts[i];
                    if (coeff != 0) {
                        terms.add(LinearExpr.term(x[j], coeff));
                    }
                }

                LinearExpr lhs;
                if (terms.isEmpty()) {
                    lhs = LinearExpr.constant(0);
                } else if (terms.size() == 1) {
                    lhs = terms.get(0);
                } else {
                    lhs = LinearExpr.sum(terms.toArray(new LinearExpr[0]));
                }

                model.addEquality(lhs, target[i]);
            }

            LinearExpr[] objectiveTerms = new LinearExpr[k];
            for (int j = 0; j < k; j++) {
                objectiveTerms[j] = LinearExpr.term(x[j], 1);
            }
            LinearExpr objective = LinearExpr.sum(objectiveTerms);
            model.minimize(objective);

            CpSolver solver = new CpSolver();
            CpSolverStatus status = solver.solve(model);

            if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
                long total = 0;
                for (int j = 0; j < k; j++) {
                    total += solver.value(x[j]);
                }
                return (int) total;
            }

            return Integer.MAX_VALUE;
        }

        Output solvePart2(Input input) {
            int answer = 0;
            for (Machine m : input.machines()) {
                int presses = solveMachineWithORTools(m);
                if (presses == Integer.MAX_VALUE) {
                    return new Output(presses);
                }
                answer += presses;
            }
            return new Output(answer);
        }

        enum Status {
            PENDING,
            FAILED,
            DONE
        }

        void printArr(int[] arr) {
            for (int i = 0; i < arr.length; i++) {
                System.out.printf("%s", arr[i]);
                if (i != arr.length-1) {
                    System.out.print(",");
                }
            }
            System.out.print("\n");
        }

        Status finished(int[] state, int[] end) {
            for (int i = 0; i < state.length; i++) {
                if (state[i] < end[i]) {
                    return Status.PENDING;
                } else if (state[i] > end[i]) {
                    return Status.FAILED;
                }
            }

            return Status.DONE;
        }

        int next(int[] nums, int[] state) {
            int m = Integer.MAX_VALUE;
            int n = 0;
            for (int i = 0; i < nums.length; i++) {
                int candidate = nums[i] - state[i];
                if (candidate > 0 && candidate < m) {
                    m = candidate;
                    n = i;
                }
            }

            return n;
        }

        int solvePart2Rec(List<Button> buttons, int[] start, int[] end) {
            Queue<int[][]> queue = new ArrayDeque<>();
            queue.add(new int[][]{start, new int[]{0}});
            while (!queue.isEmpty()) {
                int[][] top = queue.poll();
                Status status = finished(top[0], end);
                if (Status.DONE.equals(status)) {
                    return top[1][0];
                }
                if (Status.FAILED.equals(status)) {
                    continue;
                }
                String key = serializeState(top[0]);
                if (seen.contains(key)) {
                    continue;
                }
                seen.add(key);
                for (Button b : buttons) {
                    queue.add(new int[][]{
                            b.apply(top[0]),
                            new int[]{top[1][0] + 1}
                    });
                }
            }

            return Integer.MAX_VALUE;
        }

        public Output solvePart1(Input input) {
            int answer = 0;
            for (Machine machine : input.machines()) {
                answer += configure(machine);
            }

            return new Output(answer);
        }

        @Override
        public Output solve(Input input) {
            if (part2) {
                return solvePart2(input);
            }
            return solvePart1(input);
        }
    }

    public static void main(String[] args) {
        InputStream is = Main.class.getResourceAsStream("/day10/input.in");
        assert is != null;
        try (Scanner scanner = new Scanner(new InputStreamReader(is))) {
            Parser<Input> parser = new Day10Parser(scanner);
            Input input = parser.parse();
            Solver<Input, Output> solver = new Day10(true /* part2 */);
            Output output = solver.solve(input);

            System.out.printf("Answer: %s\n", output.answer());
        }
    }
}
