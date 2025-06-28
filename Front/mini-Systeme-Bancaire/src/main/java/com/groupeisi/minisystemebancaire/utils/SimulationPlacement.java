package gm.rahmanproperties.optibank.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SimulationPlacement {
    public static class ResultatSimulation {
        private final BigDecimal montantInitial;
        private final BigDecimal montantFinal;
        private final BigDecimal interetsGeneres;
        private final int dureeAnnees;
        private final BigDecimal tauxInteret;

        public ResultatSimulation(BigDecimal montantInitial, BigDecimal montantFinal, 
                                BigDecimal interetsGeneres, int dureeAnnees, BigDecimal tauxInteret) {
            this.montantInitial = montantInitial;
            this.montantFinal = montantFinal;
            this.interetsGeneres = interetsGeneres;
            this.dureeAnnees = dureeAnnees;
            this.tauxInteret = tauxInteret;
        }

        public BigDecimal getMontantInitial() { return montantInitial; }
        public BigDecimal getMontantFinal() { return montantFinal; }
        public BigDecimal getInteretsGeneres() { return interetsGeneres; }
        public int getDureeAnnees() { return dureeAnnees; }
        public BigDecimal getTauxInteret() { return tauxInteret; }
    }

    public static ResultatSimulation simulerPlacement(BigDecimal montantInitial, int dureeAnnees, 
                                                    BigDecimal tauxInteretAnnuel, boolean interetsComposes) {
        BigDecimal montantFinal;
        if (interetsComposes) {
            // Formule des intérêts composés : M = C * (1 + t)^n
            BigDecimal base = BigDecimal.ONE.add(tauxInteretAnnuel.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP));
            BigDecimal exposant = BigDecimal.valueOf(Math.pow(base.doubleValue(), dureeAnnees));
            montantFinal = montantInitial.multiply(exposant).setScale(2, RoundingMode.HALF_UP);
        } else {
            // Formule des intérêts simples : M = C * (1 + n * t)
            BigDecimal interets = montantInitial
                .multiply(tauxInteretAnnuel.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP))
                .multiply(BigDecimal.valueOf(dureeAnnees));
            montantFinal = montantInitial.add(interets).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal interetsGeneres = montantFinal.subtract(montantInitial);

        return new ResultatSimulation(
            montantInitial,
            montantFinal,
            interetsGeneres,
            dureeAnnees,
            tauxInteretAnnuel
        );
    }

    public static BigDecimal calculerMensualiteEpargne(BigDecimal objectifEpargne, int dureeAnnees, BigDecimal tauxInteretAnnuel) {
        // Formule pour calculer la mensualité nécessaire pour atteindre un objectif d'épargne
        double taux = tauxInteretAnnuel.doubleValue() / 12 / 100; // Taux mensuel
        int nombreMois = dureeAnnees * 12;
        double objectif = objectifEpargne.doubleValue();

        double mensualite = (objectif * taux) / (Math.pow(1 + taux, nombreMois) - 1);

        return BigDecimal.valueOf(mensualite).setScale(2, RoundingMode.HALF_UP);
    }
}
