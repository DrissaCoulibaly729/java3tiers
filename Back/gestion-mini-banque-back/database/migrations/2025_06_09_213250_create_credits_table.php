<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

class CreateCreditsTable extends Migration
{
    public function up()
    {
        Schema::create('credits', function (Blueprint $table) {
            $table->id();
            $table->decimal('montant', 15, 2);
            $table->decimal('taux_interet', 5, 2);
            $table->integer('duree_mois');
            $table->decimal('mensualite', 15, 2);
            $table->timestamp('date_demande')->default(now());
            $table->enum('statut', ['En attente', 'Approuvé', 'Refusé', 'Terminé'])->default('En attente');
            $table->foreignId('client_id')->constrained()->onDelete('cascade');
            $table->timestamps();
        });
    }

    public function down()
    {
        Schema::dropIfExists('credits');
    }
}
