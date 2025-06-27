<?php

namespace App\Models;


use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class CarteBancaire extends Model
{
    use HasFactory;

    protected $fillable = [
        'numero', 'type', 'cvv', 'date_expiration', 'solde', 'statut', 'code_pin', 'compte_id'
    ];

    public function compte() {
        return $this->belongsTo(Compte::class);
    }
}
