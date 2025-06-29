package com.groupeisi.minisystemebancaire.controllers.admin;

import com.groupeisi.minisystemebancaire.dto.AdminDTO;
import com.groupeisi.minisystemebancaire.dto.ClientDTO;
import com.groupeisi.minisystemebancaire.dto.CompteDTO;
import com.groupeisi.minisystemebancaire.dto.TransactionDTO;
import com.groupeisi.minisystemebancaire.services.ClientService;
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

public class AdminDashboardController {

    // Éléments qui EXISTENT dans votre FXML
    @FXML private Label lblNbClients;
    @FXML private Label lblNbComptes;
    @FXML private Label lblNbTransactions;
    @FXML private Label lblNbCartes;

    // Tables qui EXISTENT dans votre FXML
    @FXML private TableView<TransactionDTO> tableOperationsSuspectes;
    @FXML private TableColumn<TransactionDTO, Long> colIdOpSuspecte;
    @FXML private TableColumn<TransactionDTO, Double> colMontantOpSuspecte;
    @FXML private TableColumn<TransactionDTO, String> colTypeOpSuspecte;
    @FXML private TableColumn<TransactionDTO, String> colCompteSourceOp;
    @FXML private TableColumn<TransactionDTO, String> colDateOpSuspecte;

    @FXML private TableView tableReclamations;
    @FXML private TableColumn colIdReclamation;
    @FXML private TableColumn colClientReclamation;
    @FXML private TableColumn colSujetReclamation;
    @FXML private TableColumn colStatutReclamation;

    // Boutons de navigation qui EXISTENT dans votre FXML
    @FXML private Button btnDashboard;
    @FXML private Button btnClients;
    @FXML private Button btnComptes;
    @FXML private Button btnTransactions;
    @FXML private Button btnCredits;
    @FXML private Button btnCartes;
    @FXML private Button btnSupport;
    @FXML private Button btnDeconnexion;

    // Services
    private final ClientService clientService = new ClientService();
    private final CompteService compteService = new CompteService();
    private final TransactionService transactionService = new TransactionService();

    @FXML
    public void initialize() {
        // Vérification de la session admin
        AdminDTO currentAdmin = SessionManager.getCurrentAdmin();
        if (currentAdmin == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Session expirée");
            redirectToLogin();
            return;
        }

        setupTableColumns();
        loadDashboardData();
    }

    private void setupTableColumns() {
        // Configuration des colonnes de la table des opérations suspectes
        if (colIdOpSuspecte != null) {
            colIdOpSuspecte.setCellValueFactory(new PropertyValueFactory<>("id"));
        }
        if (colMontantOpSuspecte != null) {
            colMontantOpSuspecte.setCellValueFactory(new PropertyValueFactory<>("montant"));
            colMontantOpSuspecte.setCellFactory(col -> new TableCell<TransactionDTO, Double>() {
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
        if (colTypeOpSuspecte != null) {
            colTypeOpSuspecte.setCellValueFactory(new PropertyValueFactory<>("type"));
        }
        if (colCompteSourceOp != null) {
            colCompteSourceOp.setCellValueFactory(cellData -> {
                TransactionDTO transaction = cellData.getValue();
                if (transaction.getCompteSource() != null) {
                    return new javafx.beans.property.SimpleStringProperty(transaction.getCompteSource().getNumero());
                }
                return new javafx.beans.property.SimpleStringProperty("N/A");
            });
        }
        if (colDateOpSuspecte != null) {
            colDateOpSuspecte.setCellValueFactory(new PropertyValueFactory<>("date"));
        }

        // Configuration basique des colonnes réclamations
        if (colIdReclamation != null) {
            colIdReclamation.setCellValueFactory(new PropertyValueFactory<>("id"));
        }
        if (colClientReclamation != null) {
            colClientReclamation.setCellValueFactory(new PropertyValueFactory<>("client"));
        }
        if (colSujetReclamation != null) {
            colSujetReclamation.setCellValueFactory(new PropertyValueFactory<>("sujet"));
        }
        if (colStatutReclamation != null) {
            colStatutReclamation.setCellValueFactory(new PropertyValueFactory<>("statut"));
        }
    }

    private void loadDashboardData() {
        Thread loadThread = new Thread(() -> {
            try {
                // Charger les statistiques de base
                List<ClientDTO> clients = clientService.getAllClients();
                List<CompteDTO> comptes = compteService.getAllComptes();
                List<TransactionDTO> transactions = transactionService.getAllTransactions();

                // Simuler les données pour les cartes (ou utiliser un service réel si disponible)
                int nbCartes = 0; // À remplacer par le vrai service

                Platform.runLater(() -> {
                    // Mise à jour des statistiques - avec vérification null
                    if (lblNbClients != null) {
                        lblNbClients.setText(String.valueOf(clients.size()));
                    }
                    if (lblNbComptes != null) {
                        lblNbComptes.setText(String.valueOf(comptes.size()));
                    }
                    if (lblNbTransactions != null) {
                        lblNbTransactions.setText(String.valueOf(transactions.size()));
                    }
                    if (lblNbCartes != null) {
                        lblNbCartes.setText(String.valueOf(nbCartes));
                    }

                    // Charger les opérations suspectes (simulées ou réelles)
                    loadOperationsSuspectes(transactions);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    System.err.println("Erreur lors du chargement : " + e.getMessage());
                    // Mettre des valeurs par défaut en cas d'erreur
                    if (lblNbClients != null) lblNbClients.setText("0");
                    if (lblNbComptes != null) lblNbComptes.setText("0");
                    if (lblNbTransactions != null) lblNbTransactions.setText("0");
                    if (lblNbCartes != null) lblNbCartes.setText("0");
                });
            }
        });

        loadThread.setDaemon(true);
        loadThread.start();
    }

    private void loadOperationsSuspectes(List<TransactionDTO> transactions) {
        try {
            // Filtrer les transactions suspectes (montants élevés ou statut particulier)
            List<TransactionDTO> suspectes = transactions.stream()
                    .filter(t -> t.getMontant() > 100000 || "En attente".equals(t.getStatut()))
                    .limit(10) // Limiter à 10 pour l'affichage
                    .toList();

            if (tableOperationsSuspectes != null) {
                ObservableList<TransactionDTO> suspectesData = FXCollections.observableArrayList(suspectes);
                tableOperationsSuspectes.setItems(suspectesData);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des opérations suspectes : " + e.getMessage());
        }
    }

    // === HANDLERS DE NAVIGATION ===

    @FXML
    private void handleGestionClients(ActionEvent event) {
        navigateTo("/com/groupeisi/minisystemebancaire/admin/UI_Gestion_Clients.fxml", event);
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

    // === MÉTHODES UTILITAIRES ===

    private void redirectToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/groupeisi/minisystemebancaire/UI_Main.fxml"));
            Parent root = loader.load();

            Stage stage = null;
            if (btnDeconnexion != null && btnDeconnexion.getScene() != null) {
                stage = (Stage) btnDeconnexion.getScene().getWindow();
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
            showAlert(Alert.AlertType.ERROR, "Erreur", "Navigation impossible");
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