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

public class ClientTransactionsController {

    @FXML private ChoiceBox<CompteDTO> choiceCompteSource;
    @FXML private ChoiceBox<CompteDTO> choiceCompteDestination;
    @FXML private ChoiceBox<String> choiceTypeTransaction;
    @FXML private TextField txtMontant;
    @FXML private TextField txtDescription;
    @FXML private TextField txtNumeroCompteDestinataire;

    @FXML private TableView<TransactionDTO> tableTransactions;
    @FXML private TableColumn<TransactionDTO, String> colType;
    @FXML private TableColumn<TransactionDTO, Double> colMontant;
    @FXML private TableColumn<TransactionDTO, String> colDate;
    @FXML private TableColumn<TransactionDTO, String> colStatut;
    @FXML private TableColumn<TransactionDTO, String> colDescription;

    @FXML private Button btnEffectuerTransaction;
    @FXML private Button btnRafraichir;
    @FXML private Button btnDashboard;
    @FXML private Button btnCredits;
    @FXML private Button btnCartes;
    @FXML private Button btnSupport;
    @FXML private Button btnDeconnexion;

    private Long clientId;
    private final TransactionService transactionService = new TransactionService();
    private final CompteService compteService = new CompteService();

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
            setupComponents();
            loadData();
        }
    }

    private void setupComponents() {
        // Configuration des types de transactions
        choiceTypeTransaction.setItems(FXCollections.observableArrayList(
                "Dépôt", "Retrait", "Virement"
        ));

        // Configuration des colonnes du tableau
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Formatage de la date
        colDate.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDate() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        // Gestion des changements de type de transaction
        choiceTypeTransaction.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateFormBasedOnTransactionType(newVal);
        });
    }

    private void updateFormBasedOnTransactionType(String type) {
        if (type == null) return;

        switch (type) {
            case "Dépôt":
                choiceCompteSource.setVisible(false);
                choiceCompteDestination.setVisible(true);
                txtNumeroCompteDestinataire.setVisible(false);
                break;
            case "Retrait":
                choiceCompteSource.setVisible(true);
                choiceCompteDestination.setVisible(false);
                txtNumeroCompteDestinataire.setVisible(false);
                break;
            case "Virement":
                choiceCompteSource.setVisible(true);
                choiceCompteDestination.setVisible(false);
                txtNumeroCompteDestinataire.setVisible(true);
                break;
        }
    }

    private void loadData() {
        try {
            // Charger les comptes du client
            List<CompteDTO> comptes = compteService.getComptesByClientId(clientId);
            choiceCompteSource.setItems(FXCollections.observableArrayList(comptes));
            choiceCompteDestination.setItems(FXCollections.observableArrayList(comptes));

            // Charger les transactions
            refreshTransactions();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les données");
        }
    }

    @FXML
    private void handleEffectuerTransaction() {
        if (!validateTransactionForm()) {
            return;
        }

        try {
            String type = choiceTypeTransaction.getValue();
            Double montant = Double.parseDouble(txtMontant.getText());
            String description = txtDescription.getText().trim();

            TransactionDTO transaction = new TransactionDTO();
            transaction.setType(type);
            transaction.setMontant(montant);
            transaction.setDescription(description);

            switch (type) {
                case "Dépôt":
                    transaction.setCompteDestId(choiceCompteDestination.getValue().getId());
                    break;
                case "Retrait":
                    transaction.setCompteSourceId(choiceCompteSource.getValue().getId());
                    break;
                case "Virement":
                    transaction.setCompteSourceId(choiceCompteSource.getValue().getId());
                    // Rechercher le compte destinataire par numéro
                    CompteDTO compteDestinataire = compteService.getCompteByNumero(txtNumeroCompteDestinataire.getText());
                    transaction.setCompteDestId(compteDestinataire.getId());
                    break;
            }

            transactionService.createTransaction(transaction);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Transaction effectuée avec succès !");
            clearForm();
            refreshTransactions();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la transaction : " + e.getMessage());
        }
    }

    private boolean validateTransactionForm() {
        if (choiceTypeTransaction.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez sélectionner un type de transaction");
            return false;
        }

        if (txtMontant.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez saisir un montant");
            return false;
        }

        try {
            double montant = Double.parseDouble(txtMontant.getText());
            if (montant <= 0) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Le montant doit être positif");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le montant doit être un nombre valide");
            return false;
        }

        String type = choiceTypeTransaction.getValue();

        if ("Dépôt".equals(type) && choiceCompteDestination.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez sélectionner un compte de destination");
            return false;
        }

        if ("Retrait".equals(type) && choiceCompteSource.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez sélectionner un compte source");
            return false;
        }

        if ("Virement".equals(type)) {
            if (choiceCompteSource.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez sélectionner un compte source");
                return false;
            }
            if (txtNumeroCompteDestinataire.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez saisir le numéro du compte destinataire");
                return false;
            }
        }

        return true;
    }

    private void clearForm() {
        choiceTypeTransaction.getSelectionModel().clearSelection();
        choiceCompteSource.getSelectionModel().clearSelection();
        choiceCompteDestination.getSelectionModel().clearSelection();
        txtMontant.clear();
        txtDescription.clear();
        txtNumeroCompteDestinataire.clear();
    }

    @FXML
    private void handleRafraichir() {
        refreshTransactions();
    }

    private void refreshTransactions() {
        try {
            List<TransactionDTO> transactions = transactionService.getTransactionsByClient(clientId);
            ObservableList<TransactionDTO> transactionsData = FXCollections.observableArrayList(transactions);
            tableTransactions.setItems(transactionsData);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de rafraîchir les transactions");
        }
    }

    // Navigation methods
    @FXML private void goToDashboard() { navigateToPage("UI_Dashboard"); }
    @FXML private void goToCredits() { navigateToPage("UI_Credits"); }
    @FXML private void goToCartes() { navigateToPage("UI_Cartes_Bancaires"); }
    @FXML private void goToSupport() { navigateToPage("UI_Service_Client"); }

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
            if (controller instanceof ClientDashboardController) {
                ((ClientDashboardController) controller).setClientId(clientId);
            } else if (controller instanceof ClientCreditsController) {
                ((ClientCreditsController) controller).setClientId(clientId);
            } else if (controller instanceof ClientCartesController) {
                ((ClientCartesController) controller).setClientId(clientId);
            } else if (controller instanceof ClientSupportController) {
                ((ClientSupportController) controller).setClientId(clientId);
            }

            Stage stage = (Stage) btnDashboard.getScene().getWindow();
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