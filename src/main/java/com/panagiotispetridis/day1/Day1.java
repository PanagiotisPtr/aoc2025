package com.panagiotispetridis.day1;

import com.panagiotispetridis.common.Solver;

public class Day1 implements Solver<Input, Output> {

    final private boolean part2;

    public Day1(boolean part2) {
        this.part2 = part2;
    }

    public Output solve(Input input) {
        Dial d = new Dial();
        long password = 0;

        //989
        for (Rotation r : input.rotations()) {
            System.out.println("Dial at: " + d.getValue());
            System.out.println("Rotating at: " + r);
            long throughZero = r.amount() / 100L;
            switch (r.direction()) {
                case LEFT -> {
                    System.out.println("Need to left: " + d.distanceToZeroLeft());
                    if (d.getValue() != 0 && d.distanceToZeroLeft() < r.amount() % 100) {
                        System.out.println("Went through 0 left");
                        throughZero++;
                    }
                }
                case RIGHT -> {
                    System.out.println("Need to right: " + d.distanceToZeroRight());
                    if (d.getValue() != 0 && d.distanceToZeroRight() < r.amount() % 100) {
                        System.out.println("Went through 0 right");
                        throughZero++;
                    }
                }
            }
            d.rotate(r);
            if (d.getValue() == 0L) {
                password++;
            }
            if (part2) {
                password += throughZero;
            }
        }

        return new Output(password);
    }
}
