<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class CarteBancaire extends Model
{
    protected $fillable = [
        'numero', 'cvv', 'date_expiration', 'solde', 
        'statut', 'compte_id', 'code_pin'
    ];

    protected $casts = [
        'date_expiration' => 'date',
        'solde' => 'decimal:2',
        'code_pin' => 'hashed',
    ];

    public function compte(): BelongsTo
    {
        return $this->belongsTo(Compte::class);
    }

    // Générer un numéro de carte unique
    public static function genererNumeroCarte(): string
    {
        do {
            $numero = '4' . str_pad(rand(0, 999999999999999), 15, '0', STR_PAD_LEFT);
        } while (self::where('numero', $numero)->exists());
        
        return $numero;
    }

    // Générer un CVV
    public static function genererCVV(): string
    {
        return str_pad(rand(0, 999), 3, '0', STR_PAD_LEFT);
    }

    // Générer un PIN
    public static function genererPIN(): string
    {
        return str_pad(rand(0, 9999), 4, '0', STR_PAD_LEFT);
    }
}