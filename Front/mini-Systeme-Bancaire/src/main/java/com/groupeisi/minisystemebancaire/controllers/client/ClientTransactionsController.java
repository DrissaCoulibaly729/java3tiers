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
        // Configuration des types de transaction - FORMAT ATTENDU PAR LARAVEL
        cmbTypeTransaction.setItems(FXCollections.observableArrayList(
                "Dépôt", "Retrait", "Virement"  // CORRIGÉ : Format Laravel avec accents
        ));

        // Gestionnaire de changement de type
        cmbTypeTransaction.setOnAction(e -> {
            String type = cmbTypeTransaction.getValue();
            boolean isVirement = "Virement".equals(type);
            cmbCompteDestination.setVisible(isVirement);
            cmbCompteDestination.setManaged(isVirement);

            // Afficher des conseils selon le type
            if (type != null) {
                switch (type) {
                    case "Dépôt":
                        showMessage("💰 Ajouter de l'argent sur votre compte", "info");
                        break;
                    case "Retrait":
                        showMessage("💸 Retirer de l'argent de votre compte", "info");
                        break;
                    case "Virement":
                        showMessage("🔄 Transférer de l'argent entre vos comptes", "info");
                        break;
                }
            }
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

        // Formatage du montant avec couleurs selon le type
        colMontant.setCellFactory(column -> new TableCell<TransactionDTO, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.2f FCFA", item));

                    // Couleur selon le type de transaction
                    TransactionDTO transaction = getTableRow().getItem();
                    if (transaction != null) {
                        switch (transaction.getType()) {
                            case "Dépôt":
                                setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                                break;
                            case "Retrait":
                                setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                                break;
                            case "Virement":
                                setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
                                break;
                            default:
                                setStyle("");
                        }
                    }
                }
            }
        });

        // Formatage du statut avec icônes
        colStatut.setCellFactory(column -> new TableCell<TransactionDTO, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    String displayText;
                    String style;

                    switch (item.toLowerCase()) {
                        case "validé":
                        case "completee":
                            displayText = "✅ Validé";
                            style = "-fx-text-fill: #27ae60; -fx-font-weight: bold;";
                            break;
                        case "en attente":
                            displayText = "⏳ En attente";
                            style = "-fx-text-fill: #f39c12; -fx-font-weight: bold;";
                            break;
                        case "rejeté":
                        case "echouee":
                            displayText = "❌ Rejeté";
                            style = "-fx-text-fill: #e74c3c; -fx-font-weight: bold;";
                            break;
                        case "bloqué":
                            displayText = "🚫 Bloqué";
                            style = "-fx-text-fill: #8B0000; -fx-font-weight: bold;";
                            break;
                        default:
                            displayText = item;
                            style = "";
                    }

                    setText(displayText);
                    setStyle(style);
                }
            }
        });

        // Formatage du type avec icônes
        colType.setCellFactory(column -> new TableCell<TransactionDTO, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String icon = switch (item) {
                        case "Dépôt" -> "💰 Dépôt";
                        case "Retrait" -> "💸 Retrait";
                        case "Virement" -> "🔄 Virement";
                        default -> item;
                    };
                    setText(icon);
                }
            }
        });
    }

    private void loadClientComptes() {
        Thread loadThread = new Thread(() -> {
            try {
                List<CompteDTO> comptes = compteService.getComptesByClientId(currentClient.getId());

                Platform.runLater(() -> {
                    // Filtrer seulement les comptes actifs
                    List<CompteDTO> comptesActifs = comptes.stream()
                            .filter(compte -> compte != null && "Actif".equals(compte.getStatut()))
                            .toList();

                    ObservableList<CompteDTO> comptesData = FXCollections.observableArrayList(comptesActifs);

                    cmbCompteSource.setItems(comptesData);
                    cmbCompteDestination.setItems(comptesData);

                    // Configuration de l'affichage des comptes
                    cmbCompteSource.setCellFactory(lv -> new CompteListCell());
                    cmbCompteSource.setButtonCell(new CompteListCell());
                    cmbCompteDestination.setCellFactory(lv -> new CompteListCell());
                    cmbCompteDestination.setButtonCell(new CompteListCell());

                    System.out.println("✅ Comptes client chargés: " + comptesActifs.size());
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Erreur",
                            "Impossible de charger les comptes: " + e.getMessage());
                    System.err.println("❌ Erreur chargement comptes client: " + e.getMessage());
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
                    System.out.println("✅ Transactions client chargées: " + transactions.size());
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    System.err.println("❌ Erreur lors du chargement des transactions: " + e.getMessage());
                    showMessage("Erreur lors du chargement des transactions", "error");
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

        // Vérifications bancaires côté client
        if (!performClientValidation(type, montant, compteSource, compteDestination)) {
            return;
        }

        // Confirmation de l'utilisateur
        String confirmationMessage = buildConfirmationMessage(type, montant, compteSource, compteDestination);
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de transaction");
        confirmation.setHeaderText("Confirmer votre " + type.toLowerCase());
        confirmation.setContentText(confirmationMessage);

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        // Désactiver le bouton pendant le traitement
        btnEffectuer.setDisable(true);
        btnEffectuer.setText("Traitement...");

        Thread transactionThread = new Thread(() -> {
            try {
                TransactionDTO transaction = new TransactionDTO();
                transaction.setType(type);
                transaction.setMontant(montant);
                transaction.setDescription(description);

                // CONFIGURATION SELON LE TYPE DE TRANSACTION (logique Laravel)
                switch (type) {
                    case "Dépôt":
                        // Pour un dépôt, le compte destination reçoit l'argent
                        transaction.setCompteDestId(compteSource.getId());
                        break;

                    case "Retrait":
                        // Pour un retrait, l'argent sort du compte source
                        transaction.setCompteSourceId(compteSource.getId());
                        break;

                    case "Virement":
                        // Pour un virement, transfert du source vers destination
                        transaction.setCompteSourceId(compteSource.getId());
                        transaction.setCompteDestId(compteDestination.getId());
                        break;
                }

                transaction.setStatut("En attente"); // Les clients créent des transactions en attente

                TransactionDTO result = transactionService.createTransaction(transaction);

                Platform.runLater(() -> {
                    if (result != null) {
                        String successMessage = String.format(
                                "✅ %s de %.2f FCFA effectué avec succès!\n\n" +
                                        "Votre transaction est en attente de validation.\n" +
                                        "Numéro de transaction: %s",
                                type, montant, result.getId()
                        );

                        showMessage("Transaction effectuée avec succès !", "success");
                        showAlert(Alert.AlertType.INFORMATION, "Succès", successMessage);
                        clearForm();
                        loadTransactions();
                        loadClientComptes(); // Recharger pour mettre à jour les soldes
                    } else {
                        showMessage("Erreur lors de la transaction", "error");
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la création de transaction");
                    }
                    resetButton();
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    String errorMessage = "Erreur: " + e.getMessage();
                    showMessage(errorMessage, "error");
                    showAlert(Alert.AlertType.ERROR, "Erreur de transaction", errorMessage);
                    resetButton();
                });
                System.err.println("❌ Erreur transaction: " + e.getMessage());
                e.printStackTrace();
            }
        });

        transactionThread.setDaemon(true);
        transactionThread.start();
    }

    private boolean validateForm() {
        if (cmbTypeTransaction.getValue() == null) {
            showMessage("❌ Veuillez sélectionner un type de transaction", "error");
            return false;
        }

        if (cmbCompteSource.getValue() == null) {
            showMessage("❌ Veuillez sélectionner un compte", "error");
            return false;
        }

        if (txtMontant.getText().trim().isEmpty()) {
            showMessage("❌ Veuillez saisir un montant", "error");
            return false;
        }

        try {
            double montant = Double.parseDouble(txtMontant.getText());
            if (montant <= 0) {
                showMessage("❌ Le montant doit être positif", "error");
                return false;
            }
            if (montant > 5000000) { // Limite client 5M
                showMessage("❌ Montant trop élevé (max: 5,000,000 FCFA)", "error");
                return false;
            }
        } catch (NumberFormatException e) {
            showMessage("❌ Montant invalide - Utilisez seulement des chiffres", "error");
            return false;
        }

        if ("Virement".equals(cmbTypeTransaction.getValue()) && cmbCompteDestination.getValue() == null) {
            showMessage("❌ Veuillez sélectionner un compte de destination", "error");
            return false;
        }

        if ("Virement".equals(cmbTypeTransaction.getValue()) &&
                cmbCompteSource.getValue().getId().equals(cmbCompteDestination.getValue().getId())) {
            showMessage("❌ Les comptes source et destination doivent être différents", "error");
            return false;
        }

        return true;
    }

    private boolean performClientValidation(String type, double montant, CompteDTO compteSource, CompteDTO compteDestination) {
        // Vérifications supplémentaires selon le type
        switch (type) {
            case "Retrait":
                if (compteSource.getSolde() < montant) {
                    showAlert(Alert.AlertType.ERROR, "Solde insuffisant",
                            String.format("Solde disponible: %.2f FCFA\nMontant demandé: %.2f FCFA\n\n" +
                                            "Veuillez réduire le montant ou effectuer un dépôt.",
                                    compteSource.getSolde(), montant));
                    return false;
                }
                if (montant > 200000) { // Limite retrait client 200k
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Retrait important");
                    alert.setHeaderText("Montant élevé détecté");
                    alert.setContentText("Retrait supérieur à 200,000 FCFA.\nContinuer?");
                    if (alert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                        return false;
                    }
                }
                break;

            case "Dépôt":
                if (montant > 500000) { // Limite dépôt client 500k
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Dépôt important");
                    alert.setHeaderText("Montant élevé détecté");
                    alert.setContentText("Dépôt supérieur à 500,000 FCFA.\nContinuer?");
                    if (alert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                        return false;
                    }
                }
                break;

            case "Virement":
                if (compteSource.getSolde() < montant) {
                    showAlert(Alert.AlertType.ERROR, "Solde insuffisant",
                            String.format("Solde disponible: %.2f FCFA\nMontant demandé: %.2f FCFA",
                                    compteSource.getSolde(), montant));
                    return false;
                }
                break;
        }

        return true;
    }

    private String buildConfirmationMessage(String type, double montant, CompteDTO compteSource, CompteDTO compteDestination) {
        StringBuilder message = new StringBuilder();

        switch (type) {
            case "Dépôt":
                message.append("💰 DÉPÔT\n\n");
                message.append(String.format("Montant: %.2f FCFA\n", montant));
                message.append(String.format("Sur le compte: %s\n", compteSource.getNumero()));
                message.append(String.format("Nouveau solde prévu: %.2f FCFA", compteSource.getSolde() + montant));
                break;

            case "Retrait":
                message.append("💸 RETRAIT\n\n");
                message.append(String.format("Montant: %.2f FCFA\n", montant));
                message.append(String.format("Du compte: %s\n", compteSource.getNumero()));
                message.append(String.format("Nouveau solde prévu: %.2f FCFA", compteSource.getSolde() - montant));
                break;

            case "Virement":
                message.append("🔄 VIREMENT\n\n");
                message.append(String.format("Montant: %.2f FCFA\n", montant));
                message.append(String.format("De: %s (%.2f FCFA)\n", compteSource.getNumero(), compteSource.getSolde()));
                message.append(String.format("Vers: %s (%.2f FCFA)\n\n", compteDestination.getNumero(), compteDestination.getSolde()));
                message.append("Nouveaux soldes prévus:\n");
                message.append(String.format("Source: %.2f FCFA\n", compteSource.getSolde() - montant));
                message.append(String.format("Destination: %.2f FCFA", compteDestination.getSolde() + montant));
                break;
        }

        message.append("\n\n⚠️ Transaction en attente de validation");
        return message.toString();
    }

    private void clearForm() {
        cmbTypeTransaction.setValue(null);
        cmbCompteSource.setValue(null);
        cmbCompteDestination.setValue(null);
        txtMontant.clear();
        txtDescription.clear();
        showMessage("Formulaire réinitialisé", "info");
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
        String style;
        switch (type) {
            case "error":
                style = "-fx-text-fill: #e74c3c; -fx-font-weight: bold;";
                break;
            case "success":
                style = "-fx-text-fill: #27ae60; -fx-font-weight: bold;";
                break;
            case "info":
                style = "-fx-text-fill: #3498db; -fx-font-weight: bold;";
                break;
            default:
                style = "";
        }
        lblMessage.setStyle(style);
    }

    // Classe pour afficher les comptes dans les ComboBox
    private static class CompteListCell extends ListCell<CompteDTO> {
        @Override
        protected void updateItem(CompteDTO item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(String.format("%s - %s (%.2f FCFA)",
                        item.getNumero(),
                        item.getType(),
                        item.getSolde()));
            }
        }
    }
}