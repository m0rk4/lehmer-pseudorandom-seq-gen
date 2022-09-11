package com.morka.pseudorandomseqgen.service;

public final class PseudoRandomSequenceGeneratorFacadeImpl implements PseudoRandomGeneratorFacade {

    @Override
    public long getLastAfterIterations(PseudoRandomSequenceGenerator generator, long iterationsCount) {
        long last = generator.getNext();
        for (long i = 1; i < iterationsCount; i++)
            last = generator.getNext();
        return last;
    }

    @Override
    public long getPeriod(PseudoRandomSequenceGenerator generator, long target) {
        long total = 1;
        while (generator.getNext() != target)
            total++;

        long firstOccurrence = total;
        while (generator.getNext() != target)
            total++;
        return total - firstOccurrence;
    }
}
