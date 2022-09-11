package com.morka.pseudorandomseqgen.controller;

import com.morka.pseudorandomseqgen.service.LehmerPseudoRandomSequenceGenerator;
import javafx.concurrent.Task;

public final class TailCounterTaskFactory {

    private TailCounterTaskFactory() {
        throw new AssertionError();
    }

    public static Task<Long> create(LehmerPseudoRandomSequenceGenerator.Builder builder, long period) {
        return new Task<>() {
            @Override
            protected Long call() {
                return getTailLengthInAperiodicSequence(builder, period);
            }
        };
    }

    private static long getTailLengthInAperiodicSequence(
            LehmerPseudoRandomSequenceGenerator.Builder builder,
            long period
    ) {
        LehmerPseudoRandomSequenceGenerator firstGenerator = builder.build();
        for (long i = 0; i < period; i++)
            firstGenerator.getNext();

        LehmerPseudoRandomSequenceGenerator secondGenerator = builder.build();
        for (int i = 0; i < period; i++)
            if (firstGenerator.getNext() == secondGenerator.getNext())
                return i;

        throw new IllegalStateException("Unreachable.");
    }
}
