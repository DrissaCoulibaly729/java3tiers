package com.groupeisi.minisystemebancaire.controllers.client;

import com.groupeisi.minisystemebancaire.dto.ClientDTO;
import com.groupeisi.minisystemebancaire.dto.CompteDTO;
import com.groupeisi.minisystemebancaire.dto.TransactionDTO;
// Note: Imports commentés car services non utilisés actuellement
// import com.groupeisi.minisystemebancaire.dto.CarteBancaireDTO;
// import com.groupeisi.minisystemebancaire.dto.CreditDTO;
import com.groupeisi.minisystemebancaire.services.CompteService;
import com.groupeisi.minisystemebancaire.services.TransactionService;
// import com.groupeisi.minisystemebancaire.services.CarteBancaireService;
// import com.groupeisi.minisystemebancaire.services.CreditService;
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

    // === ÉLÉMENTS FXML - CORRESPONDANCE AVEC LE FXML ===
    @FXML private Label lblWelcome;
    @FXML private Label lblNbComptes;
    @FXML private Label lblSoldeTotal;
    @FXML private Label lblNbCartes;
    @FXML private Label lblNbCredits;

    // Buttons navigation
    @FXML private Button btnDashboard;
    @FXML private Button btnTransactions;
    @FXML private Button btnCredits;
    @FXML private Button btnCartes;
    @FXML private Button btnSupport;
    @FXML private Button btnDeconnexion;

    // Tables
    @FXML private TableView<CompteDTO> tableComptes;
    @FXML private TableColumn<CompteDTO, String> colNumeroCompte;
    @FXML private TableColumn<CompteDTO, String> colTypeCompte;
    @FXML private TableColumn<CompteDTO, Double> colSoldeCompte;
    @FXML private TableColumn<CompteDTO, String> colStatutCompte;

    @FXML private TableView<TransactionDTO> tableTransactionsRecentes;
    @FXML private TableColumn<TransactionDTO, String> colTypeTransaction;
    @FXML private TableColumn<TransactionDTO, Double> colMontantTransaction;
    @FXML private TableColumn<TransactionDTO, String> colDateTransaction;
    @FXML private TableColumn<TransactionDTO, String> colStatutTransaction;

    // Services
    private final CompteService compteService = new CompteService();
    private final TransactionService transactionService = new TransactionService();
    // Note: Services cartes et crédits commentés car méthodes spécifiques non disponibles
    // private final CarteBancaireService carteService = new CarteBancaireService();
    // private final CreditService creditService = new CreditService();

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
        loadDashboardData();
    }

    // Méthode pour la compatibilité
    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getClientId() {
        return this.clientId;
    }

    private void setupUI() {
        // Mise à jour du titre avec le nom du client - STYLE IDENTIQUE À L'ADMIN
        if (lblWelcome != null) {
            lblWelcome.setText("📊 Tableau de Bord - " + currentClient.getPrenom() + " " + currentClient.getNom());
        }

        // Mettre le bouton Dashboard en actif - STYLE IDENTIQUE À L'ADMIN
        if (btnDashboard != null) {
            btnDashboard.setStyle("-fx-background-color: #3498db; -fx-background-radius: 10; " +
                    "-fx-text-fill: white; -fx-font-weight: bold; " +
                    "-fx-pref-width: 220px; -fx-padding: 12;");
        }
    }

    private void setupTableColumns() {
        // Configuration des colonnes de comptes
        if (colNumeroCompte != null) {
            colNumeroCompte.setCellValueFactory(new PropertyValueFactory<>("numero"));
        }
        if (colTypeCompte != null) {
            colTypeCompte.setCellValueFactory(new PropertyValueFactory<>("type"));
        }
        if (colSoldeCompte != null) {
            colSoldeCompte.setCellValueFactory(new PropertyValueFactory<>("solde"));
            // Formatage du solde
            colSoldeCompte.setCellFactory(column -> new TableCell<CompteDTO, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(String.format("%.2f FCFA", item));
                        // Couleur selon le solde
                        if (item > 0) {
                            setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                        } else if (item < 0) {
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                        }
                    }
                }
            });
        }
        if (colStatutCompte != null) {
            colStatutCompte.setCellValueFactory(new PropertyValueFactory<>("statut"));
            // Formatage du statut avec icônes
            colStatutCompte.setCellFactory(column -> new TableCell<CompteDTO, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        if ("Actif".equals(item)) {
                            setText("✅ Actif");
                            setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                        } else if ("Suspendu".equals(item)) {
                            setText("⏸️ Suspendu");
                            setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                        } else {
                            setText("❌ " + item);
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                        }
                    }
                }
            });
        }

        // Configuration des colonnes de transactions
        if (colTypeTransaction != null) {
            colTypeTransaction.setCellValueFactory(new PropertyValueFactory<>("type"));
            colTypeTransaction.setCellFactory(column -> new TableCell<TransactionDTO, String>() {
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
        if (colMontantTransaction != null) {
            colMontantTransaction.setCellValueFactory(new PropertyValueFactory<>("montant"));
            colMontantTransaction.setCellFactory(column -> new TableCell<TransactionDTO, Double>() {
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
        if (colDateTransaction != null) {
            colDateTransaction.setCellValueFactory(new PropertyValueFactory<>("date"));
        }
        if (colStatutTransaction != null) {
            colStatutTransaction.setCellValueFactory(new PropertyValueFactory<>("statut"));
            colStatutTransaction.setCellFactory(column -> new TableCell<TransactionDTO, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        switch (item.toLowerCase()) {
                            case "validé":
                                setText("✅ Validé");
                                setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                                break;
                            case "en attente":
                                setText("⏳ En attente");
                                setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                                break;
                            case "rejeté":
                                setText("❌ Rejeté");
                                setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                                break;
                            default:
                                setText(item);
                                setStyle("");
                        }
                    }
                }
            });
        }
    }

    private void loadDashboardData() {
        Thread loadThread = new Thread(() -> {
            try {
                // Chargement des comptes
                List<CompteDTO> comptes = compteService.getComptesByClientId(currentClient.getId());

                // Chargement des transactions récentes
                List<TransactionDTO> transactions = transactionService.getTransactionsByClient(currentClient.getId());

                // Chargement des cartes (méthode alternative ou placeholder)
                int nombreCartes = 0;
                try {
                    // Si vous avez une méthode différente, utilisez-la ici
                    // Exemple: cartes = carteService.getAllCartes().stream()
                    //    .filter(carte -> carte.getClientId().equals(currentClient.getId()))
                    //    .toList();
                    // Pour l'instant, on utilise un placeholder
                    nombreCartes = 0; // À remplacer par la vraie logique
                } catch (Exception e) {
                    System.out.println("Service cartes non disponible: " + e.getMessage());
                }

                // Chargement des crédits (méthode alternative ou placeholder)
                int nombreCredits = 0;
                try {
                    // Si vous avez une méthode différente, utilisez-la ici
                    // Exemple: credits = creditService.getAllCredits().stream()
                    //    .filter(credit -> credit.getClientId().equals(currentClient.getId()))
                    //    .toList();
                    // Pour l'instant, on utilise un placeholder
                    nombreCredits = 0; // À remplacer par la vraie logique
                } catch (Exception e) {
                    System.out.println("Service crédits non disponible: " + e.getMessage());
                }

                // Mise à jour de l'interface
                final int finalNombreCartes = nombreCartes;
                final int finalNombreCredits = nombreCredits;

                Platform.runLater(() -> {
                    // Mise à jour des comptes
                    if (tableComptes != null) {
                        ObservableList<CompteDTO> comptesData = FXCollections.observableArrayList(comptes);
                        tableComptes.setItems(comptesData);
                    }

                    // Mise à jour des transactions récentes (5 dernières)
                    if (tableTransactionsRecentes != null) {
                        List<TransactionDTO> recentTransactions = transactions.stream()
                                .limit(5)
                                .toList();
                        ObservableList<TransactionDTO> transactionsData = FXCollections.observableArrayList(recentTransactions);
                        tableTransactionsRecentes.setItems(transactionsData);
                    }

                    // Mise à jour des statistiques
                    updateStatistics(comptes, finalNombreCartes, finalNombreCredits);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Erreur",
                            "Impossible de charger les données du tableau de bord: " + e.getMessage());
                    System.err.println("❌ Erreur chargement dashboard: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });

        loadThread.setDaemon(true);
        loadThread.start();
    }

    private void updateStatistics(List<CompteDTO> comptes, int nombreCartes, int nombreCredits) {
        // Nombre de comptes
        if (lblNbComptes != null) {
            lblNbComptes.setText(String.valueOf(comptes.size()));
        }

        // Solde total
        if (lblSoldeTotal != null) {
            double soldeTotal = comptes.stream()
                    .filter(c -> "Actif".equals(c.getStatut()))
                    .mapToDouble(CompteDTO::getSolde)
                    .sum();
            lblSoldeTotal.setText(String.format("%.2f FCFA", soldeTotal));
        }

        // Nombre de cartes
        if (lblNbCartes != null) {
            lblNbCartes.setText(String.valueOf(nombreCartes));
        }

        // Nombre de crédits
        if (lblNbCredits != null) {
            lblNbCredits.setText(String.valueOf(nombreCredits));
        }
    }

    // === MÉTHODES DE NAVIGATION - CORRESPONDANCE AVEC LE FXML ===

    @FXML
    private void goToTransactions(ActionEvent event) {
        navigateTo("/com/groupeisi/minisystemebancaire/client/UI_Transactions.fxml", event);
    }

    @FXML
    private void goToCredits(ActionEvent event) {
        navigateTo("/com/groupeisi/minisystemebancaire/client/UI_Credits.fxml", event);
    }

    @FXML
    private void goToCartes(ActionEvent event) {
        navigateTo("/com/groupeisi/minisystemebancaire/client/UI_Cartes.fxml", event);
    }

    @FXML
    private void goToSupport(ActionEvent event) {
        navigateTo("/com/groupeisi/minisystemebancaire/client/UI_Support.fxml", event);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
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

            Stage stage = (Stage) lblWelcome.getScene().getWindow();
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

    // === GETTERS/SETTERS POUR COMPATIBILITÉ ===

    public ClientDTO getCurrentClient() {
        return currentClient;
    }

    public void setCurrentClient(ClientDTO currentClient) {
        this.currentClient = currentClient;
    }
}