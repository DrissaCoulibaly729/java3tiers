package com.groupeisi.minisystemebancaire.controllers.admin;

import com.groupeisi.minisystemebancaire.dto.TransactionDTO;
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
import java.time.LocalDate;
import java.util.List;

public class AdminTransactionsController {

    @FXML private TableView<TransactionDTO> tableTransactions;
    @FXML private TableColumn<TransactionDTO, String> colType;
    @FXML private TableColumn<TransactionDTO, Double> colMontant;
    @FXML private TableColumn<TransactionDTO, String> colDate;
    @FXML private TableColumn<TransactionDTO, String> colStatut;
    @FXML private TableColumn<TransactionDTO, String> colCompteSource;
    @FXML private TableColumn<TransactionDTO, String> colCompteDestination;
    @FXML private ComboBox<String> cmbStatutFiltre;
    @FXML private ComboBox<String> cmbTypeFiltre;
    @FXML private DatePicker dateDebut;
    @FXML private DatePicker dateFin;
    @FXML private TextField txtMontantMin;
    @FXML private TextField txtMontantMax;
    @FXML private Button btnFiltrer;
    @FXML private Button btnReinitialiser;
    @FXML private Button btnValider;
    @FXML private Button btnRejeter;
    @FXML private Button btnAnnuler;
    @FXML private Button btnExporter;
    @FXML private Label lblMessage;
    @FXML private Label lblNombreTransactions;

    private final TransactionService transactionService = new TransactionService();
    private TransactionDTO selectedTransaction;
    private List<TransactionDTO> allTransactions;

    @FXML
    public void initialize() {
        if (!SessionManager.isAdminLoggedIn()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Accès non autorisé");
            return;
        }

        setupUI();
        setupTableColumns();
        loadTransactions();
    }

    private void setupUI() {
        // Configuration des filtres
        cmbStatutFiltre.setItems(FXCollections.observableArrayList(
                "Tous", "Validé", "Rejeté", "En attente"
        ));
        cmbStatutFiltre.setValue("Tous");

        cmbTypeFiltre.setItems(FXCollections.observableArrayList(
                "Tous", "Dépôt", "Retrait", "Virement"
        ));
        cmbTypeFiltre.setValue("Tous");

        // Configuration des boutons
        btnFiltrer.setOnAction(this::handleFiltrer);
        btnReinitialiser.setOnAction(this::handleReinitialiser);
        btnValider.setOnAction(this::handleValider);
        btnRejeter.setOnAction(this::handleRejeter);
        btnAnnuler.setOnAction(this::handleAnnuler);
        btnExporter.setOnAction(this::handleExporter);

        // Sélection de transaction
        tableTransactions.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedTransaction = newSelection;
            updateButtonStates();
        });

        // Dates par défaut (dernier mois)
        dateFin.setValue(LocalDate.now());
        dateDebut.setValue(LocalDate.now().minusMonths(1));

        lblMessage.setText("");
        updateButtonStates();
    }

    @FXML
    private void handleGestionCartes() {
        navigateToPage("UI_Gestion_Cartes");
    }

    @FXML
    private void handleGestionSupport() {
        navigateToPage("UI_Service_Client_Rapports");
    }

    private void setupTableColumns() {
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Colonnes des comptes
        colCompteSource.setCellValueFactory(cellData -> {
            TransactionDTO transaction = cellData.getValue();
            if (transaction.getCompteSource() != null) {
                return new javafx.beans.property.SimpleStringProperty(transaction.getCompteSource().getNumero());
            } else {
                return new javafx.beans.property.SimpleStringProperty("Compte ID: " + transaction.getCompteSourceId());
            }
        });

        colCompteDestination.setCellValueFactory(cellData -> {
            TransactionDTO transaction = cellData.getValue();
            if (transaction.getCompteDestination() != null) {
                return new javafx.beans.property.SimpleStringProperty(transaction.getCompteDestination().getNumero());
            } else if (transaction.getCompteDestId() != null) {
                return new javafx.beans.property.SimpleStringProperty("Compte ID: " + transaction.getCompteDestId());
            } else {
                return new javafx.beans.property.SimpleStringProperty("-");
            }
        });

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
                    } else if ("En attente".equals(item)) {
                        setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
    }

    private void loadTransactions() {
        Thread loadThread = new Thread(() -> {
            try {
                allTransactions = transactionService.getAllTransactions();

                Platform.runLater(() -> {
                    ObservableList<TransactionDTO> transactionsData = FXCollections.observableArrayList(allTransactions);
                    tableTransactions.setItems(transactionsData);
                    updateTransactionCount(allTransactions.size());
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Erreur",
                            "Impossible de charger les transactions: " + e.getMessage());
                });
            }
        });

        loadThread.setDaemon(true);
        loadThread.start();
    }

    @FXML
    private void handleFiltrer(ActionEvent event) {
        if (allTransactions == null) {
            return;
        }

        String statutFiltre = cmbStatutFiltre.getValue();
        String typeFiltre = cmbTypeFiltre.getValue();
        LocalDate debut = dateDebut.getValue();
        LocalDate fin = dateFin.getValue();

        double montantMin = 0;
        double montantMax = Double.MAX_VALUE;

        try {
            if (!txtMontantMin.getText().trim().isEmpty()) {
                montantMin = Double.parseDouble(txtMontantMin.getText());
            }
            if (!txtMontantMax.getText().trim().isEmpty()) {
                montantMax = Double.parseDouble(txtMontantMax.getText());
            }
        } catch (NumberFormatException e) {
            showMessage("Montants de filtrage invalides", "error");
            return;
        }

        final double finalMontantMin = montantMin;
        final double finalMontantMax = montantMax;

        List<TransactionDTO> filteredTransactions = allTransactions.stream()
                .filter(t -> "Tous".equals(statutFiltre) || statutFiltre.equals(t.getStatut()))
                .filter(t -> "Tous".equals(typeFiltre) || typeFiltre.equals(t.getType()))
                .filter(t -> debut == null || t.getDate().toLocalDate().isAfter(debut.minusDays(1)))
                .filter(t -> fin == null || t.getDate().toLocalDate().isBefore(fin.plusDays(1)))
                .filter(t -> t.getMontant() >= finalMontantMin && t.getMontant() <= finalMontantMax)
                .toList();

        ObservableList<TransactionDTO> filteredData = FXCollections.observableArrayList(filteredTransactions);
        tableTransactions.setItems(filteredData);
        updateTransactionCount(filteredTransactions.size());

        showMessage(filteredTransactions.size() + " transaction(s) trouvée(s)", "success");
    }

    @FXML
    private void handleReinitialiser(ActionEvent event) {
        cmbStatutFiltre.setValue("Tous");
        cmbTypeFiltre.setValue("Tous");
        dateDebut.setValue(LocalDate.now().minusMonths(1));
        dateFin.setValue(LocalDate.now());
        txtMontantMin.clear();
        txtMontantMax.clear();

        if (allTransactions != null) {
            ObservableList<TransactionDTO> allData = FXCollections.observableArrayList(allTransactions);
            tableTransactions.setItems(allData);
            updateTransactionCount(allTransactions.size());
        }

        showMessage("Filtres réinitialisés", "success");
    }

    @FXML
    private void handleValider(ActionEvent event) {
        if (selectedTransaction == null) {
            showMessage("Veuillez sélectionner une transaction", "error");
            return;
        }

        if (!"En attente".equals(selectedTransaction.getStatut())) {
            showMessage("Seules les transactions en attente peuvent être validées", "error");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Validation de transaction");
        confirmation.setHeaderText("Valider la transaction");
        confirmation.setContentText("Êtes-vous sûr de vouloir valider cette transaction ?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Thread validationThread = new Thread(() -> {
                    try {
                        transactionService.validerTransaction(selectedTransaction.getId());

                        Platform.runLater(() -> {
                            showMessage("Transaction validée avec succès !", "success");
                            loadTransactions();
                        });

                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            showMessage("Erreur: " + e.getMessage(), "error");
                        });
                    }
                });

                validationThread.setDaemon(true);
                validationThread.start();
            }
        });
    }

    @FXML
    private void handleRejeter(ActionEvent event) {
        if (selectedTransaction == null) {
            showMessage("Veuillez sélectionner une transaction", "error");
            return;
        }

        if (!"En attente".equals(selectedTransaction.getStatut())) {
            showMessage("Seules les transactions en attente peuvent être rejetées", "error");
            return;
        }

        // Dialog pour saisir le motif de rejet
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Rejet de transaction");
        dialog.setHeaderText("Rejeter la transaction");
        dialog.setContentText("Motif du rejet:");

        dialog.showAndWait().ifPresent(motif -> {
            if (!motif.trim().isEmpty()) {
                Thread rejetThread = new Thread(() -> {
                    try {
                        transactionService.rejeterTransaction(selectedTransaction.getId(), motif);

                        Platform.runLater(() -> {
                            showMessage("Transaction rejetée avec succès !", "success");
                            loadTransactions();
                        });

                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            showMessage("Erreur: " + e.getMessage(), "error");
                        });
                    }
                });

                rejetThread.setDaemon(true);
                rejetThread.start();
            }
        });
    }

    @FXML
    private void handleAnnuler(ActionEvent event) {
        if (selectedTransaction == null) {
            showMessage("Veuillez sélectionner une transaction", "error");
            return;
        }

        if ("Rejeté".equals(selectedTransaction.getStatut())) {
            showMessage("Une transaction déjà rejetée ne peut pas être annulée", "error");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Annulation de transaction");
        confirmation.setHeaderText("Annuler la transaction");
        confirmation.setContentText("Êtes-vous sûr de vouloir annuler cette transaction ? Cette action est irréversible.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Thread annulationThread = new Thread(() -> {
                    try {
                        transactionService.annulerTransaction(selectedTransaction.getId());

                        Platform.runLater(() -> {
                            showMessage("Transaction annulée avec succès !", "success");
                            loadTransactions();
                        });

                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            showMessage("Erreur: " + e.getMessage(), "error");
                        });
                    }
                });

                annulationThread.setDaemon(true);
                annulationThread.start();
            }
        });
    }

    @FXML
    private void handleExporter(ActionEvent event) {
        showMessage("Fonctionnalité d'export en cours de développement", "info");
        // TODO: Implémenter l'export vers Excel/PDF
    }

    @FXML
    private void handleVoirTransactionsSuspectes(ActionEvent event) {
        Thread suspectesThread = new Thread(() -> {
            try {
                List<TransactionDTO> transactionsSuspectes = transactionService.getTransactionsSuspectes();

                Platform.runLater(() -> {
                    ObservableList<TransactionDTO> suspectesData = FXCollections.observableArrayList(transactionsSuspectes);
                    tableTransactions.setItems(suspectesData);
                    updateTransactionCount(transactionsSuspectes.size());
                    showMessage(transactionsSuspectes.size() + " transaction(s) suspecte(s) trouvée(s)", "warning");
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showMessage("Erreur lors du chargement des transactions suspectes: " + e.getMessage(), "error");
                });
            }
        });

        suspectesThread.setDaemon(true);
        suspectesThread.start();
    }

    private void updateButtonStates() {
        boolean hasSelection = selectedTransaction != null;
        boolean canValidate = hasSelection && "En attente".equals(selectedTransaction.getStatut());
        boolean canReject = hasSelection && "En attente".equals(selectedTransaction.getStatut());
        boolean canCancel = hasSelection && !"Rejeté".equals(selectedTransaction.getStatut());

        btnValider.setDisable(!canValidate);
        btnRejeter.setDisable(!canReject);
        btnAnnuler.setDisable(!canCancel);
    }

    private void updateTransactionCount(int count) {
        lblNombreTransactions.setText("Total: " + count + " transaction(s)");
    }

    // Navigation methods
    @FXML private void handleDashboard() { navigateToPage("UI_Dashboard"); }
    @FXML private void handleGestionClients() { navigateToPage("UI_Gestion_Clients"); }
    @FXML private void handleGestionComptes() { navigateToPage("UI_Gestion_Comptes"); }
    @FXML private void handleGestionCredits() { navigateToPage("UI_Gestion_Credits"); }
    @FXML private void handleServiceClient() { navigateToPage("UI_Service_Client_Rapports"); }
    @FXML private void handleDeconnexion() {
        SessionManager.logout();
        navigateToPage("../UI_Main");
    }

    private void navigateToPage(String page) {
        try {
            String path = "/com/groupeisi/minisystemebancaire/admin/" + page + ".fxml";
            if (page.startsWith("../")) {
                path = "/com/groupeisi/minisystemebancaire/" + page.substring(3) + ".fxml";
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();

            Stage stage = (Stage) lblMessage.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de naviguer vers " + page);
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
            case "warning":
                style = "-fx-text-fill: #f39c12; -fx-font-weight: bold;";
                break;
            case "info":
                style = "-fx-text-fill: #3498db; -fx-font-weight: bold;";
                break;
            default:
                style = "";
                break;
        }
        lblMessage.setStyle(style);
    }
}