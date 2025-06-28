package com.groupeisi.minisystemebancaire;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // CORRECTION: Chemin correct vers le fichier FXML
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/com/groupeisi/minisystemebancaire/connexion.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Mini - Système Bancaire");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}