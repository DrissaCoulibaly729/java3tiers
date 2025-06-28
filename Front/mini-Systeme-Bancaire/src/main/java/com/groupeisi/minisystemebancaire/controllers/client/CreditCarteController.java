package com.groupeisi.minisystemebancaire.controllers.client;

import com.groupeisi.minisystemebancaire.config.ApiConfig;
import com.groupeisi.minisystemebancaire.dtos.CreditDTo;
import com.groupeisi.minisystemebancaire.services.CreditService;
import com.groupeisi.minisystemebancaire.services.HttpService;
import com.groupeisi.minisystemebancaire.utils.CurrencyFormatter;
import com.groupeisi.minisystemebancaire.utils.ValidationUtils;
import com.groupeisi.minisystemebancaire.utils.WindowManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class CreditCarteController implements Initializable {

    // Formulaire de demande de crédit
    @FXML private TextField montantField;
    @FXML private Slider dureeSlider;
    @FXML private Label dureeLabel;
    @FXML private TextArea motifTextArea;
    @FXML private Button simulerButton;
    @FXML private Button demanderButton;
    @FXML private Button reinitialiserButton;

    // Résultats de simulation
    @FXML private Label tauxLabel;
    @FXML private Label mensualiteLabel;
    @FXML private Label coutTotalLabel;
    @FXML private Label dureeAffichageLabel;

    // Table des crédits
    @FXML private TableView<CreditDTo> creditsTable;
    @FXML private TableColumn<CreditDTo, BigDecimal> montantCol;
    @FXML private TableColumn<CreditDTo, Integer> dureeCol;
    @FXML private TableColumn<CreditDTo, BigDecimal> mensualiteCol;
    @FXML private TableColumn<CreditDTo, String> statutCol;
    @FXML private TableColumn<CreditDTo, LocalDateTime> dateDemandeCol;

    // Boutons d'actions sur les crédits
    @FXML private Button voirDetailsButton;
    @FXML private Button imprimerTableauButton;
    @FXML private Button actualiserButton;

    // Informations statistiques
    @FXML private Label nombreCreditsLabel;
    @FXML private Label totalMontantLabel;
    @FXML private Label creditEnCoursLabel;

    private Long clientId;
    private BigDecimal tauxInteretCourant = new BigDecimal("12.5"); // Taux par défaut

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Vérifier l'authentification
        clientId = ApiConfig.getCurrentUserId();
        if (clientId == null) {
            redirectToLogin();
            return;
        }

        configurerControles();
        configurerTable();
        configurerBoutons();
        chargerCredits();
    }

    private void configurerControles() {
        // Configuration du slider de durée
        if (dureeSlider != null) {
            dureeSlider.setMin(6);
            dureeSlider.setMax(60);
            dureeSlider.setValue(24);
            dureeSlider.setMajorTickUnit(12);
            dureeSlider.setMinorTickCount(5);
            dureeSlider.setShowTickLabels(true);
            dureeSlider.setShowTickMarks(true);
            dureeSlider.setSnapToTicks(true);

            // Listener pour mettre à jour le label de durée
            dureeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                int duree = newVal.intValue();
                if (dureeLabel != null) {
                    dureeLabel.setText(duree + " mois");
                }
                if (dureeAffichageLabel != null) {
                    dureeAffichageLabel.setText(duree + " mois");
                }
                // Recalculer automatiquement si un montant est saisi
                if (ValidationUtils.isValidAmount(montantField.getText())) {
                    calculerSimulation();
                }
            });

            // Initialiser le label
            if (dureeLabel != null) {
                dureeLabel.setText("24 mois");
            }
        }

        // Configuration de la validation du montant
        if (montantField != null) {
            montantField.textProperty().addListener((obs, old, newVal) -> {
                if (!ValidationUtils.isValidAmount(newVal) && !newVal.isEmpty()) {
                    montantField.setText(old);
                } else if (ValidationUtils.isValidAmount(newVal)) {
                    // Recalculer automatiquement la simulation
                    calculerSimulation();
                }
            });
        }
    }

    private void configurerTable() {
        // Configuration des colonnes
        if (montantCol != null) {
            montantCol.setCellValueFactory(new PropertyValueFactory<>("montant"));
            montantCol.setCellFactory(column -> new TableCell<CreditDTo, BigDecimal>() {
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

        if (dureeCol != null) {
            dureeCol.setCellValueFactory(new PropertyValueFactory<>("dureeEnMois"));
            dureeCol.setCellFactory(column -> new TableCell<CreditDTo, Integer>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item + " mois");
                    }
                }
            });
        }

        if (mensualiteCol != null) {
            mensualiteCol.setCellValueFactory(new PropertyValueFactory<>("mensualite"));
            mensualiteCol.setCellFactory(column -> new TableCell<CreditDTo, BigDecimal>() {
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

        if (statutCol != null) {
            statutCol.setCellValueFactory(new PropertyValueFactory<>("statut"));
            statutCol.setCellFactory(column -> new TableCell<CreditDTo, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(formatStatut(item));
                        switch (item.toLowerCase()) {
                            case "approuve":
                                setStyle("-fx-text-fill: green;");
                                break;
                            case "en_attente":
                                setStyle("-fx-text-fill: orange;");
                                break;
                            case "refuse":
                                setStyle("-fx-text-fill: red;");
                                break;
                            case "rembourse":
                                setStyle("-fx-text-fill: blue;");
                                break;
                            default:
                                setStyle("");
                        }
                    }
                }
            });
        }

        if (dateDemandeCol != null) {
            dateDemandeCol.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
            dateDemandeCol.setCellFactory(column -> new TableCell<CreditDTo, LocalDateTime>() {
                @Override
                protected void updateItem(LocalDateTime item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    }
                }
            });
        }

        // Gestion de la sélection dans la table
        if (creditsTable != null) {
            creditsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                mettreAJourBoutonsTable();
            });
        }
    }

    private void configurerBoutons() {
        if (simulerButton != null) {
            simulerButton.setOnAction(e -> calculerSimulation());
        }
        if (demanderButton != null) {
            demanderButton.setOnAction(e -> demanderCredit());
        }
        if (reinitialiserButton != null) {
            reinitialiserButton.setOnAction(e -> reinitialiserFormulaire());
        }
        if (voirDetailsButton != null) {
            voirDetailsButton.setOnAction(e -> voirDetailsCredit());
        }
        if (imprimerTableauButton != null) {
            imprimerTableauButton.setOnAction(e -> imprimerTableauAmortissement());
        }
        if (actualiserButton != null) {
            actualiserButton.setOnAction(e -> chargerCredits());
        }

        mettreAJourBoutonsTable();
    }

    private void chargerCredits() {
        CreditService.getCreditsByClient(clientId)
                .thenAccept(credits -> {
                    Platform.runLater(() -> {
                        if (creditsTable != null && credits != null) {
                            creditsTable.setItems(FXCollections.observableArrayList(credits));
                            mettreAJourStatistiques(credits);
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        WindowManager.showError("Erreur",
                                "Impossible de charger les crédits",
                                throwable.getMessage());
                    });
                    return null;
                });
    }

    private void mettreAJourStatistiques(List<CreditDTo> credits) {
        if (credits == null) return;

        int nombreCredits = credits.size();
        BigDecimal totalMontant = credits.stream()
                .map(CreditDTo::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long creditsEnCours = credits.stream()
                .filter(c -> "approuve".equals(c.getStatut()))
                .count();

        if (nombreCreditsLabel != null) {
            nombreCreditsLabel.setText(String.valueOf(nombreCredits));
        }
        if (totalMontantLabel != null) {
            totalMontantLabel.setText(CurrencyFormatter.format(totalMontant.doubleValue()));
        }
        if (creditEnCoursLabel != null) {
            creditEnCoursLabel.setText(String.valueOf(creditsEnCours));
        }
    }

    @FXML
    private void calculerSimulation() {
        if (!validerChampsSimulation()) return;

        BigDecimal montant = new BigDecimal(montantField.getText());
        Integer dureeEnMois = (int) dureeSlider.getValue();

        CreditService.simulerCreditMap(montant, dureeEnMois, tauxInteretCourant)
                .thenAccept(simulation -> {
                    Platform.runLater(() -> {
                        if (simulation != null) {
                            afficherResultatsSimulation(simulation);
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        // Si le service de simulation n'existe pas, calculer localement
                        calculerSimulationLocale(montant, dureeEnMois);
                    });
                    return null;
                });
    }

    private void calculerSimulationLocale(BigDecimal montant, Integer dureeEnMois) {
        // Calcul local de la mensualité avec la formule des annuités
        double capitalDouble = montant.doubleValue();
        double tauxMensuel = tauxInteretCourant.doubleValue() / 100 / 12;
        double nbMois = dureeEnMois.doubleValue();

        double mensualite;
        if (tauxMensuel == 0) {
            // Si taux = 0, mensualité = capital / durée
            mensualite = capitalDouble / nbMois;
        } else {
            // Formule classique : M = C * (t * (1+t)^n) / ((1+t)^n - 1)
            mensualite = capitalDouble *
                    (tauxMensuel * Math.pow(1 + tauxMensuel, nbMois)) /
                    (Math.pow(1 + tauxMensuel, nbMois) - 1);
        }

        double coutTotal = mensualite * nbMois;
        double coutCredit = coutTotal - capitalDouble;

        // Afficher les résultats
        if (tauxLabel != null) {
            tauxLabel.setText(tauxInteretCourant + " % /an");
        }
        if (mensualiteLabel != null) {
            mensualiteLabel.setText(CurrencyFormatter.format(mensualite));
        }
        if (coutTotalLabel != null) {
            coutTotalLabel.setText(CurrencyFormatter.format(coutTotal) +
                    " (dont " + CurrencyFormatter.format(coutCredit) + " d'intérêts)");
        }
    }

    private void afficherResultatsSimulation(Map<String, Object> simulation) {
        if (tauxLabel != null && simulation.containsKey("taux")) {
            tauxLabel.setText(simulation.get("taux") + " % /an");
        }
        if (mensualiteLabel != null && simulation.containsKey("mensualite")) {
            Double mensualite = ((Number) simulation.get("mensualite")).doubleValue();
            mensualiteLabel.setText(CurrencyFormatter.format(mensualite));
        }
        if (coutTotalLabel != null && simulation.containsKey("cout_total")) {
            Double coutTotal = ((Number) simulation.get("cout_total")).doubleValue();
            coutTotalLabel.setText(CurrencyFormatter.format(coutTotal));
        }
    }

    @FXML
    private void demanderCredit() {
        if (!validerDemandeCredit()) return;

        BigDecimal montant = new BigDecimal(montantField.getText());
        Integer dureeEnMois = (int) dureeSlider.getValue();
        String motif = motifTextArea.getText().trim();

        // Désactiver le bouton pendant la requête
        if (demanderButton != null) {
            demanderButton.setDisable(true);
            demanderButton.setText("Envoi en cours...");
        }

        CreditService.demanderCredit(clientId, montant, dureeEnMois, motif)
                .thenAccept(credit -> {
                    Platform.runLater(() -> {
                        // Réactiver le bouton
                        if (demanderButton != null) {
                            demanderButton.setDisable(false);
                            demanderButton.setText("Demander le crédit");
                        }

                        if (credit != null) {
                            viderFormulaire();
                            chargerCredits();
                            WindowManager.showSuccess("Succès",
                                    "Demande envoyée",
                                    "Votre demande de crédit a été envoyée avec succès. " +
                                            "Vous recevrez une réponse sous 48h ouvrées.");
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        // Réactiver le bouton en cas d'erreur
                        if (demanderButton != null) {
                            demanderButton.setDisable(false);
                            demanderButton.setText("Demander le crédit");
                        }

                        WindowManager.showError("Erreur",
                                "Impossible d'envoyer la demande",
                                "Une erreur s'est produite lors de l'envoi de votre demande. " +
                                        "Veuillez réessayer plus tard.");
                    });
                    return null;
                });
    }

    private boolean validerChampsSimulation() {
        if (!ValidationUtils.isNotEmpty(montantField.getText())) {
            WindowManager.showError("Erreur de validation",
                    "Montant manquant",
                    "Veuillez saisir le montant souhaité.");
            montantField.requestFocus();
            return false;
        }

        if (!ValidationUtils.isValidAmount(montantField.getText())) {
            WindowManager.showError("Erreur de validation",
                    "Montant invalide",
                    "Veuillez saisir un montant valide (nombres et points autorisés).");
            montantField.requestFocus();
            return false;
        }

        try {
            BigDecimal montant = new BigDecimal(montantField.getText());
            if (montant.compareTo(new BigDecimal("10000")) < 0) {
                WindowManager.showError("Erreur de validation",
                        "Montant trop faible",
                        "Le montant minimum est de 10 000 FCFA.");
                montantField.requestFocus();
                return false;
            }

            if (montant.compareTo(new BigDecimal("10000000")) > 0) {
                WindowManager.showError("Erreur de validation",
                        "Montant trop élevé",
                        "Le montant maximum est de 10 000 000 FCFA.");
                montantField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            WindowManager.showError("Erreur de validation",
                    "Format de montant invalide",
                    "Veuillez saisir un montant numérique valide.");
            montantField.requestFocus();
            return false;
        }

        return true;
    }

    private boolean validerDemandeCredit() {
        if (!validerChampsSimulation()) return false;

        if (!ValidationUtils.isNotEmpty(motifTextArea.getText())) {
            WindowManager.showError("Erreur de validation",
                    "Motif manquant",
                    "Veuillez préciser le motif de votre demande de crédit.");
            motifTextArea.requestFocus();
            return false;
        }

        if (motifTextArea.getText().trim().length() < 10) {
            WindowManager.showError("Erreur de validation",
                    "Motif trop court",
                    "Le motif doit contenir au moins 10 caractères.");
            motifTextArea.requestFocus();
            return false;
        }

        if (motifTextArea.getText().trim().length() > 500) {
            WindowManager.showError("Erreur de validation",
                    "Motif trop long",
                    "Le motif ne peut pas dépasser 500 caractères.");
            motifTextArea.requestFocus();
            return false;
        }

        return true;
    }

    private void viderFormulaire() {
        if (montantField != null) montantField.clear();
        if (dureeSlider != null) {
            dureeSlider.setValue(24);
            if (dureeLabel != null) dureeLabel.setText("24 mois");
        }
        if (motifTextArea != null) motifTextArea.clear();

        // Vider les résultats de simulation
        if (tauxLabel != null) tauxLabel.setText("-");
        if (mensualiteLabel != null) mensualiteLabel.setText("-");
        if (coutTotalLabel != null) coutTotalLabel.setText("-");
    }

    private String formatStatut(String statut) {
        switch (statut.toLowerCase()) {
            case "en_attente": return "En attente";
            case "approuve": return "Approuvé";
            case "refuse": return "Refusé";
            case "rembourse": return "Remboursé";
            default: return statut;
        }
    }

    @FXML
    private void reinitialiserFormulaire() {
        viderFormulaire();
    }

    @FXML
    private void voirDetailsCredit() {
        CreditDTo creditSelectionne = creditsTable.getSelectionModel().getSelectedItem();
        if (creditSelectionne == null) {
            WindowManager.showWarning("Attention",
                    "Aucun crédit sélectionné",
                    "Veuillez sélectionner un crédit dans la table pour voir les détails.");
            return;
        }

        // Afficher les détails du crédit dans une boîte de dialogue
        StringBuilder details = new StringBuilder();
        details.append("Montant : ").append(CurrencyFormatter.format(creditSelectionne.getMontant().doubleValue())).append("\n");
        details.append("Durée : ").append(creditSelectionne.getDureeEnMois()).append(" mois\n");
        details.append("Taux : ").append(creditSelectionne.getTauxInteret()).append(" %\n");
        if (creditSelectionne.getMensualite() != null) {
            details.append("Mensualité : ").append(CurrencyFormatter.format(creditSelectionne.getMensualite().doubleValue())).append("\n");
        }
        details.append("Statut : ").append(formatStatut(creditSelectionne.getStatut())).append("\n");
        details.append("Date de demande : ").append(creditSelectionne.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"))).append("\n");
        if (creditSelectionne.getMotif() != null) {
            details.append("Motif : ").append(creditSelectionne.getMotif());
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails du crédit");
        alert.setHeaderText("Crédit #" + creditSelectionne.getId());
        alert.setContentText(details.toString());
        alert.showAndWait();
    }

    @FXML
    private void imprimerTableauAmortissement() {
        CreditDTo creditSelectionne = creditsTable.getSelectionModel().getSelectedItem();
        if (creditSelectionne == null || !"approuve".equals(creditSelectionne.getStatut())) {
            WindowManager.showWarning("Attention",
                    "Aucun crédit approuvé sélectionné",
                    "Veuillez sélectionner un crédit approuvé pour imprimer le tableau d'amortissement.");
            return;
        }

        // TODO: Implémenter la génération du tableau d'amortissement
        WindowManager.showWarning("Fonctionnalité",
                "Impression en cours de développement",
                "La génération du tableau d'amortissement sera disponible prochainement.");
    }

    private void mettreAJourBoutonsTable() {
        boolean creditSelectionne = creditsTable != null &&
                creditsTable.getSelectionModel().getSelectedItem() != null;

        if (voirDetailsButton != null) {
            voirDetailsButton.setDisable(!creditSelectionne);
        }

        if (imprimerTableauButton != null) {
            boolean creditApprouve = creditSelectionne &&
                    "approuve".equals(creditsTable.getSelectionModel().getSelectedItem().getStatut());
            imprimerTableauButton.setDisable(!creditApprouve);
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