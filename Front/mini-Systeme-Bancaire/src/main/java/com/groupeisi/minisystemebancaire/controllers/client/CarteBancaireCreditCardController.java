package com.groupeisi.minisystemebancaire.controllers.client;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXPasswordField;
import com.groupeisi.minisystemebancaire.config.ApiConfig;
import com.groupeisi.minisystemebancaire.dtos.CarteBancaireDTo;
import com.groupeisi.minisystemebancaire.dtos.CompteDTo;
import com.groupeisi.minisystemebancaire.services.HttpService;
import com.groupeisi.minisystemebancaire.services.CompteService;
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
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class CarteBancaireCreditCardController implements Initializable {

    // Tables et colonnes
    @FXML private TableView<CarteBancaireDTo> cartesTable;
    @FXML private TableColumn<CarteBancaireDTo, String> numeroCarteCol;
    @FXML private TableColumn<CarteBancaireDTo, String> typeCarteCol;
    @FXML private TableColumn<CarteBancaireDTo, String> statutCol;
    @FXML private TableColumn<CarteBancaireDTo, LocalDate> dateExpirationCol;
    @FXML private TableColumn<CarteBancaireDTo, BigDecimal> plafondJournalierCol;

    // Formulaire de demande
    @FXML private JFXComboBox<String> typeCarteCombo;
    @FXML private JFXComboBox<CompteDTo> compteCombo;
    @FXML private JFXTextField plafondJournalierField;
    @FXML private JFXTextField plafondMensuelField;
    @FXML private JFXPasswordField pinField;
    @FXML private JFXPasswordField confirmPinField;

    // Boutons
    @FXML private JFXButton demanderCarteBtn;
    @FXML private JFXButton bloquerCarteBtn;
    @FXML private JFXButton debloquerCarteBtn;
    @FXML private JFXButton modifierPlafondBtn;
    @FXML private JFXButton actualiserBtn;

    // Informations
    @FXML private Label infoNombreCartesLabel;
    @FXML private Label infoCartesActivesLabel;
    @FXML private Label infoCartesBloquees;

    private Long clientId;
    private CarteBancaireDTo carteSelectionnee;
    private List<CompteDTo> comptesClient;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Vérifier l'authentification
        clientId = ApiConfig.getCurrentUserId();
        if (clientId == null) {
            redirectToLogin();
            return;
        }

        configurerTable();
        configurerFormulaire();
        configurerBoutons();
        chargerDonnees();
    }

    private void configurerTable() {
        // Configuration des colonnes
        if (numeroCarteCol != null) {
            numeroCarteCol.setCellValueFactory(new PropertyValueFactory<>("numeroCarte"));
            numeroCarteCol.setCellFactory(column -> new TableCell<CarteBancaireDTo, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        // Masquer une partie du numéro de carte pour la sécurité
                        setText(item.replaceAll("\\d(?=\\d{4})", "*"));
                    }
                }
            });
        }

        if (typeCarteCol != null) {
            typeCarteCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        }

        if (statutCol != null) {
            statutCol.setCellValueFactory(new PropertyValueFactory<>("statut"));
            statutCol.setCellFactory(column -> new TableCell<CarteBancaireDTo, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        switch (item.toLowerCase()) {
                            case "active":
                                setStyle("-fx-text-fill: green;");
                                break;
                            case "bloquee":
                                setStyle("-fx-text-fill: red;");
                                break;
                            case "expiree":
                                setStyle("-fx-text-fill: orange;");
                                break;
                            default:
                                setStyle("");
                        }
                    }
                }
            });
        }

        if (dateExpirationCol != null) {
            dateExpirationCol.setCellValueFactory(new PropertyValueFactory<>("dateExpiration"));
            dateExpirationCol.setCellFactory(column -> new TableCell<CarteBancaireDTo, LocalDate>() {
                @Override
                protected void updateItem(LocalDate item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.format(DateTimeFormatter.ofPattern("MM/yy")));
                    }
                }
            });
        }

        if (plafondJournalierCol != null) {
            plafondJournalierCol.setCellValueFactory(new PropertyValueFactory<>("plafondJournalier"));
            plafondJournalierCol.setCellFactory(column -> new TableCell<CarteBancaireDTo, BigDecimal>() {
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

        // Gestion de la sélection
        if (cartesTable != null) {
            cartesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                carteSelectionnee = newSelection;
                mettreAJourBoutons();
            });
        }
    }

    private void configurerFormulaire() {
        // Types de cartes disponibles
        if (typeCarteCombo != null) {
            typeCarteCombo.setItems(FXCollections.observableArrayList("debit", "credit"));
            typeCarteCombo.setValue("debit");
        }

        // Validation des champs numériques
        if (plafondJournalierField != null) {
            plafondJournalierField.textProperty().addListener((obs, old, newValue) -> {
                if (!ValidationUtils.isValidAmount(newValue) && !newValue.isEmpty()) {
                    plafondJournalierField.setText(old);
                }
            });
        }

        if (plafondMensuelField != null) {
            plafondMensuelField.textProperty().addListener((obs, old, newValue) -> {
                if (!ValidationUtils.isValidAmount(newValue) && !newValue.isEmpty()) {
                    plafondMensuelField.setText(old);
                }
            });
        }

        // Validation du PIN
        if (pinField != null) {
            pinField.textProperty().addListener((obs, old, newValue) -> {
                if (!newValue.matches("\\d{0,4}")) {
                    pinField.setText(old);
                }
            });
        }

        if (confirmPinField != null) {
            confirmPinField.textProperty().addListener((obs, old, newValue) -> {
                if (!newValue.matches("\\d{0,4}")) {
                    confirmPinField.setText(old);
                }
            });
        }
    }

    private void configurerBoutons() {
        if (demanderCarteBtn != null) {
            demanderCarteBtn.setOnAction(e -> demanderCarte());
        }
        if (bloquerCarteBtn != null) {
            bloquerCarteBtn.setOnAction(e -> bloquerCarte());
        }
        if (debloquerCarteBtn != null) {
            debloquerCarteBtn.setOnAction(e -> debloquerCarte());
        }
        if (modifierPlafondBtn != null) {
            modifierPlafondBtn.setOnAction(e -> modifierPlafond());
        }
        if (actualiserBtn != null) {
            actualiserBtn.setOnAction(e -> chargerCartes());
        }

        mettreAJourBoutons();
    }

    private void chargerDonnees() {
        chargerComptes();
        chargerCartes();
    }

    private void chargerComptes() {
        CompteService.getComptesByClient(clientId)
                .thenAccept(comptes -> {
                    Platform.runLater(() -> {
                        this.comptesClient = comptes;
                        if (compteCombo != null && comptes != null) {
                            compteCombo.setItems(FXCollections.observableArrayList(comptes));

                            // Converter pour afficher le numéro de compte
                            compteCombo.setConverter(new javafx.util.StringConverter<CompteDTo>() {
                                @Override
                                public String toString(CompteDTo compte) {
                                    return compte != null ? compte.getNumeroCompte() + " (" + compte.getType() + ")" : "";
                                }

                                @Override
                                public CompteDTo fromString(String string) {
                                    return null;
                                }
                            });
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        WindowManager.showError("Erreur",
                                "Impossible de charger les comptes",
                                throwable.getMessage());
                    });
                    return null;
                });
    }

    private void chargerCartes() {
        HttpService.getListAsync("/api/cartes/client/" + clientId, CarteBancaireDTo.class)
                .thenAccept(cartes -> {
                    Platform.runLater(() -> {
                        if (cartesTable != null && cartes != null) {
                            cartesTable.setItems(FXCollections.observableArrayList(cartes));
                            mettreAJourStatistiques(cartes);
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        WindowManager.showError("Erreur",
                                "Impossible de charger les cartes",
                                throwable.getMessage());
                    });
                    return null;
                });
    }

    private void mettreAJourStatistiques(List<CarteBancaireDTo> cartes) {
        if (cartes == null) return;

        int nombreTotal = cartes.size();
        long cartesActives = cartes.stream().filter(c -> "active".equals(c.getStatut())).count();
        long cartesBloquees = cartes.stream().filter(c -> "bloquee".equals(c.getStatut())).count();

        if (infoNombreCartesLabel != null) {
            infoNombreCartesLabel.setText(String.valueOf(nombreTotal));
        }
        if (infoCartesActivesLabel != null) {
            infoCartesActivesLabel.setText(String.valueOf(cartesActives));
        }
        if (infoCartesBloquees != null) {
            infoCartesBloquees.setText(String.valueOf(cartesBloquees));
        }
    }

    @FXML
    private void demanderCarte() {
        if (!validerFormulaire()) return;

        Map<String, Object> demande = new HashMap<>();
        demande.put("type", typeCarteCombo.getValue());
        demande.put("compte_id", compteCombo.getValue().getId());
        demande.put("plafond_journalier", new BigDecimal(plafondJournalierField.getText()));
        demande.put("plafond_mensuel", new BigDecimal(plafondMensuelField.getText()));
        demande.put("pin", pinField.getText());

        HttpService.postAsync("/api/cartes", demande, CarteBancaireDTo.class)
                .thenAccept(carte -> {
                    Platform.runLater(() -> {
                        if (carte != null) {
                            viderFormulaire();
                            chargerCartes();
                            WindowManager.showSuccess("Succès",
                                    "Demande envoyée",
                                    "Votre demande de carte a été envoyée avec succès.");
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        WindowManager.showError("Erreur",
                                "Impossible de créer la carte",
                                throwable.getMessage());
                    });
                    return null;
                });
    }

    @FXML
    private void bloquerCarte() {
        if (carteSelectionnee == null) {
            WindowManager.showWarning("Attention",
                    "Aucune carte sélectionnée",
                    "Veuillez sélectionner une carte à bloquer.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Bloquer la carte");
        confirmation.setContentText("Êtes-vous sûr de vouloir bloquer cette carte ?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Map<String, String> request = new HashMap<>();
                request.put("statut", "bloquee");

                HttpService.putAsync("/api/cartes/" + carteSelectionnee.getId() + "/statut",
                                request, CarteBancaireDTo.class)
                        .thenAccept(carte -> {
                            Platform.runLater(() -> {
                                chargerCartes();
                                WindowManager.showSuccess("Succès",
                                        "Carte bloquée",
                                        "La carte a été bloquée avec succès.");
                            });
                        })
                        .exceptionally(throwable -> {
                            Platform.runLater(() -> {
                                WindowManager.showError("Erreur",
                                        "Impossible de bloquer la carte",
                                        throwable.getMessage());
                            });
                            return null;
                        });
            }
        });
    }

    @FXML
    private void debloquerCarte() {
        if (carteSelectionnee == null) {
            WindowManager.showWarning("Attention",
                    "Aucune carte sélectionnée",
                    "Veuillez sélectionner une carte à débloquer.");
            return;
        }

        Map<String, String> request = new HashMap<>();
        request.put("statut", "active");

        HttpService.putAsync("/api/cartes/" + carteSelectionnee.getId() + "/statut",
                        request, CarteBancaireDTo.class)
                .thenAccept(carte -> {
                    Platform.runLater(() -> {
                        chargerCartes();
                        WindowManager.showSuccess("Succès",
                                "Carte débloquée",
                                "La carte a été débloquée avec succès.");
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        WindowManager.showError("Erreur",
                                "Impossible de débloquer la carte",
                                throwable.getMessage());
                    });
                    return null;
                });
    }

    @FXML
    private void modifierPlafond() {
        if (carteSelectionnee == null) {
            WindowManager.showWarning("Attention",
                    "Aucune carte sélectionnée",
                    "Veuillez sélectionner une carte pour modifier les plafonds.");
            return;
        }

        // TODO: Ouvrir une fenêtre de modification des plafonds
        WindowManager.showWarning("Fonctionnalité",
                "En cours de développement",
                "Cette fonctionnalité sera disponible prochainement.");
    }

    private boolean validerFormulaire() {
        if (typeCarteCombo.getValue() == null) {
            WindowManager.showError("Erreur de validation",
                    "Type de carte manquant",
                    "Veuillez sélectionner un type de carte.");
            return false;
        }

        if (compteCombo.getValue() == null) {
            WindowManager.showError("Erreur de validation",
                    "Compte manquant",
                    "Veuillez sélectionner un compte.");
            return false;
        }

        if (!ValidationUtils.isNotEmpty(plafondJournalierField.getText()) ||
                !ValidationUtils.isValidAmount(plafondJournalierField.getText())) {
            WindowManager.showError("Erreur de validation",
                    "Plafond journalier invalide",
                    "Veuillez saisir un plafond journalier valide.");
            plafondJournalierField.requestFocus();
            return false;
        }

        if (!ValidationUtils.isNotEmpty(plafondMensuelField.getText()) ||
                !ValidationUtils.isValidAmount(plafondMensuelField.getText())) {
            WindowManager.showError("Erreur de validation",
                    "Plafond mensuel invalide",
                    "Veuillez saisir un plafond mensuel valide.");
            plafondMensuelField.requestFocus();
            return false;
        }

        if (!ValidationUtils.isNotEmpty(pinField.getText()) || pinField.getText().length() != 4) {
            WindowManager.showError("Erreur de validation",
                    "PIN invalide",
                    "Le PIN doit contenir exactement 4 chiffres.");
            pinField.requestFocus();
            return false;
        }

        if (!pinField.getText().equals(confirmPinField.getText())) {
            WindowManager.showError("Erreur de validation",
                    "Confirmation PIN",
                    "Les codes PIN ne correspondent pas.");
            confirmPinField.requestFocus();
            return false;
        }

        return true;
    }

    private void viderFormulaire() {
        if (typeCarteCombo != null) typeCarteCombo.setValue("debit");
        if (compteCombo != null) compteCombo.setValue(null);
        if (plafondJournalierField != null) plafondJournalierField.clear();
        if (plafondMensuelField != null) plafondMensuelField.clear();
        if (pinField != null) pinField.clear();
        if (confirmPinField != null) confirmPinField.clear();
    }

    private void mettreAJourBoutons() {
        boolean carteSelectionnee = this.carteSelectionnee != null;

        if (bloquerCarteBtn != null) {
            bloquerCarteBtn.setDisable(!carteSelectionnee ||
                    !"active".equals(this.carteSelectionnee != null ? this.carteSelectionnee.getStatut() : ""));
        }

        if (debloquerCarteBtn != null) {
            debloquerCarteBtn.setDisable(!carteSelectionnee ||
                    !"bloquee".equals(this.carteSelectionnee != null ? this.carteSelectionnee.getStatut() : ""));
        }

        if (modifierPlafondBtn != null) {
            modifierPlafondBtn.setDisable(!carteSelectionnee);
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