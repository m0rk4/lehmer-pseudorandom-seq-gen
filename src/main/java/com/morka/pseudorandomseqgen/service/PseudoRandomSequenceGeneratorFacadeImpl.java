package com.morka.pseudorandomseqgen.service;

import com.morka.pseudorandomseqgen.dto.GeneratorDistributionDto;

import java.util.Arrays;

public final class PseudoRandomSequenceGeneratorFacadeImpl implements PseudoRandomGeneratorFacade {

    @Override
    public long getLastValueAfterIterations(PseudoRandomSequenceGenerator generator, long iterationsCount) {
        long last = generator.getNext();
        for (long i = 1; i < iterationsCount; i++)
            last = generator.getNext();
        return last;
    }

    @Override
    public long getPeriodLength(PseudoRandomSequenceGenerator generator, long target) {
        long total = 0;
        while (generator.getNext() != target)
            total++;

        long firstOccurrence = total;
        while (generator.getNext() != target)
            total++;
        return total - firstOccurrence + 1;
    }

    @Override
    public double getIndirectEstimateOfUniformity(PseudoRandomSequenceGenerator generator, long iterations) {
        long evenIterations = iterations % 2 == 0 ? iterations : iterations + 1;

        int inCircle = 0;
        for (long i = 1; i < iterations; i += 2) {
            double prev = generator.getNextDouble();
            double next = generator.getNextDouble();
            if (prev * prev + next * next < 1.0)
                inCircle++;
        }

        return 2.0 * inCircle / evenIterations;
    }

    @Override
    public long getTailLengthInAperiodicSequence(PseudoRandomGeneratorBuilder builder, long period) {
        PseudoRandomSequenceGenerator firstGenerator = builder.build();
        for (long i = 0; i < period; i++)
            firstGenerator.getNext();

        PseudoRandomSequenceGenerator secondGenerator = builder.build();
        for (long i = 0; i < period; i++)
            if (firstGenerator.getNext() == secondGenerator.getNext())
                return i;

        throw new IllegalStateException("Unreachable.");
    }

    @Override
    public GeneratorDistributionDto getGeneratorDistributionInformation(
            PseudoRandomSequenceGenerator generator,
            int iterationsCount,
            int intervalsCount
    ) {
        double[] numbers = new double[iterationsCount];
        for (int i = 0; i < numbers.length; i++)
            numbers[i] = generator.getNextDouble();

        Arrays.sort(numbers);

        final double maxDiff = numbers[numbers.length - 1] - numbers[0];
        final double intervalLength = maxDiff / intervalsCount;

        int[] hits = new int[intervalsCount];
        for (double number : numbers) {
            for (int j = 0; j < intervalsCount; j++) {
                double start = j * intervalLength;
                double end = (j + 1) * intervalLength;
                if (number > start && number <= end) {
                    hits[j]++;
                    break;
                }
            }
        }

        return new GeneratorDistributionDto(hits, intervalsCount, iterationsCount, intervalLength);
    }
}
