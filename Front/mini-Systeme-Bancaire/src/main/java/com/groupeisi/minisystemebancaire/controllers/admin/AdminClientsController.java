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
import java.util.regex.Pattern;

public class AdminClientsController {

    // === ÉLÉMENTS FXML ===
    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelephone;
    @FXML private TextField txtAdresse;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtRechercheClient;
    @FXML private ComboBox<String> cmbStatutClient;

    @FXML private TableView<ClientDTO> tableClients;
    @FXML private TableColumn<ClientDTO, Long> colId;
    @FXML private TableColumn<ClientDTO, String> colNom;
    @FXML private TableColumn<ClientDTO, String> colPrenom;
    @FXML private TableColumn<ClientDTO, String> colEmail;
    @FXML private TableColumn<ClientDTO, String> colTelephone;
    @FXML private TableColumn<ClientDTO, String> colStatut;

    // Boutons
    @FXML private Button btnAjouterClient;
    @FXML private Button btnModifierClient;
    @FXML private Button btnSupprimerClient;
    @FXML private Button btnAnnulerClient;
    @FXML private Button btnRechercherClient;
    @FXML private Button btnVoirComptes;
    @FXML private Button btnSuspendreClient;
    @FXML private Button btnReactiverClient;
    @FXML private Button btnExporterClients;
    @FXML private Button btnRafraichir;

    // Navigation
    @FXML private Button btnDashboard;
    @FXML private Button btnComptes;
    @FXML private Button btnTransactions;
    @FXML private Button btnCredits;
    @FXML private Button btnCartes;
    @FXML private Button btnSupport;
    @FXML private Button btnDeconnexion;

    @FXML private Label lblMessage;
    @FXML private Label lblTotalClients;
    @FXML private Label lblClientsActifs;
    @FXML private Label lblClientsSuspendus;

    // === SERVICES ET VARIABLES ===
    private final ClientService clientService = new ClientService();
    private ClientDTO selectedClient;
    private boolean isModificationMode = false;

    @FXML
    public void initialize() {
        System.out.println("🚀 Initialisation AdminClientsController");

        initializeStatutComboBox();
        setupTableColumns();
        setupTableSelection();
        setupSearchFunctionality();
        loadClients();
        updateButtonStates();
        updateStatistics();

        System.out.println("✅ AdminClientsController initialisé avec succès");
    }

    // === INITIALISATION DES COMPOSANTS ===

    private void initializeStatutComboBox() {
        if (cmbStatutClient != null) {
            ObservableList<String> statutOptions = FXCollections.observableArrayList(
                    "Actif", "Suspendu", "Fermé"
            );
            cmbStatutClient.setItems(statutOptions);
            cmbStatutClient.setValue("Actif");
            cmbStatutClient.setPromptText("Sélectionner un statut");

            System.out.println("✅ ComboBox statut initialisé");
        }
    }

    private void setupTableColumns() {
        if (tableClients != null) {
            if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("id"));
            if (colNom != null) colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
            if (colPrenom != null) colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
            if (colEmail != null) colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
            if (colTelephone != null) colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
            if (colStatut != null) colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

            System.out.println("✅ Colonnes du tableau configurées");
        }
    }

    private void setupTableSelection() {
        if (tableClients != null) {
            tableClients.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                selectedClient = newSelection;
                updateButtonStates();

                if (newSelection != null) {
                    fillForm(newSelection);
                    if (cmbStatutClient != null) {
                        cmbStatutClient.setValue(newSelection.getStatut());
                    }
                }
            });
        }
    }

    private void setupSearchFunctionality() {
        if (txtRechercheClient != null) {
            txtRechercheClient.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    loadClients();
                } else {
                    rechercherClients(newValue.trim());
                }
            });
        }
    }

    // === GESTION DES CLIENTS ===

    @FXML
    private void handleAjouterClient(ActionEvent event) {
        if (!validateClientForm()) return;

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Créer un nouveau client");
        confirmation.setContentText(
                "Client: " + txtNom.getText() + " " + txtPrenom.getText() + "\n\n" +
                        "🔐 Un mot de passe temporaire sera généré automatiquement\n" +
                        "📧 Les identifiants seront envoyés par email au client\n" +
                        "🏦 Un compte courant sera automatiquement créé\n\n" +
                        "Confirmer la création ?"
        );

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            ClientDTO client = new ClientDTO();
            client.setNom(txtNom.getText().trim());
            client.setPrenom(txtPrenom.getText().trim());
            client.setEmail(txtEmail.getText().trim());
            client.setTelephone(txtTelephone.getText().trim());
            client.setAdresse(txtAdresse.getText().trim());

            // Générer un mot de passe temporaire
            String motDePasseTemporaire = genererMotDePasseTemporaire();
            client.setPassword(motDePasseTemporaire);

            btnAjouterClient.setDisable(true);
            btnAjouterClient.setText("Création...");

            Thread createThread = new Thread(() -> {
                try {
                    ClientDTO savedClient = clientService.createClient(client);

                    Platform.runLater(() -> {
                        if (savedClient != null) {
                            showAlert(Alert.AlertType.INFORMATION, "Succès",
                                    "✅ Client créé avec succès !\n" +
                                            "📧 Email avec identifiants envoyé à : " + savedClient.getEmail() + "\n" +
                                            "🔐 Mot de passe temporaire : " + motDePasseTemporaire + "\n" +
                                            "🏦 Compte courant créé automatiquement");
                            clearForm();
                            loadClients();
                            updateStatistics();
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la création du client");
                        }
                        resetCreateButton();
                    });

                } catch (Exception e) {
                    Platform.runLater(() -> {
                        String errorMessage = e.getMessage();
                        if (errorMessage.contains("422")) {
                            showAlert(Alert.AlertType.ERROR, "Erreur",
                                    "Données invalides. Vérifiez tous les champs obligatoires.");
                        } else if (errorMessage.contains("email")) {
                            showAlert(Alert.AlertType.ERROR, "Erreur",
                                    "Cette adresse email est déjà utilisée");
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Erreur",
                                    "Erreur lors de la création : " + errorMessage);
                        }
                        resetCreateButton();
                    });
                }
            });

            createThread.setDaemon(true);
            createThread.start();
        }
    }

    @FXML
    private void handleModifierClient(ActionEvent event) {
        if (selectedClient == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Veuillez sélectionner un client à modifier");
            return;
        }

        if (!validateClientForm()) return;

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Modifier le client");
        confirmation.setContentText("Voulez-vous vraiment modifier les informations de " + selectedClient.getNomComplet() + " ?");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            selectedClient.setNom(txtNom.getText().trim());
            selectedClient.setPrenom(txtPrenom.getText().trim());
            selectedClient.setEmail(txtEmail.getText().trim());
            selectedClient.setTelephone(txtTelephone.getText().trim());
            selectedClient.setAdresse(txtAdresse.getText().trim());
            selectedClient.setStatut(cmbStatutClient.getValue());

            // Mot de passe optionnel
            if (txtPassword.getText() != null && !txtPassword.getText().trim().isEmpty()) {
                selectedClient.setPassword(txtPassword.getText());
            }

            try {
                ClientDTO updatedClient = clientService.updateClient(selectedClient);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Client modifié avec succès");
                clearForm();
                loadClients();
                updateStatistics();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSupprimerClient(ActionEvent event) {
        if (selectedClient == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Veuillez sélectionner un client à supprimer");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer le client");
        confirmation.setContentText(
                "⚠️ ATTENTION : Cette action est irréversible !\n\n" +
                        "Client: " + selectedClient.getNomComplet() + "\n" +
                        "Email: " + selectedClient.getEmail() + "\n\n" +
                        "Tous les comptes et transactions associés seront également supprimés.\n\n" +
                        "Continuer ?"
        );

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                clientService.deleteClient(selectedClient.getId());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Client et comptes supprimés avec succès");
                clearForm();
                loadClients();
                updateStatistics();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSuspendreClient(ActionEvent event) {
        if (selectedClient == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Veuillez sélectionner un client");
            return;
        }

        if (!"Actif".equals(selectedClient.getStatut())) {
            showAlert(Alert.AlertType.WARNING, "Statut", "Seuls les clients actifs peuvent être suspendus");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Suspendre le client");
        confirmation.setHeaderText("Confirmation de suspension");
        confirmation.setContentText("Voulez-vous vraiment suspendre le client " + selectedClient.getNomComplet() + " ?");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                clientService.suspendClient(selectedClient.getId());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Client suspendu avec succès");
                loadClients();
                clearForm();
                updateStatistics();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suspension: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleReactiverClient(ActionEvent event) {
        if (selectedClient == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Veuillez sélectionner un client");
            return;
        }

        if (!"Suspendu".equals(selectedClient.getStatut())) {
            showAlert(Alert.AlertType.WARNING, "Statut", "Seuls les clients suspendus peuvent être réactivés");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Réactiver le client");
        confirmation.setHeaderText("Confirmation de réactivation");
        confirmation.setContentText("Voulez-vous vraiment réactiver le client " + selectedClient.getNomComplet() + " ?");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                clientService.reactivateClient(selectedClient.getId());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Client réactivé avec succès");
                loadClients();
                clearForm();
                updateStatistics();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la réactivation: " + e.getMessage());
            }
        }
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
    private void handleAnnulerClient(ActionEvent event) {
        clearForm();
        isModificationMode = false;
        System.out.println("✅ Formulaire client réinitialisé");
    }

    @FXML
    private void handleRechercherClient(ActionEvent event) {
        String searchTerm = txtRechercheClient.getText();
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            loadClients();
        } else {
            rechercherClients(searchTerm.trim());
        }
    }

    @FXML
    private void handleRafraichir(ActionEvent event) {
        loadClients();
        updateStatistics();
        showMessage("Liste des clients actualisée", "success");
    }

    @FXML
    private void handleExporterClients(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Export",
                "Fonctionnalité d'export en cours de développement...");
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
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Déconnexion");
        confirmation.setHeaderText("Confirmer la déconnexion");
        confirmation.setContentText("Voulez-vous vraiment vous déconnecter ?");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            SessionManager.logout();
            navigateTo("/com/groupeisi/minisystemebancaire/UI_Main.fxml", event);
        }
    }

    // === MÉTHODES UTILITAIRES ===

    private String genererMotDePasseTemporaire() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%";
        StringBuilder motDePasse = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            int index = (int) (Math.random() * characters.length());
            motDePasse.append(characters.charAt(index));
        }

        System.out.println("🔐 Mot de passe temporaire généré");
        return motDePasse.toString();
    }

    private boolean validateClientForm() {
        if (txtNom == null || txtNom.getText().trim().isEmpty()) {
            showMessage("Le nom est obligatoire", "error");
            return false;
        }

        if (txtPrenom == null || txtPrenom.getText().trim().isEmpty()) {
            showMessage("Le prénom est obligatoire", "error");
            return false;
        }

        if (txtEmail == null || txtEmail.getText().trim().isEmpty()) {
            showMessage("L'email est obligatoire", "error");
            return false;
        }

        if (!isValidEmail(txtEmail.getText().trim())) {
            showMessage("Format d'email invalide", "error");
            return false;
        }

        if (txtTelephone == null || txtTelephone.getText().trim().isEmpty()) {
            showMessage("Le téléphone est obligatoire", "error");
            return false;
        }

        if (txtAdresse == null || txtAdresse.getText().trim().isEmpty()) {
            showMessage("L'adresse est obligatoire", "error");
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }

    private void loadClients() {
        Thread loadThread = new Thread(() -> {
            try {
                List<ClientDTO> clients = clientService.getAllClients();
                Platform.runLater(() -> {
                    if (tableClients != null) {
                        ObservableList<ClientDTO> clientsData = FXCollections.observableArrayList(clients);
                        tableClients.setItems(clientsData);
                    }
                    System.out.println("✅ " + clients.size() + " clients chargés");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les clients: " + e.getMessage());
                });
            }
        });
        loadThread.setDaemon(true);
        loadThread.start();
    }

    private void rechercherClients(String searchTerm) {
        if (tableClients == null || tableClients.getItems() == null) return;

        ObservableList<ClientDTO> allClients = tableClients.getItems();
        ObservableList<ClientDTO> filteredClients = FXCollections.observableArrayList();

        for (ClientDTO client : allClients) {
            if (client.getNom().toLowerCase().contains(searchTerm.toLowerCase()) ||
                    client.getPrenom().toLowerCase().contains(searchTerm.toLowerCase()) ||
                    client.getEmail().toLowerCase().contains(searchTerm.toLowerCase()) ||
                    client.getTelephone().contains(searchTerm)) {
                filteredClients.add(client);
            }
        }

        tableClients.setItems(filteredClients);
        System.out.println("🔍 " + filteredClients.size() + " clients trouvés pour: " + searchTerm);
    }

    private void updateStatistics() {
        if (tableClients == null || tableClients.getItems() == null) return;

        ObservableList<ClientDTO> clients = tableClients.getItems();
        int total = clients.size();
        int actifs = (int) clients.stream().filter(c -> "Actif".equals(c.getStatut())).count();
        int suspendus = (int) clients.stream().filter(c -> "Suspendu".equals(c.getStatut())).count();

        if (lblTotalClients != null) lblTotalClients.setText(String.valueOf(total));
        if (lblClientsActifs != null) lblClientsActifs.setText(String.valueOf(actifs));
        if (lblClientsSuspendus != null) lblClientsSuspendus.setText(String.valueOf(suspendus));
    }

    private void fillForm(ClientDTO client) {
        if (txtNom != null) txtNom.setText(client.getNom());
        if (txtPrenom != null) txtPrenom.setText(client.getPrenom());
        if (txtEmail != null) txtEmail.setText(client.getEmail());
        if (txtTelephone != null) txtTelephone.setText(client.getTelephone());
        if (txtAdresse != null) txtAdresse.setText(client.getAdresse());
        if (cmbStatutClient != null) cmbStatutClient.setValue(client.getStatut());
        if (txtPassword != null) txtPassword.clear(); // Ne pas afficher le mot de passe
    }

    private void clearForm() {
        if (txtNom != null) txtNom.clear();
        if (txtPrenom != null) txtPrenom.clear();
        if (txtEmail != null) txtEmail.clear();
        if (txtTelephone != null) txtTelephone.clear();
        if (txtAdresse != null) txtAdresse.clear();
        if (txtPassword != null) txtPassword.clear();
        if (cmbStatutClient != null) cmbStatutClient.setValue("Actif");
        if (txtRechercheClient != null) txtRechercheClient.clear();

        selectedClient = null;
        if (tableClients != null) {
            tableClients.getSelectionModel().clearSelection();
        }
        updateButtonStates();
        clearMessage();
    }

    private void updateButtonStates() {
        boolean hasSelection = selectedClient != null;
        String statut = hasSelection ? selectedClient.getStatut() : "";

        if (btnModifierClient != null) btnModifierClient.setDisable(!hasSelection);
        if (btnSupprimerClient != null) btnSupprimerClient.setDisable(!hasSelection);
        if (btnVoirComptes != null) btnVoirComptes.setDisable(!hasSelection);

        if (btnSuspendreClient != null) {
            btnSuspendreClient.setDisable(!hasSelection || !"Actif".equals(statut));
        }
        if (btnReactiverClient != null) {
            btnReactiverClient.setDisable(!hasSelection || !"Suspendu".equals(statut));
        }
    }

    private void resetCreateButton() {
        if (btnAjouterClient != null) {
            btnAjouterClient.setDisable(false);
            btnAjouterClient.setText("Ajouter Client");
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

    private void showMessage(String message, String type) {
        if (lblMessage != null) {
            lblMessage.setText(message);
            lblMessage.setStyle(type.equals("error") ?
                    "-fx-text-fill: #e74c3c; -fx-font-weight: bold;" :
                    "-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        }
    }

    private void clearMessage() {
        if (lblMessage != null) {
            lblMessage.setText("");
        }
    }
}