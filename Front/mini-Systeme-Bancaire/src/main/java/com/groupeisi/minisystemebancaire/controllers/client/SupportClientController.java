package com.groupeisi.minisystemebancaire.controllers.client;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import com.groupeisi.minisystemebancaire.config.ApiConfig;
import com.groupeisi.minisystemebancaire.dtos.TicketSupportDTo;
import com.groupeisi.minisystemebancaire.services.HttpService;
import com.groupeisi.minisystemebancaire.utils.ValidationUtils;
import com.groupeisi.minisystemebancaire.utils.WindowManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class SupportClientController implements Initializable {

    // Formulaire de création de ticket
    @FXML private JFXTextField sujetField;
    @FXML private JFXComboBox<String> prioriteComboBox;
    @FXML private JFXComboBox<String> categorieComboBox;
    @FXML private JFXTextArea messageTextArea;
    @FXML private JFXButton creerTicketButton;
    @FXML private JFXButton reinitialiserButton;

    // Table des tickets
    @FXML private TableView<TicketSupportDTo> ticketsTable;
    @FXML private TableColumn<TicketSupportDTo, Long> idCol;
    @FXML private TableColumn<TicketSupportDTo, String> sujetCol;
    @FXML private TableColumn<TicketSupportDTo, String> statutCol;
    @FXML private TableColumn<TicketSupportDTo, String> prioriteCol;
    @FXML private TableColumn<TicketSupportDTo, LocalDateTime> dateCreationCol;

    // Détails du ticket sélectionné
    @FXML private Label detailIdLabel;
    @FXML private Label detailSujetLabel;
    @FXML private Label detailStatutLabel;
    @FXML private Label detailPrioriteLabel;
    @FXML private Label detailDateLabel;
    @FXML private TextArea detailMessageTextArea;

    // Boutons d'actions
    @FXML private JFXButton actualiserButton;
    @FXML private JFXButton fermerTicketButton;
    @FXML private JFXButton ajouterCommentaireButton;

    // Statistiques
    @FXML private Label totalTicketsLabel;
    @FXML private Label ticketsOuvertsLabel;
    @FXML private Label ticketsFermesLabel;

    private Long clientId;
    private TicketSupportDTo ticketSelectionne;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Vérifier l'authentification
        clientId = ApiConfig.getCurrentUserId();
        if (clientId == null) {
            redirectToLogin();
            return;
        }

        configurerInterface();
        configurerTable();
        configurerBoutons();
        chargerTickets();
    }

    private void configurerInterface() {
        // Configuration des ComboBox
        if (prioriteComboBox != null) {
            prioriteComboBox.setItems(FXCollections.observableArrayList(
                    "basse", "normale", "haute", "urgente"
            ));
            prioriteComboBox.setValue("normale");
        }

        if (categorieComboBox != null) {
            categorieComboBox.setItems(FXCollections.observableArrayList(
                    "Compte", "Carte bancaire", "Virement", "Crédit", "Application", "Autre"
            ));
            categorieComboBox.setValue("Autre");
        }

        // Configuration des champs
        if (sujetField != null) {
            sujetField.setPromptText("Résumé de votre demande");
        }
        if (messageTextArea != null) {
            messageTextArea.setPromptText("Décrivez votre problème en détail...");
            messageTextArea.setWrapText(true);
        }

        // Validation de la longueur
        if (messageTextArea != null) {
            messageTextArea.textProperty().addListener((obs, old, newValue) -> {
                if (newValue.length() > 1000) {
                    messageTextArea.setText(old);
                }
            });
        }
    }

    private void configurerTable() {
        // Configuration des colonnes
        if (idCol != null) {
            idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        }

        if (sujetCol != null) {
            sujetCol.setCellValueFactory(new PropertyValueFactory<>("sujet"));
        }

        if (statutCol != null) {
            statutCol.setCellValueFactory(new PropertyValueFactory<>("statut"));
            statutCol.setCellFactory(column -> new TableCell<TicketSupportDTo, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(formatStatut(item));
                        switch (item.toLowerCase()) {
                            case "ouvert":
                                setStyle("-fx-text-fill: green;");
                                break;
                            case "en_cours":
                                setStyle("-fx-text-fill: orange;");
                                break;
                            case "ferme":
                                setStyle("-fx-text-fill: red;");
                                break;
                            default:
                                setStyle("");
                        }
                    }
                }
            });
        }

        if (prioriteCol != null) {
            prioriteCol.setCellValueFactory(new PropertyValueFactory<>("priorite"));
            prioriteCol.setCellFactory(column -> new TableCell<TicketSupportDTo, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(formatPriorite(item));
                        switch (item.toLowerCase()) {
                            case "urgente":
                                setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                                break;
                            case "haute":
                                setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                                break;
                            case "normale":
                                setStyle("-fx-text-fill: blue;");
                                break;
                            case "basse":
                                setStyle("-fx-text-fill: gray;");
                                break;
                            default:
                                setStyle("");
                        }
                    }
                }
            });
        }

        if (dateCreationCol != null) {
            dateCreationCol.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
            dateCreationCol.setCellFactory(column -> new TableCell<TicketSupportDTo, LocalDateTime>() {
                @Override
                protected void updateItem(LocalDateTime item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                    }
                }
            });
        }

        // Gestion de la sélection
        if (ticketsTable != null) {
            ticketsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                ticketSelectionne = newSelection;
                afficherDetailsTicket(newSelection);
                mettreAJourBoutons();
            });
        }
    }

    private void configurerBoutons() {
        if (creerTicketButton != null) {
            creerTicketButton.setOnAction(e -> creerTicket());
        }
        if (reinitialiserButton != null) {
            reinitialiserButton.setOnAction(e -> reinitialiserFormulaire());
        }
        if (actualiserButton != null) {
            actualiserButton.setOnAction(e -> chargerTickets());
        }
        if (fermerTicketButton != null) {
            fermerTicketButton.setOnAction(e -> fermerTicket());
        }
        if (ajouterCommentaireButton != null) {
            ajouterCommentaireButton.setOnAction(e -> ajouterCommentaire());
        }

        mettreAJourBoutons();
    }

    private void chargerTickets() {
        HttpService.getListAsync("/api/support/tickets/client/" + clientId, TicketSupportDTo.class)
                .thenAccept(tickets -> {
                    Platform.runLater(() -> {
                        if (ticketsTable != null && tickets != null) {
                            ticketsTable.setItems(FXCollections.observableArrayList(tickets));
                            mettreAJourStatistiques(tickets);
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        WindowManager.showError("Erreur",
                                "Impossible de charger les tickets",
                                throwable.getMessage());
                    });
                    return null;
                });
    }

    private void mettreAJourStatistiques(List<TicketSupportDTo> tickets) {
        if (tickets == null) return;

        int total = tickets.size();
        long ouverts = tickets.stream().filter(t -> "ouvert".equals(t.getStatut()) || "en_cours".equals(t.getStatut())).count();
        long fermes = tickets.stream().filter(t -> "ferme".equals(t.getStatut())).count();

        if (totalTicketsLabel != null) {
            totalTicketsLabel.setText(String.valueOf(total));
        }
        if (ticketsOuvertsLabel != null) {
            ticketsOuvertsLabel.setText(String.valueOf(ouverts));
        }
        if (ticketsFermesLabel != null) {
            ticketsFermesLabel.setText(String.valueOf(fermes));
        }
    }

    @FXML
    private void creerTicket() {
        if (!validerFormulaire()) return;

        Map<String, Object> ticketData = new HashMap<>();
        ticketData.put("sujet", sujetField.getText().trim());
        ticketData.put("message", messageTextArea.getText().trim());
        ticketData.put("priorite", prioriteComboBox.getValue());
        ticketData.put("categorie", categorieComboBox.getValue());
        ticketData.put("client_id", clientId);

        // Désactiver le bouton pendant la création
        if (creerTicketButton != null) {
            creerTicketButton.setDisable(true);
            creerTicketButton.setText("Création en cours...");
        }

        HttpService.postAsync("/api/support/tickets", ticketData, TicketSupportDTo.class)
                .thenAccept(ticket -> {
                    Platform.runLater(() -> {
                        // Réactiver le bouton
                        if (creerTicketButton != null) {
                            creerTicketButton.setDisable(false);
                            creerTicketButton.setText("Créer le ticket");
                        }

                        if (ticket != null) {
                            reinitialiserFormulaire();
                            chargerTickets();
                            WindowManager.showSuccess("Succès",
                                    "Ticket créé",
                                    "Votre ticket de support a été créé avec succès. " +
                                            "Vous recevrez une réponse dans les meilleurs délais.");
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        // Réactiver le bouton en cas d'erreur
                        if (creerTicketButton != null) {
                            creerTicketButton.setDisable(false);
                            creerTicketButton.setText("Créer le ticket");
                        }

                        WindowManager.showError("Erreur",
                                "Impossible de créer le ticket",
                                "Une erreur s'est produite lors de la création du ticket. " +
                                        "Veuillez réessayer plus tard.");
                    });
                    return null;
                });
    }

    @FXML
    private void fermerTicket() {
        if (ticketSelectionne == null) {
            WindowManager.showWarning("Attention",
                    "Aucun ticket sélectionné",
                    "Veuillez sélectionner un ticket à fermer.");
            return;
        }

        if ("ferme".equals(ticketSelectionne.getStatut())) {
            WindowManager.showWarning("Attention",
                    "Ticket déjà fermé",
                    "Ce ticket est déjà fermé.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Fermer le ticket");
        confirmation.setContentText("Êtes-vous sûr de vouloir fermer ce ticket ? " +
                "Cette action est irréversible.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Map<String, String> updateData = new HashMap<>();
                updateData.put("statut", "ferme");

                HttpService.putAsync("/api/support/tickets/" + ticketSelectionne.getId(),
                                updateData, TicketSupportDTo.class)
                        .thenAccept(ticket -> {
                            Platform.runLater(() -> {
                                chargerTickets();
                                WindowManager.showSuccess("Succès",
                                        "Ticket fermé",
                                        "Le ticket a été fermé avec succès.");
                            });
                        })
                        .exceptionally(throwable -> {
                            Platform.runLater(() -> {
                                WindowManager.showError("Erreur",
                                        "Impossible de fermer le ticket",
                                        throwable.getMessage());
                            });
                            return null;
                        });
            }
        });
    }

    @FXML
    private void ajouterCommentaire() {
        if (ticketSelectionne == null) {
            WindowManager.showWarning("Attention",
                    "Aucun ticket sélectionné",
                    "Veuillez sélectionner un ticket pour ajouter un commentaire.");
            return;
        }

        // TODO: Ouvrir une fenêtre pour ajouter un commentaire
        WindowManager.showWarning("Fonctionnalité",
                "Commentaires en cours de développement",
                "La fonctionnalité de commentaires sera disponible prochainement.");
    }

    private boolean validerFormulaire() {
        if (!ValidationUtils.isNotEmpty(sujetField.getText())) {
            WindowManager.showError("Erreur de validation",
                    "Sujet manquant",
                    "Veuillez saisir un sujet pour votre ticket.");
            sujetField.requestFocus();
            return false;
        }

        if (sujetField.getText().trim().length() < 5) {
            WindowManager.showError("Erreur de validation",
                    "Sujet trop court",
                    "Le sujet doit contenir au moins 5 caractères.");
            sujetField.requestFocus();
            return false;
        }

        if (!ValidationUtils.isNotEmpty(messageTextArea.getText())) {
            WindowManager.showError("Erreur de validation",
                    "Message manquant",
                    "Veuillez décrire votre problème.");
            messageTextArea.requestFocus();
            return false;
        }

        if (messageTextArea.getText().trim().length() < 20) {
            WindowManager.showError("Erreur de validation",
                    "Message trop court",
                    "Le message doit contenir au moins 20 caractères.");
            messageTextArea.requestFocus();
            return false;
        }

        if (prioriteComboBox.getValue() == null) {
            WindowManager.showError("Erreur de validation",
                    "Priorité manquante",
                    "Veuillez sélectionner une priorité.");
            prioriteComboBox.requestFocus();
            return false;
        }

        return true;
    }

    private void reinitialiserFormulaire() {
        if (sujetField != null) sujetField.clear();
        if (messageTextArea != null) messageTextArea.clear();
        if (prioriteComboBox != null) prioriteComboBox.setValue("normale");
        if (categorieComboBox != null) categorieComboBox.setValue("Autre");
    }

    private void afficherDetailsTicket(TicketSupportDTo ticket) {
        if (ticket == null) {
            viderDetailsTicket();
            return;
        }

        if (detailIdLabel != null) {
            detailIdLabel.setText("#" + ticket.getId());
        }
        if (detailSujetLabel != null) {
            detailSujetLabel.setText(ticket.getSujet());
        }
        if (detailStatutLabel != null) {
            detailStatutLabel.setText(formatStatut(ticket.getStatut()));
        }
        if (detailPrioriteLabel != null) {
            detailPrioriteLabel.setText(formatPriorite(ticket.getPriorite()));
        }
        if (detailDateLabel != null && ticket.getDateCreation() != null) {
            detailDateLabel.setText(ticket.getDateCreation()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")));
        }
        if (detailMessageTextArea != null) {
            detailMessageTextArea.setText(ticket.getMessage());
        }
    }

    private void viderDetailsTicket() {
        if (detailIdLabel != null) detailIdLabel.setText("-");
        if (detailSujetLabel != null) detailSujetLabel.setText("-");
        if (detailStatutLabel != null) detailStatutLabel.setText("-");
        if (detailPrioriteLabel != null) detailPrioriteLabel.setText("-");
        if (detailDateLabel != null) detailDateLabel.setText("-");
        if (detailMessageTextArea != null) detailMessageTextArea.clear();
    }

    private void mettreAJourBoutons() {
        boolean ticketSelectionne = this.ticketSelectionne != null;
        boolean ticketOuvert = ticketSelectionne &&
                !"ferme".equals(this.ticketSelectionne.getStatut());

        if (fermerTicketButton != null) {
            fermerTicketButton.setDisable(!ticketOuvert);
        }
        if (ajouterCommentaireButton != null) {
            ajouterCommentaireButton.setDisable(!ticketOuvert);
        }
    }

    private String formatStatut(String statut) {
        switch (statut.toLowerCase()) {
            case "ouvert": return "Ouvert";
            case "en_cours": return "En cours";
            case "ferme": return "Fermé";
            default: return statut;
        }
    }

    private String formatPriorite(String priorite) {
        switch (priorite.toLowerCase()) {
            case "basse": return "Basse";
            case "normale": return "Normale";
            case "haute": return "Haute";
            case "urgente": return "Urgente";
            default: return priorite;
        }
    }

    private void redirectToLogin() {
        try {
            WindowManager.closeWindow();
            WindowManager.openWindow("/fxml/connexion.fxml", "Connexion");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}