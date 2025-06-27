<?php
namespace App\Http\Controllers\Api;

use App\Models\Credit;
use Illuminate\Http\Request;
use App\Http\Controllers\Controller;

class CreditController extends Controller
{
    // ✅ GET /api/credits
    public function index()
    {
        return Credit::with(['client', 'remboursements'])->get();
    }

    // ✅ POST /api/credits (demande de crédit)
    public function store(Request $request)
    {
        $request->validate([
            'montant' => 'required|numeric|min:1',
            'taux_interet' => 'required|numeric',
            'duree_mois' => 'required|integer|min:1',
            'mensualite' => 'required|numeric',
            'date_demande' => 'required|date',
            'client_id' => 'required|exists:clients,id',
        ]);

        $credit = Credit::create([
            'montant' => $request->montant,
            'taux_interet' => $request->taux_interet,
            'duree_mois' => $request->duree_mois,
            'mensualite' => $request->mensualite,
            'date_demande' => $request->date_demande,
            'statut' => 'En attente',
            'client_id' => $request->client_id,
        ]);

        return response()->json($credit, 201);
    }

    // ✅ GET /api/credits/{id}
    public function show(Credit $credit)
    {
        return $credit->load(['client', 'remboursements']);
    }

    // ✅ PUT /api/credits/{id}
    public function update(Request $request, Credit $credit)
    {
        $request->validate([
            'montant' => 'required|numeric',
            'taux_interet' => 'required|numeric',
            'duree_mois' => 'required|integer',
            'statut' => 'required|in:En attente,Accepté,Refusé',
        ]);

        $credit->update($request->only(['montant', 'taux_interet', 'duree_mois', 'statut']));

        return response()->json($credit);
    }

    // ✅ DELETE /api/credits/{id}
    public function destroy(Credit $credit)
    {
        if ($credit->statut !== 'En attente') {
            return response()->json(['error' => 'Impossible de supprimer un crédit validé'], 403);
        }

        $credit->delete();
        return response()->json(null, 204);
    }

    // ✅ PUT /api/credits/{id}/accepter
    public function accepter($id)
    {
        $credit = Credit::findOrFail($id);
        $credit->update(['statut' => 'Accepté']);
        return response()->json(['message' => 'Crédit accepté']);
    }

    // ✅ PUT /api/credits/{id}/refuser
    public function refuser($id)
    {
        $credit = Credit::findOrFail($id);
        $credit->update(['statut' => 'Refusé']);
        return response()->json(['message' => 'Crédit refusé']);
    }

    // ✅ GET /api/clients/{id}/credits
    public function getByClient($id)
    {
        return Credit::where('client_id', $id)->get();
    }

    // ✅ GET /api/credits/statut/{statut}
    public function getByStatut($statut)
    {
        return Credit::where('statut', $statut)->get();
    }
}
