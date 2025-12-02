package com.panagiotispetridis.common;

public interface Solver<Input, Output> {

    Output solve(Input input);

}
