package com.panagiotispetridis.day1;

import java.util.ArrayList;
import java.util.Scanner;

public class Parser extends com.panagiotispetridis.common.Parser<Input> {

    public Parser(Scanner scanner) {
        super(scanner);
    }

    public Input parse() {
        Input input = new Input(new ArrayList<>());

        while(scanner.hasNextLine()) {
            input.rotations().add(Rotation.fromString(scanner.nextLine()));
        }

        return input;
    }

}
