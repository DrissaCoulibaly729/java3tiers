package com.groupeisi.minisystemebancaire.mappers;

import com.groupeisi.minisystemebancaire.dto.TransactionDTO;
import com.groupeisi.minisystemebancaire.models.Compte;
import com.groupeisi.minisystemebancaire.models.Transaction;
import com.groupeisi.minisystemebancaire.repositories.CompteRepository;

public class TransactionMapper {
    private static final CompteRepository compteRepository = new CompteRepository();
    public static TransactionDTO toDTO(Transaction transaction) {
        if (transaction == null) return null;

        return new TransactionDTO(
                transaction.getId(),
                transaction.getType(),
                transaction.getMontant(),
                transaction.getDate(), // LocalDateTime est directement passé
                transaction.getCompteSource() != null ? transaction.getCompteSource().getId() : null,
                transaction.getCompteDest() != null ? transaction.getCompteDest().getId() : null,
                transaction.getStatut()
        );
    }

    public static Transaction toEntity(TransactionDTO dto) {
        if (dto == null) return null;

        Transaction transaction = new Transaction();
        transaction.setId(dto.getId());
        transaction.setType(dto.getType());
        transaction.setMontant(dto.getMontant());
        transaction.setDate(dto.getDate()); // LocalDateTime est directement utilisé
        transaction.setStatut(dto.getStatut());
        // Récupération des comptes à partir des IDs
        if (dto.getCompteSourceId() != null) {
            Compte compteSource = compteRepository.findById(dto.getCompteSourceId());
            transaction.setCompteSource(compteSource);
        }

        if (dto.getCompteDestId() != null) {
            Compte compteDest = compteRepository.findById(dto.getCompteDestId());
            transaction.setCompteDest(compteDest);
        }
        return transaction;
    }
}
