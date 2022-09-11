package com.morka.pseudorandomseqgen.service;

public sealed interface PseudoRandomSequenceGenerator permits LehmerPseudoRandomSequenceGenerator {

    long getNext();

    double getNextDouble();
}
