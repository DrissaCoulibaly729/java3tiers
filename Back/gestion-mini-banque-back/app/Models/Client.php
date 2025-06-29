<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasMany;

class Client extends Model
{
    use HasFactory;

    protected $fillable = [
        'nom',
        'prenom',
        'email',
        'telephone',
        'adresse',
        'password',
        'statut',
        'date_inscription'
    ];

    protected $hidden = [
        'password',
    ];

    protected $casts = [
        'date_inscription' => 'datetime',
        'created_at' => 'datetime',
        'updated_at' => 'datetime',
    ];

    // ✅ RELATIONS

    /**
     * Un client peut avoir plusieurs comptes
     */
    public function comptes(): HasMany
    {
        return $this->hasMany(Compte::class);
    }

    /**
     * Un client peut avoir plusieurs crédits
     */
    public function credits(): HasMany
    {
        return $this->hasMany(Credit::class);
    }

    /**
     * Un client peut avoir plusieurs tickets de support
     */
    public function ticketsSupport(): HasMany
    {
        return $this->hasMany(TicketSupport::class);
    }

    // ✅ ACCESSEURS (GETTERS)

    /**
     * Obtenir le nom complet du client
     */
    public function getNomCompletAttribute(): string
    {
        return $this->prenom . ' ' . $this->nom;
    }

    /**
     * Obtenir le statut avec couleur pour l'affichage
     */
    public function getStatutColorAttribute(): string
    {
        return match($this->statut) {
            'Actif' => 'success',
            'Suspendu' => 'warning',
            'Fermé' => 'danger',
            default => 'secondary'
        };
    }

    // ✅ SCOPES (REQUÊTES PRÉDÉFINIES)

    /**
     * Scope pour les clients actifs
     */
    public function scopeActif($query)
    {
        return $query->where('statut', 'Actif');
    }

    /**
     * Scope pour les clients suspendus
     */
    public function scopeSuspendu($query)
    {
        return $query->where('statut', 'Suspendu');
    }

    /**
     * Scope pour rechercher par nom ou email
     */
    public function scopeRecherche($query, $term)
    {
        return $query->where(function($q) use ($term) {
            $q->where('nom', 'LIKE', "%{$term}%")
              ->orWhere('prenom', 'LIKE', "%{$term}%")
              ->orWhere('email', 'LIKE', "%{$term}%")
              ->orWhere('telephone', 'LIKE', "%{$term}%");
        });
    }

    // ✅ MÉTHODES MÉTIER

    /**
     * Vérifier si le client peut effectuer des transactions
     */
    public function peutEffectuerTransactions(): bool
    {
        return $this->statut === 'Actif';
    }

    /**
     * Obtenir le compte principal du client (premier compte courant)
     */
    public function getComptePrincipal()
    {
        return $this->comptes()
                    ->where('type', 'Courant')
                    ->where('statut', 'Actif')
                    ->first();
    }

    /**
     * Obtenir le solde total de tous les comptes
     */
    public function getSoldeTotal(): float
    {
        return $this->comptes()
                    ->where('statut', 'Actif')
                    ->sum('solde');
    }

    /**
     * Obtenir le nombre de comptes actifs
     */
    public function getNombreComptesActifs(): int
    {
        return $this->comptes()
                    ->where('statut', 'Actif')
                    ->count();
    }

    /**
     * Suspendre le client et tous ses comptes
     */
    public function suspendre(): bool
    {
        if ($this->statut !== 'Actif') {
            return false;
        }

        // Suspendre le client
        $this->update(['statut' => 'Suspendu']);

        // Suspendre tous ses comptes actifs
        $this->comptes()
             ->where('statut', 'Actif')
             ->update(['statut' => 'Suspendu']);

        return true;
    }

    /**
     * Réactiver le client et tous ses comptes
     */
    public function reactiver(): bool
    {
        if ($this->statut !== 'Suspendu') {
            return false;
        }

        // Réactiver le client
        $this->update(['statut' => 'Actif']);

        // Réactiver tous ses comptes suspendus
        $this->comptes()
             ->where('statut', 'Suspendu')
             ->update(['statut' => 'Actif']);

        return true;
    }

    // ✅ ÉVÉNEMENTS DU MODÈLE

    protected static function boot()
    {
        parent::boot();

        // Lors de la création d'un client
        static::creating(function ($client) {
            if (!$client->date_inscription) {
                $client->date_inscription = now();
            }
            if (!$client->statut) {
                $client->statut = 'Actif';
            }
        });

        // Lors de la suppression d'un client
        static::deleting(function ($client) {
            // Supprimer tous les comptes associés
            $client->comptes()->delete();
            
            // Supprimer tous les crédits associés
            $client->credits()->delete();
            
            // Supprimer tous les tickets de support associés
            $client->ticketsSupport()->delete();
        });
    }
}