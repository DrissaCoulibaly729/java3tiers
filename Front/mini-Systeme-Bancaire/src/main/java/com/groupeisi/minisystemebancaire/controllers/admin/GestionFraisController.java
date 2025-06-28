package com.groupeisi.minisystemebancaire.controllers.admin;

import com.groupeisi.minisystemebancaire.config.ApiConfig;
import com.groupeisi.minisystemebancaire.dtos.FraisBancaireDTo;
import com.groupeisi.minisystemebancaire.services.HttpService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class GestionFraisController implements Initializable {

    @FXML private TableView<FraisBancaireDTo> fraisTable;
    @FXML private TableColumn<FraisBancaireDTo, String> libelleCol;
    @FXML private TableColumn<FraisBancaireDTo, String> descriptionCol;
    @FXML private TableColumn<FraisBancaireDTo, BigDecimal> montantCol;
    @FXML private TableColumn<FraisBancaireDTo, String> typeCol;
    @FXML private TableColumn<FraisBancaireDTo, Boolean> actifCol;

    @FXML private TextField libelleField;
    @FXML private TextField montantField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private CheckBox actifCheckBox;

    @FXML private Button ajouterButton;
    @FXML private Button modifierButton;
    @FXML private Button supprimerButton;
    @FXML private Button actualiserButton;

    private FraisBancaireDTo fraisSelectionne;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Vérifier l'authentification
        if (!ApiConfig.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        configurerTable();
        configurerFormulaire();
        configurerBoutons();
        chargerFrais();
    }

    private void configurerTable() {
        // Configuration des colonnes
        if (libelleCol != null) {
            libelleCol.setCellValueFactory(new PropertyValueFactory<>("libelle"));
        }
        if (descriptionCol != null) {
            descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        }
        if (montantCol != null) {
            montantCol.setCellValueFactory(new PropertyValueFactory<>("montant"));
            montantCol.setCellFactory(column -> new TableCell<FraisBancaireDTo, BigDecimal>() {
                @Override
                protected void updateItem(BigDecimal item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.toString() + " FCFA");
                    }
                }
            });
        }
        if (typeCol != null) {
            typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        }
        if (actifCol != null) {
            actifCol.setCellValueFactory(new PropertyValueFactory<>("actif"));
            actifCol.setCellFactory(column -> new TableCell<FraisBancaireDTo, Boolean>() {
                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item ? "Actif" : "Inactif");
                        setStyle(item ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
                    }
                }
            });
        }

        // Gestion de la sélection
        if (fraisTable != null) {
            fraisTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                fraisSelectionne = newSelection;
                remplirFormulaire(newSelection);
                mettreAJourBoutons();
            });
        }
    }

    private void configurerFormulaire() {
        if (typeComboBox != null) {
            typeComboBox.setItems(FXCollections.observableArrayList("fixe", "pourcentage"));
            typeComboBox.setValue("fixe");
        }

        if (actifCheckBox != null) {
            actifCheckBox.setSelected(true);
        }

        // Validation du montant
        if (montantField != null) {
            montantField.textProperty().addListener((obs, old, newValue) -> {
                if (!ValidationUtils.isValidAmount(newValue) && !newValue.isEmpty()) {
                    montantField.setText(old);
                }
            });
        }
    }

    private void configurerBoutons() {
        if (ajouterButton != null) {
            ajouterButton.setOnAction(e -> ajouterFrais());
        }
        if (modifierButton != null) {
            modifierButton.setOnAction(e -> modifierFrais());
        }
        if (supprimerButton != null) {
            supprimerButton.setOnAction(e -> supprimerFrais());
        }
        if (actualiserButton != null) {
            actualiserButton.setOnAction(e -> chargerFrais());
        }

        mettreAJourBoutons();
    }

    private void chargerFrais() {
        HttpService.getListAsync("/api/admin/frais", FraisBancaireDTo.class)
                .thenAccept(frais -> {
                    Platform.runLater(() -> {
                        if (fraisTable != null && frais != null) {
                            fraisTable.setItems(FXCollections.observableArrayList(frais));
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        WindowManager.showError("Erreur",
                                "Impossible de charger les frais bancaires",
                                throwable.getMessage());
                    });
                    return null;
                });
    }

    @FXML
    private void ajouterFrais() {
        if (!validerChamps()) return;

        FraisBancaireDTo nouveauFrais = creerFraisDepuisFormulaire();

        HttpService.postAsync("/api/admin/frais", nouveauFrais, FraisBancaireDTo.class)
                .thenAccept(fraisAjoute -> {
                    Platform.runLater(() -> {
                        if (fraisAjoute != null) {
                            reinitialiserFormulaire();
                            chargerFrais();
                            WindowManager.showSuccess("Succès",
                                    "Frais ajouté",
                                    "Les frais ont été ajoutés avec succès.");
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        WindowManager.showError("Erreur",
                                "Impossible d'ajouter les frais",
                                throwable.getMessage());
                    });
                    return null;
                });
    }

    @FXML
    private void modifierFrais() {
        if (fraisSelectionne == null) {
            WindowManager.showWarning("Attention",
                    "Aucun frais sélectionné",
                    "Veuillez sélectionner un frais à modifier.");
            return;
        }

        if (!validerChamps()) return;

        FraisBancaireDTo fraisModifie = creerFraisDepuisFormulaire();
        fraisModifie.setId(fraisSelectionne.getId());

        HttpService.putAsync("/api/admin/frais/" + fraisSelectionne.getId(), fraisModifie, FraisBancaireDTo.class)
                .thenAccept(frais -> {
                    Platform.runLater(() -> {
                        if (frais != null) {
                            reinitialiserFormulaire();
                            chargerFrais();
                            WindowManager.showSuccess("Succès",
                                    "Frais modifié",
                                    "Les frais ont été modifiés avec succès.");
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        WindowManager.showError("Erreur",
                                "Impossible de modifier les frais",
                                throwable.getMessage());
                    });
                    return null;
                });
    }

    @FXML
    private void supprimerFrais() {
        if (fraisSelectionne == null) {
            WindowManager.showWarning("Attention",
                    "Aucun frais sélectionné",
                    "Veuillez sélectionner un frais à supprimer.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer les frais");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer ces frais ?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                HttpService.deleteAsync("/api/admin/frais/" + fraisSelectionne.getId())
                        .thenAccept(success -> {
                            Platform.runLater(() -> {
                                if (success) {
                                    reinitialiserFormulaire();
                                    chargerFrais();
                                    WindowManager.showSuccess("Succès",
                                            "Frais supprimé",
                                            "Les frais ont été supprimés avec succès.");
                                }
                            });
                        })
                        .exceptionally(throwable -> {
                            Platform.runLater(() -> {
                                WindowManager.showError("Erreur",
                                        "Impossible de supprimer les frais",
                                        throwable.getMessage());
                            });
                            return null;
                        });
            }
        });
    }

    private boolean validerChamps() {
        if (!ValidationUtils.isNotEmpty(libelleField.getText())) {
            WindowManager.showError("Erreur de validation",
                    "Libellé manquant",
                    "Veuillez saisir un libellé.");
            libelleField.requestFocus();
            return false;
        }

        if (!ValidationUtils.isNotEmpty(montantField.getText())) {
            WindowManager.showError("Erreur de validation",
                    "Montant manquant",
                    "Veuillez saisir un montant.");
            montantField.requestFocus();
            return false;
        }

        if (!ValidationUtils.isValidAmount(montantField.getText())) {
            WindowManager.showError("Erreur de validation",
                    "Montant invalide",
                    "Veuillez saisir un montant valide.");
            montantField.requestFocus();
            return false;
        }

        return true;
    }

    private FraisBancaireDTo creerFraisDepuisFormulaire() {
        FraisBancaireDTo frais = new FraisBancaireDTo();
        frais.setLibelle(libelleField.getText().trim());
        frais.setMontant(new BigDecimal(montantField.getText()));
        frais.setDescription(descriptionField.getText().trim());
        frais.setType(typeComboBox.getValue());
        frais.setActif(actifCheckBox.isSelected());
        return frais;
    }

    private void remplirFormulaire(FraisBancaireDTo frais) {
        if (frais == null) {
            reinitialiserFormulaire();
            return;
        }

        libelleField.setText(frais.getLibelle());
        montantField.setText(frais.getMontant().toString());
        descriptionField.setText(frais.getDescription());
        typeComboBox.setValue(frais.getType());
        actifCheckBox.setSelected(frais.getActif() != null ? frais.getActif() : true);
    }

    private void reinitialiserFormulaire() {
        libelleField.clear();
        montantField.clear();
        descriptionField.clear();
        typeComboBox.setValue("fixe");
        actifCheckBox.setSelected(true);
        fraisSelectionne = null;
        if (fraisTable != null) {
            fraisTable.getSelectionModel().clearSelection();
        }
        mettreAJourBoutons();
    }

    private void mettreAJourBoutons() {
        boolean selection = fraisSelectionne != null;
        if (modifierButton != null) {
            modifierButton.setDisable(!selection);
        }
        if (supprimerButton != null) {
            supprimerButton.setDisable(!selection);
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