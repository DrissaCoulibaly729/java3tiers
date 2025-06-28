<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class FraisBancaire extends Model
{
    protected $fillable = ['compte_id', 'type', 'montant', 'date_application'];

    protected $casts = [
        'date_application' => 'datetime',
        'montant' => 'decimal:2',
    ];

    public function compte(): BelongsTo
    {
        return $this->belongsTo(Compte::class);
    }
}
