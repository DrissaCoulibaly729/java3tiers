package gm.rahmanproperties.optibank.controllers.admin;

import gm.rahmanproperties.optibank.config.ApiConfig;
import gm.rahmanproperties.optibank.dtos.FraisBancaireDTo;
import gm.rahmanproperties.optibank.utils.WindowManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;

public class GestionFraisController implements Initializable {
    @FXML private TableView<FraisBancaireDTo> fraisTable;
    @FXML private TextField libelleField;
    @FXML private TextField montantField;
    @FXML private TextField descriptionField;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String API_URL = ApiConfig.getApiUrl() + "/api/frais";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurerTable();
        chargerFrais();
    }

    private void configurerTable() {
        // Configuration des colonnes de la table...
    }

    private void chargerFrais() {
        try {
            FraisBancaireDTo[] frais = restTemplate.exchange(
                API_URL,
                HttpMethod.GET,
                new HttpEntity<>(null, ApiConfig.createHeaders()),
                FraisBancaireDTo[].class
            ).getBody();

            if (frais != null) {
                fraisTable.getItems().setAll(frais);
            }
        } catch (Exception e) {
            WindowManager.showError("Erreur",
                "Impossible de charger les frais bancaires",
                e.getMessage());
        }
    }

    @FXML
    private void ajouterFrais() {
//        if (!validerChamps()) return;
//
//        FraisBancaireDto nouveauFrais = new FraisBancaireDto();
//        nouveauFrais.setLibelle(libelleField.getText());
//        nouveauFrais.setMontant(new BigDecimal(montantField.getText()));
//        nouveauFrais.setDescription(descriptionField.getText());
//
//        try {
//            FraisBancaireDto fraisAjoute = restTemplate.postForObject(
//                API_URL,
//                nouveauFrais,
//                FraisBancaireDto.class
//            );
//
//            if (fraisAjoute != null) {
//                reinitialiserChamps();
//                chargerFrais();
//                WindowManager.showSuccess("Succès",
//                    "Frais ajouté",
//                    "Les frais ont été ajoutés avec succès.");
//            }
//        } catch (Exception e) {
//            WindowManager.showError("Erreur",
//                "Impossible d'ajouter les frais",
//                e.getMessage());
//        }
    }

    @FXML
    private void modifierFrais() {
//        FraisBancaireDto fraisSelectionne = fraisTable.getSelectionModel().getSelectedItem();
//        if (fraisSelectionne == null || !validerChamps()) return;
//
//        fraisSelectionne.setLibelle(libelleField.getText());
//        fraisSelectionne.setMontant(new BigDecimal(montantField.getText()));
//        fraisSelectionne.setDescription(descriptionField.getText());
//
//        try {
//            restTemplate.exchange(
//                API_URL + "/" + fraisSelectionne.getId(),
//                HttpMethod.PUT,
//                new HttpEntity<>(fraisSelectionne, ApiConfig.createHeaders()),
//                FraisBancaireDto.class
//            );
//
//            reinitialiserChamps();
//            chargerFrais();
//            WindowManager.showSuccess("Succès",
//                "Frais modifiés",
//                "Les frais ont été modifiés avec succès.");
//        } catch (Exception e) {
//            WindowManager.showError("Erreur",
//                "Impossible de modifier les frais",
//                e.getMessage());
//        }
    }

    @FXML
    private void supprimerFrais() {
        FraisBancaireDTo fraisSelectionne = fraisTable.getSelectionModel().getSelectedItem();
        if (fraisSelectionne == null) return;

        WindowManager.showConfirmation(
            "Confirmation",
            "Êtes-vous sûr de vouloir supprimer ces frais ?\n" +
            "Cette action est irréversible.",
                "Cette action nécessitera une validation supplémentaire.", () -> {
                try {
                    restTemplate.exchange(
                        API_URL + "/" + fraisSelectionne.getId(),
                        HttpMethod.DELETE,
                        new HttpEntity<>(null, ApiConfig.createHeaders()),
                        Void.class
                    );

                    reinitialiserChamps();
                    chargerFrais();
                    WindowManager.showSuccess("Succès",
                        "Frais supprimés\n" +
                        "Les frais ont été supprimés avec succès.");
                } catch (Exception e) {
                    WindowManager.showError("Erreur",
                        "Impossible de supprimer les frais",
                        e.getMessage());
                }
            }
        );
    }

    @FXML
    private void selectionnerFrais() {
//        FraisBancaireDto fraisSelectionne = fraisTable.getSelectionModel().getSelectedItem();
//        if (fraisSelectionne != null) {
//            libelleField.setText(fraisSelectionne.getLibelle());
//            montantField.setText(fraisSelectionne.getMontant().toString());
//            descriptionField.setText(fraisSelectionne.getDescription());
//        }
    }

    private boolean validerChamps() {
        if (libelleField.getText().trim().isEmpty()) {
            WindowManager.showError("Erreur",
                "Libellé requis",
                "Veuillez saisir un libellé.");
            return false;
        }

        try {
            new BigDecimal(montantField.getText());
        } catch (NumberFormatException e) {
            WindowManager.showError("Erreur",
                "Montant invalide",
                "Veuillez saisir un montant valide.");
            return false;
        }

        return true;
    }

    private void reinitialiserChamps() {
        libelleField.clear();
        montantField.clear();
        descriptionField.clear();
        fraisTable.getSelectionModel().clearSelection();
    }

    public void appliquerFrais(ActionEvent actionEvent) {
    }
}
