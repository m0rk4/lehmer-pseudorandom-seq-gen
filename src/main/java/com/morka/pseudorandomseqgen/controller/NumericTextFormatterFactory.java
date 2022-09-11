package com.morka.pseudorandomseqgen.controller;

import javafx.scene.control.TextFormatter;
import javafx.util.converter.LongStringConverter;

public final class NumericTextFormatterFactory {

    private static final String NUMERAL_REGEXP = "-?([1-9][0-9]*)?";

    private NumericTextFormatterFactory() {
        throw new AssertionError();
    }

    public static TextFormatter<Long> createNumericTextFormatter() {
        return new TextFormatter<>(
                new LongStringConverter(),
                0L,
                change -> change.getControlNewText().matches(NUMERAL_REGEXP) ? change : null
        );
    }
}
