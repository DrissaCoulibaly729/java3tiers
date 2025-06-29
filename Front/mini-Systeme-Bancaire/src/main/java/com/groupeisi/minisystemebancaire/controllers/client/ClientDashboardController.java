package com.groupeisi.minisystemebancaire.controllers.client;

import com.groupeisi.minisystemebancaire.dto.ClientDTO;
import com.groupeisi.minisystemebancaire.dto.CompteDTO;
import com.groupeisi.minisystemebancaire.dto.TransactionDTO;
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
import java.util.stream.Collectors;

public class ClientDashboardController {

    // === ÉLÉMENTS FXML - CORRESPONDANCE AVEC LE FXML ===
    @FXML private Label lblWelcome;
    @FXML private Label lblNbComptes;
    @FXML private Label lblSoldeTotal;
    @FXML private Label lblNbCartes;
    @FXML private Label lblNbCredits;

    // Buttons navigation
    @FXML private Button btnDashboard;
    @FXML private Button btnTransactions;
    @FXML private Button btnCredits;
    @FXML private Button btnCartes;
    @FXML private Button btnSupport;
    @FXML private Button btnDeconnexion;

    // Tables
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

    // Services
    private final CompteService compteService = new CompteService();
    private final TransactionService transactionService = new TransactionService();

    private ClientDTO currentClient;
    private Long clientId;

    @FXML
    public void initialize() {
        currentClient = SessionManager.getCurrentClient();

        if (currentClient == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Session expirée");
            redirectToLogin();
            return;
        }

        // ✅ VÉRIFICATION CRITIQUE : S'assurer que l'ID client existe
        if (currentClient.getId() == null) {
            System.err.println("❌ ERREUR CRITIQUE : Client en session sans ID !");
            showAlert(Alert.AlertType.ERROR, "Erreur", "Session corrompue - Veuillez vous reconnecter");
            SessionManager.clearCurrentClient();
            redirectToLogin();
            return;
        }

        this.clientId = currentClient.getId();
        System.out.println("🔧 Dashboard initialisé pour le client ID: " + clientId);

        setupTableColumns();
        updateWelcomeMessage();
        loadDashboardData();
    }

    private void setupTableColumns() {
        // Configuration des colonnes de la table des comptes
        if (colNumeroCompte != null) colNumeroCompte.setCellValueFactory(new PropertyValueFactory<>("numero"));
        if (colTypeCompte != null) colTypeCompte.setCellValueFactory(new PropertyValueFactory<>("type"));
        if (colSoldeCompte != null) {
            colSoldeCompte.setCellValueFactory(new PropertyValueFactory<>("solde"));
            colSoldeCompte.setCellFactory(column -> new TableCell<CompteDTO, Double>() {
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
        if (colStatutCompte != null) colStatutCompte.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Configuration des colonnes de la table des transactions
        if (colTypeTransaction != null) colTypeTransaction.setCellValueFactory(new PropertyValueFactory<>("type"));
        if (colMontantTransaction != null) {
            colMontantTransaction.setCellValueFactory(new PropertyValueFactory<>("montant"));
            colMontantTransaction.setCellFactory(column -> new TableCell<TransactionDTO, Double>() {
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
        if (colDateTransaction != null) colDateTransaction.setCellValueFactory(new PropertyValueFactory<>("dateFormatted"));
        if (colStatutTransaction != null) colStatutTransaction.setCellValueFactory(new PropertyValueFactory<>("statut"));
    }

    private void updateWelcomeMessage() {
        if (lblWelcome != null && currentClient != null) {
            String welcomeText = "Bienvenue, " + currentClient.getPrenom() + " " + currentClient.getNom();
            lblWelcome.setText(welcomeText);
        }
    }

    private void loadDashboardData() {
        loadComptes();
        loadTransactions();
        // Note: Cartes et crédits commentés car API non implémentée
        // loadCartes();
        // loadCredits();
    }

    // ✅ CORRECTION : Méthode loadComptes avec vérification d'ID
    private void loadComptes() {
        if (clientId == null) {
            System.err.println("❌ Impossible de charger les comptes : clientId est null");
            showAlert(Alert.AlertType.ERROR, "Erreur", "Session invalide");
            return;
        }

        Thread loadThread = new Thread(() -> {
            try {
                System.out.println("🔄 Chargement des comptes pour client ID: " + clientId);
                List<CompteDTO> comptes = compteService.getComptesByClientId(clientId);

                Platform.runLater(() -> {
                    if (tableComptes != null && comptes != null) {
                        ObservableList<CompteDTO> comptesData = FXCollections.observableArrayList(comptes);
                        tableComptes.setItems(comptesData);
                        updateComptesStatistics(comptes);
                        System.out.println("✅ " + comptes.size() + " comptes chargés");
                    } else {
                        updateComptesStatistics(null);
                        System.out.println("ℹ️ Aucun compte trouvé pour ce client");
                    }
                });

            } catch (Exception e) {
                System.err.println("❌ Erreur lors du chargement des comptes: " + e.getMessage());
                Platform.runLater(() -> {
                    updateComptesStatistics(null);
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les comptes: " + e.getMessage());
                });
            }
        });

        loadThread.setDaemon(true);
        loadThread.start();
    }

    // ✅ CORRECTION : Méthode loadTransactions avec vérification d'ID
    private void loadTransactions() {
        if (clientId == null) {
            System.err.println("❌ Impossible de charger les transactions : clientId est null");
            return;
        }

        Thread loadThread = new Thread(() -> {
            try {
                System.out.println("🔄 Chargement des transactions pour client ID: " + clientId);
                List<TransactionDTO> transactions = transactionService.getTransactionsByClientId(clientId);

                Platform.runLater(() -> {
                    if (tableTransactionsRecentes != null && transactions != null) {
                        // Prendre seulement les 10 dernières transactions
                        List<TransactionDTO> recentTransactions = transactions.stream()
                                .limit(10)
                                .collect(Collectors.toList());

                        ObservableList<TransactionDTO> transactionsData = FXCollections.observableArrayList(recentTransactions);
                        tableTransactionsRecentes.setItems(transactionsData);
                        System.out.println("✅ " + recentTransactions.size() + " transactions récentes chargées");
                    }
                });

            } catch (Exception e) {
                System.err.println("❌ Erreur lors du chargement des transactions: " + e.getMessage());
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.WARNING, "Avertissement", "Impossible de charger les transactions récentes");
                });
            }
        });

        loadThread.setDaemon(true);
        loadThread.start();
    }

    private void updateComptesStatistics(List<CompteDTO> comptes) {
        if (comptes == null || comptes.isEmpty()) {
            if (lblNbComptes != null) lblNbComptes.setText("0");
            if (lblSoldeTotal != null) lblSoldeTotal.setText("0.00 FCFA");
        } else {
            if (lblNbComptes != null) lblNbComptes.setText(String.valueOf(comptes.size()));

            double soldeTotal = comptes.stream()
                    .filter(c -> "Actif".equals(c.getStatut()))
                    .mapToDouble(c -> c.getSolde() != null ? c.getSolde() : 0.0)
                    .sum();

            if (lblSoldeTotal != null) lblSoldeTotal.setText(String.format("%.2f FCFA", soldeTotal));
        }

        // Valeurs par défaut pour les cartes et crédits (non implémentés)
        if (lblNbCartes != null) lblNbCartes.setText("0");
        if (lblNbCredits != null) lblNbCredits.setText("0");
    }

    // === GESTIONNAIRES D'ÉVÉNEMENTS NAVIGATION ===

    @FXML
    private void handleDashboard(ActionEvent event) {
        // Déjà sur le dashboard, rafraîchir les données
        loadDashboardData();
    }

    // ✅ AJOUT : Méthodes manquantes pour correspondre au FXML
    @FXML
    private void goToTransactions(ActionEvent event) {
        handleTransactions(event);
    }

    @FXML
    private void goToCredits(ActionEvent event) {
        handleCredits(event);
    }

    @FXML
    private void goToCartes(ActionEvent event) {
        handleCartes(event);
    }

    @FXML
    private void goToSupport(ActionEvent event) {
        handleSupport(event);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        handleDeconnexion(event);
    }

    @FXML
    private void handleTransactions(ActionEvent event) {
        navigateTo("/com/groupeisi/minisystemebancaire/client/UI_Transactions.fxml", event);
    }

    @FXML
    private void handleCredits(ActionEvent event) {
        navigateTo("/com/groupeisi/minisystemebancaire/client/UI_Credits.fxml", event);
    }

    @FXML
    private void handleCartes(ActionEvent event) {
        navigateTo("/com/groupeisi/minisystemebancaire/client/UI_Cartes.fxml", event);
    }

    @FXML
    private void handleSupport(ActionEvent event) {
        navigateTo("/com/groupeisi/minisystemebancaire/client/UI_Support.fxml", event);
    }

    @FXML
    private void handleDeconnexion(ActionEvent event) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Déconnexion");
        confirmation.setContentText("Êtes-vous sûr de vouloir vous déconnecter ?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                SessionManager.logout();
                redirectToLogin();
            }
        });
    }

    // === MÉTHODES UTILITAIRES ===

    private void redirectToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/groupeisi/minisystemebancaire/UI_Main.fxml"));
            Parent root = loader.load();

            Stage stage = null;
            if (lblWelcome != null && lblWelcome.getScene() != null) {
                stage = (Stage) lblWelcome.getScene().getWindow();
            }

            if (stage != null) {
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.centerOnScreen();
            }
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

    // === GETTERS/SETTERS POUR COMPATIBILITÉ ===

    public ClientDTO getCurrentClient() {
        return currentClient;
    }

    public void setCurrentClient(ClientDTO currentClient) {
        this.currentClient = currentClient;
        if (currentClient != null) {
            this.clientId = currentClient.getId();
        }
    }
}