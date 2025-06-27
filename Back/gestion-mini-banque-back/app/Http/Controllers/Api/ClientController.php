<?php

namespace App\Http\Controllers\Api;

use App\Models\Client;
use Illuminate\Http\Request;
use App\Http\Controllers\Controller;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Str;
use App\Models\Compte;
use App\Mail\NouveauClientMail;
use Illuminate\Support\Facades\Mail;
use Illuminate\Support\Facades\DB;



class ClientController extends Controller
{
    // ✅ GET /api/clients
    public function index()
    {
        return Client::with(['comptes', 'credits', 'tickets'])->get();
    }

    // ✅ POST /api/clients/register
    // public function register(Request $request)
    // {
    //     $request->validate([
    //         'nom' => 'required',
    //         'prenom' => 'required',
    //         'email' => 'required|email|unique:clients',
    //         'password' => 'required|min:6',
    //         'telephone' => 'nullable|string',
    //         'adresse' => 'nullable|string',
    //     ]);

    //     $client = Client::create([
    //         'nom' => $request->nom,
    //         'prenom' => $request->prenom,
    //         'email' => $request->email,
    //         'password' => bcrypt($request->password),
    //         'telephone' => $request->telephone,
    //         'adresse' => $request->adresse,
    //         'statut' => 'Actif',
    //     ]);

    //     return response()->json($client, 201);
    // }

    public function register(Request $request)
    {
        $request->validate([
            'nom'       => 'required',
            'prenom'    => 'required',
            'email'     => 'required|email|unique:clients',
            'telephone' => 'nullable|string',
            'adresse'   => 'nullable|string',
        ]);

        // Mot de passe aléatoire (10 car.)
        $plainPassword = Str::random(10);

        $result = DB::transaction(function () use ($request, $plainPassword) {

            /** 1. Création du client */
            $client = Client::create([
                'nom'       => $request->nom,
                'prenom'    => $request->prenom,
                'email'     => $request->email,
                'password'  => bcrypt($plainPassword),
                'telephone' => $request->telephone,
                'adresse'   => $request->adresse,
                'statut'    => 'Actif',
            ]);

            /** 2. Création du compte courant */
            $compte = Compte::create([
                'numero'        => $this->generateUniqueAccountNumber(),
                'type'          => 'Courant',
                'solde'         => 10_000,
                'date_creation' => now(),
                'statut'        => 'Actif',
                'client_id'     => $client->id,
            ]);

            /** 3. Envoi du mail */
            Mail::to($client->email)
                ->send(new NouveauClientMail($client, $plainPassword, $compte));

            return ['client' => $client, 'compte' => $compte];
        });

        return response()->json($result, 201);
    }

    public function generateUniqueAccountNumber($length = 12)
{
    do {
        $numero = collect(range(1, $length))
                    ->map(fn() => random_int(0, 9))
                    ->implode('');
    } while (Compte::where('numero', $numero)->exists());

    return $numero;
}


    // ✅ POST /api/clients/login
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

        return response()->json([
            'id' => $client->id,
            'nom' => $client->nom,
            'prenom' => $client->prenom,
            'email' => $client->email,
            'statut' => $client->statut,
        ]);
    }

    // ✅ GET /api/clients/{client}
    public function show(Client $client)
    {
        return $client->load(['comptes', 'credits', 'tickets']);
    }

    // ✅ PUT /api/clients/{client}
    public function update(Request $request, Client $client)
    {
        $request->validate([
            'nom' => 'required',
            'prenom' => 'required',
            'email' => 'required|email|unique:clients,email,' . $client->id,
            'telephone' => 'nullable|string',
            'adresse' => 'nullable|string',
            'statut' => 'required|in:Actif,Suspendu',
        ]);

        $client->update($request->all());

        return $client;
    }

    // ✅ DELETE /api/clients/{client}
    public function destroy(Client $client)
    {
        if ($client->comptes()->exists()) {
            return response()->json(['error' => 'Client avec comptes actifs – suppression interdite'], 403);
        }

        $client->delete();
        return response()->json(null, 204);
    }

    // ✅ PUT /api/clients/{client}/suspend
    public function suspend(Client $client)
    {
        $client->update(['statut' => 'Suspendu']);
        return response()->json(['message' => 'Client suspendu']);
    }

    // ✅ PUT /api/clients/{client}/reactivate
    public function reactivate(Client $client)
    {
        if ($client->statut !== 'Suspendu') {
            return response()->json(['error' => 'Ce client n’est pas suspendu'], 400);
        }

        $client->update(['statut' => 'Actif']);
        return response()->json(['message' => 'Client réactivé']);
    }
}
