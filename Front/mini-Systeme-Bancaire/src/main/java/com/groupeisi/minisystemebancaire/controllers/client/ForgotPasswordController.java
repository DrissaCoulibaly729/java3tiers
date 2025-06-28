package com.groupeisi.minisystemebancaire.controllers.client;

import gm.rahmanproperties.optibank.config.ApiConfig;
import gm.rahmanproperties.optibank.utils.WindowManager;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

public class ForgotPasswordController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;

    private final RestTemplate restTemplate = new RestTemplate();

    @FXML
    private void handleForgotPassword() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            WindowManager.showFxPopupError("Veuillez remplir tous les champs");
            return;
        }

        if (!password.equals(confirmPassword)) {
            WindowManager.showFxPopupError("Les mots de passe ne correspondent pas");
            return;
        }

        if (password.length() < 8) {
            WindowManager.showFxPopupError("Le mot de passe doit contenir au moins 8 caractères");
            return;
        }

        try {
            String url = ApiConfig.getApiUrl() + "/api/clients/modify-password";

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("username", username);
            requestBody.put("password", password);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.remove("Authorization");

            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Void> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    Void.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                WindowManager.showFxPopupSuccess("Mot de passe modifié avec succès");
                WindowManager.closeWindow();
                WindowManager.openWindow("/fxml/connexion.fxml", "Connexion");
            } else {
                WindowManager.showFxPopupError("Erreur lors de la modification du mot de passe");
            }
        } catch (HttpClientErrorException e) {
            handlePasswordResetError(e);
        } catch (Exception e) {
            WindowManager.showFxPopupError("Erreur inattendue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handlePasswordResetError(HttpClientErrorException e) {
        if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
            WindowManager.showFxPopupError("Utilisateur non trouvé");
        } else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
            WindowManager.showFxPopupError("Données invalides: " + e.getResponseBodyAsString());
        } else {
            WindowManager.showFxPopupError("Erreur serveur: " + e.getStatusCode());
        }
    }
}
