<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

class CreateRemboursementsTable extends Migration
{
    public function up()
    {
        Schema::create('remboursements', function (Blueprint $table) {
            $table->id();
            $table->decimal('montant', 15, 2);
            $table->timestamp('date')->default(now());
            $table->foreignId('credit_id')->constrained()->onDelete('cascade');
            $table->timestamps();
        });
    }

    public function down()
    {
        Schema::dropIfExists('remboursements');
    }
}
