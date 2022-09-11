package com.morka.pseudorandomseqgen.controller;

import javafx.concurrent.Task;

import java.util.function.Supplier;

public final class TaskFactory {

    private TaskFactory() {
        throw new AssertionError();
    }

    public static <T> Task<T> create(Supplier<T> supplier) {
        return new Task<>() {
            @Override
            protected T call() {
                return supplier.get();
            }
        };
    }
}
