module com.groupeisi.minisystemebancaire {
    // Modules JavaFX requis
    requires java.net.http;
    requires com.google.gson;
    requires javafx.controls;
    requires javafx.fxml;
    requires gson.javatime.serialisers;

    // SUPPRIMÉ : jakarta.persistence (pas nécessaire pour JavaFX)
    // SUPPRIMÉ : org.hibernate.orm.core (pas nécessaire pour JavaFX)
    // SUPPRIMÉ : java.sql (pas nécessaire pour JavaFX)

    // Exports
    exports com.groupeisi.minisystemebancaire;
    exports com.groupeisi.minisystemebancaire.controllers;
    exports com.groupeisi.minisystemebancaire.controllers.admin;
    exports com.groupeisi.minisystemebancaire.controllers.client;
    exports com.groupeisi.minisystemebancaire.dto;
    exports com.groupeisi.minisystemebancaire.services;
    exports com.groupeisi.minisystemebancaire.utils;

    // Opens pour JavaFX FXML
    opens com.groupeisi.minisystemebancaire to javafx.fxml;
    opens com.groupeisi.minisystemebancaire.controllers to javafx.fxml;
    opens com.groupeisi.minisystemebancaire.controllers.admin to javafx.fxml;
    opens com.groupeisi.minisystemebancaire.controllers.client to javafx.fxml;

    // Opens pour Gson
    opens com.groupeisi.minisystemebancaire.dto to com.google.gson;

    // SUPPRIMÉ : opens pour Hibernate (pas nécessaire)
}