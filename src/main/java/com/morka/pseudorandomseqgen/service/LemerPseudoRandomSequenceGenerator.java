package com.morka.pseudorandomseqgen.service;

public final class LemerPseudoRandomSequenceGenerator implements PseudoRandomSequenceGenerator {

    private long currentSeed;

    private final long coefficient;

    private final long mod;

    private LemerPseudoRandomSequenceGenerator(long coefficient, long mod, long seed) {
        validateParams(coefficient, mod);
        this.currentSeed = seed;
        this.coefficient = coefficient;
        this.mod = mod;
    }

    public double getNext() {
        currentSeed = (coefficient * (currentSeed % mod)) % mod;
        return (double) currentSeed / mod;
    }

    private static void validateParams(long coefficient, long mod) {
        if (mod <= 0 || coefficient <= 0)
            throw new IllegalArgumentException("Mod and coefficient should be greater than zero.");
        if (mod <= coefficient)
            throw new IllegalArgumentException("Mod should be greater than coefficient.");
    }

    public static class Builder {
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

        public LemerPseudoRandomSequenceGenerator build() {
            return new LemerPseudoRandomSequenceGenerator(coefficient, mod, seed);
        }
    }
}
