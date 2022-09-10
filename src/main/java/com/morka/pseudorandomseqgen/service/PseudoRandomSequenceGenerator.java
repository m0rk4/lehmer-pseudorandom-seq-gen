package com.morka.pseudorandomseqgen.service;

public sealed interface PseudoRandomSequenceGenerator permits LemerPseudoRandomSequenceGenerator {

    double getNext();
}
