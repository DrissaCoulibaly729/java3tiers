<?php
namespace App\Http\Controllers\Api;

use App\Models\Remboursement;
use Illuminate\Http\Request;
use App\Http\Controllers\Controller;

class RemboursementController extends Controller
{
    // ✅ GET /api/remboursements
    public function index()
    {
        return Remboursement::with('credit')->get();
    }

    // ✅ POST /api/remboursements
    public function store(Request $request)
    {
        $request->validate([
            'montant' => 'required|numeric|min:0',
            'date' => 'required|date',
            'credit_id' => 'required|exists:credits,id',
        ]);

        $remboursement = Remboursement::create($request->all());

        return response()->json($remboursement, 201);
    }

    // ✅ GET /api/remboursements/{id}
    public function show(Remboursement $remboursement)
    {
        return $remboursement->load('credit');
    }

    // ✅ PUT /api/remboursements/{id}
    public function update(Request $request, Remboursement $remboursement)
    {
        $request->validate([
            'date' => 'required|date',
        ]);

        $remboursement->update(['date' => $request->date]);

        return response()->json($remboursement);
    }

    // ✅ DELETE /api/remboursements/{id}
    public function destroy(Remboursement $remboursement)
    {
        $remboursement->delete();
        return response()->json(null, 204);
    }

    // ✅ GET /api/credits/{id}/remboursements
    public function getByCredit($id)
    {
        return Remboursement::where('credit_id', $id)->get();
    }
}
