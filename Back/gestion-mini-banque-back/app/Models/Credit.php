<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;

class Credit extends Model
{
    protected $fillable = [
        'montant', 'taux_interet', 'duree_mois', 'mensualite', 
        'date_demande', 'statut', 'client_id'
    ];

    protected $casts = [
        'date_demande' => 'datetime',
        'montant' => 'decimal:2',
        'taux_interet' => 'decimal:2',
        'mensualite' => 'decimal:2',
    ];

    public function client(): BelongsTo
    {
        return $this->belongsTo(Client::class);
    }

    public function remboursements(): HasMany
    {
        return $this->hasMany(Remboursement::class);
    }

    // Calculer la mensualité
    public static function calculerMensualite(float $montant, float $taux, int $duree): float
    {
        $tauxMensuel = $taux / 100 / 12;
        if ($tauxMensuel == 0) {
            return $montant / $duree;
        }
        
        return $montant * ($tauxMensuel * pow(1 + $tauxMensuel, $duree)) / 
               (pow(1 + $tauxMensuel, $duree) - 1);
    }
}
