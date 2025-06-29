package com.groupeisi.minisystemebancaire.controllers.admin;

import com.groupeisi.minisystemebancaire.dto.TransactionDTO;
import com.groupeisi.minisystemebancaire.dto.CompteDTO;
import com.groupeisi.minisystemebancaire.services.TransactionService;
import com.groupeisi.minisystemebancaire.services.CompteService;
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

public class AdminTransactionsController {

    // === ÉLÉMENTS FXML ===
    @FXML private TextField txtRechercheTransaction;
    @FXML private TextField txtMontantTransaction;
    @FXML private ChoiceBox<String> choiceTypeTransaction;
    @FXML private ChoiceBox<CompteDTO> choiceCompteSource;
    @FXML private ChoiceBox<CompteDTO> choiceCompteDest;

    @FXML private TableView<TransactionDTO> tableTransactions;
    @FXML private TableColumn<TransactionDTO, Integer> colIdTransaction;
    @FXML private TableColumn<TransactionDTO, String> colType;
    @FXML private TableColumn<TransactionDTO, Double> colMontant;
    @FXML private TableColumn<TransactionDTO, String> colCompteSource;
    @FXML private TableColumn<TransactionDTO, String> colCompteDest;
    @FXML private TableColumn<TransactionDTO, String> colStatut;
    @FXML private TableColumn<TransactionDTO, String> colDate;

    @FXML private Button btnValiderTransaction;
    @FXML private Button btnAnnulerTransaction;
    @FXML private Button btnRechercherTransaction;
    @FXML private Button btnBloquerTransaction;
    @FXML private Button btnDebloquerTransaction;

    // Navigation buttons
    @FXML private Button btnDashboard;
    @FXML private Button btnClients;
    @FXML private Button btnComptes;
    @FXML private Button btnCredits;
    @FXML private Button btnCartes;
    @FXML private Button btnSupport;
    @FXML private Button btnDeconnexion;

    // Variables pour afficher les messages (créé dynamiquement)
    private String currentMessage = "";
    private String currentMessageStyle = "";

    // Services
    private final TransactionService transactionService = new TransactionService();
    private final CompteService compteService = new CompteService();

    // Variables
    private List<TransactionDTO> allTransactions;
    private List<CompteDTO> allComptes;
    private TransactionDTO selectedTransaction;

    @FXML
    public void initialize() {
        if (!SessionManager.isAdminLoggedIn()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Accès non autorisé");
            return;
        }

        setupUI();
        setupTableColumns();
        loadData();
    }

    private void setupUI() {
        // Types de transaction avec logique bancaire - FORMAT ATTENDU PAR LARAVEL
        if (choiceTypeTransaction != null) {
            choiceTypeTransaction.setItems(FXCollections.observableArrayList(
                    "Dépôt", "Retrait", "Virement"  // CORRIGÉ : Format Laravel
            ));
            choiceTypeTransaction.setValue("Dépôt");

            // Gestion dynamique du compte destination
            choiceTypeTransaction.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (choiceCompteDest != null) {
                    boolean needDestination = "Virement".equals(newVal);
                    choiceCompteDest.setVisible(needDestination);
                    choiceCompteDest.setDisable(!needDestination);

                    // Message d'aide dans la console ou titre de fenêtre
                    if ("Dépôt".equals(newVal)) {
                        currentMessage = "💰 Dépôt : Ajouter de l'argent sur le compte";
                        System.out.println(currentMessage);
                    } else if ("Retrait".equals(newVal)) {
                        currentMessage = "💸 Retrait : Retirer de l'argent du compte";
                        System.out.println(currentMessage);
                    } else if ("Virement".equals(newVal)) {
                        currentMessage = "🔄 Virement : Transférer entre deux comptes";
                        System.out.println(currentMessage);
                    }
                }
            });
        }

        // Sélection dans la table
        if (tableTransactions != null) {
            tableTransactions.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                selectedTransaction = newSelection;
                updateButtonStates();
                displayTransactionDetails(newSelection);
            });
        }

        updateButtonStates();
    }

    private void setupTableColumns() {
        if (colIdTransaction != null) {
            colIdTransaction.setCellValueFactory(new PropertyValueFactory<>("id"));
        }
        if (colType != null) {
            colType.setCellValueFactory(new PropertyValueFactory<>("type"));
            // Formatage avec icônes
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

        if (colMontant != null) {
            colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
            colMontant.setCellFactory(column -> new TableCell<TransactionDTO, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.format("%.2f FCFA", item));
                        // Couleur selon le type
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
                            }
                        }
                    }
                }
            });
        }

        if (colStatut != null) {
            colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
            colStatut.setCellFactory(column -> new TableCell<TransactionDTO, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        switch (item.toLowerCase()) {
                            case "validé":
                            case "completee":
                                setText("✅ Validé");
                                setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                                break;
                            case "en attente":
                                setText("⏳ En attente");
                                setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                                break;
                            case "rejeté":
                            case "echouee":
                                setText("❌ Rejeté");
                                setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                                break;
                            case "bloqué":
                                setText("🚫 Bloqué");
                                setStyle("-fx-text-fill: #8B0000; -fx-font-weight: bold;");
                                break;
                            default:
                                setText(item);
                                setStyle("");
                        }
                    }
                }
            });
        }

        if (colDate != null) {
            colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        }

        // Colonnes de compte
        if (colCompteSource != null) {
            colCompteSource.setCellValueFactory(cellData -> {
                try {
                    TransactionDTO transaction = cellData.getValue();
                    if (transaction.getCompteSource() != null) {
                        return new javafx.beans.property.SimpleStringProperty(
                                "💳 " + transaction.getCompteSource().getNumero()
                        );
                    }
                    return new javafx.beans.property.SimpleStringProperty("N/A");
                } catch (Exception e) {
                    return new javafx.beans.property.SimpleStringProperty("Erreur");
                }
            });
        }

        if (colCompteDest != null) {
            colCompteDest.setCellValueFactory(cellData -> {
                try {
                    TransactionDTO transaction = cellData.getValue();
                    if ("Virement".equals(transaction.getType()) && transaction.getCompteDestination() != null) {
                        return new javafx.beans.property.SimpleStringProperty(
                                "💳 " + transaction.getCompteDestination().getNumero()
                        );
                    } else if (!"Virement".equals(transaction.getType())) {
                        return new javafx.beans.property.SimpleStringProperty("-");
                    }
                    return new javafx.beans.property.SimpleStringProperty("N/A");
                } catch (Exception e) {
                    return new javafx.beans.property.SimpleStringProperty("Erreur");
                }
            });
        }
    }

    private void loadData() {
        Thread loadThread = new Thread(() -> {
            try {
                // Chargement des transactions
                allTransactions = transactionService.getAllTransactions();

                // Chargement des comptes avec gestion d'erreur
                try {
                    allComptes = compteService.getAllComptes();
                    System.out.println("✅ Comptes chargés: " + allComptes.size());
                } catch (Exception e) {
                    System.err.println("❌ Erreur chargement comptes: " + e.getMessage());
                    allComptes = List.of(); // Liste vide en cas d'erreur
                }

                Platform.runLater(() -> {
                    // Mise à jour de la table des transactions
                    if (tableTransactions != null && allTransactions != null) {
                        ObservableList<TransactionDTO> transactionsData = FXCollections.observableArrayList(allTransactions);
                        tableTransactions.setItems(transactionsData);
                        System.out.println("✅ Transactions affichées: " + allTransactions.size());
                    }

                    // CONFIGURATION SIMPLE DES CHOICEBOX SANS CONVERTER
                    if (choiceCompteSource != null && allComptes != null && !allComptes.isEmpty()) {
                        // Filtrer seulement les comptes actifs
                        List<CompteDTO> comptesActifs = allComptes.stream()
                                .filter(c -> c != null && "Actif".equals(c.getStatut()))
                                .toList();

                        if (!comptesActifs.isEmpty()) {
                            ObservableList<CompteDTO> comptesData = FXCollections.observableArrayList(comptesActifs);
                            choiceCompteSource.setItems(comptesData);

                            // AFFICHAGE SIMPLE - override toString dans CompteDTO ou utilisation simple
                            System.out.println("✅ Comptes source chargés: " + comptesActifs.size());
                        } else {
                            System.out.println("⚠️ Aucun compte actif trouvé");
                        }
                    }

                    if (choiceCompteDest != null && allComptes != null && !allComptes.isEmpty()) {
                        List<CompteDTO> comptesActifs = allComptes.stream()
                                .filter(c -> c != null && "Actif".equals(c.getStatut()))
                                .toList();

                        if (!comptesActifs.isEmpty()) {
                            ObservableList<CompteDTO> comptesData = FXCollections.observableArrayList(comptesActifs);
                            choiceCompteDest.setItems(comptesData);
                            System.out.println("✅ Comptes destination chargés: " + comptesActifs.size());
                        }
                    }

                    System.out.println("📊 Total: " + (allTransactions != null ? allTransactions.size() : 0) + " transaction(s) chargées");
                });

            } catch (Exception e) {
                System.err.println("❌ Erreur générale lors du chargement: " + e.getMessage());
                e.printStackTrace();

                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Erreur",
                            "Impossible de charger les données.\n\n" +
                                    "Vérifiez que :\n" +
                                    "• Le serveur Laravel est démarré\n" +
                                    "• La base de données est accessible\n" +
                                    "• Les services fonctionnent\n\n" +
                                    "Erreur: " + e.getMessage());
                });
            }
        });

        loadThread.setDaemon(true);
        loadThread.start();
    }

    // === LOGIQUE BANCAIRE RÉELLE ===

    @FXML
    private void handleValiderTransaction(ActionEvent event) {
        if (!validateTransactionForm()) {
            return;
        }

        try {
            String type = choiceTypeTransaction.getValue();
            double montant = Double.parseDouble(txtMontantTransaction.getText().trim());
            CompteDTO compteSource = choiceCompteSource.getValue();
            CompteDTO compteDest = null;

            if ("Virement".equals(type)) {
                compteDest = choiceCompteDest.getValue();
                if (compteDest == null) {
                    showAlert(Alert.AlertType.WARNING, "Validation", "Compte destination requis pour un virement");
                    return;
                }
                if (compteSource.getId().equals(compteDest.getId())) {
                    showAlert(Alert.AlertType.WARNING, "Validation", "Les comptes source et destination doivent être différents");
                    return;
                }
            }

            // VÉRIFICATIONS BANCAIRES RÉELLES
            if (!performBankingValidation(type, montant, compteSource, compteDest)) {
                return;
            }

            // Confirmation de l'utilisateur
            String confirmationMessage = buildConfirmationMessage(type, montant, compteSource, compteDest);
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmation de transaction");
            confirmation.setHeaderText("Confirmer la transaction");
            confirmation.setContentText(confirmationMessage);

            if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                executeTransaction(type, montant, compteSource, compteDest);
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Montant invalide");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur: " + e.getMessage());
        }
    }

    private boolean validateTransactionForm() {
        if (choiceTypeTransaction == null || txtMontantTransaction == null || choiceCompteSource == null) {
            showAlert(Alert.AlertType.WARNING, "Interface", "Éléments de formulaire manquants");
            return false;
        }

        String type = choiceTypeTransaction.getValue();
        String montantText = txtMontantTransaction.getText().trim();
        CompteDTO compteSource = choiceCompteSource.getValue();

        // VALIDATION DÉTAILLÉE
        if (type == null || type.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "❌ Veuillez sélectionner un type de transaction");
            return false;
        }

        if (montantText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "❌ Veuillez saisir un montant");
            return false;
        }

        if (compteSource == null) {
            showAlert(Alert.AlertType.WARNING, "Validation", "❌ Veuillez sélectionner un compte source");
            return false;
        }

        // Validation du montant
        try {
            double montant = Double.parseDouble(montantText);
            if (montant <= 0) {
                showAlert(Alert.AlertType.WARNING, "Validation", "❌ Le montant doit être positif");
                return false;
            }
            if (montant > 10000000) { // 10 millions max
                showAlert(Alert.AlertType.WARNING, "Validation", "❌ Montant trop élevé (max: 10,000,000 FCFA)");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation", "❌ Montant invalide - Utilisez seulement des chiffres");
            return false;
        }

        // Validation spéciale pour les virements
        if ("Virement".equals(type)) {
            CompteDTO compteDest = choiceCompteDest != null ? choiceCompteDest.getValue() : null;
            if (compteDest == null) {
                showAlert(Alert.AlertType.WARNING, "Validation", "❌ Veuillez sélectionner un compte destination pour le virement");
                return false;
            }
        }

        return true;
    }

    private boolean performBankingValidation(String type, double montant, CompteDTO compteSource, CompteDTO compteDest) {
        // Vérification du compte source
        if (!"Actif".equals(compteSource.getStatut())) {
            showAlert(Alert.AlertType.ERROR, "Compte bloqué", "Le compte source est inactif");
            return false;
        }

        // Vérifications spécifiques par type
        switch (type) {
            case "Retrait":
                if (compteSource.getSolde() < montant) {
                    showAlert(Alert.AlertType.ERROR, "Solde insuffisant",
                            String.format("Solde actuel: %.2f FCFA\nMontant demandé: %.2f FCFA",
                                    compteSource.getSolde(), montant));
                    return false;
                }
                if (montant > 500000) { // Limite retrait 500k
                    showAlert(Alert.AlertType.WARNING, "Limite dépassée", "Montant maximum pour un retrait: 500,000 FCFA");
                    return false;
                }
                break;

            case "Dépôt":
                if (montant > 1000000) { // Limite dépôt 1M
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Dépôt important");
                    alert.setHeaderText("Montant élevé détecté");
                    alert.setContentText("Dépôt supérieur à 1,000,000 FCFA. Continuer?");
                    if (alert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                        return false;
                    }
                }
                break;

            case "Virement":
                if (compteDest == null) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Compte destination requis");
                    return false;
                }
                if (!"Actif".equals(compteDest.getStatut())) {
                    showAlert(Alert.AlertType.ERROR, "Compte bloqué", "Le compte destination est inactif");
                    return false;
                }
                if (compteSource.getSolde() < montant) {
                    showAlert(Alert.AlertType.ERROR, "Solde insuffisant",
                            String.format("Solde actuel: %.2f FCFA\nMontant demandé: %.2f FCFA",
                                    compteSource.getSolde(), montant));
                    return false;
                }
                break;
        }

        return true;
    }

    private String buildConfirmationMessage(String type, double montant, CompteDTO compteSource, CompteDTO compteDest) {
        StringBuilder message = new StringBuilder();

        switch (type) {
            case "Dépôt":
                message.append("💰 DÉPÔT\n\n");
                message.append(String.format("Montant: %.2f FCFA\n", montant));
                message.append(String.format("Sur le compte: %s\n", compteSource.getNumero()));
                message.append(String.format("Nouveau solde: %.2f FCFA", compteSource.getSolde() + montant));
                break;

            case "Retrait":
                message.append("💸 RETRAIT\n\n");
                message.append(String.format("Montant: %.2f FCFA\n", montant));
                message.append(String.format("Du compte: %s\n", compteSource.getNumero()));
                message.append(String.format("Nouveau solde: %.2f FCFA", compteSource.getSolde() - montant));
                break;

            case "Virement":
                message.append("🔄 VIREMENT\n\n");
                message.append(String.format("Montant: %.2f FCFA\n", montant));
                message.append(String.format("De: %s (%.2f FCFA)\n", compteSource.getNumero(), compteSource.getSolde()));
                message.append(String.format("Vers: %s (%.2f FCFA)\n\n", compteDest.getNumero(), compteDest.getSolde()));
                message.append("Nouveaux soldes:\n");
                message.append(String.format("Source: %.2f FCFA\n", compteSource.getSolde() - montant));
                message.append(String.format("Destination: %.2f FCFA", compteDest.getSolde() + montant));
                break;
        }

        return message.toString();
    }

    private void executeTransaction(String type, double montant, CompteDTO compteSource, CompteDTO compteDest) {
        try {
            // Créer la transaction
            TransactionDTO transaction = new TransactionDTO();
            transaction.setType(type);
            transaction.setMontant(montant);

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
                    transaction.setCompteDestId(compteDest.getId());
                    break;
            }

            transaction.setStatut("Validé"); // Exécution immédiate pour l'admin

            // Sauvegarder la transaction (Laravel se charge des mises à jour de solde)
            TransactionDTO result = transactionService.createTransaction(transaction);

            if (result != null) {
                // Message de succès avec détails
                String successMessage = String.format("✅ Transaction %s réussie!\n\nMontant: %.2f FCFA\nTransaction ID: %s",
                        type.toLowerCase(), montant, result.getId());

                showAlert(Alert.AlertType.INFORMATION, "Succès", successMessage);

                clearForm();
                loadData(); // Recharger pour voir les nouveaux soldes
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la création de transaction");
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la transaction: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayTransactionDetails(TransactionDTO transaction) {
        if (transaction != null) {
            String details = String.format("Transaction sélectionnée: %s - %.2f FCFA - %s",
                    transaction.getType(), transaction.getMontant(), transaction.getStatut());
            System.out.println("📋 " + details);
            currentMessage = details;
        }
    }

    // === AUTRES HANDLERS ===

    @FXML
    private void handleAnnulerTransaction(ActionEvent event) {
        clearForm();
        System.out.println("✅ Formulaire réinitialisé");
        currentMessage = "Formulaire réinitialisé";
    }

    @FXML
    private void handleRechercherTransaction(ActionEvent event) {
        if (txtRechercheTransaction == null || allTransactions == null) {
            return;
        }

        String recherche = txtRechercheTransaction.getText().trim();
        if (recherche.isEmpty()) {
            loadData();
            return;
        }

        try {
            List<TransactionDTO> resultats = allTransactions.stream()
                    .filter(t -> t.getId().toString().contains(recherche) ||
                            t.getType().toLowerCase().contains(recherche.toLowerCase()) ||
                            t.getMontant().toString().contains(recherche) ||
                            t.getStatut().toLowerCase().contains(recherche.toLowerCase()))
                    .toList();

            if (tableTransactions != null) {
                ObservableList<TransactionDTO> resultatsData = FXCollections.observableArrayList(resultats);
                tableTransactions.setItems(resultatsData);
            }

            String message = resultats.size() + " transaction(s) trouvée(s)";
            System.out.println("🔍 " + message);
            currentMessage = message;

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la recherche");
        }
    }

    @FXML
    private void handleBloquerTransaction(ActionEvent event) {
        if (selectedTransaction == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Veuillez sélectionner une transaction");
            return;
        }

        if ("Validé".equals(selectedTransaction.getStatut())) {
            showAlert(Alert.AlertType.WARNING, "Transaction validée", "Impossible de bloquer une transaction déjà validée");
            return;
        }

        try {
            selectedTransaction.setStatut("Bloqué");
            transactionService.updateTransaction(selectedTransaction);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Transaction bloquée");
            loadData();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du blocage");
        }
    }

    @FXML
    private void handleDebloquerTransaction(ActionEvent event) {
        if (selectedTransaction == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Veuillez sélectionner une transaction");
            return;
        }

        if (!"Bloqué".equals(selectedTransaction.getStatut())) {
            showAlert(Alert.AlertType.WARNING, "Transaction non bloquée", "Cette transaction n'est pas bloquée");
            return;
        }

        try {
            selectedTransaction.setStatut("En attente");
            transactionService.updateTransaction(selectedTransaction);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Transaction débloquée");
            loadData();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du déblocage");
        }
    }

    // === NAVIGATION ===

    @FXML
    private void handleDashboard(ActionEvent event) {
        navigateTo("/com/groupeisi/minisystemebancaire/admin/UI_Dashboard.fxml", event);
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
    private void handleGestionCredits(ActionEvent event) {
        navigateTo("/com/groupeisi/minisystemebancaire/admin/UI_Gestion_Credits.fxml", event);
    }

    @FXML
    private void handleGestionCartes(ActionEvent event) {
        navigateTo("/com/groupeisi/minisystemebancaire/admin/UI_Gestion_Cartes_Bancaires.fxml", event);
    }

    @FXML
    private void handleGestionSupport(ActionEvent event) {
        navigateTo("/com/groupeisi/minisystemebancaire/admin/UI_Service_Client_Rapports.fxml", event);
    }

    @FXML
    private void handleDeconnexion(ActionEvent event) {
        SessionManager.logout();
        navigateTo("/com/groupeisi/minisystemebancaire/UI_Main.fxml", event);
    }

    // === UTILITAIRES ===

    private void clearForm() {
        if (choiceTypeTransaction != null) choiceTypeTransaction.setValue("Dépôt");
        if (choiceCompteSource != null) choiceCompteSource.setValue(null);
        if (choiceCompteDest != null) choiceCompteDest.setValue(null);
        if (txtMontantTransaction != null) txtMontantTransaction.clear();
        if (txtRechercheTransaction != null) txtRechercheTransaction.clear();
    }

    private void updateButtonStates() {
        boolean hasSelection = selectedTransaction != null;

        if (btnBloquerTransaction != null) {
            boolean canBlock = hasSelection && !"Validé".equals(selectedTransaction.getStatut()) && !"Bloqué".equals(selectedTransaction.getStatut());
            btnBloquerTransaction.setDisable(!canBlock);
        }
        if (btnDebloquerTransaction != null) {
            boolean canUnblock = hasSelection && "Bloqué".equals(selectedTransaction.getStatut());
            btnDebloquerTransaction.setDisable(!canUnblock);
        }
    }

    private void navigateTo(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Navigation impossible");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}