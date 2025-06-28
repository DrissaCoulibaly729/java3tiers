<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Factories\HasFactory;

class Compte extends Model
{
    protected $fillable = [
        'numero', 'type', 'solde', 'date_creation', 'statut', 'client_id'
    ];

    protected $casts = [
        'date_creation' => 'datetime',
        'solde' => 'decimal:2',
    ];

    public function client(): BelongsTo
    {
        return $this->belongsTo(Client::class);
    }

    public function transactionsSource(): HasMany
    {
        return $this->hasMany(Transaction::class, 'compte_source_id');
    }

    public function transactionsDestination(): HasMany
    {
        return $this->hasMany(Transaction::class, 'compte_dest_id');
    }

    public function carteBancaires(): HasMany
    {
        return $this->hasMany(CarteBancaire::class);
    }

    public function fraisBancaires(): HasMany
    {
        return $this->hasMany(FraisBancaire::class);
    }

    // Générer un numéro de compte unique
    public static function genererNumeroCompte(): string
    {
        do {
            $numero = 'CPT' . str_pad(rand(0, 99999999), 8, '0', STR_PAD_LEFT);
        } while (self::where('numero', $numero)->exists());
        
        return $numero;
    }
}
