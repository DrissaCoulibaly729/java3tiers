<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class Transaction extends Model
{
    protected $fillable = [
        'type', 'montant', 'date', 'compte_source_id', 
        'compte_dest_id', 'statut', 'description'
    ];

    protected $casts = [
        'date' => 'datetime',
        'montant' => 'decimal:2',
    ];

    public function compteSource(): BelongsTo
    {
        return $this->belongsTo(Compte::class, 'compte_source_id');
    }

    public function compteDestination(): BelongsTo
    {
        return $this->belongsTo(Compte::class, 'compte_dest_id');
    }

    // Vérifier si une transaction est suspecte
    public function estSuspecte(): bool
    {
        return $this->montant > 10000 || 
               ($this->type === 'Retrait' && $this->montant > 5000);
    }
}