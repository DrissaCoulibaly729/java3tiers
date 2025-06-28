<?php

namespace Database\Seeders;

use App\Models\Admin;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\Hash;

class AdminSeeder extends Seeder
{
    public function run()
    {
        Admin::create([
            'username' => 'admin',
            'password' => Hash::make('admin123'),
            'role' => 'ADMIN',
        ]);

        Admin::create([
            'username' => 'gestionnaire',
            'password' => Hash::make('gestionnaire123'),
            'role' => 'GESTIONNAIRE',
        ]);
    }
}