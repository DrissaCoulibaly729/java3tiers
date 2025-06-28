<?php
namespace App\Http\Controllers\Api;

use App\Models\Credit;
use Illuminate\Http\Request;
use App\Http\Controllers\Controller;

class CreditController extends Controller
{
    public function index()
    {
        return Credit::with('client')->get();
    }

    public function store(Request $request)
    {
        $request->validate([
            'montant' => 'required|numeric|min:1000',
            'taux_interet' => 'required|numeric|min:0|max:30',
            'duree_mois' => 'required|integer|min:12|max:360',
            'client_id' => 'required|exists:clients,id',
        ]);

        $mensualite = Credit::calculerMensualite(
            $request->montant,
            $request->taux_interet,
            $request->duree_mois
        );

        $credit = Credit::create([
            'montant' => $request->montant,
            'taux_interet' => $request->taux_interet,
            'duree_mois' => $request->duree_mois,
            'mensualite' => $mensualite,
            'client_id' => $request->client_id,
            'statut' => 'En attente',
        ]);

        return response()->json($credit->load('client'), 201);
    }

    public function show(Credit $credit)
    {
        return $credit->load(['client', 'remboursements']);
    }

    public function update(Request $request, Credit $credit)
    {
        $request->validate([
            'statut' => 'required|in:En attente,Approuvé,Refusé,Terminé',
        ]);

        $credit->update(['statut' => $request->statut]);
        return response()->json($credit);
    }

    public function destroy(Credit $credit)
    {
        if ($credit->statut === 'Approuvé') {
            return response()->json(['error' => 'Impossible de supprimer un crédit approuvé'], 403);
        }

        $credit->delete();
        return response()->json(null, 204);
    }

    public function accepter($id)
    {
        $credit = Credit::findOrFail($id);
        $credit->update(['statut' => 'Approuvé']);
        return response()->json(['message' => 'Crédit approuvé avec succès']);
    }

    public function refuser($id)
    {
        $credit = Credit::findOrFail($id);
        $credit->update(['statut' => 'Refusé']);
        return response()->json(['message' => 'Crédit refusé']);
    }

    public function getByClient($id)
    {
        return Credit::where('client_id', $id)->with('remboursements')->get();
    }

    public function getByStatut($statut)
    {
        return Credit::where('statut', $statut)->with('client')->get();
    }
}