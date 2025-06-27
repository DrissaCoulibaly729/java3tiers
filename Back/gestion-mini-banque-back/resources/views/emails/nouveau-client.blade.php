@component('mail::message')
# Bienvenue {{ $client->prenom }} !

Vos identifiants :

- **Email :** {{ $client->email }}
- **Mot de passe initial :** {{ $plainPassword }}

Un compte **{{ $compte->type }}** a été ouvert pour vous :

- **N° Compte :** {{ $compte->numero }}
- **Solde initial :** {{ number_format($compte->solde, 0, ',', ' ') }} FCFA

Vous pouvez changer votre mot de passe depuis votre espace client.

Thanks,<br>
{{ config('app.name') }}
@endcomponent
