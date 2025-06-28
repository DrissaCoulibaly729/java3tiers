package com.groupeisi.minisystemebancaire.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

/**
 * ✅ Gestionnaire de navigation fluide pour éviter la fermeture/ouverture de fenêtres
 */
public class NavigationManager {

    private static BorderPane mainContainer;
    private static Node sidebar;

    /**
     * ✅ Initialiser le gestionnaire avec le conteneur principal
     */
    public static void initialize(BorderPane container) {
        mainContainer = container;
        sidebar = container.getLeft(); // Sauvegarder la sidebar
        System.out.println("🚀 NavigationManager initialisé");
    }

    /**
     * ✅ Naviguer vers une nouvelle vue en gardant la sidebar
     */
    public static void navigateTo(String fxmlPath) {
        if (mainContainer == null) {
            showError("NavigationManager non initialisé !");
            return;
        }

        try {
            System.out.println("🔄 Navigation vers : " + fxmlPath);

            // Charger la nouvelle vue
            FXMLLoader loader = new FXMLLoader(NavigationManager.class.getResource(fxmlPath));
            Node content = loader.load();

            // Remplacer seulement le contenu central
            mainContainer.setCenter(content);

            // S'assurer que la sidebar reste en place
            if (mainContainer.getLeft() == null) {
                mainContainer.setLeft(sidebar);
            }

            System.out.println("✅ Navigation réussie vers : " + fxmlPath);

        } catch (IOException e) {
            System.err.println("❌ Erreur navigation : " + e.getMessage());
            showError("Impossible de charger la page : " + fxmlPath + "\nErreur : " + e.getMessage());
        }
    }

    /**
     * ✅ Naviguer vers une vue avec passage de données au contrôleur
     */
    public static void navigateToWithData(String fxmlPath, Object data, String setterMethod) {
        if (mainContainer == null) {
            showError("NavigationManager non initialisé !");
            return;
        }

        try {
            System.out.println("🔄 Navigation avec données vers : " + fxmlPath);

            // Charger la nouvelle vue
            FXMLLoader loader = new FXMLLoader(NavigationManager.class.getResource(fxmlPath));
            Node content = loader.load();

            // Passer les données au contrôleur
            Object controller = loader.getController();
            if (controller != null && data != null) {
                try {
                    // Utiliser la réflexion pour appeler la méthode setter
                    controller.getClass().getMethod(setterMethod, data.getClass()).invoke(controller, data);
                    System.out.println("📋 Données passées au contrôleur via " + setterMethod);
                } catch (Exception e) {
                    System.err.println("⚠️ Impossible de passer les données : " + e.getMessage());
                }
            }

            // Remplacer le contenu central
            mainContainer.setCenter(content);

            // S'assurer que la sidebar reste en place
            if (mainContainer.getLeft() == null) {
                mainContainer.setLeft(sidebar);
            }

            System.out.println("✅ Navigation avec données réussie");

        } catch (IOException e) {
            System.err.println("❌ Erreur navigation avec données : " + e.getMessage());
            showError("Impossible de charger la page : " + fxmlPath + "\nErreur : " + e.getMessage());
        }
    }

    /**
     * ✅ Mettre à jour la sidebar (pour changer l'état actif des boutons)
     */
    public static void updateSidebar(String activeButton) {
        // Cette méthode peut être étendue pour mettre à jour visuellement
        // quel bouton est actif dans la sidebar
        System.out.println("🎯 Page active : " + activeButton);
    }

    /**
     * ✅ Obtenir le conteneur principal
     */
    public static BorderPane getMainContainer() {
        return mainContainer;
    }

    /**
     * ✅ Afficher une erreur
     */
    private static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur de Navigation");
        alert.setHeaderText("Problème de navigation");
        alert.setContentText(message);
        alert.showAndWait();
    }
}