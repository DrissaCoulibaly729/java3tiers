<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class TicketSupport extends Model
{
    protected $fillable = [
        'sujet', 'description', 'date_ouverture', 
        'statut', 'reponse', 'client_id', 'admin_id'
    ];

    protected $casts = [
        'date_ouverture' => 'datetime',
    ];

    public function client(): BelongsTo
    {
        return $this->belongsTo(Client::class);
    }

    public function admin(): BelongsTo
    {
        return $this->belongsTo(Admin::class);
    }
}
