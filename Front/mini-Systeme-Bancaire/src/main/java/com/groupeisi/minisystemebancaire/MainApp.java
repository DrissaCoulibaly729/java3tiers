package com.groupeisi.minisystemebancaire;

import com.groupeisi.minisystemebancaire.dto.AdminDTO;
import com.groupeisi.minisystemebancaire.services.AdminService;
import com.groupeisi.minisystemebancaire.utils.SessionManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Configuration de l'application
        Platform.setImplicitExit(true);

        // Initialiser l'admin par défaut en arrière-plan
        initializeDefaultAdminAsync();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/groupeisi/minisystemebancaire/UI_Main.fxml"));
            Scene scene = new Scene(loader.load());

            // Configuration de la fenêtre principale
            primaryStage.setTitle("🏦 Mini Système Bancaire - Groupe 1");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.centerOnScreen();

            // Icône de l'application (optionnel)
            // primaryStage.getIcons().add(new Image("/icons/bank-icon.png"));

            // Gestionnaire de fermeture
            primaryStage.setOnCloseRequest(event -> {
                SessionManager.clearSession();
                System.out.println("👋 Application fermée proprement");
                Platform.exit();
            });

            primaryStage.show();

            System.out.println("🚀 Application démarrée avec succès");

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur de démarrage", "Impossible de charger l'interface principale");
            Platform.exit();
        }
    }

    private void initializeDefaultAdminAsync() {
        // Exécuter en arrière-plan pour ne pas bloquer le démarrage
        Thread initThread = new Thread(() -> {
            try {
                AdminService adminService = new AdminService();
                AdminDTO admin = new AdminDTO("admin", "admin123", "ADMIN");
                AdminDTO createdAdmin = adminService.createAdmin(admin);

                if (createdAdmin != null) {
                    System.out.println("✅ Admin par défaut créé avec succès");
                } else {
                    System.out.println("ℹ️ Admin par défaut existe déjà");
                }
            } catch (Exception e) {
                System.out.println("ℹ️ Admin par défaut probablement déjà existant ou erreur de connexion API");
                System.out.println("💡 Vérifiez que le backend Laravel est démarré (php artisan serve)");
            }
        });
        initThread.setDaemon(true); // Thread daemon pour qu'il se ferme avec l'application
        initThread.start();
    }

    @Override
    public void stop() {
        // Nettoyer la session au fermeture bien
        SessionManager.clearSession();
        System.out.println("🔐 Session nettoyée lors de la fermeture");
    }

    private void showErrorAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}