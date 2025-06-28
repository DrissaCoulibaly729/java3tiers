<?php
// routes/api.php
use Illuminate\Http\Request;
use App\Http\Controllers\Api\{
    ClientController,
    AdminController,
    CompteController,
    CarteBancaireController,
    CreditController,
    RemboursementController,
    TransactionController,
    TicketSupportController,
    FraisBancaireController
};

// Routes Clients
Route::apiResource('clients', ClientController::class);
Route::post('clients/register', [ClientController::class, 'register']);
Route::post('clients/login', [ClientController::class, 'login']);
Route::put('clients/{client}/suspend', [ClientController::class, 'suspend']);
Route::put('clients/{client}/reactivate', [ClientController::class, 'reactivate']);

// Routes Admins
Route::apiResource('admins', AdminController::class);
Route::post('admins/login', [AdminController::class, 'login']);

// Routes Comptes
Route::apiResource('comptes', CompteController::class);
Route::get('comptes/client/{id}', [CompteController::class, 'getByClient']);
Route::get('comptes/numero/{numero}', [CompteController::class, 'getByNumero']);
Route::put('comptes/{id}/frais', [CompteController::class, 'appliquerFrais']);
Route::put('comptes/{id}/fermer', [CompteController::class, 'fermerCompte']);

// Routes Cartes Bancaires
Route::apiResource('carte-bancaires', CarteBancaireController::class);
Route::get('compte/{id}/cartes', [CarteBancaireController::class, 'getCartesByCompte']);
Route::put('carte-bancaires/{id}/bloquer', [CarteBancaireController::class, 'bloquer']);
Route::put('carte-bancaires/{id}/debloquer', [CarteBancaireController::class, 'debloquer']);
Route::get('carte-bancaires/valide/{id}', [CarteBancaireController::class, 'isValide']);

// Routes Crédits
Route::apiResource('credits', CreditController::class);
Route::put('credits/{id}/accepter', [CreditController::class, 'accepter']);
Route::put('credits/{id}/refuser', [CreditController::class, 'refuser']);
Route::get('credits/client/{id}', [CreditController::class, 'getByClient']);
Route::get('credits/statut/{statut}', [CreditController::class, 'getByStatut']);

// Routes Remboursements
Route::apiResource('remboursements', RemboursementController::class);
Route::get('credits/{id}/remboursements', [RemboursementController::class, 'getByCredit']);

// Routes Transactions
Route::apiResource('transactions', TransactionController::class);
Route::get('transactions/suspectes', [TransactionController::class, 'getSuspectes']);
Route::put('transactions/{id}/annuler', [TransactionController::class, 'annuler']);
Route::get('comptes/{id}/transactions', [TransactionController::class, 'getByCompte']);
Route::get('clients/{id}/transactions', [TransactionController::class, 'getByClient']);

// Routes Tickets Support
Route::apiResource('ticket-supports', TicketSupportController::class);
Route::get('tickets/client/{id}', [TicketSupportController::class, 'getByClient']);
Route::get('ticket-supports/recherche/{value}', [TicketSupportController::class, 'rechercher']);
Route::put('ticket-supports/{id}/repondre', [TicketSupportController::class, 'repondre']);
Route::put('ticket-supports/{id}/resolu', [TicketSupportController::class, 'marquerResolu']);

// Routes Frais Bancaires
Route::apiResource('frais-bancaires', FraisBancaireController::class);
Route::get('comptes/{id}/frais', [FraisBancaireController::class, 'getByCompte']);
