<?php
namespace App\Http\Controllers\Api;

use App\Models\CarteBancaire;
use Illuminate\Http\Request;
use App\Http\Controllers\Controller;
use Carbon\Carbon;

class CarteBancaireController extends Controller
{
    public function index()
    {
        return CarteBancaire::with('compte.client')->get();
    }

    public function store(Request $request)
    {
        $request->validate([
            'compte_id' => 'required|exists:comptes,id',
            'solde' => 'required|numeric|min:0',
        ]);

        $carte = CarteBancaire::create([
            'numero' => CarteBancaire::genererNumeroCarte(),
            'cvv' => CarteBancaire::genererCVV(),
            'date_expiration' => Carbon::now()->addYears(3),
            'solde' => $request->solde,
            'compte_id' => $request->compte_id,
            'code_pin' => CarteBancaire::genererPIN(),
            'statut' => 'Active',
        ]);

        return response()->json($carte->load('compte'), 201);
    }

    public function show(CarteBancaire $carteBancaire)
    {
        return $carteBancaire->load('compte.client');
    }

    public function update(Request $request, CarteBancaire $carteBancaire)
    {
        $request->validate([
            'solde' => 'required|numeric|min:0',
            'statut' => 'required|in:Active,Bloquée',
        ]);

        $carteBancaire->update($request->only(['solde', 'statut']));
        return response()->json($carteBancaire);
    }

    public function destroy(CarteBancaire $carteBancaire)
    {
        $carteBancaire->delete();
        return response()->json(null, 204);
    }

    public function getCartesByCompte($id)
    {
        return CarteBancaire::where('compte_id', $id)->get();
    }

    public function bloquer($id)
    {
        $carte = CarteBancaire::findOrFail($id);
        $carte->update(['statut' => 'Bloquée']);
        return response()->json(['message' => 'Carte bloquée avec succès']);
    }

    public function debloquer($id)
    {
        $carte = CarteBancaire::findOrFail($id);
        $carte->update(['statut' => 'Active']);
        return response()->json(['message' => 'Carte débloquée avec succès']);
    }

    public function isValide($id)
    {
        $carte = CarteBancaire::findOrFail($id);
        $isValide = $carte->statut === 'Active' && $carte->date_expiration > now();
        
        return response()->json(['valide' => $isValide]);
    }
}
