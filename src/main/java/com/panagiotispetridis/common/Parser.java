package com.panagiotispetridis.common;

import java.util.Scanner;

public abstract class Parser<Input> {

    final protected Scanner scanner;

    public Parser(Scanner scanner) {
        this.scanner = scanner;
    }

    public abstract Input parse();

}
