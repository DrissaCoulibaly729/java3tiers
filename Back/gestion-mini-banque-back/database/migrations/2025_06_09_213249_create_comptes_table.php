<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

class CreateComptesTable extends Migration
{
    public function up()
    {
        Schema::create('comptes', function (Blueprint $table) {
            $table->id();
            $table->string('numero')->unique();
            $table->enum('type', ['Courant', 'Épargne']);
            $table->decimal('solde', 15, 2)->default(0);
            $table->timestamp('date_creation')->default(now());
            $table->enum('statut', ['Actif', 'Fermé'])->default('Actif');
            $table->foreignId('client_id')->constrained()->onDelete('cascade');
            $table->timestamps();
        });
    }

    public function down()
    {
        Schema::dropIfExists('comptes');
    }
}
