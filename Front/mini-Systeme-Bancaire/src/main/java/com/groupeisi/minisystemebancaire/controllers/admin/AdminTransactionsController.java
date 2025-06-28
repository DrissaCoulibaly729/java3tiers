package com.groupeisi.minisystemebancaire.controllers.admin;

import com.groupeisi.minisystemebancaire.dto.TransactionDTO;
import com.groupeisi.minisystemebancaire.dto.CompteDTO;
import com.groupeisi.minisystemebancaire.services.TransactionService;
import com.groupeisi.minisystemebancaire.services.CompteService;
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

public class AdminTransactionsController {

    @FXML private TextField txtMontant, txtDescription, txtRechercheTransaction;
    @FXML private ChoiceBox<String> choiceTypeTransaction;
    @FXML private ChoiceBox<CompteDTO> choiceCompteSource, choiceCompteDestination;
    @FXML private TableView<TransactionDTO> tableTransactions, tableTransactionsSuspectes;
    @FXML private TableColumn<TransactionDTO, String> colType, colStatut, colTypeSuspecte, colStatutSuspecte;
    @FXML private TableColumn<TransactionDTO, Double> colMontant, colMontantSuspecte;
    @FXML private TableColumn<TransactionDTO, String> colDate, colDateSuspecte, colCompteSource, colCompteSourceSuspecte;
    @FXML private Button btnEnregistrerTransaction, btnAnnulerTransaction, btnValiderSuspecte, btnRejeterSuspecte, btnDeconnexion;

    private final TransactionService transactionService = new TransactionService();
    private final CompteService compteService = new CompteService();

    @FXML
    private void initialize() {
        setupComponents();
        setupTableColumns();
        loadData();
    }

    private void setupComponents() {
        choiceTypeTransaction.setItems(FXCollections.observableArrayList("Dépôt", "Retrait", "Virement"));

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
                break;
            case "Retrait":
                choiceCompteSource.setVisible(true);
                choiceCompteDestination.setVisible(false);
                break;
            case "Virement":
                choiceCompteSource.setVisible(true);
                choiceCompteDestination.setVisible(true);
                break;
        }
    }

    private void setupTableColumns() {
        // Table transactions normales
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        colDate.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDate() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        colCompteSource.setCellValueFactory(cellData -> {
            TransactionDTO transaction = cellData.getValue();
            if (transaction.getCompteSource() != null) {
                return new javafx.beans.property.SimpleStringProperty(transaction.getCompteSource().getNumero());
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });

        // Table transactions suspectes (mêmes colonnes)
        colTypeSuspecte.setCellValueFactory(new PropertyValueFactory<>("type"));
        colMontantSuspecte.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colStatutSuspecte.setCellValueFactory(new PropertyValueFactory<>("statut"));

        colDateSuspecte.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDate() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        colCompteSourceSuspecte.setCellValueFactory(cellData -> {
            TransactionDTO transaction = cellData.getValue();
            if (transaction.getCompteSource() != null) {
                return new javafx.beans.property.SimpleStringProperty(transaction.getCompteSource().getNumero());
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });
    }

    @FXML
    private void handleEnregistrerTransaction() {
        if (!validateTransactionForm()) return;

        try {
            // ✅ CORRECTION : Utiliser setters au lieu de constructeur problématique
            TransactionDTO transaction = new TransactionDTO();
            transaction.setType(choiceTypeTransaction.getValue());
            transaction.setMontant(Double.parseDouble(txtMontant.getText()));
            transaction.setDescription(txtDescription.getText().trim());
            transaction.setStatut("Validé");

            switch (choiceTypeTransaction.getValue()) {
                case "Dépôt":
                    transaction.setCompteDestId(choiceCompteDestination.getValue().getId());
                    break;
                case "Retrait":
                    transaction.setCompteSourceId(choiceCompteSource.getValue().getId());
                    break;
                case "Virement":
                    transaction.setCompteSourceId(choiceCompteSource.getValue().getId());
                    transaction.setCompteDestId(choiceCompteDestination.getValue().getId());
                    break;
            }

            // ✅ CORRECTION : Utiliser createTransaction au lieu de enregistrerTransaction
            transactionService.createTransaction(transaction);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Transaction enregistrée avec succès !");
            clearForm();
            loadTransactions();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le montant doit être un nombre valide");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'enregistrement : " + e.getMessage());
        }
    }

    @FXML
    private void handleAnnulerTransaction() {
        TransactionDTO selectedTransaction = tableTransactions.getSelectionModel().getSelectedItem();
        if (selectedTransaction == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Veuillez sélectionner une transaction");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setContentText("Êtes-vous sûr de vouloir annuler cette transaction ?");

        if (confirmation.showAndWait().get() == ButtonType.OK) {
            try {
                transactionService.annulerTransaction(selectedTransaction.getId());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Transaction annulée avec succès");
                loadTransactions();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'annulation : " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleValiderSuspecte() {
        TransactionDTO selectedTransaction = tableTransactionsSuspectes.getSelectionModel().getSelectedItem();
        if (selectedTransaction == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Veuillez sélectionner une transaction suspecte");
            return;
        }

        try {
            selectedTransaction.setStatut("Validé");
            transactionService.updateTransaction(selectedTransaction);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Transaction validée");
            loadTransactionsSuspectes();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la validation : " + e.getMessage());
        }
    }

    @FXML
    private void handleRejeterSuspecte() {
        TransactionDTO selectedTransaction = tableTransactionsSuspectes.getSelectionModel().getSelectedItem();
        if (selectedTransaction == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Veuillez sélectionner une transaction suspecte");
            return;
        }

        try {
            transactionService.annulerTransaction(selectedTransaction.getId());
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Transaction rejetée");
            loadTransactionsSuspectes();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du rejet : " + e.getMessage());
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
            if (choiceCompteSource.getValue() == null || choiceCompteDestination.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez sélectionner les comptes source et destination");
                return false;
            }

            if (choiceCompteSource.getValue().getId().equals(choiceCompteDestination.getValue().getId())) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Les comptes source et destination doivent être différents");
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
    }

    private void loadData() {
        try {
            // Charger les comptes
            List<CompteDTO> comptes = compteService.getAllComptes();
            choiceCompteSource.setItems(FXCollections.observableArrayList(comptes));
            choiceCompteDestination.setItems(FXCollections.observableArrayList(comptes));

            loadTransactions();
            loadTransactionsSuspectes();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les données");
        }
    }

    private void loadTransactions() {
        try {
            List<TransactionDTO> transactions = transactionService.getAllTransactions();
            ObservableList<TransactionDTO> transactionsData = FXCollections.observableArrayList(transactions);
            tableTransactions.setItems(transactionsData);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les transactions");
        }
    }

    private void loadTransactionsSuspectes() {
        try {
            List<TransactionDTO> transactionsSuspectes = transactionService.getTransactionsSuspectes();
            ObservableList<TransactionDTO> suspectesData = FXCollections.observableArrayList(transactionsSuspectes);
            tableTransactionsSuspectes.setItems(suspectesData);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les transactions suspectes");
        }
    }

    // Navigation methods
    @FXML private void handleDashboard() { navigateToPage("UI_Dashboard"); }
    @FXML private void handleGestionClients() { navigateToPage("UI_Gestion_Clients"); }
    @FXML private void handleGestionComptes() { navigateToPage("UI_Gestion_Comptes"); }
    @FXML private void handleGestionCredits() { navigateToPage("UI_Gestion_Credits"); }
    @FXML private void handleGestionCartes() { navigateToPage("UI_Gestion_Cartes_Bancaires"); }

    @FXML
    private void handleLogout() {
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/groupeisi/minisystemebancaire/admin/" + pageName + ".fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) btnEnregistrerTransaction.getScene().getWindow();
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