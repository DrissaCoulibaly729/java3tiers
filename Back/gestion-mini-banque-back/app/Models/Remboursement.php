<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Remboursement extends Model
{
    use HasFactory;

    protected $fillable = ['montant', 'date', 'credit_id'];

    public function credit() {
        return $this->belongsTo(Credit::class);
    }
}
