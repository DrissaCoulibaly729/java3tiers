package com.groupeisi.minisystemebancaire.controllers.client;


import gm.rahmanproperties.optibank.config.ApiConfig;
import gm.rahmanproperties.optibank.dtos.CreditDTo;
import gm.rahmanproperties.optibank.utils.CurrencyFormatter;
import gm.rahmanproperties.optibank.utils.ValidationUtils;
import gm.rahmanproperties.optibank.utils.WindowManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class CreditCarteController implements Initializable {
    @FXML private TextField montantField;
    @FXML private Slider dureeSlider;
    @FXML private Label tauxLabel;
    @FXML private Label mensualiteLabel;
    @FXML private Label coutTotalLabel;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String API_URL = ApiConfig.getApiUrl() + "/api/credits";
    private Long clientId; // À définir lors de la connexion

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurerControles();
//        configurerListeners();
    }

    private void configurerControles() {
//        dureeSlider.setMin(6);
//        dureeSlider.setMax(60);
//        dureeSlider.setValue(24);
//        dureeSlider.setBlockIncrement(6);

//        // Configuration de la validation du montant
//        montantField.textProperty().addListener((obs, old, newVal) -> {
//            if (!newVal.matches("\\d*\\.?\\d*")) {
//                montantField.setText(old);
//            }
//        });
    }

    private void configurerListeners() {
        // Mise à jour automatique de la simulation quand les valeurs changent
//        montantField.textProperty().addListener((obs, old, newVal) -> simulerCredit());
//        dureeSlider.valueProperty().addListener((obs, old, newVal) -> simulerCredit());
    }

    private void simulerCredit() {
        if (!ValidationUtils.isValidAmount(montantField.getText())) return;

        try {
            BigDecimal montant = new BigDecimal(montantField.getText());
            int duree = (int) dureeSlider.getValue();

            var response = restTemplate.getForEntity(
                API_URL + "/simulation?montant=" + montant +
                "&dureeEnMois=" + duree +
                "&tauxAnnuel=5.9",
                Map.class
            );

            if (response.getBody() != null) {
                Map<String, BigDecimal> result = response.getBody();
                mensualiteLabel.setText(CurrencyFormatter.format(result.get("mensualite")));
                tauxLabel.setText("5.9%");
                coutTotalLabel.setText(CurrencyFormatter.format(
                    result.get("mensualite").multiply(new BigDecimal(duree))
                ));
            }
        } catch (Exception e) {
            WindowManager.showError("Erreur",
                "Erreur lors de la simulation",
                e.getMessage());
        }
    }

    @FXML
    private void demanderCredit() {
        if (!validerFormulaire()) return;

        CreditDTo demande = new CreditDTo();
        demande.setMontant(new BigDecimal(montantField.getText()));
        demande.setDureeEnMois((int) dureeSlider.getValue());
        demande.setTauxAnnuel(new BigDecimal("5.9"));

        try {
            var response = restTemplate.postForEntity(
                API_URL + "/demande?clientId=" + clientId,
                demande,
                CreditDTo.class
            );

            if (response.getBody() != null) {
                WindowManager.showFxPopupSuccess("Demande envoyée\n"+
                    "Votre demande de crédit a été enregistrée avec succès.");
//                reinitialiserFormulaire();
            }
        } catch (Exception e) {
            WindowManager.showError("Erreur",
                "Impossible d'envoyer la demande",
                e.getMessage());
        }
    }

    private boolean validerFormulaire() {
        if (!ValidationUtils.isValidAmount(montantField.getText())) {
            WindowManager.showError("Erreur",
                "Montant invalide",
                "Veuillez saisir un montant valide.");
            return false;
        }


//        amortissementChart.getData().addAll(capitalSeries, interetsSeries);
        return true;
    }

    private CreditDTo createCreditRequest() {
        CreditDTo credit = new CreditDTo();
//        credit.setMontantDemande(new BigDecimal(montantField.getText()));
//        credit.setTauxInteret(new BigDecimal(tauxField.getText()));
//        credit.setDureeMois(Integer.parseInt(dureeField.getText()) * 12);
//        credit.setClientId(currentClient.getId());
//        credit.setDateDemande(LocalDateTime.now());
        return credit;
    }

    private boolean validateFields() {
        try {
            BigDecimal montant = new BigDecimal(montantField.getText());
//            BigDecimal taux = new BigDecimal(tauxField.getText());
//            int duree = Integer.parseInt(dureeField.getText());

//            if (montant.compareTo(BigDecimal.ZERO) <= 0 ||
//                taux.compareTo(BigDecimal.ZERO) <= 0 ||
//                duree <= 0) {
//                WindowManager.showError("Erreur", "Valeurs invalides",
//                    "Veuillez entrer des valeurs positives");
//                return false;
//            }

            return true;
        } catch (NumberFormatException e) {
            WindowManager.showError("Erreur", "Valeurs invalides",
                "Veuillez remplir tous les champs correctement");
            return false;
        }
    }

    private void clearFields() {

    }

    public void exporterEcheancier(ActionEvent actionEvent) {
    }
}
