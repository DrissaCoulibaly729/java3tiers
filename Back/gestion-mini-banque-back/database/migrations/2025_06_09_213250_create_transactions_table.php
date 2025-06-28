<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

class CreateTransactionsTable extends Migration
{
    public function up()
    {
        Schema::create('transactions', function (Blueprint $table) {
            $table->id();
            $table->enum('type', ['Dépôt', 'Retrait', 'Virement']);
            $table->decimal('montant', 15, 2);
            $table->timestamp('date')->default(now());
            $table->foreignId('compte_source_id')->nullable()->constrained('comptes');
            $table->foreignId('compte_dest_id')->nullable()->constrained('comptes');
            $table->enum('statut', ['Validé', 'Rejeté', 'En attente'])->default('Validé');
            $table->text('description')->nullable();
            $table->timestamps();
        });
    }

    public function down()
    {
        Schema::dropIfExists('transactions');
    }
}