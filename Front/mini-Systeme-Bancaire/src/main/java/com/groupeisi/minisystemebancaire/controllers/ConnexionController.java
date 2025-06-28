package com.groupeisi.minisystemebancaire.controllers;

import gm.rahmanproperties.optibank.config.ApiConfig;
import gm.rahmanproperties.optibank.utils.WindowManager;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

public class ConnexionController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String AUTH_URL = ApiConfig.getApiUrl() + "/auth";

    public record LoginRequest(String username, String password) {}

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (!validateInput(username, password)) {
            return;
        }

        LoginRequest loginRequest = new LoginRequest(username, password);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<LoginRequest> requestEntity = new HttpEntity<>(loginRequest, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    AUTH_URL + "/login",
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String token = (String) response.getBody().get("token");

                if (token != null && !token.isEmpty()) {
                    ApiConfig.setAuthToken(token);
                    System.out.println("Authentication Token received: " + token.substring(0, Math.min(token.length(), 10)) + "...");
                    fetchUserInfoAndRedirect(token);
                    WindowManager.showFxPopupSuccess("Connexion avec success");
                    WindowManager.closeWindow();
                } else {
                    WindowManager.showFxPopupError("Token manquant\n"+
                            "Le serveur n'a pas fourni de jeton d'authentification.");
                }
            }
        } catch (HttpClientErrorException e) {
            handleLoginError(e);
        } catch (ResourceAccessException e) {
            System.err.println("Network/Connection Error during login: " + e.getMessage());
            WindowManager.showFxPopupError("Serveur injoignable\n"+
                    "Impossible de se connecter au serveur. Vérifiez votre connexion internet.");
        } catch (Exception e) {
            System.err.println("Unhandled Exception during login: " + e.getMessage());
            e.printStackTrace();
            WindowManager.showFxPopupError("Une erreur inattendue est survenue\n"+
                    "Veuillez réessayer plus tard.");
        }
    }

    private void fetchUserInfoAndRedirect(String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);

            ResponseEntity<Map> userResponse = restTemplate.exchange(
                    ApiConfig.getApiUrl() + "/auth/fetch-user/"+ usernameField.getText().trim(),
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );

            System.out.println("User Info Response: " + userResponse.getBody());

            if (userResponse.getStatusCode() == HttpStatus.OK && userResponse.getBody() != null) {
                Map<String, Object> userInfo = userResponse.getBody();
                List<String> roles = (List<String>) userInfo.get("roles");
                String role = roles.get(0).toUpperCase();
                System.out.println("User Role: " + role);

                Object roleObj = userInfo.get("roles");

                if (roleObj instanceof List<?> rolesList && !rolesList.isEmpty()) {
//                    String role = rolesList.get(0).toString().toUpperCase();
                    switch (role) {
                        case "ROLE_ADMIN" -> WindowManager.openWindow("/fxml/admin/dashboard_admin.fxml", "Dashboard Administrateur");
                        case "ROLE_CLIENT" -> WindowManager.openWindow("/fxml/client/dashboard_client.fxml", "Dashboard Client");
                        default -> WindowManager.showFxPopupError("Rôle non reconnu\nVotre rôle (" + role + ") ne vous permet pas d'accéder à l'application.");
                    }
                } else {
                    WindowManager.showFxPopupError("Impossible de déterminer votre rôle utilisateur.");
                }
            } else {
                WindowManager.showFxPopupError("Informations utilisateur manquantes\n"+
                        "Impossible de récupérer vos informations.");
            }
        } catch (Exception e) {
            System.err.println("Error fetching user info: " + e.getMessage());
            WindowManager.showFxPopupError("Problème de chargement\n"+
                    "Impossible de charger votre tableau de bord.");
        }
    }

    @FXML
    public void handleForgotPassword() {
        WindowManager.closeWindow();
        WindowManager.openWindow("/fxml/client/forgot_password.fxml", "Modification mot de passe");
    }

    private void handleLoginError(HttpClientErrorException e) {
        System.err.println("HTTP Client Error during login: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());

        if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            WindowManager.showFxPopupError("Identifiants invalides\n"+
                    "Nom d'utilisateur ou mot de passe incorrect.");
        } else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
            WindowManager.showFxPopupError("Données invalides\n"+
                    "Veuillez vérifier vos informations.");
        } else {
            WindowManager.showFxPopupError("Erreur serveur\n"+
                    "Code d'erreur : " + e.getStatusCode());
        }
    }

    private boolean validateInput(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            WindowManager.showFxPopupError("Nom d'utilisateur requis\n"+
                    "Veuillez saisir votre nom d'utilisateur.");
            return false;
        }

        if (password == null || password.trim().isEmpty()) {
            WindowManager.showFxPopupError("Mot de passe requis\n"+
                    "Veuillez saisir votre mot de passe.");
            return false;
        }

        return true;
    }
}
