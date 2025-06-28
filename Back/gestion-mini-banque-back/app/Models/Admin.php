<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasMany;
use App\Models\TicketSupport;


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
