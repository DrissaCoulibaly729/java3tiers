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
        Schema::create('carte_bancaires', function (Blueprint $table) {
            $table->id();
            $table->string('numero');
            $table->string('type');
            $table->string('cvv');
            $table->string('date_expiration');
            $table->double('solde');
            $table->string('statut');
            $table->string('code_pin');
            $table->foreignId('compte_id')->constrained()->onDelete('cascade');
            $table->timestamps();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('carte_bancaires');
    }
};
