<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

class CreateCarteBancairesTable extends Migration
{
    public function up()
    {
        Schema::create('carte_bancaires', function (Blueprint $table) {
            $table->id();
            $table->string('numero')->unique();
            $table->string('cvv');
            $table->date('date_expiration');
            $table->decimal('solde', 15, 2)->default(0);
            $table->enum('statut', ['Active', 'Bloquée'])->default('Active');
            $table->foreignId('compte_id')->constrained()->onDelete('cascade');
            $table->string('code_pin');
            $table->timestamps();
        });
    }

    public function down()
    {
        Schema::dropIfExists('carte_bancaires');
    }
}
