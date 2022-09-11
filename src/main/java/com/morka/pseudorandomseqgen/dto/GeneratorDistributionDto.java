package com.morka.pseudorandomseqgen.dto;

public record GeneratorDistributionDto(int[] hits, int intervalsCount, int iterations, double intervalLength) {
}
