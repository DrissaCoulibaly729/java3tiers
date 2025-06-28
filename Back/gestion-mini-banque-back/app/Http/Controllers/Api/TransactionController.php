<?php
namespace App\Http\Controllers\Api;

use App\Models\Transaction;
use App\Models\Compte;
use Illuminate\Http\Request;
use App\Http\Controllers\Controller;
use Illuminate\Support\Facades\DB;

class TransactionController extends Controller
{
    public function index()
    {
        return Transaction::with(['compteSource.client', 'compteDestination.client'])->get();
    }

    public function store(Request $request)
    {
        $request->validate([
            'type' => 'required|in:Dépôt,Retrait,Virement',
            'montant' => 'required|numeric|min:0.01',
            'compte_source_id' => 'nullable|exists:comptes,id',
            'compte_dest_id' => 'nullable|exists:comptes,id',
            'description' => 'nullable|string',
        ]);

        return DB::transaction(function () use ($request) {
            $transaction = Transaction::create($request->all());

            // Traitement selon le type de transaction
            switch ($request->type) {
                case 'Dépôt':
                    $this->traiterDepot($transaction);
                    break;
                case 'Retrait':
                    $this->traiterRetrait($transaction);
                    break;
                case 'Virement':
                    $this->traiterVirement($transaction);
                    break;
            }

            return response()->json($transaction->load(['compteSource', 'compteDestination']), 201);
        });
    }

    private function traiterDepot(Transaction $transaction)
    {
        $compte = Compte::findOrFail($transaction->compte_dest_id);
        $compte->increment('solde', $transaction->montant);
    }

    private function traiterRetrait(Transaction $transaction)
    {
        $compte = Compte::findOrFail($transaction->compte_source_id);
        
        if ($compte->solde < $transaction->montant) {
            $transaction->update(['statut' => 'Rejeté']);
            throw new \Exception('Solde insuffisant');
        }

        $compte->decrement('solde', $transaction->montant);
    }

    private function traiterVirement(Transaction $transaction)
    {
        $compteSource = Compte::findOrFail($transaction->compte_source_id);
        $compteDestination = Compte::findOrFail($transaction->compte_dest_id);

        if ($compteSource->solde < $transaction->montant) {
            $transaction->update(['statut' => 'Rejeté']);
            throw new \Exception('Solde insuffisant');
        }

        $compteSource->decrement('solde', $transaction->montant);
        $compteDestination->increment('solde', $transaction->montant);
    }

    public function show(Transaction $transaction)
    {
        return $transaction->load(['compteSource.client', 'compteDestination.client']);
    }

    public function update(Request $request, Transaction $transaction)
    {
        $request->validate([
            'statut' => 'required|in:Validé,Rejeté,En attente',
            'description' => 'nullable|string',
        ]);

        $transaction->update($request->only(['statut', 'description']));
        return response()->json($transaction);
    }

    public function destroy(Transaction $transaction)
    {
        if ($transaction->statut === 'Validé') {
            return response()->json(['error' => 'Impossible de supprimer une transaction validée'], 403);
        }

        $transaction->delete();
        return response()->json(null, 204);
    }

    public function getSuspectes()
    {
        $transactions = Transaction::where('montant', '>', 10000)
            ->orWhere(function ($query) {
                $query->where('type', 'Retrait')->where('montant', '>', 5000);
            })
            ->with(['compteSource.client', 'compteDestination.client'])
            ->get();

        return response()->json($transactions);
    }

    public function annuler($id)
    {
        $transaction = Transaction::findOrFail($id);
        
        if ($transaction->statut !== 'Validé') {
            return response()->json(['error' => 'Seules les transactions validées peuvent être annulées'], 400);
        }

        return DB::transaction(function () use ($transaction) {
            // Annuler les effets de la transaction
            switch ($transaction->type) {
                case 'Dépôt':
                    $compte = Compte::findOrFail($transaction->compte_dest_id);
                    $compte->decrement('solde', $transaction->montant);
                    break;
                case 'Retrait':
                    $compte = Compte::findOrFail($transaction->compte_source_id);
                    $compte->increment('solde', $transaction->montant);
                    break;
                case 'Virement':
                    $compteSource = Compte::findOrFail($transaction->compte_source_id);
                    $compteDestination = Compte::findOrFail($transaction->compte_dest_id);
                    $compteSource->increment('solde', $transaction->montant);
                    $compteDestination->decrement('solde', $transaction->montant);
                    break;
            }

            $transaction->update(['statut' => 'Rejeté', 'description' => 'Transaction annulée']);
            return response()->json(['message' => 'Transaction annulée avec succès']);
        });
    }

    public function getByCompte($id)
    {
        return Transaction::where('compte_source_id', $id)
            ->orWhere('compte_dest_id', $id)
            ->with(['compteSource.client', 'compteDestination.client'])
            ->orderBy('date', 'desc')
            ->get();
    }

    public function getByClient($id)
    {
        $comptes = Compte::where('client_id', $id)->pluck('id');
        
        return Transaction::whereIn('compte_source_id', $comptes)
            ->orWhereIn('compte_dest_id', $comptes)
            ->with(['compteSource', 'compteDestination'])
            ->orderBy('date', 'desc')
            ->get();
    }
}
