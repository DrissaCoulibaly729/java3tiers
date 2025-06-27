<?php
namespace App\Http\Controllers\Api;

use App\Models\Transaction;
use Illuminate\Http\Request;
use App\Http\Controllers\Controller;
use Illuminate\Support\Facades\DB;
use App\Models\Compte;
use Illuminate\Support\Facades\Log;

class TransactionController extends Controller
{
    // ✅ GET /api/transactions
    public function index()
    {
        return Transaction::with(['compteSource', 'compteDest'])->get();
    }

    // ✅ POST /api/transactions
    public function store(Request $request)
    {
        Log::info('[TX] ➜ Demande de transaction reçue', $request->all());

        $request->validate([
            'type'             => 'required|in:Dépôt,Retrait,Virement',
            'montant'          => 'required|numeric|min:1',
            'date'             => 'required|date',
            'statut'           => 'required|in:validé,rejeté,Annulée,Validé,Annulé,Rejeté',
            'compte_source_id' => 'nullable|exists:comptes,id',
            'compte_dest_id'   => 'nullable|exists:comptes,id',
        ]);

        // On ne touche aux soldes QUE si la transaction est « validé »
        if ($request->statut !== 'validé') {
            Log::info('[TX] Statut ≠ validé – création simple de la ligne');
            return response()->json(
                Transaction::create($request->all()),
                201
            );
        }

        /** ------------------------------------------------------------
         *  Opérations sensibles en transaction SQL
         * ----------------------------------------------------------- */
        $resultat = DB::transaction(function () use ($request) {

            $montant = $request->montant;
            Log::debug('[TX] Début transaction DB', ['montant' => $montant]);

            /* 1. Verrouillage pessimiste des comptes */
            $source = $request->compte_source_id
                ? Compte::lockForUpdate()->find($request->compte_source_id)
                : null;

            $dest   = $request->compte_dest_id
                ? Compte::lockForUpdate()->find($request->compte_dest_id)
                : null;

            Log::debug('[TX] Comptes récupérés', [
                'source' => optional($source)->only(['id', 'solde']),
                'dest'   => optional($dest)->only(['id', 'solde']),
            ]);

            /* 2. Ajustements de solde selon le type */
            switch ($request->type) {

                case 'Dépôt':
                    if (! $dest) {
                        Log::warning('[TX] Dépôt sans compte_dest_id');
                        abort(422, 'compte_dest_id manquant');
                    }
                    $dest->increment('solde', $montant);
                    Log::info('[TX] Dépôt effectué', ['dest' => $dest->id, 'montant' => $montant]);
                    break;

                case 'Retrait':
                    if (! $source) {
                        Log::warning('[TX] Retrait sans compte_source_id');
                        abort(422, 'compte_source_id manquant');
                    }
                    if ($source->solde < $montant) {
                        Log::warning('[TX] Solde insuffisant pour retrait', [
                            'source'  => $source->id,
                            'solde'   => $source->solde,
                            'montant' => $montant,
                        ]);
                        abort(422, 'Solde insuffisant');
                    }
                    $source->decrement('solde', $montant);
                    Log::info('[TX] Retrait effectué', ['source' => $source->id, 'montant' => $montant]);
                    break;

                case 'Virement':
                    if (! $source || ! $dest) {
                        Log::warning('[TX] Virement : source/dest manquants');
                        abort(422, 'Source ou destination manquante');
                    }
                    if ($source->solde < $montant) {
                        Log::warning('[TX] Solde insuffisant pour virement', [
                            'source'  => $source->id,
                            'solde'   => $source->solde,
                            'montant' => $montant,
                        ]);
                        abort(422, 'Solde insuffisant');
                    }
                    $source->decrement('solde', $montant);
                    $dest->increment('solde', $montant);
                    Log::info('[TX] Virement effectué', [
                        'source' => $source->id,
                        'dest'   => $dest->id,
                        'montant'=> $montant,
                    ]);
                    break;
            }

            /* 3. Sauvegarde explicite (lockForUpdate ⇒ déjà chargé) */
            optional($source)->save();
            optional($dest)->save();
            Log::debug('[TX] Soldes enregistrés en BD');

            /* 4. Persistance de la transaction */
            $transaction = Transaction::create($request->all());

            Log::info('[TX] Transaction enregistrée', ['id' => $transaction->id]);

            return $transaction->load('compteSource', 'compteDest');
        });

        Log::info('[TX] ✓ Transaction terminée', ['id' => $resultat->id]);

        return response()->json($resultat, 201);
    }

    // ✅ GET /api/transactions/{id}
    public function show(Transaction $transaction)
    {
        return $transaction->load(['compteSource', 'compteDest']);
    }

    // ✅ PUT /api/transactions/{id}
    public function update(Request $request, Transaction $transaction)
    {
        $request->validate([
            'type' => 'required|in:Dépôt,Retrait,Virement',
            'montant' => 'required|numeric',
            'statut' => 'required|in:validé,rejeté,Annulée',
            'date' => 'required|date',
        ]);

        $transaction->update($request->only(['type', 'montant', 'statut', 'date']));

        return response()->json($transaction);
    }

    // ✅ DELETE /api/transactions/{id}
    public function destroy(Transaction $transaction)
    {
        $transaction->delete();
        return response()->json(null, 204);
    }

    // ✅ PUT /api/transactions/{id}/annuler
    public function annuler($id)
    {
        $transaction = Transaction::findOrFail($id);
        $transaction->update(['statut' => 'Annulée']);
        return response()->json(['message' => 'Transaction annulée']);
    }

    // ✅ GET /api/comptes/{id}/transactions
    public function getByCompte($id)
    {
        return Transaction::where('compte_source_id', $id)
            ->orWhere('compte_dest_id', $id)
            ->get();
    }

    // ✅ GET /api/clients/{id}/transactions
    public function getByClient($clientId)
    {
        return Transaction::whereHas('compteSource', function ($query) use ($clientId) {
                $query->where('client_id', $clientId);
            })->orWhereHas('compteDest', function ($query) use ($clientId) {
                $query->where('client_id', $clientId);
            })->get();
    }

    // ✅ GET /api/transactions/suspectes
  public function getSuspectes()
{
    $transactions = Transaction::with('compteSource')->get()->filter(function ($transaction) {
        $montantEleve = $transaction->montant > 1_000_000;
        $compte = $transaction->compteSource;
        $compteBloque = $compte && $compte->statut === 'Bloqué';
        return $montantEleve || $compteBloque;
    })->values();

    if ($transactions->isEmpty()) {
        return response()->json([]); // ✅ renvoie un tableau JSON vide
    }

    return response()->json($transactions); // ✅ renvoie les transactions filtrées
}

}
