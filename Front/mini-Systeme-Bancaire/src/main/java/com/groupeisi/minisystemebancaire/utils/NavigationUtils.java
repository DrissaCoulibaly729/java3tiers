package com.groupeisi.minisystemebancaire.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class NavigationUtils {

    public static void navigateTo(String fxmlPath, Node sourceNode) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationUtils.class.getResource(fxmlPath));
            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) sourceNode.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur de navigation", "Impossible de charger la page demandée");
        }
    }

    public static void navigateToWithController(String fxmlPath, Node sourceNode,
                                                ControllerCallback callback) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationUtils.class.getResource(fxmlPath));
            Scene scene = new Scene(loader.load());

            // Appeler le callback pour configurer le contrôleur
            if (callback != null) {
                callback.configure(loader.getController());
            }

            Stage stage = (Stage) sourceNode.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur de navigation", "Impossible de charger la page demandée");
        }
    }

    @FunctionalInterface
    public interface ControllerCallback {
        void configure(Object controller);
    }
}
