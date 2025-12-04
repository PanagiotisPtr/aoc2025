package com.panagiotispetridis.day4;

import com.panagiotispetridis.common.Solver;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Main {

    enum Tile {
        EMPTY,
        PAPER;

        static Tile fromChar(char c) {
            if (c == '.') {
                return EMPTY;
            } else {
                return PAPER;
            }
        }

        @Override
        public String toString() {
            return switch (this) {
                case EMPTY -> ".";
                case PAPER -> "@";
            };
        }
    }

    static class Grid<T> {
        final private List<List<T>> data;

        public Grid(List<List<T>> data) {
            this.data = data;
        }

        public T at(int i, int j) {
            if (i < 0 || j < 0 || i >= height() || j >= width()) {
                return null;
            }
            return this.data.get(i).get(j);
        }

        public void set(int i, int j, T t) {
            this.data.get(i).set(j, t);
        }

        public long height() {
            return data.size();
        }

        public long width() {
            return data.getFirst().size();
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < height(); i++) {
                for (int j = 0; j < width(); j++) {
                    builder.append(at(i, j));
                }
                builder.append("\n");
            }

            return builder.toString();
        }
    }

    record Input(Grid<Tile> grid) {}

    record Output(long accessibleRolls) {}

    static class Parser extends com.panagiotispetridis.common.Parser<Input> {

        public Parser(Scanner scanner) {
            super(scanner);
        }

        @Override
        public Input parse() {
            List<List<Tile>> data = new ArrayList<>();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                List<Tile> row = new ArrayList<>();
                for (int i = 0; i < line.length(); i++) {
                    row.add(Tile.fromChar(line.charAt(i)));
                }
                data.add(row);
            }

            return new Input(new Grid<>(data));
        }
    }

    static class Day4 implements Solver<Input, Output> {
        final private boolean part2;

        public Day4(boolean part2) {
            this.part2 = part2;
        }

        record Position(int y, int x) {}

        List<Position> adjacentPositions(Position position) {
            int x = position.x();
            int y = position.y();
            return List.of(
                    new Position(y-1, x),
                    new Position(y+1, x),
                    new Position(y, x-1),
                    new Position(y, x+1),
                    new Position(y+1, x+1),
                    new Position(y-1, x+1),
                    new Position(y+1, x-1),
                    new Position(y-1, x-1)
            );
        }

        boolean isRollAccessible(Grid<Tile> grid, int y, int x) {
            long counter = 0;
            for (Position position : adjacentPositions(new Position(y, x))) {
                if (Tile.PAPER.equals(grid.at(position.y(), position.x()))) {
                    counter++;
                }
            }

            return counter < 4;
        }

        List<Position> adjacentAccessibleRolls(Grid<Tile> grid, Position position) {
            List<Position> rolls = new ArrayList<>();
            for (Position adjacentPosition : adjacentPositions(position)) {
                if (Tile.PAPER.equals(grid.at(adjacentPosition.y(), adjacentPosition.x()))
                    && isRollAccessible(grid, adjacentPosition.y(), adjacentPosition.x())) {
                    rolls.add(adjacentPosition);
                }
            }

            return rolls;
        }

        List<Position> accessibleRolls(Grid<Tile> grid) {
            List<Position> accessible = new ArrayList<>();
            for (int y = 0; y < grid.height(); y++) {
                for (int x = 0; x < grid.width(); x++) {
                    if (!Tile.PAPER.equals(grid.at(y, x))) {
                        continue;
                    }
                    if (isRollAccessible(grid, y, x)) {
                        accessible.add(new Position(y, x));
                    }
                }
            }

            return accessible;
        }

        @Override
        public Output solve(Input input) {
            List<Position> rolls = accessibleRolls(input.grid());
            if (!part2) {
                return new Output(rolls.size());
            }

            Set<Position> seen = new HashSet<>();
            long total = 0;
            while(!rolls.isEmpty()) {
                // clear current rolls
                Set<Position> removed = new HashSet<>();
                for (Position position : rolls) {
                    if (!seen.contains(position)) {
                        total++;
                        seen.add(position);
                        removed.add(position);
                    }
                    input.grid().set(position.y, position.x, Tile.EMPTY);
                }
                List<Position> newRolls = new ArrayList<>();
                for (Position position : removed) {
                    newRolls.addAll(adjacentAccessibleRolls(input.grid(), position));
                }
                rolls = newRolls;
            }

            return new Output(total);
        }
    }

    public static void main(String[] args) {
        InputStream is = Main.class.getResourceAsStream("/day4/input.in");
        assert is != null;
        try (Scanner scanner = new Scanner(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            Parser parser = new Parser(scanner);
            Input input = parser.parse();
            Solver<Input, Output> solver = new Day4(true /* part2 */);
            Output output = solver.solve(input);

            System.out.printf("Answer: %s", output.accessibleRolls());
        }
    }
}
