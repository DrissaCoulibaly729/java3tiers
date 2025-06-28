package com.groupeisi.minisystemebancaire.controllers.admin;

import com.groupeisi.minisystemebancaire.utils.NavigationManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * ✅ Contrôleur principal pour la navigation admin avec sidebar fixe
 */
public class AdminMainController {

    @FXML private BorderPane mainContainer;

    // Boutons de navigation
    @FXML private Button btnDashboard, btnClients, btnComptes, btnTransactions;
    @FXML private Button btnCredits, btnCartes, btnSupport, btnDeconnexion;

    /**
     * ✅ Initialisation du contrôleur principal
     */
    @FXML
    public void initialize() {
        System.out.println("🚀 AdminMainController initialisé");

        // Initialiser le gestionnaire de navigation
        NavigationManager.initialize(mainContainer);

        // Charger la page d'accueil par défaut (Dashboard)
        handleDashboard();

        // Mettre en évidence le bouton Dashboard par défaut
        updateActiveButton(btnDashboard);
    }

    /**
     * ✅ Navigation vers le Dashboard
     */
    @FXML
    public void handleDashboard() {
        NavigationManager.navigateTo("/com/groupeisi/minisystemebancaire/admin/content/Dashboard_Content.fxml");
        updateActiveButton(btnDashboard);
        NavigationManager.updateSidebar("Dashboard");
    }

    /**
     * ✅ Navigation vers Gestion des Clients
     */
    @FXML
    public void handleGestionClients(ActionEvent event) {
        NavigationManager.navigateTo("/com/groupeisi/minisystemebancaire/admin/content/Clients_Content.fxml");
        updateActiveButton(btnClients);
        NavigationManager.updateSidebar("Clients");
    }

    /**
     * ✅ Navigation vers Gestion des Comptes
     */
    @FXML
    public void handleGestionComptes(ActionEvent event) {
        NavigationManager.navigateTo("/com/groupeisi/minisystemebancaire/admin/content/Comptes_Content.fxml");
        updateActiveButton(btnComptes);
        NavigationManager.updateSidebar("Comptes");
    }

    /**
     * ✅ Navigation vers Gestion des Transactions
     */
    @FXML
    public void handleGestionTransactions(ActionEvent event) {
        NavigationManager.navigateTo("/com/groupeisi/minisystemebancaire/admin/content/Transactions_Content.fxml");
        updateActiveButton(btnTransactions);
        NavigationManager.updateSidebar("Transactions");
    }

    /**
     * ✅ Navigation vers Gestion des Crédits
     */
    @FXML
    public void handleGestionCredits(ActionEvent event) {
        NavigationManager.navigateTo("/com/groupeisi/minisystemebancaire/admin/content/Credits_Content.fxml");
        updateActiveButton(btnCredits);
        NavigationManager.updateSidebar("Credits");
    }

    /**
     * ✅ Navigation vers Gestion des Cartes Bancaires
     */
    @FXML
    public void handleGestionCartes(ActionEvent event) {
        NavigationManager.navigateTo("/com/groupeisi/minisystemebancaire/admin/content/Cartes_Content.fxml");
        updateActiveButton(btnCartes);
        NavigationManager.updateSidebar("Cartes");
    }

    /**
     * ✅ Navigation vers Service Client
     */
    @FXML
    public void handleGestionSupport(ActionEvent event) {
        NavigationManager.navigateTo("/com/groupeisi/minisystemebancaire/admin/content/Support_Content.fxml");
        updateActiveButton(btnSupport);
        NavigationManager.updateSidebar("Support");
    }

    /**
     * ✅ Déconnexion avec confirmation
     */
    @FXML
    public void handleDeconnexion(ActionEvent event) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Déconnexion");
        confirmation.setHeaderText("Confirmer la déconnexion");
        confirmation.setContentText("Êtes-vous sûr de vouloir vous déconnecter ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Fermer la fenêtre actuelle
            Stage stage = (Stage) btnDeconnexion.getScene().getWindow();
            stage.close();

            // Optionnel : Retourner à la page de login
            // NavigationManager.navigateTo("/com/groupeisi/minisystemebancaire/UI_Main.fxml");
        }
    }

    /**
     * ✅ Mettre à jour le bouton actif visuellement
     */
    private void updateActiveButton(Button activeButton) {
        // Réinitialiser tous les boutons
        resetAllButtons();

        // Mettre en évidence le bouton actif
        activeButton.setStyle(
                "-fx-background-color: #3498db; " +
                        "-fx-background-radius: 10; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-pref-width: 220px; " +
                        "-fx-padding: 12;"
        );
    }

    /**
     * ✅ Réinitialiser le style de tous les boutons
     */
    private void resetAllButtons() {
        String inactiveStyle =
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: white; " +
                        "-fx-pref-width: 220px; " +
                        "-fx-padding: 12; " +
                        "-fx-font-size: 14px;";

        btnDashboard.setStyle(inactiveStyle);
        btnClients.setStyle(inactiveStyle);
        btnComptes.setStyle(inactiveStyle);
        btnTransactions.setStyle(inactiveStyle);
        btnCredits.setStyle(inactiveStyle);
        btnCartes.setStyle(inactiveStyle);
        btnSupport.setStyle(inactiveStyle);
    }
}