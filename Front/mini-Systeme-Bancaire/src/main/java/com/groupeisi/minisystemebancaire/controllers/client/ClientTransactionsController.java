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
import java.time.LocalDateTime;
import java.util.List;

public class ClientTransactionsController {

    @FXML private ComboBox<CompteDTO> cmbCompteSource;
    @FXML private ComboBox<CompteDTO> cmbCompteDestination;
    @FXML private TextField txtMontant;
    @FXML private TextArea txtDescription;
    @FXML private ComboBox<String> cmbTypeTransaction;
    @FXML private Button btnEffectuer;
    @FXML private Button btnRetour;
    @FXML private TableView<TransactionDTO> tableTransactions;
    @FXML private TableColumn<TransactionDTO, String> colType;
    @FXML private TableColumn<TransactionDTO, Double> colMontant;
    @FXML private TableColumn<TransactionDTO, String> colDate;
    @FXML private TableColumn<TransactionDTO, String> colStatut;
    @FXML private Label lblMessage;

    private final CompteService compteService = new CompteService();
    private final TransactionService transactionService = new TransactionService();
    private ClientDTO currentClient;

    @FXML
    public void initialize() {
        currentClient = SessionManager.getCurrentClient();

        if (currentClient == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Session expirée");
            redirectToLogin();
            return;
        }

        setupUI();
        setupTableColumns();
        loadClientComptes();
        loadTransactions();
    }

    private void setupUI() {
        // Configuration des types de transaction
        cmbTypeTransaction.setItems(FXCollections.observableArrayList(
                "Dépôt", "Retrait", "Virement"
        ));

        // Gestionnaire de changement de type
        cmbTypeTransaction.setOnAction(e -> {
            String type = cmbTypeTransaction.getValue();
            cmbCompteDestination.setVisible("Virement".equals(type));
            cmbCompteDestination.setManaged("Virement".equals(type));
        });

        // Configuration des boutons
        btnEffectuer.setOnAction(this::handleEffectuerTransaction);
        btnRetour.setOnAction(this::handleRetour);

        lblMessage.setText("");
    }

    private void setupTableColumns() {
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Formatage du montant
        colMontant.setCellFactory(column -> new TableCell<TransactionDTO, Double>() {
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

        // Formatage du statut
        colStatut.setCellFactory(column -> new TableCell<TransactionDTO, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Validé".equals(item)) {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else if ("Rejeté".equals(item)) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    private void loadClientComptes() {
        Thread loadThread = new Thread(() -> {
            try {
                List<CompteDTO> comptes = compteService.getComptesByClientId(currentClient.getId());

                Platform.runLater(() -> {
                    ObservableList<CompteDTO> comptesData = FXCollections.observableArrayList(
                            comptes.stream().filter(CompteDTO::isActif).toList()
                    );

                    cmbCompteSource.setItems(comptesData);
                    cmbCompteDestination.setItems(comptesData);

                    // Configuration de l'affichage des comptes
                    cmbCompteSource.setCellFactory(lv -> new CompteListCell());
                    cmbCompteSource.setButtonCell(new CompteListCell());
                    cmbCompteDestination.setCellFactory(lv -> new CompteListCell());
                    cmbCompteDestination.setButtonCell(new CompteListCell());
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Erreur",
                            "Impossible de charger les comptes: " + e.getMessage());
                });
            }
        });

        loadThread.setDaemon(true);
        loadThread.start();
    }

    private void loadTransactions() {
        Thread loadThread = new Thread(() -> {
            try {
                List<TransactionDTO> transactions = transactionService.getTransactionsByClient(currentClient.getId());

                Platform.runLater(() -> {
                    ObservableList<TransactionDTO> transactionsData = FXCollections.observableArrayList(transactions);
                    tableTransactions.setItems(transactionsData);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    System.err.println("❌ Erreur lors du chargement des transactions: " + e.getMessage());
                });
            }
        });

        loadThread.setDaemon(true);
        loadThread.start();
    }

    @FXML
    private void handleEffectuerTransaction(ActionEvent event) {
        if (!validateForm()) {
            return;
        }

        String type = cmbTypeTransaction.getValue();
        CompteDTO compteSource = cmbCompteSource.getValue();
        CompteDTO compteDestination = cmbCompteDestination.getValue();
        double montant = Double.parseDouble(txtMontant.getText());
        String description = txtDescription.getText().trim();

        btnEffectuer.setDisable(true);
        btnEffectuer.setText("Traitement...");

        Thread transactionThread = new Thread(() -> {
            try {
                TransactionDTO transaction = new TransactionDTO();
                transaction.setType(type);
                transaction.setMontant(montant);
                transaction.setDescription(description);
                transaction.setCompteSourceId(compteSource.getId());

                if ("Virement".equals(type) && compteDestination != null) {
                    transaction.setCompteDestId(compteDestination.getId());
                }

                transaction.setDate(LocalDateTime.now());
                transaction.setStatut("En attente");

                TransactionDTO result = transactionService.createTransaction(transaction);

                Platform.runLater(() -> {
                    if (result != null) {
                        showMessage("Transaction effectuée avec succès !", "success");
                        clearForm();
                        loadTransactions();
                        loadClientComptes(); // Recharger pour mettre à jour les soldes
                    } else {
                        showMessage("Erreur lors de la transaction", "error");
                    }
                    resetButton();
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showMessage("Erreur: " + e.getMessage(), "error");
                    resetButton();
                });
            }
        });

        transactionThread.setDaemon(true);
        transactionThread.start();
    }

    private boolean validateForm() {
        if (cmbTypeTransaction.getValue() == null) {
            showMessage("Veuillez sélectionner un type de transaction", "error");
            return false;
        }

        if (cmbCompteSource.getValue() == null) {
            showMessage("Veuillez sélectionner un compte source", "error");
            return false;
        }

        if (txtMontant.getText().trim().isEmpty()) {
            showMessage("Veuillez saisir un montant", "error");
            return false;
        }

        try {
            double montant = Double.parseDouble(txtMontant.getText());
            if (montant <= 0) {
                showMessage("Le montant doit être positif", "error");
                return false;
            }
        } catch (NumberFormatException e) {
            showMessage("Montant invalide", "error");
            return false;
        }

        if ("Virement".equals(cmbTypeTransaction.getValue()) && cmbCompteDestination.getValue() == null) {
            showMessage("Veuillez sélectionner un compte de destination", "error");
            return false;
        }

        return true;
    }

    private void clearForm() {
        cmbTypeTransaction.setValue(null);
        cmbCompteSource.setValue(null);
        cmbCompteDestination.setValue(null);
        txtMontant.clear();
        txtDescription.clear();
    }

    private void resetButton() {
        btnEffectuer.setDisable(false);
        btnEffectuer.setText("Effectuer Transaction");
    }

    @FXML
    private void handleRetour(ActionEvent event) {
        navigateTo("/com/groupeisi/minisystemebancaire/client/UI_Dashboard.fxml", event);
    }

    private void redirectToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/groupeisi/minisystemebancaire/UI_Main.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) lblMessage.getScene().getWindow();
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

    private void showMessage(String message, String type) {
        lblMessage.setText(message);
        lblMessage.setStyle(type.equals("error") ?
                "-fx-text-fill: #e74c3c; -fx-font-weight: bold;" :
                "-fx-text-fill: #27ae60; -fx-font-weight: bold;");
    }

    // Classe pour afficher les comptes dans les ComboBox
    private static class CompteListCell extends ListCell<CompteDTO> {
        @Override
        protected void updateItem(CompteDTO item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(item.getNumero() + " - " + item.getType() + " (" + item.getSoldeFormate() + ")");
            }
        }
    }
}