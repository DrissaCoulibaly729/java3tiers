<?php

namespace App\Models;


use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Client extends Model
{
    use HasFactory;

    protected $fillable = [
        'nom', 'prenom', 'email', 'telephone', 'adresse', 'statut', 'password'
    ];

    public function comptes() {
        return $this->hasMany(Compte::class);
    }

    public function credits() {
        return $this->hasMany(Credit::class);
    }

    public function tickets() {
        return $this->hasMany(TicketSupport::class);
    }
}
