package com.groupeisi.minisystemebancaire;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HelloController {
    public static void main(String[] args) {
        try {
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("banquePU");
            System.out.println("✅ Persistence Unit Chargé avec succès !");
            emf.close();
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement de persistence.xml !");
            e.printStackTrace();
        }
    }
}