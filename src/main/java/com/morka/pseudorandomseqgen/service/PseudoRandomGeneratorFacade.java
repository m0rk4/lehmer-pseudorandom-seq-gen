package com.morka.pseudorandomseqgen.service;

import com.morka.pseudorandomseqgen.dto.GeneratorDistributionDto;
import com.morka.pseudorandomseqgen.dto.GeneratorMathematicalExpectationDto;

public sealed interface PseudoRandomGeneratorFacade permits PseudoRandomSequenceGeneratorFacadeImpl {

    long getLastValueAfterIterations(PseudoRandomSequenceGenerator generator, long iterationsCount);

    long getPeriodLength(PseudoRandomSequenceGenerator generator, long target);

    double getIndirectEstimateOfUniformity(PseudoRandomSequenceGenerator generator, long iterations);

    long getTailLengthInAperiodicSequence(PseudoRandomGeneratorBuilder builder, long period);

    GeneratorDistributionDto getGeneratorDistributionInformation(PseudoRandomSequenceGenerator generator,
                                                                 int iterationsCount,
                                                                 int intervalsCount);

    GeneratorMathematicalExpectationDto getMathematicalExpectation(PseudoRandomSequenceGenerator generator,
                                                                   int iterations);

    double getVariance(GeneratorMathematicalExpectationDto mathematicalExpectationDto);

    double getStandardDeviation(double variance);
}
