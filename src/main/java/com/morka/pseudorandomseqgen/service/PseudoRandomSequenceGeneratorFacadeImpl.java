package com.morka.pseudorandomseqgen.service;

import com.morka.pseudorandomseqgen.dto.GeneratorDistributionDto;
import com.morka.pseudorandomseqgen.dto.GeneratorMathematicalExpectationDto;

import java.util.Arrays;

import static java.lang.Math.sqrt;

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

        long i = 0;
        PseudoRandomSequenceGenerator secondGenerator = builder.build();
        while (firstGenerator.getNext() != secondGenerator.getNext())
            i++;

        return i;
    }

    @Override
    public GeneratorDistributionDto getGeneratorDistributionInformation(PseudoRandomSequenceGenerator generator,
                                                                        int iterationsCount,
                                                                        int intervalsCount) {
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

    @Override
    public GeneratorMathematicalExpectationDto getMathematicalExpectation(PseudoRandomSequenceGenerator generator,
                                                                          int iterations) {
        double[] series = new double[iterations];
        double sum = 0.0;
        for (int i = 0; i < iterations; i++) {
            series[i] = generator.getNextDouble();
            sum += series[i];
        }

        return new GeneratorMathematicalExpectationDto(series, sum / iterations);
    }

    @Override
    public double getVariance(GeneratorMathematicalExpectationDto mathematicalExpectationDto) {
        double mathExpectation = mathematicalExpectationDto.expectation();
        double[] series = mathematicalExpectationDto.series();
        double sum = 0.0;
        for (double value : series) {
            double diff = value - mathExpectation;
            sum += diff * diff;
        }
        return sum / (series.length - 1);
    }

    @Override
    public double getStandardDeviation(double variance) {
        return sqrt(variance);
    }
}
