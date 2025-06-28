package com.groupeisi.minisystemebancaire.controllers.client;

import com.groupeisi.minisystemebancaire.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class ClientSupportController {

    @FXML private Button btnDashboard;
    @FXML private Button btnTransactions;
    @FXML private Button btnCredits;
    @FXML private Button btnCartes;
    @FXML private Button btnDeconnexion;

    private Long clientId;

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    @FXML
    private void initialize() {
        if (SessionManager.isClientLoggedIn()) {
            clientId = SessionManager.getCurrentClient().getId();
        }
    }

    // Navigation methods (à implémenter comme dans ClientTransactionsController)
    @FXML private void goToDashboard() { /* Navigation logic */ }
    @FXML private void goToTransactions() { /* Navigation logic */ }
    @FXML private void goToCredits() { /* Navigation logic */ }
    @FXML private void goToCartes() { /* Navigation logic */ }
    @FXML private void handleLogout() { /* Logout logic */ }
}