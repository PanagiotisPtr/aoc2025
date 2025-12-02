package com.panagiotispetridis.day1;

public record Rotation(Direction direction, Long amount) {

    public static Rotation fromString(String s) {
        String direction = s.substring(0, 1);
        String amount = s.substring(1);

        return new Rotation(Direction.fromString(direction), Long.parseLong(amount));
    }

    @Override
    public String toString() {
        return String.format("%s%d", direction, amount);
    }
}
