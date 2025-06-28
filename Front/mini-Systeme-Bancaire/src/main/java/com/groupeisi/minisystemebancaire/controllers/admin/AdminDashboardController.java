package com.groupeisi.minisystemebancaire.controllers.admin;

import com.groupeisi.minisystemebancaire.dto.AdminDTO;
import com.groupeisi.minisystemebancaire.dto.ClientDTO;
import com.groupeisi.minisystemebancaire.dto.CompteDTO;
import com.groupeisi.minisystemebancaire.dto.TransactionDTO;
import com.groupeisi.minisystemebancaire.services.ClientService;
import com.groupeisi.minisystemebancaire.services.CompteService;
import com.groupeisi.minisystemebancaire.services.TransactionService;
import com.groupeisi.minisystemebancaire.utils.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class AdminDashboardController {

    @FXML private Label lblBienvenue;
    @FXML private Label lblNombreClients;
    @FXML private Label lblNombreComptes;
    @FXML private Label lblNombreTransactions;
    @FXML private Label lblTransactionsSuspectes;
    @FXML private TableView<TransactionDTO> tableTransactionsRecentes;
    @FXML private TableColumn<TransactionDTO, String> colType;
    @FXML private TableColumn<TransactionDTO, Double> colMontant;
    @FXML private TableColumn<TransactionDTO, String> colDate;
    @FXML private TableColumn<TransactionDTO, String> colStatut;
    @FXML private TableView<TransactionDTO> tableTransactionsSuspectes;
    @FXML private TableColumn<TransactionDTO, String> colTypeSuspecte;
    @FXML private TableColumn<TransactionDTO, Double> colMontantSuspecte;
    @FXML private TableColumn<TransactionDTO, String> colDateSuspecte;
    @FXML private TableColumn<TransactionDTO, String> colMotifSuspecte;
    @FXML private Button btnGestionClients;
    @FXML private Button btnGestionComptes;
    @FXML private Button btnGestionTransactions;
    @FXML private Button btnGestionCredits;
    @FXML private Button btnServiceClient;
    @FXML private Button btnDeconnexion;

    private final ClientService clientService = new ClientService();
    private final CompteService compteService = new CompteService();
    private final TransactionService transactionService = new TransactionService();
    private AdminDTO currentAdmin;

    @FXML
    public void initialize() {
        currentAdmin = SessionManager.getCurrentAdmin();

        if (currentAdmin == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Session expirée");
            redirectToLogin();
            return;
        }

        setupUI();
        setupTableColumns();
        loadDashboardData();
    }

    private void setupUI() {
        lblBienvenue.setText("Bienvenue, " + currentAdmin.getUsername() + " !");

        // Configuration des boutons
        btnGestionClients.setOnAction(this::handleGestionClients);
        btnGestionComptes.setOnAction(this::handleGestionComptes);
        btnGestionTransactions.setOnAction(this::handleGestionTransactions);
        btnGestionCredits.setOnAction(this::handleGestionCredits);
        btnServiceClient.setOnAction(this::handleServiceClient);
        btnDeconnexion.setOnAction(this::handleDeconnexion);
    }

    private void setupTableColumns() {
        // Table des transactions récentes
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Table des transactions suspectes
        colTypeSuspecte.setCellValueFactory(new PropertyValueFactory<>("type"));
        colMontantSuspecte.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colDateSuspecte.setCellValueFactory(new PropertyValueFactory<>("date"));
        colMotifSuspecte.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Formatage des montants
        setupMontantColumn(colMontant);
        setupMontantColumn(colMontantSuspecte);

        // Formatage des statuts
        setupStatutColumn(colStatut);
    }

    private void setupMontantColumn(TableColumn<TransactionDTO, Double> column) {
        column.setCellFactory(col -> new TableCell<TransactionDTO, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f FCFA", item));
                }
            }
        });
    }

    private void setupStatutColumn(TableColumn<TransactionDTO, String> column) {
        column.setCellFactory(col -> new TableCell<TransactionDTO, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "Validé":
                            setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                            break;
                        case "Rejeté":
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                            break;
                        case "En attente":
                            setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });
    }

    private void loadDashboardData() {
        Thread loadThread = new Thread(() -> {
            try {
                // Charger les statistiques
                List<ClientDTO> clients = clientService.getAllClients();
                List<CompteDTO> comptes = compteService.getAllComptes();
                List<TransactionDTO> transactions = transactionService.getAllTransactions();
                List<TransactionDTO> transactionsSuspectes = transactionService.getTransactionsSuspectes();
                List<TransactionDTO> transactionsRecentes = transactionService.getTransactionsRecentes(10);

                Platform.runLater(() -> {
                    // Mise à jour des statistiques
                    lblNombreClients.setText(String.valueOf(clients.size()));
                    lblNombreComptes.setText(String.valueOf(comptes.size()));
                    lblNombreTransactions.setText(String.valueOf(transactions.size()));
                    lblTransactionsSuspectes.setText(String.valueOf(transactionsSuspectes.size()));

                    // Mise à jour des tableaux
                    ObservableList<TransactionDTO> recentesData = FXCollections.observableArrayList(transactionsRecentes);
                    tableTransactionsRecentes.setItems(recentesData);

                    ObservableList<TransactionDTO> suspectesData = FXCollections.observableArrayList(transactionsSuspectes);
                    tableTransactionsSuspectes.setItems(suspectesData);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    System.err.println("❌ Erreur lors du chargement des données: " + e.getMessage());
                    showAlert(Alert.AlertType.WARNING, "Avertissement",
                            "Certaines données n'ont pas pu être chargées");
                });
            }
        });

        loadThread.setDaemon(true);
        loadThread.start();
    }

    @FXML
    private void handleGestionClients(ActionEvent event) {
        navigateTo("/com/groupeisi/minisystemebancaire/admin/UI_Gestion_Clients.fxml", event);
    }

    @FXML
    private void handleGestionComptes(ActionEvent event) {
        navigateTo("/com/groupeisi/minisystemebancaire/admin/UI_Gestion_Comptes.fxml", event);
    }

    @FXML
    private void handleGestionTransactions(ActionEvent event) {
        navigateTo("/com/groupeisi/minisystemebancaire/admin/UI_Gestion_Transactions.fxml", event);
    }

    @FXML
    private void handleGestionCredits(ActionEvent event) {
        navigateTo("/com/groupeisi/minisystemebancaire/admin/UI_Gestion_Credits.fxml", event);
    }

    @FXML
    private void handleServiceClient(ActionEvent event) {
        navigateTo("/com/groupeisi/minisystemebancaire/admin/UI_Service_Client_Rapports.fxml", event);
    }

    @FXML
    private void handleDeconnexion(ActionEvent event) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Déconnexion");
        confirmation.setHeaderText("Confirmer la déconnexion");
        confirmation.setContentText("Êtes-vous sûr de vouloir vous déconnecter ?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                SessionManager.logout();
                redirectToLogin();
            }
        });
    }

    @FXML
    private void handleRafraichir() {
        loadDashboardData();
    }

    @FXML
    private void handleVoirToutesTransactions() {
        handleGestionTransactions(null);
    }

    @FXML
    private void handleGererTransactionsSuspectes() {
        // Ouvrir une fenêtre de gestion spécialisée pour les transactions suspectes
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/groupeisi/minisystemebancaire/admin/UI_Transactions_Suspectes.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Gestion des Transactions Suspectes");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre de gestion");
        }
    }

    private void redirectToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/groupeisi/minisystemebancaire/UI_Main.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) lblBienvenue.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de naviguer vers " + fxmlPath);
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