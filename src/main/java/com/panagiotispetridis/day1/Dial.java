package com.panagiotispetridis.day1;

public class Dial {

    private long value;

    public Dial() {
        this.value = 50L;
    }

    public void rotate(Rotation r) {
        switch (r.direction()) {
            case LEFT -> value -= r.amount();
            case RIGHT -> value += r.amount();
        }
        while (value < 0) {
            value += 100;
        }
        value = value % 100;
    }

    public long distanceToZeroLeft() {
        return value;
    }

    public long distanceToZeroRight() {
        return 100-this.distanceToZeroLeft();
    }

    public long getValue() {
        return this.value;
    }

}
