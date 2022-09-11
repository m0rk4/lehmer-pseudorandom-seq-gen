package com.morka.pseudorandomseqgen.service;

public final class LehmerPseudoRandomSequenceGenerator implements PseudoRandomSequenceGenerator {

    private final long coefficient;
    private final long mod;
    private long currentSeed;

    private LehmerPseudoRandomSequenceGenerator(long coefficient, long mod, long seed) {
        validateParams(coefficient, mod);
        this.currentSeed = seed;
        this.coefficient = coefficient;
        this.mod = mod;
    }

    private static void validateParams(long coefficient, long mod) {
        if (mod <= 0 || coefficient <= 0)
            throw new IllegalArgumentException("Mod and coefficient should be greater than zero.");
        if (mod <= coefficient)
            throw new IllegalArgumentException("Mod should be greater than coefficient.");
    }

    @Override
    public long getNext() {
        currentSeed = (coefficient * currentSeed) % mod;
        return currentSeed;
    }

    @Override
    public double getNextDouble() {
        return (double) getNext() / mod;
    }

    public final static class Builder implements PseudoRandomGeneratorBuilder {
        private long mod;

        private long seed;

        private long coefficient;

        public Builder mod(long mod) {
            this.mod = mod;
            return this;
        }

        public Builder seed(long seed) {
            this.seed = seed;
            return this;
        }

        public Builder coefficient(long coefficient) {
            this.coefficient = coefficient;
            return this;
        }

        @Override
        public LehmerPseudoRandomSequenceGenerator build() {
            return new LehmerPseudoRandomSequenceGenerator(coefficient, mod, seed);
        }
    }
}
