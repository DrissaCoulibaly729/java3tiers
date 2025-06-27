<?php
namespace App\Http\Controllers\Api;

use App\Models\TicketSupport;
use Illuminate\Http\Request;
use App\Http\Controllers\Controller;

class TicketSupportController extends Controller
{
    // ✅ GET /api/ticket-supports
    public function index()
    {
        return TicketSupport::with(['client', 'admin'])->get();
    }

    // ✅ POST /api/ticket-supports (soumettre ticket)
    public function store(Request $request)
    {
        $request->validate([
            'sujet' => 'required|string',
            'description' => 'required|string',
            'date_ouverture' => 'required|date',
            'client_id' => 'required|exists:clients,id',
            'admin_id' => 'nullable|exists:admins,id'
        ]);

        $ticket = TicketSupport::create([
            'sujet' => $request->sujet,
            'description' => $request->description,
            'date_ouverture' => $request->date_ouverture,
            'statut' => 'Ouvert',
            'client_id' => $request->client_id,
            'admin_id' => $request->admin_id,
        ]);

        return response()->json($ticket, 201);
    }

    // ✅ GET /api/ticket-supports/{id}
    public function show(TicketSupport $ticketSupport)
    {
        return $ticketSupport->load(['client', 'admin']);
    }

    // ✅ PUT /api/ticket-supports/{id} (mise à jour)
    public function update(Request $request, TicketSupport $ticketSupport)
    {
        $request->validate([
            'statut' => 'required|in:Ouvert,Répondu,Résolu',
            'reponse' => 'nullable|string',
        ]);

        $ticketSupport->update([
            'statut' => $request->statut,
            'reponse' => $request->reponse,
        ]);

        return response()->json($ticketSupport);
    }

    // ✅ DELETE /api/ticket-supports/{id}
    public function destroy(TicketSupport $ticketSupport)
    {
        if ($ticketSupport->statut !== 'Résolu') {
            return response()->json(['error' => 'Impossible de supprimer un ticket non résolu'], 403);
        }

        $ticketSupport->delete();
        return response()->json(null, 204);
    }

    // ✅ PUT /api/ticket-supports/{id}/repondre
    public function repondre(Request $request, $id)
    {
        $request->validate([
            'reponse' => 'required|string',
        ]);

        $ticket = TicketSupport::findOrFail($id);
        $ticket->update([
            'reponse' => $request->reponse,
            'statut' => 'Répondu',
        ]);

        return response()->json(['message' => 'Réponse enregistrée']);
    }

    // ✅ PUT /api/ticket-supports/{id}/resolu
    public function marquerResolu($id)
    {
        $ticket = TicketSupport::findOrFail($id);
        $ticket->update(['statut' => 'Résolu']);

        return response()->json(['message' => 'Ticket marqué comme résolu']);
    }

    // ✅ GET /api/clients/{id}/tickets
    public function getByClient($id)
    {
        return TicketSupport::where('client_id', $id)->get();
    }

    // ✅ GET /api/ticket-supports/recherche/{value}
    public function rechercher($value)
    {
        return TicketSupport::where('sujet', 'LIKE', "%$value%")
            ->orWhere('id', $value)
            ->get();
    }
}
