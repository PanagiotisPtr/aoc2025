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

        for (Rotation r : input.rotations()) {
            long throughZero = r.amount() / 100L;
            switch (r.direction()) {
                case LEFT -> {
                    if (d.getValue() != 0 && d.distanceToZeroLeft() < r.amount() % 100) {
                        throughZero++;
                    }
                }
                case RIGHT -> {
                    if (d.getValue() != 0 && d.distanceToZeroRight() < r.amount() % 100) {
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
