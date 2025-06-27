package com.groupeisi.minisystemebancaire.controllers.client;

import com.groupeisi.minisystemebancaire.dto.CompteDTO;
import com.groupeisi.minisystemebancaire.dto.TransactionDTO;
import com.groupeisi.minisystemebancaire.services.CompteService;
import com.groupeisi.minisystemebancaire.services.TransactionService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class ClientDashboardController {
    private final CompteService compteService = new CompteService();
    private final TransactionService transactionService = new TransactionService();

    private Long clientId; // ID du client connecté

    @FXML
    private TextField txtSolde;

    @FXML
    private TableView<TransactionDTO> tableTransactions;

    @FXML
    private TableColumn<TransactionDTO, String> colType, colStatut, colDate;

    @FXML
    private TableColumn<TransactionDTO, Double> colMontant;

    @FXML
    private Button btnDepot, btnRetrait, btnVirement;
    @FXML
    private Button btnTransactions, btnCredits, btnCartes, btnSupport, btnDeconnexion;

    @FXML
    public void initialize() {
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
        loadDashboard(clientId);
    }



    /**
     * ✅ Charge les données du client connecté
     */
    public void loadDashboard(Long clientId) {
        this.clientId = clientId;
        afficherSolde();
        afficherTransactions();
    }

    /**
     * ✅ Affiche le solde actuel du client
     */
    private void afficherSolde() {
        List<CompteDTO> comptes = compteService.getComptesByClientId(clientId);
        double totalSolde = comptes.stream().mapToDouble(CompteDTO::getSolde).sum();
        txtSolde.setText(totalSolde + " FCFA");
    }

    /**
     * ✅ Affiche l’historique des transactions du client
     */
    private void afficherTransactions() {
        List<TransactionDTO> transactions = transactionService.getTransactionsByClientId(clientId);

        // Nettoyer les anciennes données
        tableTransactions.getItems().clear();

        // Ajouter les nouvelles transactions
        tableTransactions.getItems().addAll(transactions);
    }



    /**
     * ✅ Gère la navigation vers une autre interface en passant l'ID du client
     */
    @FXML
    public void handleNavigation(String page) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/groupeisi/minisystemebancaire/client/" + page + ".fxml"));
            BorderPane pane = loader.load();

            // Récupérer le contrôleur de la nouvelle page
            Object controller = loader.getController();

            // Vérifier si le contrôleur a une méthode pour récupérer l'ID du client
            if (controller instanceof ClientTransactionsController) {
                ((ClientTransactionsController) controller).setClientId(clientId);
            } else if (controller instanceof ClientCreditsController) {
                ((ClientCreditsController) controller).setClientId(clientId);
            } else if (controller instanceof ClientDashboardController) {
                ((ClientDashboardController) controller).setClientId(clientId);
            } else if (controller instanceof ClientCartesController) {
                ((ClientCartesController) controller).setClientId(clientId);
            } else if (controller instanceof ClientSupportController) {
                ((ClientSupportController) controller).setClientId(clientId);
            }

            // Afficher la nouvelle scène
            Stage stage = (Stage) btnTransactions.getScene().getWindow();
            stage.setScene(new Scene(pane));
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page.");
        }
    }



    /**
     * ✅ Gestion des boutons de navigation
     */
    @FXML
    public void goToDashboard() { handleNavigation("UI_Dashboard"); }
    @FXML
    public void goToTransactions() { handleNavigation("UI_Transactions"); }

    @FXML
    public void goToCredits() { handleNavigation("UI_Credits"); }

    @FXML
    public void goToCartes() { handleNavigation("UI_Cartes_Bancaires"); }

    @FXML
    public void goToSupport() { handleNavigation("UI_Service_Client"); }

    @FXML
    public void handleLogout() { handleNavigation("UI_Login"); }

    /**
     * ✅ Affiche une alerte
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
