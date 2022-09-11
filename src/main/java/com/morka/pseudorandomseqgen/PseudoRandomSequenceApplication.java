package com.morka.pseudorandomseqgen;

import com.morka.pseudorandomseqgen.controller.MainController;
import com.morka.pseudorandomseqgen.service.PseudoRandomGeneratorFacade;
import com.morka.pseudorandomseqgen.service.PseudoRandomSequenceGeneratorFacadeImpl;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class PseudoRandomSequenceApplication extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        PseudoRandomGeneratorFacade facade = new PseudoRandomSequenceGeneratorFacadeImpl();

        URL resource = PseudoRandomSequenceApplication.class.getResource("main-view.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        fxmlLoader.setControllerFactory(__ -> new MainController(facade));

        Scene scene = new Scene(fxmlLoader.load(), 1280, 720);
        stage.setTitle("Pseudo-random sequence generator");
        stage.setScene(scene);
        stage.show();
    }
}
