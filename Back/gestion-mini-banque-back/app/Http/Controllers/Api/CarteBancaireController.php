<?php
namespace App\Http\Controllers\Api;

use App\Models\CarteBancaire;
use Illuminate\Http\Request;
use App\Http\Controllers\Controller;
use Illuminate\Support\Facades\Validator;

class CarteBancaireController extends Controller
{
    // ✅ GET /api/carte-bancaires
    public function index()
    {
        return CarteBancaire::with('compte')->get();
    }

    // ✅ POST /api/carte-bancaires (création)
    public function store(Request $request)
    {
        $request->validate([
            'numero' => 'required|unique:carte_bancaires',
            'type' => 'required',
            'cvv' => 'required',
            'date_expiration' => 'required',
            'solde' => 'required|numeric',
            'code_pin' => 'required',
            'compte_id' => 'required|exists:comptes,id',
        ]);

        $carte = CarteBancaire::create([
            'numero' => $request->numero,
            'type' => $request->type,
            'cvv' => $request->cvv,
            'date_expiration' => $request->date_expiration,
            'solde' => $request->solde,
            'statut' => 'Active', // par défaut
            'code_pin' => $request->code_pin,
            'compte_id' => $request->compte_id,
        ]);

        return response()->json($carte, 201);
    }

    // ✅ GET /api/carte-bancaires/{id}
    public function show(CarteBancaire $carteBancaire)
    {
        return $carteBancaire->load('compte');
    }

    // ✅ PUT /api/carte-bancaires/{id}
    public function update(Request $request, CarteBancaire $carteBancaire)
    {
        $request->validate([
            'statut' => 'required|in:Active,Bloquée,Expirée',
        ]);

        $carteBancaire->update([
            'statut' => $request->statut,
        ]);

        return response()->json($carteBancaire);
    }

    // ✅ DELETE /api/carte-bancaires/{id}
    public function destroy(CarteBancaire $carteBancaire)
    {
        if ($carteBancaire->statut !== 'Expirée') {
            return response()->json(['error' => 'Impossible de supprimer une carte active ou bloquée.'], 403);
        }

        $carteBancaire->delete();
        return response()->json(null, 204);
    }

    // ✅ GET /api/compte/{id}/cartes
    public function getCartesByCompte($id)
    {
        return CarteBancaire::where('compte_id', $id)->get();
    }

    // ✅ PUT /api/carte-bancaires/{id}/bloquer
    public function bloquer($id)
    {
        $carte = CarteBancaire::findOrFail($id);
        $carte->update(['statut' => 'Bloquée']);
        return response()->json(['message' => 'Carte bloquée.']);
    }

    // ✅ PUT /api/carte-bancaires/{id}/debloquer
    public function debloquer($id)
    {
        $carte = CarteBancaire::findOrFail($id);
        $carte->update(['statut' => 'Active']);
        return response()->json(['message' => 'Carte débloquée.']);
    }

    // ✅ GET /api/carte-bancaires/valide/{id}
    public function isValide($id)
    {
        $carte = CarteBancaire::find($id);
        if (!$carte) {
            return response()->json(['valide' => false]);
        }
        return response()->json(['valide' => $carte->statut !== 'Expirée']);
    }
}
