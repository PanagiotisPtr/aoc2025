package com.panagiotispetridis.day6;

import com.panagiotispetridis.common.Parser;
import com.panagiotispetridis.common.Solver;

import javax.sound.midi.SysexMessage;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class Main {

    enum Operator {
        ADDITION,
        MULTIPLICATION;

        static public Operator fromString(String s) {
            if ("+".equals(s)) {
                return ADDITION;
            } else {
                return MULTIPLICATION;
            }
        }
    }

    record Problem(List<Long> numbers, Operator operator) {
        public long evaluate() {
            long carry = Operator.MULTIPLICATION.equals(operator) ? 1 : 0;
            for (Long n : numbers) {
                switch (operator) {
                    case Operator.MULTIPLICATION -> carry *= n;
                    case Operator.ADDITION -> carry += n;
                }
            }

            return carry;
        }
    }

    record Input(List<Problem> problems) {}

    record Output(long summedResults) {}

    static class Day6Parser extends Parser<Input> {
        final private boolean part2;

        public Day6Parser(Scanner scanner, boolean part2) {
            super(scanner);
            this.part2 = part2;
        }

        Input parsePart1() {
            Input input = new Input(new ArrayList<>());
            List<List<Long>> numbers = new ArrayList<>();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (!scanner.hasNextLine()) {
                    List<Operator> operators = Arrays.stream(line.split(" "))
                            .filter(s -> !s.isEmpty())
                            .map(Operator::fromString)
                            .toList();
                    for (int i = 0; i < operators.size(); i++) {
                        input.problems().add(new Problem(
                                numbers.get(i),
                                operators.get(i)
                        ));
                    }
                    continue;
                }
                List<Long> parsed = Arrays.stream(line.split(" "))
                        .filter(s -> !s.isEmpty())
                        .map(Long::parseLong)
                        .toList();
                for (int i = 0; i < parsed.size(); i++) {
                    if (i >= numbers.size()) {
                        numbers.add(new ArrayList<>());
                    }
                    numbers.get(i).add(parsed.get(i));
                }
            }

            return input;
        }

        Input parsePart2() {
            List<String> raw = new ArrayList<>();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                raw.add(line);
            }
            List<List<String>> sections = new ArrayList<>();
            int sectionStart = 0;
            int sectionEnd = 0;
            while (sectionEnd < raw.get(0).length()) {
                boolean splitPoint = true;
                for (int i = 0; i < raw.size(); i++) {
                    if (raw.get(i).charAt(sectionEnd) != ' ') {
                        splitPoint = false;
                        break;
                    }
                }
                if (splitPoint) {
                    List<String> section = new ArrayList<>();
                    for (int i = 0; i < raw.size(); i++) {
                        section.add(raw.get(i).substring(sectionStart, sectionEnd));
                    }
                    sections.add(section);
                    sectionStart = sectionEnd+1;
                }
                sectionEnd++;
            }
            List<String> lastSection = new ArrayList<>();
            int maxLen = 0;
            for (int i = 0; i < raw.size(); i++) {
                lastSection.add(raw.get(i).substring(sectionStart));
                maxLen = Math.max(maxLen, lastSection.getLast().length());
            }
            for (int i = 0; i < lastSection.size(); i++) {
                StringBuilder builder = new StringBuilder();
                builder.append(lastSection.get(i));
                while (builder.length() < maxLen) {
                    builder.append(' ');
                }
                lastSection.set(i, builder.toString());
            }
            sections.add(lastSection);

            List<Problem> problems = new ArrayList<>();
            for (List<String> section : sections) {
                problems.add(parseSection(section));
            }

            return new Input(problems);
        }

        Problem parseSection(List<String> section) {
            int l = section.get(0).length();
            List<Long> numbers = new ArrayList<>();
            for (int i = l-1; i >= 0; i--) {
                StringBuilder number = new StringBuilder();
                for (int j = 0; j < section.size()-1; j++) {
                    String s = section.get(j);
                    if (s.charAt(i) == ' ') {
                        continue;
                    }
                    number.append(s.charAt(i));
                }
                numbers.add(Long.parseLong(number.toString()));
            }
            Operator operator = Operator.fromString(section.getLast().trim());

            return new Problem(numbers, operator);
        }

        @Override
        public Input parse() {
            if (part2) {
                return parsePart2();
            }

            return parsePart1();
        }
    }

    static class Day6 implements Solver<Input, Output> {

        public Day6() {}

        @Override
        public Output solve(Input input) {
            long result = 0;
            for (Problem p : input.problems()) {
                result += p.evaluate();
            }

            return new Output(result);
        }
    }

    public static void main(String[] args) {
        InputStream is = Main.class.getResourceAsStream("/day6/input.in");
        assert is != null;
        try (Scanner scanner = new Scanner(new InputStreamReader(is))) {
            Parser<Input> parser = new Day6Parser(scanner, true);
            Input input = parser.parse();
            Solver<Input, Output> solver = new Day6();
            Output output = solver.solve(input);

            System.out.printf("Answer: %s", output.summedResults());
        }
    }
}
