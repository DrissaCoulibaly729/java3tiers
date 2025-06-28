package com.groupeisi.minisystemebancaire.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utilitaire pour les simulations de placement financier
 */
public class SimulationPlacement {

    /**
     * Classe pour stocker les résultats d'une simulation
     */
    public static class ResultatSimulation {
        private final BigDecimal montantFinal;
        private final BigDecimal interetsGeneres;
        private final BigDecimal[] evolutionAnnuelle;

        public ResultatSimulation(BigDecimal montantFinal, BigDecimal interetsGeneres, BigDecimal[] evolutionAnnuelle) {
            this.montantFinal = montantFinal;
            this.interetsGeneres = interetsGeneres;
            this.evolutionAnnuelle = evolutionAnnuelle;
        }

        public BigDecimal getMontantFinal() {
            return montantFinal;
        }

        public BigDecimal getInteretsGeneres() {
            return interetsGeneres;
        }

        public BigDecimal[] getEvolutionAnnuelle() {
            return evolutionAnnuelle;
        }
    }

    /**
     * Calcule une simulation de placement avec intérêts simples ou composés
     *
     * @param montantInitial Le capital de départ
     * @param tauxAnnuel Le taux d'intérêt annuel (en pourcentage)
     * @param dureeAnnees La durée du placement en années
     * @param interetsComposes true pour intérêts composés, false pour intérêts simples
     * @return Le résultat de la simulation
     */
    public static ResultatSimulation calculerPlacement(
            BigDecimal montantInitial,
            double tauxAnnuel,
            int dureeAnnees,
            boolean interetsComposes) {

        // Convertir le taux en décimal
        BigDecimal taux = BigDecimal.valueOf(tauxAnnuel / 100);

        // Tableau pour stocker l'évolution année par année
        BigDecimal[] evolution = new BigDecimal[dureeAnnees + 1];
        evolution[0] = montantInitial;

        BigDecimal montantCourant = montantInitial;

        for (int annee = 1; annee <= dureeAnnees; annee++) {
            if (interetsComposes) {
                // Intérêts composés : Cn = C0 * (1 + t)^n
                montantCourant = montantCourant.multiply(BigDecimal.ONE.add(taux))
                        .setScale(2, RoundingMode.HALF_UP);
            } else {
                // Intérêts simples : Cn = C0 + (C0 * t * n)
                BigDecimal interetsAnnee = montantInitial.multiply(taux)
                        .setScale(2, RoundingMode.HALF_UP);
                montantCourant = montantCourant.add(interetsAnnee);
            }
            evolution[annee] = montantCourant;
        }

        BigDecimal montantFinal = evolution[dureeAnnees];
        BigDecimal interetsGeneres = montantFinal.subtract(montantInitial);

        return new ResultatSimulation(montantFinal, interetsGeneres, evolution);
    }

    /**
     * Calcule le montant final d'un placement avec intérêts composés
     */
    public static BigDecimal calculerInteretsComposes(BigDecimal capital, double taux, int duree) {
        BigDecimal tauxDecimal = BigDecimal.valueOf(taux / 100);
        BigDecimal facteur = BigDecimal.ONE.add(tauxDecimal);

        BigDecimal resultat = capital;
        for (int i = 0; i < duree; i++) {
            resultat = resultat.multiply(facteur);
        }

        return resultat.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcule le montant final d'un placement avec intérêts simples
     */
    public static BigDecimal calculerInteretsSimples(BigDecimal capital, double taux, int duree) {
        BigDecimal tauxDecimal = BigDecimal.valueOf(taux / 100);
        BigDecimal interets = capital.multiply(tauxDecimal).multiply(BigDecimal.valueOf(duree));

        return capital.add(interets).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcule le taux effectif d'un placement
     */
    public static double calculerTauxEffectif(BigDecimal montantInitial, BigDecimal montantFinal, int duree) {
        if (montantInitial.compareTo(BigDecimal.ZERO) == 0 || duree == 0) {
            return 0.0;
        }

        BigDecimal ratio = montantFinal.divide(montantInitial, 6, RoundingMode.HALF_UP);
        double tauxEffectif = Math.pow(ratio.doubleValue(), 1.0 / duree) - 1.0;

        return tauxEffectif * 100; // Retourner en pourcentage
    }
}