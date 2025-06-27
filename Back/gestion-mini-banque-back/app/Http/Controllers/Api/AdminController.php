<?php

namespace App\Http\Controllers\Api;

use App\Models\Admin;
use Illuminate\Http\Request;
use App\Http\Controllers\Controller;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Validator;

class AdminController extends Controller
{
    // ✅ GET /api/admins
    public function index()
    {
        return Admin::all();
    }

    // ✅ POST /api/admins (création)
    public function store(Request $request)
    {
        $request->validate([
            'username' => 'required|unique:admins',
            'password' => 'required|min:6',
            'role'     => 'required|in:ADMIN,GESTIONNAIRE',
        ]);

        $admin = Admin::create([
            'username' => $request->username,
            'password' => bcrypt($request->password),
            'role'     => $request->role,
        ]);

        return response()->json($admin, 201);
    }

    // ✅ GET /api/admins/{id}
    public function show(Admin $admin)
    {
        return $admin;
    }

    // ✅ PUT /api/admins/{id}
    public function update(Request $request, Admin $admin)
    {
        $request->validate([
            'username' => 'required|unique:admins,username,' . $admin->id,
            'password' => 'nullable|min:6',
            'role'     => 'required|in:Admin,Gestionnaire',
        ]);

        $admin->username = $request->username;
        if ($request->filled('password')) {
            $admin->password = bcrypt($request->password);
        }
        $admin->role = $request->role;
        $admin->save();

        return $admin;
    }

    // ✅ DELETE /api/admins/{id}
    public function destroy(Admin $admin)
    {
        if (Admin::count() <= 1) {
            return response()->json(['error' => 'Impossible de supprimer le dernier administrateur !'], 403);
        }

        $admin->delete();
        return response()->json(null, 204);
    }

    // ✅ POST /api/admins/login
    public function login(Request $request)
    {
        $request->validate([
            'username' => 'required',
            'password' => 'required',
        ]);

        $admin = Admin::where('username', $request->username)->first();

        if (!$admin || !Hash::check($request->password, $admin->password)) {
            return response()->json(['error' => 'Identifiants invalides'], 401);
        }

        return response()->json([
            'id' => $admin->id,
            'username' => $admin->username,
            'role' => $admin->role
        ]);
    }
}


