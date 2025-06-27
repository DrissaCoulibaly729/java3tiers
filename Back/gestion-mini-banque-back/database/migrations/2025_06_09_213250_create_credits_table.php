<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
       Schema::create('credits', function (Blueprint $table) {
            $table->id();
            $table->double('montant');
            $table->double('taux_interet');
            $table->integer('duree_mois');
            $table->double('mensualite');
            $table->dateTime('date_demande');
            $table->string('statut');
            $table->foreignId('client_id')->constrained()->onDelete('cascade');
            $table->timestamps();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('credits');
    }
};
