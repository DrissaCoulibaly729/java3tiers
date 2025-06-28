package com.groupeisi.minisystemebancaire.controllers.client;

import com.jfoenix.controls.JFXButton;
import com.groupeisi.minisystemebancaire.config.ApiConfig;
import com.groupeisi.minisystemebancaire.dtos.*;
import com.groupeisi.minisystemebancaire.services.*;
import com.groupeisi.minisystemebancaire.utils.CurrencyFormatter;
import com.groupeisi.minisystemebancaire.utils.WindowManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class DashboardClientController implements Initializable {
    @FXML private Label welcomeLabel;
    @FXML private Label soldeLabel;
    @FXML private Label nombreTransactionsLabel;

    @FXML private TableView<CompteDTo> comptesTable;
    @FXML private TableColumn<CompteDTo, String> accountNumberCol;
    @FXML private TableColumn<CompteDTo, String> accountTypeCol;
    @FXML private TableColumn<CompteDTo, BigDecimal> accountBalanceCol;

    @FXML private TableView<TransactionDTo> dernieresTransactionsTable;
    @FXML private TableColumn<TransactionDTo, String> transactionTypeCol;
    @FXML private TableColumn<TransactionDTo, BigDecimal> transactionMontantCol;
    @FXML private TableColumn<TransactionDTo, String> transactionDateCol;
    @FXML private TableColumn<TransactionDTo, String> transactionStatutCol;

    @FXML private PieChart depensesChart;

    @FXML private JFXButton virementsBtn;
    @FXML private JFXButton cartesBtn;
    @FXML private JFXButton creditsBtn;
    @FXML private JFXButton supportBtn;
    @FXML private JFXButton deconnexionBtn;

    private ClientDTo clientConnecte;
    private Long clientId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Vérifier si l'utilisateur est connecté
        clientId = ApiConfig.getCurrentUserId();
        if (clientId == null) {
            // Rediriger vers la connexion si pas connecté
            Platform.runLater(() -> {
                WindowManager.showError("Erreur", "Session expirée", "Veuillez vous reconnecter.");
                try {
                    WindowManager.closeWindow();
                    WindowManager.openWindow("/fxml/connexion.fxml", "Connexion");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            return;
        }

        // Configurer les tables
        setupTables();

        // Configurer les boutons
        setupButtons();

        // Charger les données
        loadDashboardData();
    }

    private void setupTables() {
        // Configuration de la table des comptes
        if (accountNumberCol != null) {
            accountNumberCol.setCellValueFactory(new PropertyValueFactory<>("numeroCompte"));
        }
        if (accountTypeCol != null) {
            accountTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        }
        if (accountBalanceCol != null) {
            accountBalanceCol.setCellValueFactory(new PropertyValueFactory<>("solde"));
            accountBalanceCol.setCellFactory(column -> new TableCell<CompteDTo, BigDecimal>() {
                @Override
                protected void updateItem(BigDecimal item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(CurrencyFormatter.format(item.doubleValue()));
                    }
                }
            });
        }

        // Configuration de la table des transactions
        if (transactionTypeCol != null) {
            transactionTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        }
        if (transactionMontantCol != null) {
            transactionMontantCol.setCellValueFactory(new PropertyValueFactory<>("montant"));
            transactionMontantCol.setCellFactory(column -> new TableCell<TransactionDTo, BigDecimal>() {
                @Override
                protected void updateItem(BigDecimal item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(CurrencyFormatter.format(item.doubleValue()));
                    }
                }
            });
        }
        if (transactionDateCol != null) {
            transactionDateCol.setCellValueFactory(new PropertyValueFactory<>("dateTransaction"));
            transactionDateCol.setCellFactory(column -> new TableCell<TransactionDTo, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        // Formatter la date si nécessaire
                        setText(item);
                    }
                }
            });
        }
        if (transactionStatutCol != null) {
            transactionStatutCol.setCellValueFactory(new PropertyValueFactory<>("statut"));
        }
    }

    private void setupButtons() {
        if (virementsBtn != null) {
            virementsBtn.setOnAction(e -> ouvrirVirements());
        }
        if (cartesBtn != null) {
            cartesBtn.setOnAction(e -> ouvrirCartes());
        }
        if (creditsBtn != null) {
            creditsBtn.setOnAction(e -> ouvrirCredits());
        }
        if (supportBtn != null) {
            supportBtn.setOnAction(e -> ouvrirSupport());
        }
        if (deconnexionBtn != null) {
            deconnexionBtn.setOnAction(e -> seDeconnecter());
        }
    }

    private void loadDashboardData() {
        // Charger les informations du client
        ClientService.getClientById(clientId)
                .thenAccept(client -> {
                    this.clientConnecte = client;
                    Platform.runLater(() -> {
                        if (welcomeLabel != null && client != null) {
                            welcomeLabel.setText("Bienvenue, " + client.getPrenom() + " " + client.getNom());
                        }
                    });
                })
                .exceptionally(throwable -> {
                    handleError("Erreur lors du chargement du profil client", throwable);
                    return null;
                });

        // Charger les comptes
        CompteService.getComptesByClient(clientId)
                .thenAccept(comptes -> {
                    Platform.runLater(() -> {
                        if (comptesTable != null && comptes != null) {
                            comptesTable.setItems(FXCollections.observableArrayList(comptes));

                            // Calculer le solde total
                            BigDecimal soldeTotal = comptes.stream()
                                    .map(CompteDTo::getSolde)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                            if (soldeLabel != null) {
                                soldeLabel.setText(CurrencyFormatter.format(soldeTotal.doubleValue()));
                            }
                        }
                    });
                })
                .exceptionally(throwable -> {
                    handleError("Erreur lors du chargement des comptes", throwable);
                    return null;
                });

        // Charger les dernières transactions
        TransactionService.getDernieresTransactions(clientId, 10)
                .thenAccept(transactions -> {
                    Platform.runLater(() -> {
                        if (dernieresTransactionsTable != null && transactions != null) {
                            dernieresTransactionsTable.setItems(FXCollections.observableArrayList(transactions));

                            if (nombreTransactionsLabel != null) {
                                nombreTransactionsLabel.setText(String.valueOf(transactions.size()));
                            }
                        }
                    });
                })
                .exceptionally(throwable -> {
                    handleError("Erreur lors du chargement des transactions", throwable);
                    return null;
                });

        // Charger les données pour le graphique
        loadChartData();
    }

    private void loadChartData() {
        if (depensesChart == null) return;

        // Exemple de données pour le graphique des dépenses
        // Vous pouvez adapter cela en fonction de votre API Laravel
        TransactionService.getDernieresTransactions(clientId, 50)
                .thenAccept(transactions -> {
                    Platform.runLater(() -> {
                        Map<String, BigDecimal> depensesParType = new HashMap<>();

                        if (transactions != null) {
                            for (TransactionDTo transaction : transactions) {
                                if ("retrait".equals(transaction.getType()) || "virement".equals(transaction.getType())) {
                                    String type = transaction.getType();
                                    depensesParType.merge(type, transaction.getMontant(), BigDecimal::add);
                                }
                            }
                        }

                        // Créer les données pour le PieChart
                        depensesChart.getData().clear();
                        for (Map.Entry<String, BigDecimal> entry : depensesParType.entrySet()) {
                            PieChart.Data slice = new PieChart.Data(
                                    entry.getKey(),
                                    entry.getValue().doubleValue()
                            );
                            depensesChart.getData().add(slice);
                        }
                    });
                })
                .exceptionally(throwable -> {
                    // Ignorer les erreurs du graphique pour ne pas perturber l'interface
                    return null;
                });
    }

    // Actions des boutons

    private void ouvrirVirements() {
        try {
            WindowManager.openWindow("/fxml/client/virements.fxml", "Virements");
        } catch (Exception e) {
            handleError("Erreur lors de l'ouverture des virements", e);
        }
    }

    private void ouvrirCartes() {
        try {
            WindowManager.openWindow("/fxml/client/cartes.fxml", "Gestion des cartes");
        } catch (Exception e) {
            handleError("Erreur lors de l'ouverture de la gestion des cartes", e);
        }
    }

    private void ouvrirCredits() {
        try {
            WindowManager.openWindow("/fxml/client/credits.fxml", "Demandes de crédit");
        } catch (Exception e) {
            handleError("Erreur lors de l'ouverture des crédits", e);
        }
    }

    private void ouvrirSupport() {
        try {
            WindowManager.openWindow("/fxml/client/support.fxml", "Support client");
        } catch (Exception e) {
            handleError("Erreur lors de l'ouverture du support", e);
        }
    }

    private void seDeconnecter() {
        AuthService.logout()
                .thenRun(() -> {
                    Platform.runLater(() -> {
                        try {
                            WindowManager.closeWindow();
                            WindowManager.openWindow("/fxml/connexion.fxml", "Connexion");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                })
                .exceptionally(throwable -> {
                    // Même en cas d'erreur, on déconnecte localement
                    Platform.runLater(() -> {
                        ApiConfig.logout();
                        try {
                            WindowManager.closeWindow();
                            WindowManager.openWindow("/fxml/connexion.fxml", "Connexion");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    return null;
                });
    }

    private void handleError(String message, Throwable throwable) {
        Platform.runLater(() -> {
            String errorMessage = message;
            if (throwable != null && throwable.getMessage() != null) {
                errorMessage += ": " + throwable.getMessage();
            }
            WindowManager.showError("Erreur", "Erreur de chargement", errorMessage);
        });
    }
}