<?php

namespace App\Http\Controllers\Api;

use App\Models\Client;
use Illuminate\Http\Request;
use App\Http\Controllers\Controller;
use Illuminate\Support\Facades\Hash;
use Illuminate\Validation\Rule;

class ClientController extends Controller
{
    public function index()
    {
        return Client::with('comptes')->get();
    }

    public function store(Request $request)
    {
        $request->validate([
            'nom' => 'required|string|max:255',
            'prenom' => 'required|string|max:255',
            'email' => 'required|email|unique:clients',
            'telephone' => 'required|string|max:20',
            'adresse' => 'required|string',
            'password' => 'required|min:6',
        ]);

        $client = Client::create([
            'nom' => $request->nom,
            'prenom' => $request->prenom,
            'email' => $request->email,
            'telephone' => $request->telephone,
            'adresse' => $request->adresse,
            'password' => Hash::make($request->password),
            'statut' => 'Actif',
        ]);

        return response()->json($client, 201);
    }

    public function show(Client $client)
    {
        return $client->load(['comptes', 'credits', 'ticketSupports']);
    }

    public function update(Request $request, Client $client)
    {
        $request->validate([
            'nom' => 'required|string|max:255',
            'prenom' => 'required|string|max:255',
            'email' => ['required', 'email', Rule::unique('clients')->ignore($client->id)],
            'telephone' => 'required|string|max:20',
            'adresse' => 'required|string',
            'password' => 'nullable|min:6',
        ]);

        $data = $request->only(['nom', 'prenom', 'email', 'telephone', 'adresse']);
        
        if ($request->filled('password')) {
            $data['password'] = Hash::make($request->password);
        }

        $client->update($data);
        return response()->json($client);
    }

    public function destroy(Client $client)
    {
        if ($client->comptes()->where('statut', 'Actif')->exists()) {
            return response()->json(['error' => 'Impossible de supprimer un client avec des comptes actifs'], 403);
        }

        $client->delete();
        return response()->json(null, 204);
    }

    public function register(Request $request)
    {
        return $this->store($request);
    }

    public function login(Request $request)
    {
        $request->validate([
            'email' => 'required|email',
            'password' => 'required',
        ]);

        $client = Client::where('email', $request->email)->first();

        if (!$client || !Hash::check($request->password, $client->password)) {
            return response()->json(['error' => 'Identifiants invalides'], 401);
        }

        if ($client->statut === 'Suspendu') {
            return response()->json(['error' => 'Compte suspendu'], 403);
        }

        return response()->json([
            'id' => $client->id,
            'nom' => $client->nom,
            'prenom' => $client->prenom,
            'email' => $client->email,
            'statut' => $client->statut
        ]);
    }

    public function suspend(Client $client)
    {
        $client->update(['statut' => 'Suspendu']);
        return response()->json(['message' => 'Client suspendu avec succès']);
    }

    public function reactivate(Client $client)
    {
        $client->update(['statut' => 'Actif']);
        return response()->json(['message' => 'Client réactivé avec succès']);
    }
}
