<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasMany;

class Admin extends Model
{
    protected $fillable = ['username', 'password', 'role'];

    protected $casts = [
        'password' => 'hashed',
    ];

    public function ticketSupports(): HasMany
    {
        return $this->hasMany(TicketSupport::class);
    }
}
