package com.groupeisi.minisystemebancaire;

import com.google.gson.Gson;
import com.groupeisi.minisystemebancaire.dto.AdminDTO;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 📌 Ajouter un admin par défaut (appel API Laravel)
        ajouterAdminParDefaut();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/groupeisi/minisystemebancaire/UI_Main.fxml"));
            Scene scene = new Scene(loader.load());
            primaryStage.setTitle("Mini Système Bancaire - Connexion");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ajouterAdminParDefaut() {
        try {
            AdminDTO admin = new AdminDTO();
            admin.setUsername("admin");
            admin.setPassword("admin123");
            admin.setRole("ADMIN");

            Gson gson = new Gson();
            String requestBody = gson.toJson(admin);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8000/api/admins"))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201 || response.statusCode() == 200) {
                System.out.println("✅ Admin par défaut ajouté ou déjà existant !");
            } else {
                System.out.println("❌ Échec de l'ajout de l'admin : " + response.statusCode());
                System.out.println("Réponse : " + response.body());
            }

        } catch (Exception e) {
            System.out.println("❌ Erreur lors de l'ajout de l'admin par défaut");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
