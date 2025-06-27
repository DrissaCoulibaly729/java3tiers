<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Factories\HasFactory;

class Compte extends Model
{
    
    
    use HasFactory;

    protected $fillable = [
        'numero', 'type', 'solde', 'date_creation', 'statut', 'client_id'
    ];

    public function client() {
        return $this->belongsTo(Client::class);
    }

    public function transactionsEnvoyees() {
        return $this->hasMany(Transaction::class, 'compte_source_id');
    }

    public function transactionsRecues() {
        return $this->hasMany(Transaction::class, 'compte_dest_id');
    }

    public function cartesBancaires() {
        return $this->hasMany(CarteBancaire::class);
    }

   
}
