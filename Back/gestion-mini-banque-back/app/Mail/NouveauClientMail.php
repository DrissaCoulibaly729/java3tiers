<?php

namespace App\Mail;

use App\Models\Compte;
use App\Models\Client;
use Illuminate\Bus\Queueable;
use Illuminate\Mail\Mailable;
use Illuminate\Queue\SerializesModels;

class NouveauClientMail extends Mailable
{
    use Queueable, SerializesModels;

    public function __construct(
        public Client  $client,
        public string  $plainPassword,
        public Compte $compte
    ) {}

    public function build(): self
    {
        return $this->subject('Bienvenue dans Mini-Banque 🎉')
                    ->markdown('emails.nouveau-client');
    }
}
