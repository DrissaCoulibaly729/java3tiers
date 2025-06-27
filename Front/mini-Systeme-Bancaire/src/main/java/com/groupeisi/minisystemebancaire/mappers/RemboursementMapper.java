package com.groupeisi.minisystemebancaire.mappers;

import com.groupeisi.minisystemebancaire.dto.RemboursementDTO;
import com.groupeisi.minisystemebancaire.models.Remboursement;

public class RemboursementMapper {

    public static RemboursementDTO toDTO(Remboursement remboursement) {
        return new RemboursementDTO(
                remboursement.getId(),
                remboursement.getMontant(),
                remboursement.getDate(),
                remboursement.getCredit().getId()
        );
    }

    public static Remboursement toEntity(RemboursementDTO remboursementDTO) {
        Remboursement remboursement = new Remboursement();
        remboursement.setId(remboursementDTO.getId());
        remboursement.setMontant(remboursementDTO.getMontant());
        remboursement.setDate(remboursementDTO.getDate());
        return remboursement;
    }
}
