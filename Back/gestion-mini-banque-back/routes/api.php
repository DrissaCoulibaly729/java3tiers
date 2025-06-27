<?php
use Illuminate\Http\Request;
use App\Http\Controllers\Api\{
    ClientController,
    AdminController,
    CompteController,
    CarteBancaireController,
    CreditController,
    RemboursementController,
    TransactionController,
    TicketSupportController
};

Route::get('transactions/suspectes', [TransactionController::class, 'getSuspectes']);

Route::apiResource('clients', ClientController::class);
Route::apiResource('admins', AdminController::class);
Route::apiResource('comptes', CompteController::class);
Route::apiResource('carte-bancaires', CarteBancaireController::class);
Route::apiResource('credits', CreditController::class);
Route::apiResource('remboursements', RemboursementController::class);
Route::apiResource('transactions', TransactionController::class);
Route::apiResource('ticket-supports', TicketSupportController::class);

Route::post('admins/login', [AdminController::class, 'login']);

Route::post('clients/register', [ClientController::class, 'register']);
Route::post('clients/login', [ClientController::class, 'login']);
Route::put('clients/{client}/suspend', [ClientController::class, 'suspend']);
Route::put('clients/{client}/reactivate', [ClientController::class, 'reactivate']);

Route::get('compte/{id}/cartes', [CarteBancaireController::class, 'getCartesByCompte']);
Route::put('carte-bancaires/{id}/bloquer', [CarteBancaireController::class, 'bloquer']);
Route::put('carte-bancaires/{id}/debloquer', [CarteBancaireController::class, 'debloquer']);
Route::get('carte-bancaires/valide/{id}', [CarteBancaireController::class, 'isValide']);

Route::get('comptes/client/{id}', [CompteController::class, 'getByClient']);
Route::get('comptes/numero/{numero}', [CompteController::class, 'getByNumero']);
Route::put('comptes/{id}/frais', [CompteController::class, 'appliquerFrais']);
Route::put('comptes/{id}/fermer', [CompteController::class, 'fermerCompte']);

Route::put('credits/{id}/accepter', [CreditController::class, 'accepter']);
Route::put('credits/{id}/refuser', [CreditController::class, 'refuser']);
Route::get('credits/client/{id}', [CreditController::class, 'getByClient']);
Route::get('credits/statut/{statut}', [CreditController::class, 'getByStatut']);

Route::get('credits/{id}/remboursements', [RemboursementController::class, 'getByCredit']);

Route::get('tickets/client/{id}', [TicketSupportController::class, 'getByClient']);
Route::get('ticket-supports/recherche/{value}', [TicketSupportController::class, 'rechercher']);
Route::put('ticket-supports/{id}/repondre', [TicketSupportController::class, 'repondre']);
Route::put('ticket-supports/{id}/resolu', [TicketSupportController::class, 'marquerResolu']);

Route::put('transactions/{id}/annuler', [TransactionController::class, 'annuler']);
Route::get('comptes/{id}/transactions', [TransactionController::class, 'getByCompte']);
Route::get('clients/{id}/transactions', [TransactionController::class, 'getByClient']);
