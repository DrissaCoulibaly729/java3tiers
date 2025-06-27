<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class TicketSupport extends Model
{
    use HasFactory;

    protected $fillable = [
        'sujet', 'description', 'date_ouverture', 'statut', 'reponse', 'client_id', 'admin_id'
    ];

    public function client() {
        return $this->belongsTo(Client::class);
    }

    public function admin() {
        return $this->belongsTo(Admin::class);
    }
}
