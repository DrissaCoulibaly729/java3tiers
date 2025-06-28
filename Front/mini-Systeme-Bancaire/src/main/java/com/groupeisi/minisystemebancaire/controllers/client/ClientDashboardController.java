package com.groupeisi.minisystemebancaire.controllers.client;

import com.groupeisi.minisystemebancaire.dto.*;
import com.groupeisi.minisystemebancaire.services.*;
import com.groupeisi.minisystemebancaire.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ClientDashboardController {

    @FXML private Label lblWelcome;
    @FXML private Label lblNbComptes;
    @FXML private Label lblSoldeTotal;
    @FXML private Label lblNbCartes;
    @FXML private Label lblNbCredits;

    @FXML private TableView<CompteDTO> tableComptes;
    @FXML private TableColumn<CompteDTO, String> colNumeroCompte;
    @FXML private TableColumn<CompteDTO, String> colTypeCompte;
    @FXML private TableColumn<CompteDTO, Double> colSoldeCompte;
    @FXML private TableColumn<CompteDTO, String> colStatutCompte;

    @FXML private TableView<TransactionDTO> tableTransactionsRecentes;
    @FXML private TableColumn<TransactionDTO, String> colTypeTransaction;
    @FXML private TableColumn<TransactionDTO, Double> colMontantTransaction;
    @FXML private TableColumn<TransactionDTO, String> colDateTransaction;
    @FXML private TableColumn<TransactionDTO, String> colStatutTransaction;

    @FXML private Button btnTransactions;
    @FXML private Button btnCredits;
    @FXML private Button btnCartes;
    @FXML private Button btnSupport;
    @FXML private Button btnDeconnexion;

    private Long clientId;
    private final CompteService compteService = new CompteService();
    private final TransactionService transactionService = new TransactionService();
    private final CarteBancaireService carteService = new CarteBancaireService();
    private final CreditService creditService = new CreditService();

    public void setClientId(Long clientId) {
        this.clientId = clientId;
        if (clientId != null) {
            initialize();
        }
    }

    @FXML
    private void initialize() {
        if (SessionManager.isClientLoggedIn()) {
            clientId = SessionManager.getCurrentClient().getId();
            setupTableColumns();
            loadDashboardData();
        }
    }

    private void setupTableColumns() {
        // Configuration des colonnes des comptes
        colNumeroCompte.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colTypeCompte.setCellValueFactory(new PropertyValueFactory<>("type"));
        colSoldeCompte.setCellValueFactory(new PropertyValueFactory<>("solde"));
        colStatutCompte.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Configuration des colonnes des transactions
        colTypeTransaction.setCellValueFactory(new PropertyValueFactory<>("type"));
        colMontantTransaction.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colStatutTransaction.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Formatage de la date
        colDateTransaction.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDate() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
    }

    private void loadDashboardData() {
        try {
            ClientDTO client = SessionManager.getCurrentClient();
            lblWelcome.setText("Bienvenue, " + client.getPrenom() + " " + client.getNom());

            // Charger les comptes
            List<CompteDTO> comptes = compteService.getComptesByClientId(clientId);
            ObservableList<CompteDTO> comptesData = FXCollections.observableArrayList(comptes);
            tableComptes.setItems(comptesData);

            // Calculer les statistiques
            lblNbComptes.setText(String.valueOf(comptes.size()));

            double soldeTotal = comptes.stream()
                    .mapToDouble(CompteDTO::getSolde)
                    .sum();
            lblSoldeTotal.setText(String.format("%.2f €", soldeTotal));

            // Compter les cartes
            int nbCartes = 0;
            for (CompteDTO compte : comptes) {
                nbCartes += carteService.getCartesByCompte(compte.getId()).size();
            }
            lblNbCartes.setText(String.valueOf(nbCartes));

            // Compter les crédits
            List<CreditDTO> credits = creditService.getCreditsByClient(clientId);
            lblNbCredits.setText(String.valueOf(credits.size()));

            // Charger les transactions récentes (5 dernières)
            List<TransactionDTO> transactions = transactionService.getTransactionsByClient(clientId);
            ObservableList<TransactionDTO> transactionsData = FXCollections.observableArrayList(
                    transactions.stream().limit(5).toList()
            );
            tableTransactionsRecentes.setItems(transactionsData);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les données du dashboard");
        }
    }

    @FXML
    private void goToTransactions() {
        navigateToPage("UI_Transactions");
    }

    @FXML
    private void goToCredits() {
        navigateToPage("UI_Credits");
    }

    @FXML
    private void goToCartes() {
        navigateToPage("UI_Cartes_Bancaires");
    }

    @FXML
    private void goToSupport() {
        navigateToPage("UI_Service_Client");
    }

    @FXML
    private void handleLogout() {
        SessionManager.clearSession();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/groupeisi/minisystemebancaire/UI_Main.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) btnDeconnexion.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void navigateToPage(String pageName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/groupeisi/minisystemebancaire/client/" + pageName + ".fxml"));
            Scene scene = new Scene(loader.load());

            // Passer l'ID du client au contrôleur de destination
            Object controller = loader.getController();
            if (controller instanceof ClientTransactionsController) {
                ((ClientTransactionsController) controller).setClientId(clientId);
            } else if (controller instanceof ClientCreditsController) {
                ((ClientCreditsController) controller).setClientId(clientId);
            } else if (controller instanceof ClientCartesController) {
                ((ClientCartesController) controller).setClientId(clientId);
            } else if (controller instanceof ClientSupportController) {
                ((ClientSupportController) controller).setClientId(clientId);
            }

            Stage stage = (Stage) btnTransactions.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page " + pageName);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}