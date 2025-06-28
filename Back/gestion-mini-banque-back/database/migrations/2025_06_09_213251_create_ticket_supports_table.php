<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

class CreateTicketSupportsTable extends Migration
{
    public function up()
    {
        Schema::create('ticket_supports', function (Blueprint $table) {
            $table->id();
            $table->string('sujet');
            $table->text('description');
            $table->timestamp('date_ouverture')->default(now());
            $table->enum('statut', ['Ouvert', 'Répondu', 'Résolu'])->default('Ouvert');
            $table->text('reponse')->nullable();
            $table->foreignId('client_id')->constrained()->onDelete('cascade');
            $table->foreignId('admin_id')->nullable()->constrained()->onDelete('set null');
            $table->timestamps();
        });
    }

    public function down()
    {
        Schema::dropIfExists('ticket_supports');
    }
}
