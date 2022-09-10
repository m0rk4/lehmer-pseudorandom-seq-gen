package com.morka.pseudorandomseqgen;

import com.morka.pseudorandomseqgen.service.LemerPseudoRandomSequenceGenerator;
import com.morka.pseudorandomseqgen.service.PseudoRandomSequenceGenerator;

public class MainController {
    private final PseudoRandomSequenceGenerator sequenceGenerator = new LemerPseudoRandomSequenceGenerator.Builder()
            .mod(5)
            .coefficient(3)
            .seed(1)
            .build();
}
