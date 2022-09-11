package com.morka.pseudorandomseqgen;

import com.morka.pseudorandomseqgen.controller.MainController;
import com.morka.pseudorandomseqgen.service.PseudoRandomSequenceGeneratorFacadeImpl;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class PseudoRandomSequenceApplication extends Application {

    private static final String FXML_VIEW = "main-view.fxml";
    private static final String WINDOW_TITLE = "Pseudo-random sequence generator";

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        URL resource = PseudoRandomSequenceApplication.class.getResource(FXML_VIEW);
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        fxmlLoader.setControllerFactory(__ -> new MainController(new PseudoRandomSequenceGeneratorFacadeImpl()));

        Scene scene = new Scene(fxmlLoader.load(), 1280, 720);
        stage.setScene(scene);
        stage.setTitle(WINDOW_TITLE);
        stage.show();
    }
}
