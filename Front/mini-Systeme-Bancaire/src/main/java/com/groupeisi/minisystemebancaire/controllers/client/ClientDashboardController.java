package com.groupeisi.minisystemebancaire.controllers.client;

import com.groupeisi.minisystemebancaire.dto.ClientDTO;
import com.groupeisi.minisystemebancaire.dto.CompteDTO;
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
import java.util.List;

public class ClientDashboardController {

    @FXML private Label lblBienvenue;
    @FXML private Label lblNombreComptes;
    @FXML private Label lblSoldeTotal;
    @FXML private TableView<CompteDTO> tableComptes;
    @FXML private TableColumn<CompteDTO, String> colNumero;
    @FXML private TableColumn<CompteDTO, String> colType;
    @FXML private TableColumn<CompteDTO, Double> colSolde;
    @FXML private TableColumn<CompteDTO, String> colStatut;
    @FXML private Button btnNouveauCompte;
    @FXML private Button btnTransactions;
    @FXML private Button btnCartes;
    @FXML private Button btnCredits;
    @FXML private Button btnProfil;
    @FXML private Button btnDeconnexion;

    private final CompteService compteService = new CompteService();
    private ClientDTO currentClient;
    private Long clientId; // Pour la compatibilité

    @FXML
    public void initialize() {
        currentClient = SessionManager.getCurrentClient();

        if (currentClient == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Session expirée");
            redirectToLogin();
            return;
        }

        this.clientId = currentClient.getId(); // Initialiser clientId
        setupUI();
        setupTableColumns();
        loadClientData();
    }

    // Méthode pour la compatibilité
    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getClientId() {
        return this.clientId;
    }

    private void setupUI() {
        lblBienvenue.setText("Bienvenue, " + currentClient.getNomComplet() + " !");

        // Configuration des boutons
        btnNouveauCompte.setOnAction(this::handleNouveauCompte);
        btnTransactions.setOnAction(this::handleTransactions);
        btnCartes.setOnAction(this::handleCartes);
        btnCredits.setOnAction(this::handleCredits);
        btnProfil.setOnAction(this::handleProfil);
        btnDeconnexion.setOnAction(this::handleDeconnexion);
    }

    private void setupTableColumns() {
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colSolde.setCellValueFactory(new PropertyValueFactory<>("solde"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Formatage du solde
        colSolde.setCellFactory(column -> new TableCell<CompteDTO, Double>() {
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
        colStatut.setCellFactory(column -> new TableCell<CompteDTO, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Actif".equals(item)) {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    private void loadClientData() {
        Thread loadThread = new Thread(() -> {
            try {
                List<CompteDTO> comptes = compteService.getComptesByClient(currentClient.getId());

                Platform.runLater(() -> {
                    ObservableList<CompteDTO> comptesData = FXCollections.observableArrayList(comptes);
                    tableComptes.setItems(comptesData);

                    // Mise à jour des statistiques
                    lblNombreComptes.setText(String.valueOf(comptes.size()));

                    double soldeTotal = comptes.stream()
                            .filter(c -> "Actif".equals(c.getStatut()))
                            .mapToDouble(CompteDTO::getSolde)
                            .sum();

                    lblSoldeTotal.setText(String.format("%.2f FCFA", soldeTotal));
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Erreur",
                            "Impossible de charger les données: " + e.getMessage());
                });
            }
        });

        loadThread.setDaemon(true);
        loadThread.start();
    }

    @FXML
    private void handleNouveauCompte(ActionEvent event) {
        navigateTo("/com/groupeisi/minisystemebancaire/client/UI_Nouveau_Compte.fxml", event);
    }

    @FXML
    private void handleTransactions(ActionEvent event) {
        navigateTo("/com/groupeisi/minisystemebancaire/client/UI_Transactions.fxml", event);
    }

    @FXML
    private void handleCartes(ActionEvent event) {
        navigateTo("/com/groupeisi/minisystemebancaire/client/UI_Cartes.fxml", event);
    }

    @FXML
    private void handleCredits(ActionEvent event) {
        navigateTo("/com/groupeisi/minisystemebancaire/client/UI_Credits.fxml", event);
    }

    @FXML
    private void handleProfil(ActionEvent event) {
        navigateTo("/com/groupeisi/minisystemebancaire/client/UI_Profil.fxml", event);
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