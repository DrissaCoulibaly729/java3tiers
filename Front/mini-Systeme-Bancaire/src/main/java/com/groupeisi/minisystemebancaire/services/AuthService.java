package com.groupeisi.minisystemebancaire.services;

import com.groupeisi.minisystemebancaire.config.ApiConfig;
import com.groupeisi.minisystemebancaire.dtos.ClientDTo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service d'authentification pour communiquer avec Laravel Sanctum/Passport
 */
public class AuthService {

    /**
     * Connexion utilisateur
     */
    public static CompletableFuture<LoginResponse> login(String email, String password) {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", email);
        credentials.put("password", password);

        return HttpService.postAsync("/api/auth/login", credentials, LoginResponse.class)
                .thenApply(response -> {
                    if (response != null && response.getToken() != null) {
                        // Sauvegarder le token et l'utilisateur
                        ApiConfig.setAuthToken(response.getToken());
                        ApiConfig.setCurrentUserId(response.getUser().getId());
                    }
                    return response;
                });
    }

    /**
     * Déconnexion
     */
    public static CompletableFuture<Boolean> logout() {
        return HttpService.postAsync("/api/auth/logout", null, Void.class)
                .thenApply(response -> {
                    // Effacer les données locales
                    ApiConfig.logout();
                    return true;
                })
                .exceptionally(throwable -> {
                    // Même en cas d'erreur, on efface les données locales
                    ApiConfig.logout();
                    return false;
                });
    }

    /**
     * Inscription nouveau client
     */
    public static CompletableFuture<RegisterResponse> register(RegisterRequest request) {
        return HttpService.postAsync("/api/auth/register", request, RegisterResponse.class);
    }

    /**
     * Mot de passe oublié
     */
    public static CompletableFuture<Boolean> forgotPassword(String email) {
        Map<String, String> request = new HashMap<>();
        request.put("email", email);

        return HttpService.postAsync("/api/auth/forgot-password", request, Map.class)
                .thenApply(response -> true)
                .exceptionally(throwable -> false);
    }

    /**
     * Réinitialiser mot de passe
     */
    public static CompletableFuture<Boolean> resetPassword(String token, String email, String password, String passwordConfirmation) {
        Map<String, String> request = new HashMap<>();
        request.put("token", token);
        request.put("email", email);
        request.put("password", password);
        request.put("password_confirmation", passwordConfirmation);

        return HttpService.postAsync("/api/auth/reset-password", request, Map.class)
                .thenApply(response -> true)
                .exceptionally(throwable -> false);
    }

    /**
     * Obtenir le profil utilisateur connecté
     */
    public static CompletableFuture<ClientDTo> getCurrentUser() {
        return HttpService.getAsync("/api/auth/user", ClientDTo.class);
    }

    /**
     * Vérifier si le token est valide
     */
    public static CompletableFuture<Boolean> validateToken() {
        if (!ApiConfig.isLoggedIn()) {
            return CompletableFuture.completedFuture(false);
        }

        return getCurrentUser()
                .thenApply(user -> user != null)
                .exceptionally(throwable -> {
                    ApiConfig.logout();
                    return false;
                });
    }

    // Classes pour les réponses
    public static class LoginResponse {
        private String token;
        private String tokenType;
        private ClientDTo user;
        private long expiresIn;

        // Getters et setters
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }

        public String getTokenType() { return tokenType; }
        public void setTokenType(String tokenType) { this.tokenType = tokenType; }

        public ClientDTo getUser() { return user; }
        public void setUser(ClientDTo user) { this.user = user; }

        public long getExpiresIn() { return expiresIn; }
        public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }
    }

    public static class RegisterResponse {
        private String message;
        private ClientDTo user;

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public ClientDTo getUser() { return user; }
        public void setUser(ClientDTo user) { this.user = user; }
    }

    public static class RegisterRequest {
        private String nom;
        private String prenom;
        private String email;
        private String password;
        private String passwordConfirmation;
        private String telephone;
        private String adresse;
        private String dateNaissance;
        private String numeroCNI;

        // Constructeurs
        public RegisterRequest() {}

        public RegisterRequest(String nom, String prenom, String email, String password,
                               String passwordConfirmation, String telephone, String adresse,
                               String dateNaissance, String numeroCNI) {
            this.nom = nom;
            this.prenom = prenom;
            this.email = email;
            this.password = password;
            this.passwordConfirmation = passwordConfirmation;
            this.telephone = telephone;
            this.adresse = adresse;
            this.dateNaissance = dateNaissance;
            this.numeroCNI = numeroCNI;
        }

        // Getters et setters
        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }

        public String getPrenom() { return prenom; }
        public void setPrenom(String prenom) { this.prenom = prenom; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getPasswordConfirmation() { return passwordConfirmation; }
        public void setPasswordConfirmation(String passwordConfirmation) { this.passwordConfirmation = passwordConfirmation; }

        public String getTelephone() { return telephone; }
        public void setTelephone(String telephone) { this.telephone = telephone; }

        public String getAdresse() { return adresse; }
        public void setAdresse(String adresse) { this.adresse = adresse; }

        public String getDateNaissance() { return dateNaissance; }
        public void setDateNaissance(String dateNaissance) { this.dateNaissance = dateNaissance; }

        public String getNumeroCNI() { return numeroCNI; }
        public void setNumeroCNI(String numeroCNI) { this.numeroCNI = numeroCNI; }
    }

    // Ajoutez cette méthode dans votre classe AuthService

    /**
     * Connexion administrateur
     */
    public static CompletableFuture<AdminLoginResponse> loginAdmin(String username, String password) {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", username);
        credentials.put("password", password);

        return HttpService.postAsync("/api/admins/login", credentials, AdminLoginResponse.class)
                .thenApply(response -> {
                    if (response != null && response.getId() != null) {
                        // Sauvegarder les données admin
                        ApiConfig.setCurrentUserId(response.getId());
                        // Pour les admins, vous pourriez vouloir un token différent ou gérer différemment
                    }
                    return response;
                });
    }

    // Classe DTO pour la réponse de connexion admin
    public static class AdminLoginResponse {
        private Long id;
        private String username;
        private String role;

        // Constructeurs
        public AdminLoginResponse() {}

        public AdminLoginResponse(Long id, String username, String role) {
            this.id = id;
            this.username = username;
            this.role = role;
        }

        // Getters et Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
}