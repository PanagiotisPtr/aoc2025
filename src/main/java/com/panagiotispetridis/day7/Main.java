package com.panagiotispetridis.day7;

import com.panagiotispetridis.common.Parser;
import com.panagiotispetridis.common.Solver;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    enum Tile {
        EMPTY,
        BEAM,
        SPLITTER;

        static Tile fromChar(char c) {
            return switch (c) {
                case '.' -> EMPTY;
                case '^' -> SPLITTER;
                default -> BEAM;
            };
        }

        @Override
        public String toString() {
            return switch (this) {
                case EMPTY -> ".";
                case BEAM -> "|";
                case SPLITTER -> "^";
            };
        }
    }

    record Grid(List<List<Tile>> data) {
        Tile at(int y, int x) {
            if (y < 0 || y >= data.size() || x < 0 || x >= data.get(y).size()) {
                return null;
            }
            return data.get(y).get(x);
        }

        void set(int y, int x, Tile t) {
            if (at(y,x) == null) {
                return;
            }

            data.get(y).set(x, t);
        }

        int width() {
            return data.getFirst().size();
        }

        int height() {
            return data.size();
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            for (List<Tile> row : data) {
                for (Tile tile : row) {
                    result.append(tile);
                }
                result.append("\n");
            }

            return result.toString();
        }
    }

    record Input(Grid grid) {}

    record Output(long answer) {}

    static class Day7Parser extends Parser<Input> {
        public Day7Parser(Scanner scanner) {
            super(scanner);
        }

        @Override
        public Input parse() {
            Input input = new Input(new Grid(new ArrayList<>()));
            while (scanner.hasNextLine()) {
                input.grid().data().add(
                    Arrays.stream(scanner.nextLine().split(""))
                            .map(s -> Tile.fromChar(s.charAt(0)))
                            .collect(Collectors.toList())
                );
            }

            return input;
        }
    }


    static class Day7 implements Solver<Input, Output> {

        private final Map<Frame, Long> cache;

        private final boolean part2;

        record Frame(int y, int x) {
            @Override
            public boolean equals(Object obj) {
                if (obj == null) {
                    return false;
                }
                if (obj instanceof Frame other) {
                    return other.x() == x && other.y() == y;
                }

                return false;
            }
        }

        Day7(boolean part2) {
            this.part2 = part2;
            this.cache = new HashMap<>();
        }

        long timelines(Grid grid, Frame frame) {
            if (frame.y() >= grid.height()) {
                return 1;
            }
            if (frame.x() < 0 || frame.x() >= grid.width()) {
                return 0;
            }
            if (cache.containsKey(frame)) {
                return cache.get(frame);
            }
            long result = 1;
            if (Tile.SPLITTER.equals(grid.at(frame.y(), frame.x()))) {
                result = timelines(grid, new Frame(frame.y()+1, frame.x()+1))
                        + timelines(grid, new Frame(frame.y()+1, frame.x()-1));
            } else {
                result = timelines(grid, new Frame(frame.y()+1, frame.x()));;
            }
            cache.put(frame, result);

            return result;
        }

        @Override
        public Output solve(Input input) {
            long answer = 0;
            Grid grid = input.grid();
            Set<Integer> beams = new HashSet<>();
            int startTile = 0;
            for (int x = 0; x < grid.width(); x++) {
                if (Tile.BEAM.equals(grid.at(0, x))) {
                    beams.add(x);
                    startTile = x;
                }
            }
            if (part2) {
                return new Output(timelines(grid, new Frame(0, startTile)));
            }
            for (int y = 1; y < grid.height(); y++) {
                Set<Integer> nextBeams = new HashSet<>();
                for (Integer x : beams) {
                    if (grid.at(y, x) == null) {
                        continue;
                    }
                    if (Tile.SPLITTER.equals(grid.at(y, x))) {
                        answer++;
                        grid.set(y, x-1, Tile.BEAM);
                        grid.set(y, x+1, Tile.BEAM);
                        nextBeams.add(x-1);
                        nextBeams.add(x+1);
                    } else if(Tile.EMPTY.equals(grid.at(y, x))) {
                        grid.set(y, x, Tile.BEAM);
                        nextBeams.add(x);
                    }
                }
                beams = nextBeams;
            }

            return new Output(answer);
        }
    }

    public static void main(String[] args) {
        InputStream is = Main.class.getResourceAsStream("/day7/input.in");
        assert is != null;
        try (Scanner scanner = new Scanner(new InputStreamReader(is))) {
            Parser<Input> parser = new Day7Parser(scanner);
            Input input = parser.parse();
            Solver<Input, Output> solver = new Day7(false /* part2 */);
            Output output = solver.solve(input);

            System.out.printf("Number of splits: %s\n", output.answer());

            solver = new Day7(true /* part2 */);
            output = solver.solve(input);

            System.out.printf("Number of timelines: %s\n", output.answer());
        }
    }
}
