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
        Schema::create('ticket_supports', function (Blueprint $table) {
            $table->id();
            $table->string('sujet');
            $table->text('description');
            $table->dateTime('date_ouverture');
            $table->string('statut');
            $table->text('reponse')->nullable();
            $table->foreignId('client_id')->constrained()->onDelete('cascade');
            $table->foreignId('admin_id')->nullable()->constrained()->onDelete('set null');
            $table->timestamps();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('ticket_supports');
    }
};
