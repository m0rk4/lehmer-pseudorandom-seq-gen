package com.morka.pseudorandomseqgen.service;

public sealed interface PseudoRandomGeneratorFacade permits PseudoRandomSequenceGeneratorFacadeImpl {

    long getLastAfterIterations(PseudoRandomSequenceGenerator generator, long iterationsCount);

    long getPeriod(PseudoRandomSequenceGenerator generator, long target);
}
