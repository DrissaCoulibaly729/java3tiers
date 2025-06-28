<?php

namespace App\Models;


use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Client extends Model
{
    protected $fillable = [
        'nom', 'prenom', 'email', 'telephone', 'adresse', 
        'date_inscription', 'statut', 'password'
    ];

    protected $casts = [
        'date_inscription' => 'datetime',
        'password' => 'hashed',
    ];

    public function comptes(): HasMany
    {
        return $this->hasMany(Compte::class);
    }

    public function credits(): HasMany
    {
        return $this->hasMany(Credit::class);
    }

    public function ticketSupports(): HasMany
    {
        return $this->hasMany(TicketSupport::class);
    }
}
