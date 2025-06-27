<?php

namespace App\Models;


use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Admin extends Model
{
     use HasFactory;

    protected $fillable = ['username', 'password', 'role'];

    public function ticketsGeres() {
        return $this->hasMany(TicketSupport::class);
    }
}
