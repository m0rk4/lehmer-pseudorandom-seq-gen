package com.morka.pseudorandomseqgen;

import com.morka.pseudorandomseqgen.service.LemerPseudoRandomSequenceGenerator;
import com.morka.pseudorandomseqgen.service.PseudoRandomSequenceGenerator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MainController {
    @FXML
    private Label welcomeText;

    private final PseudoRandomSequenceGenerator sequenceGenerator = new LemerPseudoRandomSequenceGenerator.Builder()
            .mod(5)
            .coefficient(3)
            .seed(1)
            .build();

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText(String.valueOf(sequenceGenerator.getNext()));
    }
}
