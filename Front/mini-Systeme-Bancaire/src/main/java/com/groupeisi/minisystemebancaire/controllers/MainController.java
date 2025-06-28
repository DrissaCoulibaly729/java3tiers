package com.groupeisi.minisystemebancaire.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.input.MouseEvent;

import java.io.IOException;

public class MainController {

    @FXML private Button btnAdmin;
    @FXML private Button btnClient;

    @FXML
    private void initialize() {
        // Configuration des effets visuels
        setupButtonEffects();
    }

    private void setupButtonEffects() {
        btnAdmin.setOnMouseEntered(e -> btnAdmin.setOpacity(0.8));
        btnAdmin.setOnMouseExited(e -> btnAdmin.setOpacity(1.0));

        btnClient.setOnMouseEntered(e -> btnClient.setOpacity(0.8));
        btnClient.setOnMouseExited(e -> btnClient.setOpacity(1.0));
    }

    @FXML
    private void handleAdminClick(ActionEvent event) {
        navigateTo("/com/groupeisi/minisystemebancaire/admin/UI_Login.fxml", event);
    }

    @FXML
    private void handleClientClick(ActionEvent event) {
        navigateTo("/com/groupeisi/minisystemebancaire/client/UI_Login.fxml", event);
    }

    private void navigateTo(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors de la navigation vers: " + fxmlPath);
        }
    }
}
