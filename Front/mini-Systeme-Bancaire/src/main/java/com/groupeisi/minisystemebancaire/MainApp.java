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
            primaryStage.setTitle("🏦 Mini Système Bancaire");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();

            // Icône de l'application (optionnel)
            // primaryStage.getIcons().add(new Image("/icons/bank-icon.png"));

            primaryStage.show();

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
                adminService.createAdmin(admin);
                System.out.println("✅ Admin par défaut créé/vérifié avec succès");
            } catch (Exception e) {
                System.out.println("ℹ️ Admin par défaut probablement déjà existant ou erreur de connexion API");
            }
        });
        initThread.setDaemon(true); // Thread daemon pour qu'il se ferme avec l'application
        initThread.start();
    }

    @Override
    public void stop() {
        // Nettoyer la session au fermeture
        SessionManager.clearSession();
        System.out.println("Application fermée proprement");
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
