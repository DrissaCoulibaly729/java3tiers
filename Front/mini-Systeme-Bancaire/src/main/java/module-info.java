module com.groupeisi.minisystemebancaire {
    requires java.net.http;
    requires com.google.gson;
    requires javafx.controls;
    requires javafx.fxml;
    requires jakarta.persistence;
    requires org.hibernate.orm.core; // Ajoute Hibernate ORM
    requires java.sql;
    requires gson.javatime.serialisers; // Pour JDBC




    // Ouvre les contrôleurs pour JavaFX
    opens com.groupeisi.minisystemebancaire.controllers.admin to javafx.fxml;
    opens com.groupeisi.minisystemebancaire.controllers to javafx.fxml;
    opens com.groupeisi.minisystemebancaire to javafx.fxml;

    // 🔥 Ouvre le package des modèles pour Hibernate
    opens com.groupeisi.minisystemebancaire.models to org.hibernate.orm.core, jakarta.persistence;

    exports com.groupeisi.minisystemebancaire;
    exports com.groupeisi.minisystemebancaire.dto;
    opens com.groupeisi.minisystemebancaire.dto to javafx.fxml, com.google.gson;
    exports com.groupeisi.minisystemebancaire.controllers;
    exports com.groupeisi.minisystemebancaire.mappers;
    opens com.groupeisi.minisystemebancaire.mappers to javafx.fxml;
    exports com.groupeisi.minisystemebancaire.models;
    //opens com.groupeisi.minisystemebancaire.models to javafx.fxml;
    exports com.groupeisi.minisystemebancaire.repositories;
    opens com.groupeisi.minisystemebancaire.repositories to javafx.fxml;
    exports com.groupeisi.minisystemebancaire.services;
    opens com.groupeisi.minisystemebancaire.services to javafx.fxml;
    exports com.groupeisi.minisystemebancaire.controllers.client;
    opens com.groupeisi.minisystemebancaire.controllers.client to javafx.fxml;
    exports com.groupeisi.minisystemebancaire.controllers.admin;
}
