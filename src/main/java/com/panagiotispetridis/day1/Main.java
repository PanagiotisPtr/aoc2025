package com.panagiotispetridis.day1;

import com.panagiotispetridis.common.Solver;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        InputStream is = Main.class.getResourceAsStream("/day1/input1.in");
        try (Scanner scanner = new Scanner(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            Input input = Parser.parse(scanner);
            Solver<Input, Output> solver = new Day1(true /* part2 */);
            Output output = solver.solve(input);

            System.out.printf("Answer: %s%n", output);
        }
    }

}
