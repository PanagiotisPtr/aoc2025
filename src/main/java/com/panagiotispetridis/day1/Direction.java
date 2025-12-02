package com.panagiotispetridis.day1;

public enum Direction {
    LEFT("L"),
    RIGHT("R");

    final private String serialized;

    Direction(String s) {
        this.serialized = s;
    }

    @Override
    public String toString() {
        return serialized;
    }

    public static Direction fromString(String s) {
        if (s.equals("L")) {
            return LEFT;
        }
        return RIGHT;
    }
}
