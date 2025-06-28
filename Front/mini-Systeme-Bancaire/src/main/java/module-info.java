module com.groupeisi.minisystemebancaire {
    // Modules JavaFX requis
    requires javafx.controls;
    requires javafx.fxml;

    // Modules réseau et HTTP pour communication avec Laravel
    requires java.net.http;
    requires okhttp3;

    // Modules pour JSON (communication avec Laravel API)
    requires com.google.gson;

    // Modules JFoenix
    requires com.jfoenix;

    // Modules Lombok pour les DTOs
    requires static lombok;

    // Modules pour PDF (iText seulement)
    requires itextpdf;
    requires java.desktop;

    // Exports
    exports com.groupeisi.minisystemebancaire;
    exports com.groupeisi.minisystemebancaire.controllers;
    exports com.groupeisi.minisystemebancaire.controllers.admin;
    exports com.groupeisi.minisystemebancaire.controllers.client;
    exports com.groupeisi.minisystemebancaire.dtos;
    exports com.groupeisi.minisystemebancaire.services;
    exports com.groupeisi.minisystemebancaire.utils;
    exports com.groupeisi.minisystemebancaire.config;

    // Opens pour JavaFX FXML
    opens com.groupeisi.minisystemebancaire to javafx.fxml;
    opens com.groupeisi.minisystemebancaire.controllers to javafx.fxml;
    opens com.groupeisi.minisystemebancaire.controllers.admin to javafx.fxml;
    opens com.groupeisi.minisystemebancaire.controllers.client to javafx.fxml;

    // Opens pour Gson (communication avec Laravel)
    opens com.groupeisi.minisystemebancaire.dtos to com.google.gson;
}