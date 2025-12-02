package com.panagiotispetridis.day1;

public record Output(Long password) {

    @Override
    public String toString() {
        return String.format("%d", password);
    }
}
