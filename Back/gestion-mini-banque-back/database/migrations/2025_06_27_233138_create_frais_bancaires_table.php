<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

class CreateFraisBancairesTable extends Migration
{
    public function up()
    {
        Schema::create('frais_bancaires', function (Blueprint $table) {
            $table->id();
            $table->foreignId('compte_id')->constrained()->onDelete('cascade');
            $table->enum('type', ['Mensuel', 'Transaction', 'Maintenance']);
            $table->decimal('montant', 15, 2);
            $table->timestamp('date_application')->default(now());
            $table->timestamps();
        });
    }

    public function down()
    {
        Schema::dropIfExists('frais_bancaires');
    }
}
