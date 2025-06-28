<?php
namespace App\Http\Controllers\Api;

use App\Models\Compte;
use App\Models\FraisBancaire;
use Illuminate\Http\Request;
use App\Http\Controllers\Controller;

class CompteController extends Controller
{
    public function index()
    {
        return Compte::with('client')->get();
    }

    public function store(Request $request)
    {
        $request->validate([
            'type' => 'required|in:Courant,Épargne',
            'solde' => 'required|numeric|min:0',
            'client_id' => 'required|exists:clients,id',
        ]);

        $compte = Compte::create([
            'numero' => Compte::genererNumeroCompte(),
            'type' => $request->type,
            'solde' => $request->solde,
            'client_id' => $request->client_id,
            'statut' => 'Actif',
        ]);

        return response()->json($compte->load('client'), 201);
    }

    public function show(Compte $compte)
    {
        return $compte->load(['client', 'carteBancaires', 'transactionsSource', 'transactionsDestination']);
    }

    public function update(Request $request, Compte $compte)
    {
        $request->validate([
            'type' => 'required|in:Courant,Épargne',
            'statut' => 'required|in:Actif,Fermé',
        ]);

        $compte->update($request->only(['type', 'statut']));
        return response()->json($compte);
    }

    public function destroy(Compte $compte)
    {
        if ($compte->solde > 0) {
            return response()->json(['error' => 'Impossible de supprimer un compte avec un solde positif'], 403);
        }

        $compte->delete();
        return response()->json(null, 204);
    }

    public function getByClient($id)
    {
        return Compte::where('client_id', $id)->with('carteBancaires')->get();
    }

    public function getByNumero($numero)
    {
        $compte = Compte::where('numero', $numero)->with('client')->first();
        
        if (!$compte) {
            return response()->json(['error' => 'Compte non trouvé'], 404);
        }

        return response()->json($compte);
    }

    public function appliquerFrais(Request $request, $id)
    {
        $request->validate([
            'type' => 'required|in:Mensuel,Transaction,Maintenance',
            'montant' => 'required|numeric|min:0',
        ]);

        $compte = Compte::findOrFail($id);
        
        if ($compte->solde < $request->montant) {
            return response()->json(['error' => 'Solde insuffisant'], 400);
        }

        // Débiter le compte
        $compte->decrement('solde', $request->montant);

        // Créer l'enregistrement des frais
        FraisBancaire::create([
            'compte_id' => $id,
            'type' => $request->type,
            'montant' => $request->montant,
        ]);

        return response()->json(['message' => 'Frais appliqués avec succès']);
    }

    public function fermerCompte($id)
    {
        $compte = Compte::findOrFail($id);
        
        if ($compte->solde > 0) {
            return response()->json(['error' => 'Impossible de fermer un compte avec un solde positif'], 400);
        }

        $compte->update(['statut' => 'Fermé']);
        return response()->json(['message' => 'Compte fermé avec succès']);
    }
}