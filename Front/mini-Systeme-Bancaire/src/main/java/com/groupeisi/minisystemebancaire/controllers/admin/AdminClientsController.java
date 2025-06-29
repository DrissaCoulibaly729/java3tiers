package com.groupeisi.minisystemebancaire.controllers.admin;

import com.groupeisi.minisystemebancaire.dto.ClientDTO;
import com.groupeisi.minisystemebancaire.services.ClientService;
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

public class AdminClientsController {

    // === ÉLÉMENTS FXML POTENTIELS - avec vérification null partout ===
    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelephone;
    @FXML private TextField txtAdresse;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtRechercheClient;

    @FXML private TableView<ClientDTO> tableClients;
    @FXML private TableColumn<ClientDTO, Long> colId;
    @FXML private TableColumn<ClientDTO, String> colNom;
    @FXML private TableColumn<ClientDTO, String> colPrenom;
    @FXML private TableColumn<ClientDTO, String> colEmail;
    @FXML private TableColumn<ClientDTO, String> colTelephone;
    @FXML private TableColumn<ClientDTO, String> colStatut;

    // Boutons - PEUVENT NE PAS EXISTER
    @FXML private Button btnAjouterClient;
    @FXML private Button btnModifierClient;
    @FXML private Button btnSupprimerClient;
    @FXML private Button btnAnnulerClient;
    @FXML private Button btnRechercherClient;
    @FXML private Button btnVoirComptes;
    @FXML private Button btnReinitialiserClient;
    @FXML private Button btnValiderClient;
    @FXML private Button btnSuspendreClient;
    @FXML private Button btnReactiverClient;
    @FXML private Button btnBloquerClient;
    @FXML private Button btnExporterClients;
    @FXML private Button btnImporterClients;

    // Navigation
    @FXML private Button btnDashboard;
    @FXML private Button btnComptes;
    @FXML private Button btnTransactions;
    @FXML private Button btnCredits;
    @FXML private Button btnCartes;
    @FXML private Button btnSupport;
    @FXML private Button btnDeconnexion;

    // Services et variables
    private final ClientService clientService = new ClientService();
    private List<ClientDTO> allClients;
    private ClientDTO selectedClient;

    @FXML
    public void initialize() {
        if (!SessionManager.isAdminLoggedIn()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Accès non autorisé");
            return;
        }

        setupTableColumns();
        setupUI();
        loadClients();
    }

    private void setupTableColumns() {
        // Configuration SEULEMENT si les colonnes existent
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colNom != null) colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        if (colPrenom != null) colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        if (colEmail != null) colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        if (colTelephone != null) colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        if (colStatut != null) {
            colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
            colStatut.setCellFactory(column -> new TableCell<ClientDTO, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        switch (item.toLowerCase()) {
                            case "actif":
                                setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                                break;
                            case "suspendu":
                                setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                                break;
                            case "fermé":
                                setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                                break;
                            default:
                                setStyle("");
                        }
                    }
                }
            });
        }
    }

    private void setupUI() {
        // Sélection dans la table SEULEMENT si elle existe
        if (tableClients != null) {
            tableClients.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                selectedClient = newSelection;
                updateButtonStates();
                if (newSelection != null) {
                    fillForm(newSelection);
                }
            });
        }

        updateButtonStates();
    }

    private void loadClients() {
        Thread loadThread = new Thread(() -> {
            try {
                allClients = clientService.getAllClients();

                Platform.runLater(() -> {
                    if (tableClients != null && allClients != null) {
                        ObservableList<ClientDTO> clientsData = FXCollections.observableArrayList(allClients);
                        tableClients.setItems(clientsData);
                    }
                    System.out.println("✅ Clients chargés: " + (allClients != null ? allClients.size() : 0));
                });

            } catch (Exception e) {
                System.err.println("❌ Erreur chargement clients: " + e.getMessage());
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les clients: " + e.getMessage());
                });
            }
        });

        loadThread.setDaemon(true);
        loadThread.start();
    }

    // === HANDLERS - TOUS DÉFINIS POUR ÉVITER LES ERREURS FXML ===

    @FXML
    private void handleAjouterClient(ActionEvent event) {
        if (!validateForm()) {
            return;
        }

        try {
            // Confirmation
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmer la création");
            confirmation.setHeaderText("Créer un nouveau client");
            confirmation.setContentText(
                    "Client: " + (txtNom != null ? txtNom.getText() : "") + " " +
                            (txtPrenom != null ? txtPrenom.getText() : "") + "\n\n" +
                            "🏦 Un compte courant sera automatiquement créé\n" +
                            "avec un solde initial de 0 FCFA.\n\n" +
                            "Confirmer la création ?"
            );

            if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                ClientDTO client = new ClientDTO();
                if (txtNom != null) client.setNom(txtNom.getText().trim());
                if (txtPrenom != null) client.setPrenom(txtPrenom.getText().trim());
                if (txtEmail != null) client.setEmail(txtEmail.getText().trim());
                if (txtTelephone != null) client.setTelephone(txtTelephone.getText().trim());
                if (txtAdresse != null) client.setAdresse(txtAdresse.getText().trim());
                if (txtPassword != null) client.setPassword(txtPassword.getText());

                Thread createThread = new Thread(() -> {
                    try {
                        ClientDTO savedClient = clientService.createClient(client);

                        Platform.runLater(() -> {
                            if (savedClient != null) {
                                showAlert(Alert.AlertType.INFORMATION, "Succès",
                                        "✅ Client et compte créés avec succès !\n" +
                                                "🏦 Compte courant prêt pour les transactions.");
                                clearForm();
                                loadClients();
                            } else {
                                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la création du client");
                            }
                        });

                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur: " + e.getMessage());
                        });
                    }
                });

                createThread.setDaemon(true);
                createThread.start();
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void handleModifierClient(ActionEvent event) {
        if (selectedClient == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Veuillez sélectionner un client à modifier");
            return;
        }

        if (!validateForm()) {
            return;
        }

        try {
            if (txtNom != null) selectedClient.setNom(txtNom.getText().trim());
            if (txtPrenom != null) selectedClient.setPrenom(txtPrenom.getText().trim());
            if (txtEmail != null) selectedClient.setEmail(txtEmail.getText().trim());
            if (txtTelephone != null) selectedClient.setTelephone(txtTelephone.getText().trim());
            if (txtAdresse != null) selectedClient.setAdresse(txtAdresse.getText().trim());

            ClientDTO updatedClient = clientService.updateClient(selectedClient);

            if (updatedClient != null) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Client modifié avec succès");
                clearForm();
                loadClients();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la modification");
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void handleSupprimerClient(ActionEvent event) {
        if (selectedClient == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Veuillez sélectionner un client à supprimer");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmer la suppression");
        confirmation.setHeaderText("Supprimer le client");
        confirmation.setContentText(
                "Client: " + selectedClient.getNomComplet() + "\n\n" +
                        "⚠️ ATTENTION: Cette action supprimera également\n" +
                        "tous les comptes associés à ce client.\n\n" +
                        "Cette action est irréversible. Continuer ?"
        );

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                clientService.deleteClient(selectedClient.getId());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Client et comptes supprimés avec succès");
                clearForm();
                loadClients();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }

    // ✅ HANDLER MANQUANT QUI CAUSAIT L'ERREUR
    @FXML
    private void handleAnnulerClient(ActionEvent event) {
        clearForm();
        System.out.println("✅ Formulaire client réinitialisé");
    }

    // Autres handlers possibles pour éviter les erreurs FXML
    @FXML
    private void handleValiderClient(ActionEvent event) {
        handleAjouterClient(event);
    }

    @FXML
    private void handleReinitialiserClient(ActionEvent event) {
        clearForm();
    }

    @FXML
    private void handleVoirComptes(ActionEvent event) {
        if (selectedClient == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Veuillez sélectionner un client");
            return;
        }

        showAlert(Alert.AlertType.INFORMATION, "Comptes du client",
                "Client: " + selectedClient.getNomComplet() + "\n\n" +
                        "Pour voir les détails des comptes, allez dans\n" +
                        "la section 'Gestion des Comptes'.");
    }

    @FXML
    private void handleRechercherClient(ActionEvent event) {
        if (txtRechercheClient == null || allClients == null) {
            return;
        }

        String recherche = txtRechercheClient.getText().trim();
        if (recherche.isEmpty()) {
            loadClients();
            return;
        }

        try {
            List<ClientDTO> resultats = allClients.stream()
                    .filter(c -> c.getNom().toLowerCase().contains(recherche.toLowerCase()) ||
                            c.getPrenom().toLowerCase().contains(recherche.toLowerCase()) ||
                            c.getEmail().toLowerCase().contains(recherche.toLowerCase()) ||
                            c.getTelephone().contains(recherche))
                    .toList();

            if (tableClients != null) {
                ObservableList<ClientDTO> resultatsData = FXCollections.observableArrayList(resultats);
                tableClients.setItems(resultatsData);
            }

            System.out.println("🔍 " + resultats.size() + " client(s) trouvé(s)");

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la recherche");
        }
    }

    // ✅ HANDLERS MANQUANTS POUR ÉVITER LES ERREURS FXML
    @FXML
    private void handleSuspendreClient(ActionEvent event) {
        if (selectedClient == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Veuillez sélectionner un client à suspendre");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Suspendre le client");
        confirmation.setHeaderText("Confirmer la suspension");
        confirmation.setContentText("Voulez-vous suspendre le client " + selectedClient.getNomComplet() + " ?");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                selectedClient.setStatut("Suspendu");
                ClientDTO updatedClient = clientService.updateClient(selectedClient);

                if (updatedClient != null) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Client suspendu avec succès");
                    loadClients();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la suspension");
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleReactiverClient(ActionEvent event) {
        if (selectedClient == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Veuillez sélectionner un client à réactiver");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Réactiver le client");
        confirmation.setHeaderText("Confirmer la réactivation");
        confirmation.setContentText("Voulez-vous réactiver le client " + selectedClient.getNomComplet() + " ?");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                selectedClient.setStatut("Actif");
                ClientDTO updatedClient = clientService.updateClient(selectedClient);

                if (updatedClient != null) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Client réactivé avec succès");
                    loadClients();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la réactivation");
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleBloquerClient(ActionEvent event) {
        if (selectedClient == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Veuillez sélectionner un client à bloquer");
            return;
        }

        try {
            selectedClient.setStatut("Fermé");
            ClientDTO updatedClient = clientService.updateClient(selectedClient);

            if (updatedClient != null) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Client bloqué avec succès");
                loadClients();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec du blocage");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void handleExporterClients(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Export", "Fonctionnalité d'export en cours de développement");
    }

    @FXML
    private void handleImporterClients(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Import", "Fonctionnalité d'import en cours de développement");
    }

    // === VALIDATION ET UTILITAIRES ===

    private boolean validateForm() {
        // Validation basique - seulement si les champs existent
        if (txtNom != null && txtNom.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le nom est obligatoire");
            return false;
        }
        if (txtPrenom != null && txtPrenom.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le prénom est obligatoire");
            return false;
        }
        if (txtEmail != null && (txtEmail.getText().trim().isEmpty() || !txtEmail.getText().contains("@"))) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Email invalide");
            return false;
        }
        if (txtTelephone != null && txtTelephone.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le téléphone est obligatoire");
            return false;
        }
        if (txtAdresse != null && txtAdresse.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "L'adresse est obligatoire");
            return false;
        }
        if (txtPassword != null && txtPassword.getText().length() < 6) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le mot de passe doit contenir au moins 6 caractères");
            return false;
        }

        return true;
    }

    private void fillForm(ClientDTO client) {
        if (txtNom != null) txtNom.setText(client.getNom());
        if (txtPrenom != null) txtPrenom.setText(client.getPrenom());
        if (txtEmail != null) txtEmail.setText(client.getEmail());
        if (txtTelephone != null) txtTelephone.setText(client.getTelephone());
        if (txtAdresse != null) txtAdresse.setText(client.getAdresse());
        if (txtPassword != null) txtPassword.clear();
    }

    private void clearForm() {
        if (txtNom != null) txtNom.clear();
        if (txtPrenom != null) txtPrenom.clear();
        if (txtEmail != null) txtEmail.clear();
        if (txtTelephone != null) txtTelephone.clear();
        if (txtAdresse != null) txtAdresse.clear();
        if (txtPassword != null) txtPassword.clear();
        if (txtRechercheClient != null) txtRechercheClient.clear();

        selectedClient = null;
        if (tableClients != null) {
            tableClients.getSelectionModel().clearSelection();
        }
    }

    private void updateButtonStates() {
        boolean hasSelection = selectedClient != null;
        String statut = selectedClient != null ? selectedClient.getStatut() : "";

        // Boutons de base
        if (btnModifierClient != null) btnModifierClient.setDisable(!hasSelection);
        if (btnSupprimerClient != null) btnSupprimerClient.setDisable(!hasSelection);
        if (btnVoirComptes != null) btnVoirComptes.setDisable(!hasSelection);

        // Boutons de gestion de statut
        if (btnSuspendreClient != null) {
            btnSuspendreClient.setDisable(!hasSelection || !"Actif".equals(statut));
        }
        if (btnReactiverClient != null) {
            btnReactiverClient.setDisable(!hasSelection || !"Suspendu".equals(statut));
        }
        if (btnBloquerClient != null) {
            btnBloquerClient.setDisable(!hasSelection || "Fermé".equals(statut));
        }
    }

    // === NAVIGATION ===

    @FXML
    private void handleDashboard(ActionEvent event) {
        navigateTo("/com/groupeisi/minisystemebancaire/admin/UI_Dashboard.fxml", event);
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

    private void navigateTo(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Navigation impossible: " + e.getMessage());
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