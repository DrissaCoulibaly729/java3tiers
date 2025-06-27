<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Credit extends Model
{
    use HasFactory;

    protected $fillable = [
        'montant', 'taux_interet', 'duree_mois', 'mensualite', 'date_demande', 'statut', 'client_id'
    ];

    public function client() {
        return $this->belongsTo(Client::class);
    }

    public function remboursements() {
        return $this->hasMany(Remboursement::class);
    }
}
