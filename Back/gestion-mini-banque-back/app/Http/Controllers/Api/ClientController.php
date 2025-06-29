<?php
namespace App\Http\Controllers\Api;

use App\Models\Client;
use App\Models\Compte;
use App\Mail\NouveauClientMail;
use Illuminate\Http\Request;
use App\Http\Controllers\Controller;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Mail;
use Illuminate\Support\Facades\Log;

class ClientController extends Controller
{
    // GET /api/clients
    public function index()
    {
        return Client::with('comptes')->get();
    }

    // POST /api/clients - AVEC CRÉATION AUTOMATIQUE DE COMPTE
    public function store(Request $request)
    {
        $request->validate([
            'nom' => 'required|string|max:255',
            'prenom' => 'required|string|max:255',
            'email' => 'required|email|unique:clients',
            'telephone' => 'required|string|max:20',
            'adresse' => 'required|string|max:255',
            'password' => 'required|string|min:6'
        ]);

        // 🏦 TRANSACTION POUR CRÉER CLIENT + COMPTE ENSEMBLE
        DB::beginTransaction();
        
        try {
            // 1️⃣ Créer le client
            $client = Client::create([
                'nom' => $request->nom,
                'prenom' => $request->prenom,
                'email' => $request->email,
                'telephone' => $request->telephone,
                'adresse' => $request->adresse,
                'password' => bcrypt($request->password),
                'statut' => 'Actif',
                'date_inscription' => now()
            ]);

            // 2️⃣ Générer un numéro de compte unique
            $numeroCompte = $this->genererNumeroCompte();

            // 3️⃣ Créer automatiquement un compte courant pour le nouveau client
            $compte = Compte::create([
                'numero' => $numeroCompte,
                'type' => 'Courant', // Compte courant par défaut
                'solde' => 0.00, // Solde initial à 0
                'statut' => 'Actif',
                'client_id' => $client->id,
                'date_creation' => now()
            ]);

            // 4️⃣ Confirmer la transaction
            DB::commit();

            // 📧 Envoyer email de bienvenue (optionnel)
            try {
                Mail::to($client->email)
                    ->send(new NouveauClientMail($client, $request->password, $compte));
            } catch (\Exception $e) {
                // Log l'erreur mais ne pas faire échouer la création
                Log::warning('Erreur envoi email nouveau client: ' . $e->getMessage());
            }

            // 5️⃣ Retourner le client avec son compte
            $client->load('comptes');
            
            return response()->json([
                'success' => true,
                'message' => 'Client et compte créés avec succès',
                'client' => $client,
                'compte' => $compte
            ], 201);

        } catch (\Exception $e) {
            // ❌ Annuler en cas d'erreur
            DB::rollback();
            
            return response()->json([
                'success' => false,
                'message' => 'Erreur lors de la création du client et compte',
                'error' => $e->getMessage()
            ], 500);
        }
    }

    // 🔐 POST /api/clients/login - MÉTHODE LOGIN MANQUANTE
    public function login(Request $request)
    {
        $request->validate([
            'email' => 'required|email',
            'password' => 'required'
        ]);

        // Chercher le client par email
        $client = Client::where('email', $request->email)->first();

        // Vérifier si le client existe et le mot de passe est correct
        if (!$client || !Hash::check($request->password, $client->password)) {
            return response()->json([
                'success' => false,
                'message' => 'Email ou mot de passe incorrect'
            ], 401);
        }

        // Vérifier le statut du client
        if ($client->statut !== 'Actif') {
            return response()->json([
                'success' => false,
                'message' => 'Votre compte est ' . strtolower($client->statut) . '. Contactez votre banque.'
            ], 403);
        }

        // Charger les comptes du client
        $client->load('comptes');

        return response()->json([
            'success' => true,
            'message' => 'Connexion réussie',
            'client' => [
                'id' => $client->id,
                'nom' => $client->nom,
                'prenom' => $client->prenom,
                'email' => $client->email,
                'telephone' => $client->telephone,
                'adresse' => $client->adresse,
                'statut' => $client->statut,
                'date_inscription' => $client->date_inscription,
                'comptes' => $client->comptes
            ]
        ], 200);
    }

    // 📝 POST /api/clients/register - Alias pour store (inscription)
    public function register(Request $request)
    {
        return $this->store($request);
    }

    // GET /api/clients/{id}
    public function show(Client $client)
    {
        return $client->load('comptes');
    }

    // PUT /api/clients/{id}
    public function update(Request $request, Client $client)
    {
        $request->validate([
            'nom' => 'sometimes|required|string|max:255',
            'prenom' => 'sometimes|required|string|max:255',
            'email' => 'sometimes|required|email|unique:clients,email,' . $client->id,
            'telephone' => 'sometimes|required|string|max:20',
            'adresse' => 'sometimes|required|string|max:255',
            'statut' => 'sometimes|required|in:Actif,Suspendu,Fermé'
        ]);

        $client->update($request->all());
        return response()->json($client->load('comptes'));
    }

    // PUT /api/clients/{client}/suspend - Suspendre un client
    public function suspend(Client $client)
    {
        $client->update(['statut' => 'Suspendu']);
        
        // Suspendre aussi tous ses comptes
        $client->comptes()->update(['statut' => 'Suspendu']);
        
        return response()->json([
            'success' => true,
            'message' => 'Client et comptes suspendus avec succès',
            'client' => $client->load('comptes')
        ]);
    }

    // PUT /api/clients/{client}/reactivate - Réactiver un client
    public function reactivate(Client $client)
    {
        $client->update(['statut' => 'Actif']);
        
        // Réactiver aussi tous ses comptes
        $client->comptes()->update(['statut' => 'Actif']);
        
        return response()->json([
            'success' => true,
            'message' => 'Client et comptes réactivés avec succès',
            'client' => $client->load('comptes')
        ]);
    }

    // DELETE /api/clients/{id}
    public function destroy(Client $client)
    {
        // Vérifier qu'il n'y a pas de comptes avec solde > 0
        $comptesAvecSolde = $client->comptes()->where('solde', '>', 0)->count();
        
        if ($comptesAvecSolde > 0) {
            return response()->json([
                'success' => false,
                'message' => 'Impossible de supprimer: le client a des comptes avec solde positif'
            ], 400);
        }

        // Supprimer les comptes puis le client
        $client->comptes()->delete();
        $client->delete();
        
        return response()->json([
            'success' => true,
            'message' => 'Client et comptes supprimés avec succès'
        ]);
    }

    // 🔢 MÉTHODE POUR GÉNÉRER UN NUMÉRO DE COMPTE UNIQUE
    private function genererNumeroCompte()
    {
        do {
            // Format: COMPTE + 6 chiffres aléatoires
            $numero = 'COMPTE' . str_pad(rand(1, 999999), 6, '0', STR_PAD_LEFT);
            
            // Vérifier que le numéro n'existe pas déjà
            $existe = Compte::where('numero', $numero)->exists();
            
        } while ($existe);
        
        return $numero;
    }

    // 📊 MÉTHODES SUPPLÉMENTAIRES UTILES

    // GET /api/clients/avec-comptes
    public function clientsAvecComptes()
    {
        return Client::with('comptes')->whereHas('comptes')->get();
    }

    // GET /api/clients/{id}/comptes
    public function getComptes(Client $client)
    {
        return $client->comptes;
    }

    // POST /api/clients/{id}/comptes - Ajouter un compte supplémentaire
    public function ajouterCompte(Request $request, Client $client)
    {
        $request->validate([
            'type' => 'required|in:Courant,Épargne'
        ]);

        $numeroCompte = $this->genererNumeroCompte();

        $compte = Compte::create([
            'numero' => $numeroCompte,
            'type' => $request->type,
            'solde' => 0.00,
            'statut' => 'Actif',
            'client_id' => $client->id,
            'date_creation' => now()
        ]);

        return response()->json([
            'success' => true,
            'message' => 'Compte supplémentaire créé avec succès',
            'compte' => $compte
        ], 201);
    }

    // 🔍 POST /api/clients/verify-password - Vérifier le mot de passe (utile pour certaines opérations)
    public function verifyPassword(Request $request)
    {
        $request->validate([
            'client_id' => 'required|exists:clients,id',
            'password' => 'required'
        ]);

        $client = Client::findOrFail($request->client_id);

        if (Hash::check($request->password, $client->password)) {
            return response()->json([
                'success' => true,
                'message' => 'Mot de passe correct'
            ]);
        }

        return response()->json([
            'success' => false,
            'message' => 'Mot de passe incorrect'
        ], 401);
    }

    // 🔄 PUT /api/clients/{id}/change-password - Changer le mot de passe
    public function changePassword(Request $request, Client $client)
    {
        $request->validate([
            'current_password' => 'required',
            'new_password' => 'required|min:6|confirmed'
        ]);

        // Vérifier l'ancien mot de passe
        if (!Hash::check($request->current_password, $client->password)) {
            return response()->json([
                'success' => false,
                'message' => 'Mot de passe actuel incorrect'
            ], 401);
        }

        // Mettre à jour le mot de passe
        $client->update([
            'password' => bcrypt($request->new_password)
        ]);

        return response()->json([
            'success' => true,
            'message' => 'Mot de passe modifié avec succès'
        ]);
    }
}