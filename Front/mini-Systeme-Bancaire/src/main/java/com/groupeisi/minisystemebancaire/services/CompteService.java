package com.groupeisi.minisystemebancaire.services;

import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.groupeisi.minisystemebancaire.dto.CompteDTO;
import com.groupeisi.minisystemebancaire.dto.ClientDTO;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class CompteService {
    private static final String BASE_URL = "http://localhost:8000/api";
    private final HttpClient httpClient;
    private final Gson gson;

    public CompteService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        this.gson = Converters.registerOffsetDateTime(new GsonBuilder())
                .setLenient()
                .create();
    }

    private HttpRequest.Builder createRequest(String endpoint) {
        return HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(30));
    }

    private String sendRequestForString(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        } else {
            throw new RuntimeException("Erreur API: " + response.statusCode() + " - " + response.body());
        }
    }

    /**
     * ✅ MÉTHODE PRINCIPALE : Récupérer les comptes d'un client par son ID
     */
    public List<CompteDTO> getComptesByClientId(Long clientId) {
        try {
            System.out.println("🔄 Envoi de la requête vers: " + BASE_URL + "/comptes/client/" + clientId);

            HttpRequest request = createRequest("/comptes/client/" + clientId).GET().build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("📡 Réponse reçue - Status: " + response.statusCode());
            System.out.println("📄 Corps de la réponse: " + response.body());

            if (response.statusCode() == 200) {
                Type compteListType = new TypeToken<List<CompteDTO>>(){}.getType();
                List<CompteDTO> comptes = gson.fromJson(response.body(), compteListType);

                if (comptes == null) {
                    comptes = new ArrayList<>();
                }

                System.out.println("✅ " + comptes.size() + " comptes récupérés pour le client ID: " + clientId);
                return comptes;

            } else if (response.statusCode() == 404) {
                System.out.println("ℹ️ Aucun compte trouvé pour le client ID: " + clientId);
                return new ArrayList<>();

            } else {
                System.err.println("❌ Erreur lors de la récupération des comptes - Status: " + response.statusCode());
                throw new RuntimeException("Erreur API: " + response.statusCode() + " - " + response.body());
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des comptes du client: " + e.getMessage());
            throw new RuntimeException("Impossible de récupérer les comptes: " + e.getMessage());
        }
    }

    /**
     * Récupérer tous les comptes
     */
    public List<CompteDTO> getAllComptes() {
        try {
            System.out.println("🔄 Récupération de tous les comptes...");
            System.out.println("🔄 Envoi de la requête vers: " + BASE_URL + "/comptes");

            HttpRequest request = createRequest("/comptes").GET().build();
            String response = sendRequestForString(request);

            System.out.println("📡 Réponse reçue - Status: 200");
            System.out.println("📄 Corps de la réponse: " + response);

            Type compteListType = new TypeToken<List<CompteDTO>>(){}.getType();
            List<CompteDTO> comptes = gson.fromJson(response, compteListType);

            if (comptes == null) {
                comptes = new ArrayList<>();
            }

            System.out.println("✅ " + comptes.size() + " comptes récupérés avec succès");
            return comptes;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération de la liste des comptes: " + e.getMessage());
            throw new RuntimeException("Impossible de récupérer la liste des comptes: " + e.getMessage());
        }
    }

    /**
     * Récupérer un compte par son ID
     */
    public CompteDTO getCompteById(Long id) {
        try {
            System.out.println("🔄 Récupération du compte ID: " + id);

            HttpRequest request = createRequest("/comptes/" + id).GET().build();
            String response = sendRequestForString(request);

            CompteDTO compte = gson.fromJson(response, CompteDTO.class);
            System.out.println("✅ Compte récupéré: " + compte.getNumero());
            return compte;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération du compte ID " + id + ": " + e.getMessage());
            throw new RuntimeException("Impossible de récupérer le compte: " + e.getMessage());
        }
    }

    /**
     * Récupérer un compte par son numéro
     */
    public CompteDTO getCompteByNumero(String numero) {
        try {
            System.out.println("🔄 Récupération du compte numéro: " + numero);

            HttpRequest request = createRequest("/comptes/numero/" + numero).GET().build();
            String response = sendRequestForString(request);

            CompteDTO compte = gson.fromJson(response, CompteDTO.class);
            System.out.println("✅ Compte récupéré: " + compte.getNumero());
            return compte;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération du compte " + numero + ": " + e.getMessage());
            throw new RuntimeException("Impossible de récupérer le compte: " + e.getMessage());
        }
    }

    /**
     * Créer un nouveau compte
     */
    public CompteDTO createCompte(CompteDTO compte) {
        try {
            System.out.println("🔄 Création d'un nouveau compte pour le client ID: " + compte.getClientId());

            String json = gson.toJson(compte);
            System.out.println("📤 Création compte avec données: " + json);

            HttpRequest request = createRequest("/comptes")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("📡 Réponse reçue - Status: " + response.statusCode());
            System.out.println("📄 Corps de la réponse: " + response.body());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                CompteDTO nouveauCompte = gson.fromJson(response.body(), CompteDTO.class);
                System.out.println("✅ Compte créé avec succès: " + nouveauCompte.getNumero());
                return nouveauCompte;
            } else {
                System.err.println("❌ Erreur lors de la création du compte - Status: " + response.statusCode());
                throw new RuntimeException("Erreur API: " + response.statusCode() + " - " + response.body());
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la création du compte: " + e.getMessage());
            throw new RuntimeException("Impossible de créer le compte: " + e.getMessage());
        }
    }

    /**
     * Mettre à jour un compte
     */
    public CompteDTO updateCompte(CompteDTO compte) {
        try {
            System.out.println("🔄 Mise à jour du compte: " + compte.getNumero());

            String json = gson.toJson(compte);
            System.out.println("📤 Mise à jour compte avec données: " + json);

            HttpRequest request = createRequest("/comptes/" + compte.getId())
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("📡 Réponse reçue - Status: " + response.statusCode());
            System.out.println("📄 Corps de la réponse: " + response.body());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                CompteDTO compteModifie = gson.fromJson(response.body(), CompteDTO.class);
                System.out.println("✅ Compte mis à jour avec succès: " + compteModifie.getNumero());
                return compteModifie;
            } else {
                System.err.println("❌ Erreur lors de la mise à jour du compte - Status: " + response.statusCode());
                throw new RuntimeException("Erreur API: " + response.statusCode() + " - " + response.body());
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la mise à jour du compte: " + e.getMessage());
            throw new RuntimeException("Impossible de mettre à jour le compte: " + e.getMessage());
        }
    }

    /**
     * Supprimer un compte
     */
    public void deleteCompte(Long id) {
        try {
            System.out.println("🔄 Suppression du compte ID: " + id);

            HttpRequest request = createRequest("/comptes/" + id)
                    .DELETE()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("📡 Réponse reçue - Status: " + response.statusCode());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("✅ Compte supprimé avec succès");
            } else {
                System.err.println("❌ Erreur lors de la suppression du compte - Status: " + response.statusCode());
                throw new RuntimeException("Erreur API: " + response.statusCode() + " - " + response.body());
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la suppression du compte: " + e.getMessage());
            throw new RuntimeException("Impossible de supprimer le compte: " + e.getMessage());
        }
    }

    /**
     * Fermer un compte
     */
    public CompteDTO fermerCompte(Long id) {
        try {
            System.out.println("🔄 Fermeture du compte ID: " + id);

            HttpRequest request = createRequest("/comptes/" + id + "/fermer")
                    .PUT(HttpRequest.BodyPublishers.ofString("{}"))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("📡 Réponse reçue - Status: " + response.statusCode());
            System.out.println("📄 Corps de la réponse: " + response.body());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                CompteDTO compteFerme = gson.fromJson(response.body(), CompteDTO.class);
                System.out.println("✅ Compte fermé avec succès: " + compteFerme.getNumero());
                return compteFerme;
            } else {
                System.err.println("❌ Erreur lors de la fermeture du compte - Status: " + response.statusCode());
                throw new RuntimeException("Erreur API: " + response.statusCode() + " - " + response.body());
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la fermeture du compte: " + e.getMessage());
            throw new RuntimeException("Impossible de fermer le compte: " + e.getMessage());
        }
    }

    /**
     * Appliquer des frais à un compte - Version avec 2 paramètres (pour la compatibilité)
     */
    public CompteDTO appliquerFrais(Long id, Double montantFrais) {
        return appliquerFrais(id, "Frais bancaires", montantFrais);
    }

    /**
     * Appliquer des frais à un compte - Version complète avec type de frais
     */
    public CompteDTO appliquerFrais(Long id, String typeFrais, Double montantFrais) {
        try {
            System.out.println("🔄 Application de frais au compte ID: " + id + " - Type: " + typeFrais + " - Montant: " + montantFrais);

            FraisRequest fraisRequest = new FraisRequest(montantFrais, typeFrais);
            String json = gson.toJson(fraisRequest);
            System.out.println("📤 Frais avec données: " + json);

            HttpRequest request = createRequest("/comptes/" + id + "/frais")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("📡 Réponse reçue - Status: " + response.statusCode());
            System.out.println("📄 Corps de la réponse: " + response.body());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                CompteDTO compteModifie = gson.fromJson(response.body(), CompteDTO.class);
                System.out.println("✅ Frais appliqués avec succès au compte: " + compteModifie.getNumero());
                return compteModifie;
            } else {
                System.err.println("❌ Erreur lors de l'application des frais - Status: " + response.statusCode());
                throw new RuntimeException("Erreur API: " + response.statusCode() + " - " + response.body());
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'application des frais: " + e.getMessage());
            throw new RuntimeException("Impossible d'appliquer les frais: " + e.getMessage());
        }
    }

    /**
     * Vérifier si un compte existe
     */
    public boolean compteExists(Long id) {
        try {
            getCompteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Vérifier si un numéro de compte existe
     */
    public boolean numeroCompteExists(String numero) {
        try {
            getCompteByNumero(numero);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // === CLASSES INTERNES POUR LES REQUÊTES ===

    public static class FraisRequest {
        private Double montant;
        private String type;

        public FraisRequest(Double montant) {
            this.montant = montant;
            this.type = "Frais bancaires";
        }

        public FraisRequest(Double montant, String type) {
            this.montant = montant;
            this.type = type != null ? type : "Frais bancaires";
        }

        public Double getMontant() { return montant; }
        public void setMontant(Double montant) { this.montant = montant; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }
}