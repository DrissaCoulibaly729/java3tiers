<?php

namespace App\Http\Controllers\Api;

use App\Models\Client;
use App\Models\Compte;
use App\Mail\NouveauClientMail;
use Illuminate\Http\Request;
use App\Http\Controllers\Controller;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Mail;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;
use Illuminate\Validation\Rule;

class ClientController extends Controller
{
    public function index()
    {
        return Client::with('comptes')->get();
    }

    /**
     * Création complète d'un client avec compte et envoi d'email
     */
    public function store(Request $request)
    {
        $request->validate([
            'nom' => 'required|string|max:255',
            'prenom' => 'required|string|max:255',
            'email' => 'required|email|unique:clients',
            'telephone' => 'required|string|max:20',
            'adresse' => 'required|string',
            'type_compte' => 'required|in:Courant,Épargne',
            'solde_initial' => 'required|numeric|min:0',
            'password' => 'nullable|string|min:6',
        ]);

        return DB::transaction(function () use ($request) {
            // Générer un mot de passe si non fourni
            $plainPassword = $request->password ?? Str::random(8);
            
            // Créer le client
            $client = Client::create([
                'nom' => $request->nom,
                'prenom' => $request->prenom,
                'email' => $request->email,
                'telephone' => $request->telephone,
                'adresse' => $request->adresse,
                'password' => Hash::make($plainPassword),
                'statut' => 'Actif',
                'date_inscription' => now(),
            ]);

            // Créer automatiquement un compte pour ce client
            $compte = Compte::create([
                'numero' => Compte::genererNumeroCompte(),
                'type' => $request->type_compte,
                'solde' => $request->solde_initial,
                'client_id' => $client->id,
                'statut' => 'Actif',
                'date_creation' => now(),
            ]);

            // Envoyer l'email de bienvenue
            try {
                Mail::to($client->email)->send(
                    new NouveauClientMail($client, $plainPassword, $compte)
                );
                
                $emailSent = true;
            } catch (\Exception $e) {
                // Log l'erreur mais ne fait pas échouer la création
                \Log::error('Erreur envoi email nouveau client: ' . $e->getMessage());
                $emailSent = false;
            }

            return response()->json([
                'client' => $client->load('comptes'),
                'compte' => $compte,
                'email_sent' => $emailSent,
                'plain_password' => $plainPassword, // À utiliser uniquement en développement
                'message' => 'Client et compte créés avec succès'
            ], 201);
        });
    }

    /**
     * Inscription client (pour l'interface client)
     */
    public function register(Request $request)
    {
        $request->validate([
            'nom' => 'required|string|max:255',
            'prenom' => 'required|string|max:255',
            'email' => 'required|email|unique:clients',
            'telephone' => 'required|string|max:20',
            'adresse' => 'required|string',
            'password' => 'required|string|min:6|confirmed',
        ]);

        return DB::transaction(function () use ($request) {
            $client = Client::create([
                'nom' => $request->nom,
                'prenom' => $request->prenom,
                'email' => $request->email,
                'telephone' => $request->telephone,
                'adresse' => $request->adresse,
                'password' => Hash::make($request->password),
                'statut' => 'Actif',
                'date_inscription' => now(),
            ]);

            // Créer un compte courant par défaut
            $compte = Compte::create([
                'numero' => Compte::genererNumeroCompte(),
                'type' => 'Courant',
                'solde' => 0,
                'client_id' => $client->id,
                'statut' => 'Actif',
                'date_creation' => now(),
            ]);

            return response()->json([
                'client' => $client->load('comptes'),
                'message' => 'Inscription réussie'
            ], 201);
        });
    }

    /**
     * Connexion client
     */
    public function login(Request $request)
    {
        $request->validate([
            'email' => 'required|email',
            'password' => 'required',
        ]);

        $client = Client::where('email', $request->email)->first();

        if (!$client || !Hash::check($request->password, $client->password)) {
            return response()->json([
                'error' => 'Identifiants incorrects'
            ], 401);
        }

        if ($client->statut !== 'Actif') {
            return response()->json([
                'error' => 'Compte suspendu ou inactif'
            ], 403);
        }

        return response()->json([
            'id' => $client->id,
            'nom' => $client->nom,
            'prenom' => $client->prenom,
            'email' => $client->email,
            'message' => 'Connexion réussie'
        ]);
    }

    public function show(Client $client)
    {
        return $client->load(['comptes', 'credits', 'ticketSupports']);
    }

    public function update(Request $request, Client $client)
    {
        $request->validate([
            'nom' => 'required|string|max:255',
            'prenom' => 'required|string|max:255',
            'email' => ['required', 'email', Rule::unique('clients')->ignore($client->id)],
            'telephone' => 'required|string|max:20',
            'adresse' => 'required|string',
            'password' => 'nullable|min:6',
            'statut' => 'nullable|in:Actif,Suspendu,Inactif',
        ]);

        $data = $request->only(['nom', 'prenom', 'email', 'telephone', 'adresse']);
        
        if ($request->filled('password')) {
            $data['password'] = Hash::make($request->password);
        }

        if ($request->filled('statut')) {
            $data['statut'] = $request->statut;
        }

        $client->update($data);
        return response()->json($client->load('comptes'));
    }

    public function destroy(Client $client)
    {
        if ($client->comptes()->where('statut', 'Actif')->exists()) {
            return response()->json([
                'error' => 'Impossible de supprimer un client avec des comptes actifs'
            ], 403);
        }

        $client->delete();
        return response()->json(null, 204);
    }

    /**
     * Suspendre un client
     */
    public function suspend(Client $client)
    {
        $client->update(['statut' => 'Suspendu']);
        
        return response()->json([
            'message' => 'Client suspendu avec succès',
            'client' => $client
        ]);
    }

    /**
     * Réactiver un client
     */
    public function reactivate(Client $client)
    {
        $client->update(['statut' => 'Actif']);
        
        return response()->json([
            'message' => 'Client réactivé avec succès',
            'client' => $client
        ]);
    }

    /**
     * Obtenir les statistiques des clients
     */
    public function stats()
    {
        $stats = [
            'total' => Client::count(),
            'actifs' => Client::where('statut', 'Actif')->count(),
            'suspendus' => Client::where('statut', 'Suspendu')->count(),
            'nouveaux_ce_mois' => Client::whereMonth('date_inscription', now()->month)
                                       ->whereYear('date_inscription', now()->year)
                                       ->count(),
        ];

        return response()->json($stats);
    }
}