module com.morka.pseudorandomseqgen {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.morka.pseudorandomseqgen.controller to javafx.fxml;
    exports com.morka.pseudorandomseqgen;
}
