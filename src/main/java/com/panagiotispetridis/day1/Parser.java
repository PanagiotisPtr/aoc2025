package com.panagiotispetridis.day1;

import java.util.ArrayList;
import java.util.Scanner;

public class Parser {

    public static Input parse(Scanner scanner) {
        Input input = new Input(new ArrayList<>());

        while(scanner.hasNextLine()) {
            input.rotations().add(Rotation.fromString(scanner.nextLine()));
        }

        return input;
    }
}
