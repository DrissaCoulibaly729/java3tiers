package com.groupeisi.minisystemebancaire.controllers.admin;

import com.groupeisi.minisystemebancaire.config.ApiConfig;
import com.groupeisi.minisystemebancaire.dtos.*;
import com.groupeisi.minisystemebancaire.services.*;
import com.groupeisi.minisystemebancaire.utils.CurrencyFormatter;
import com.groupeisi.minisystemebancaire.utils.WindowManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class DashboardAdminController implements Initializable {

    // Labels pour les statistiques
    @FXML private Label totalClientsLabel;
    @FXML private Label totalComptesLabel;
    @FXML private Label totalTransactionsLabel;
    @FXML private Label soldeGlobalLabel;

    // Tables
    @FXML private TableView<TransactionDTo> dernieresTransactionsTable;
    @FXML private TableColumn<TransactionDTo, String> transactionTypeCol;
    @FXML private TableColumn<TransactionDTo, BigDecimal> transactionMontantCol;
    @FXML private TableColumn<TransactionDTo, String> transactionDateCol;
    @FXML private TableColumn<TransactionDTo, String> transactionStatutCol;

    @FXML private TableView<CreditDTo> demandesCreditsTable;
    @FXML private TableColumn<CreditDTo, BigDecimal> creditMontantCol;
    @FXML private TableColumn<CreditDTo, String> creditStatutCol;
    @FXML private TableColumn<CreditDTo, String> creditDateCol;

    @FXML private TableView<TicketSupportDTo> ticketsSupportTable;
    @FXML private TableColumn<TicketSupportDTo, String> ticketSujetCol;
    @FXML private TableColumn<TicketSupportDTo, String> ticketStatutCol;
    @FXML private TableColumn<TicketSupportDTo, String> ticketPrioriteCol;
    @FXML private TableColumn<TicketSupportDTo, String> ticketDateCol;

    // Graphiques
    @FXML private PieChart transactionsChart;
    @FXML private BarChart<String, Number> evolutionComptesChart;

    // Boutons de navigation
    @FXML private Button gestionClientsBtn;
    @FXML private Button gestionComptesBtn;
    @FXML private Button gestionCreditsBtn;
    @FXML private Button gestionFraisBtn;
    @FXML private Button parametresBtn;
    @FXML private Button deconnexionBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Vérifier si l'utilisateur est admin
        if (!ApiConfig.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        // Configurer les tables
        setupTables();

        // Configurer les boutons
        setupButtons();

        // Charger les données du tableau de bord
        loadDashboardData();
    }

    private void setupTables() {
        // Configuration table des transactions
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
        }
        if (transactionStatutCol != null) {
            transactionStatutCol.setCellValueFactory(new PropertyValueFactory<>("statut"));
        }

        // Configuration table des crédits
        if (creditMontantCol != null) {
            creditMontantCol.setCellValueFactory(new PropertyValueFactory<>("montant"));
            creditMontantCol.setCellFactory(column -> new TableCell<CreditDTo, BigDecimal>() {
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
        if (creditStatutCol != null) {
            creditStatutCol.setCellValueFactory(new PropertyValueFactory<>("statut"));
        }
        if (creditDateCol != null) {
            creditDateCol.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
        }

        // Configuration table des tickets de support
        if (ticketSujetCol != null) {
            ticketSujetCol.setCellValueFactory(new PropertyValueFactory<>("sujet"));
        }
        if (ticketStatutCol != null) {
            ticketStatutCol.setCellValueFactory(new PropertyValueFactory<>("statut"));
        }
        if (ticketPrioriteCol != null) {
            ticketPrioriteCol.setCellValueFactory(new PropertyValueFactory<>("priorite"));
        }
        if (ticketDateCol != null) {
            ticketDateCol.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
        }
    }

    private void setupButtons() {
        if (gestionClientsBtn != null) {
            gestionClientsBtn.setOnAction(e -> ouvrirGestionClients());
        }
        if (gestionComptesBtn != null) {
            gestionComptesBtn.setOnAction(e -> ouvrirGestionComptes());
        }
        if (gestionCreditsBtn != null) {
            gestionCreditsBtn.setOnAction(e -> ouvrirGestionCredits());
        }
        if (gestionFraisBtn != null) {
            gestionFraisBtn.setOnAction(e -> ouvrirGestionFrais());
        }
        if (parametresBtn != null) {
            parametresBtn.setOnAction(e -> ouvrirParametres());
        }
        if (deconnexionBtn != null) {
            deconnexionBtn.setOnAction(e -> seDeconnecter());
        }
    }

    private void loadDashboardData() {
        // Charger les statistiques générales
        loadStatistiques();

        // Charger les dernières transactions
        loadDernieresTransactions();

        // Charger les demandes de crédit en attente
        loadDemandesCredits();

        // Charger les tickets de support
        loadTicketsSupport();

        // Charger les données des graphiques
        loadChartsData();
    }

    private void loadStatistiques() {
        // Nombre total de clients
        ClientService.getAllClients()
                .thenAccept(clients -> {
                    Platform.runLater(() -> {
                        if (totalClientsLabel != null && clients != null) {
                            totalClientsLabel.setText(String.valueOf(clients.size()));
                        }
                    });
                })
                .exceptionally(throwable -> {
                    handleError("Erreur lors du chargement des clients", throwable);
                    return null;
                });

        // Vous pouvez ajouter d'autres statistiques ici
        // Par exemple, appeler des endpoints Laravel spécifiques pour les statistiques
    }

    private void loadDernieresTransactions() {
        // Charger les dernières transactions globales
        // Vous devrez peut-être créer un endpoint spécifique dans Laravel
        HttpService.getListAsync("/api/admin/transactions/recent?limit=20", TransactionDTo.class)
                .thenAccept(transactions -> {
                    Platform.runLater(() -> {
                        if (dernieresTransactionsTable != null && transactions != null) {
                            dernieresTransactionsTable.setItems(FXCollections.observableArrayList(transactions));

                            if (totalTransactionsLabel != null) {
                                totalTransactionsLabel.setText(String.valueOf(transactions.size()));
                            }
                        }
                    });
                })
                .exceptionally(throwable -> {
                    handleError("Erreur lors du chargement des transactions", throwable);
                    return null;
                });
    }

    private void loadDemandesCredits() {
        CreditService.getAllCredits()
                .thenAccept(credits -> {
                    Platform.runLater(() -> {
                        if (demandesCreditsTable != null && credits != null) {
                            // Filtrer les crédits en attente
                            List<CreditDTo> creditsEnAttente = credits.stream()
                                    .filter(credit -> "en_attente".equals(credit.getStatut()))
                                    .collect(Collectors.toList());

                            demandesCreditsTable.setItems(FXCollections.observableArrayList(creditsEnAttente));
                        }
                    });
                })
                .exceptionally(throwable -> {
                    handleError("Erreur lors du chargement des crédits", throwable);
                    return null;
                });
    }

    private void loadTicketsSupport() {
        // Charger les tickets de support
        // Vous devrez créer ce service
        HttpService.getListAsync("/api/admin/support/tickets", TicketSupportDTo.class)
                .thenAccept(tickets -> {
                    Platform.runLater(() -> {
                        if (ticketsSupportTable != null && tickets != null) {
                            ticketsSupportTable.setItems(FXCollections.observableArrayList(tickets));
                        }
                    });
                })
                .exceptionally(throwable -> {
                    // Gérer l'erreur silencieusement si le service n'existe pas encore
                    return null;
                });
    }

    private void loadChartsData() {
        // Charger les données pour les graphiques
        if (transactionsChart != null) {
            // Exemple de données pour le graphique en secteurs
            Platform.runLater(() -> {
                transactionsChart.getData().clear();
                transactionsChart.getData().addAll(
                        new PieChart.Data("Virements", 45),
                        new PieChart.Data("Dépôts", 30),
                        new PieChart.Data("Retraits", 25)
                );
            });
        }

        if (evolutionComptesChart != null) {
            // Exemple de données pour le graphique en barres
            Platform.runLater(() -> {
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("Nouveaux comptes");
                series.getData().add(new XYChart.Data<>("Jan", 23));
                series.getData().add(new XYChart.Data<>("Fév", 14));
                series.getData().add(new XYChart.Data<>("Mar", 35));
                series.getData().add(new XYChart.Data<>("Avr", 28));

                evolutionComptesChart.getData().clear();
                evolutionComptesChart.getData().add(series);
            });
        }
    }

    // Actions des boutons

    private void ouvrirGestionClients() {
        try {
            WindowManager.openWindow("/fxml/admin/gestion-clients.fxml", "Gestion des clients");
        } catch (Exception e) {
            handleError("Erreur lors de l'ouverture de la gestion des clients", e);
        }
    }

    private void ouvrirGestionComptes() {
        try {
            WindowManager.openWindow("/fxml/admin/gestion-comptes.fxml", "Gestion des comptes");
        } catch (Exception e) {
            handleError("Erreur lors de l'ouverture de la gestion des comptes", e);
        }
    }

    private void ouvrirGestionCredits() {
        try {
            WindowManager.openWindow("/fxml/admin/gestion-credits.fxml", "Gestion des crédits");
        } catch (Exception e) {
            handleError("Erreur lors de l'ouverture de la gestion des crédits", e);
        }
    }

    private void ouvrirGestionFrais() {
        try {
            WindowManager.openWindow("/fxml/admin/gestion-frais.fxml", "Gestion des frais");
        } catch (Exception e) {
            handleError("Erreur lors de l'ouverture de la gestion des frais", e);
        }
    }

    private void ouvrirParametres() {
        try {
            WindowManager.openWindow("/fxml/admin/parametres.fxml", "Paramètres");
        } catch (Exception e) {
            handleError("Erreur lors de l'ouverture des paramètres", e);
        }
    }

    private void seDeconnecter() {
        AuthService.logout()
                .thenRun(() -> {
                    Platform.runLater(this::redirectToLogin);
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        ApiConfig.logout();
                        redirectToLogin();
                    });
                    return null;
                });
    }

    private void redirectToLogin() {
        try {
            WindowManager.closeWindow();
            WindowManager.openWindow("/fxml/connexion.fxml", "Connexion");
        } catch (Exception e) {
            e.printStackTrace();
        }
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