<?php
namespace App\Http\Controllers\Api;

use App\Models\Client;
use App\Models\Compte;
use Illuminate\Http\Request;
use App\Http\Controllers\Controller;
use Illuminate\Support\Facades\DB;

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
}