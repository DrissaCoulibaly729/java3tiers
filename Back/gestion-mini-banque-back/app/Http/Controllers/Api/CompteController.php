<?php
namespace App\Http\Controllers\Api;

use App\Models\Compte;
use Illuminate\Http\Request;
use App\Http\Controllers\Controller;

class CompteController extends Controller
{
    // ✅ GET /api/comptes
    public function index()
    {
        return Compte::with(['client', 'transactionsEnvoyees', 'transactionsRecues', 'cartesBancaires'])->get();
    }

    // ✅ POST /api/comptes (création)
    public function store(Request $request)
    {
        $request->validate([
            'numero' => 'required|unique:comptes',
            'type' => 'required|in:courant,épargne,Courant,Épargne',
            'solde' => 'required|numeric',
            'date_creation' => 'required|date',
            'statut' => 'required|in:Actif,Bloqué,Fermé',
            'client_id' => 'required|exists:clients,id',
        ]);

        $compte = Compte::create($request->all());

        return response()->json($compte, 201);
    }

    // ✅ GET /api/comptes/{id}
    public function show(Compte $compte)
    {
        return $compte->load(['client', 'transactionsEnvoyees', 'transactionsRecues', 'cartesBancaires']);
    }

    // ✅ PUT /api/comptes/{id}
    public function update(Request $request, Compte $compte)
    {
        $request->validate([
            'type' => 'required|in:courant,épargne',
            'solde' => 'required|numeric',
            'statut' => 'required|in:Actif,Bloqué,Fermé',
        ]);

        $compte->update($request->only(['type', 'solde', 'statut']));

        return response()->json($compte);
    }

    // ✅ DELETE /api/comptes/{id}
    public function destroy(Compte $compte)
    {
        if ($compte->solde > 0) {
            return response()->json(['error' => 'Impossible de supprimer un compte avec un solde positif'], 403);
        }

        $compte->delete();
        return response()->json(null, 204);
    }

    // ✅ GET /api/clients/{id}/comptes
    public function getByClient($id)
    {
        return Compte::where('client_id', $id)->get();
    }

    // ✅ GET /api/comptes/numero/{numero}
    public function getByNumero($numero)
    {
        $compte = Compte::where('numero', $numero)->first();
        if (!$compte) {
            return response()->json(['error' => 'Compte introuvable'], 404);
        }
        return $compte;
    }

    // ✅ PUT /api/comptes/{id}/frais
    public function appliquerFrais(Request $request, $id)
    {
        $request->validate([
            'montant' => 'required|numeric|min:0',
        ]);

        $compte = Compte::findOrFail($id);

        if ($compte->solde < $request->montant) {
            return response()->json(['error' => 'Fonds insuffisants pour appliquer les frais'], 403);
        }

        $compte->solde -= $request->montant;
        $compte->save();

        return response()->json(['message' => 'Frais appliqués avec succès', 'nouveau_solde' => $compte->solde]);
    }

    // ✅ PUT /api/comptes/{id}/fermer
    public function fermerCompte($id)
    {
        $compte = Compte::findOrFail($id);

        if ($compte->solde > 0) {
            return response()->json(['error' => 'Impossible de fermer un compte avec un solde positif'], 403);
        }

        $compte->statut = 'Fermé';
        $compte->save();

        return response()->json(['message' => 'Compte fermé avec succès']);
    }
}
