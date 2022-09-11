package com.morka.pseudorandomseqgen.service;

public sealed interface PseudoRandomGeneratorBuilder permits LehmerPseudoRandomSequenceGenerator.Builder {

    PseudoRandomSequenceGenerator build();
}
