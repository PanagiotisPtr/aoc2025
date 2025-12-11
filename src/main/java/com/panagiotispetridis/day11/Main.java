package com.panagiotispetridis.day11;

import com.panagiotispetridis.common.Parser;
import com.panagiotispetridis.common.Solver;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class Main {
    static class Day11Parser extends Parser<Map<String, String[]>> {
        public Day11Parser(Scanner scanner) {
            super(scanner);
        }

        @Override
        public Map<String, String[]> parse() {
            Map<String, String[]> graph = new HashMap<>();
            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().split(": ");
                String from = parts[0];
                String[] to = parts[1].split(" ");

                graph.put(from, to);
            }

            return graph;
        }
    }

    static class Day11 implements Solver<Map<String, String[]>, Long> {
        Set<String> visited = new HashSet<>();
        Map<String, Long> cache = new HashMap<>();

        final boolean part2;

        Day11(boolean part2) {
            this.part2 = part2;
        }

        @Override
        public Long solve(Map<String, String[]> graph) {
            if (part2) {
                return part2(graph, "svr", false, false);
            }

            return part1(graph, "you");
        }

        long part1(Map<String, String[]> graph, String curr) {
            if ("out".equals(curr)) {
                return 1;
            }
            if (visited.contains(curr)) {
                return 0;
            }
            visited.add(curr);
            long paths = 0;
            String[] edges = graph.get(curr);
            for (int i = 0; i < edges.length; i++) {
                paths += part1(graph, edges[i]);
            }
            visited.remove(curr);

            return paths;
        }

        String key(String curr, boolean fft, boolean dac) {
            StringBuilder builder = new StringBuilder(2 + curr.length());
            builder.append(curr);
            builder.append(fft ? 't' : 'f');
            builder.append(dac ? 't' : 'f');

            return builder.toString();
        }

        long part2(Map<String, String[]> graph, String curr, boolean fft, boolean dac) {
            if ("out".equals(curr)) {
                return fft && dac ? 1 : 0;
            }
            if (visited.contains(curr)) {
                return 0;
            }
            String k = key(curr, fft, dac);
            if (cache.containsKey(k)) {
                return cache.get(k);
            }
            visited.add(curr);
            if ("fft".equals(curr)) {
                fft = true;
            }
            if ("dac".equals(curr)) {
                dac = true;
            }
            long paths = 0;
            String[] edges = graph.get(curr);
            for (int i = 0; i < edges.length; i++) {
                paths += part2(graph, edges[i], fft, dac);
            }
            cache.put(k, paths);
            visited.remove(curr);

            return paths;
        }
    }

    public static void main(String[] args) {
        InputStream is = Main.class.getResourceAsStream("/day11/input.in");
        assert is != null;
        try (Scanner scanner = new Scanner(new InputStreamReader(is))) {
            Day11Parser parser = new Day11Parser(scanner);
            Map<String, String[]> graph = parser.parse();
            Day11 solver = new Day11(true /* part2 */);
            long answer = solver.solve(graph);

            System.out.printf("Answer: %s\n", answer);
        }
    }
}
