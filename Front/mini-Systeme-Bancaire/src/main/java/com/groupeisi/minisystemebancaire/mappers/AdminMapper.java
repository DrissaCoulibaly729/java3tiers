package com.groupeisi.minisystemebancaire.mappers;

import com.groupeisi.minisystemebancaire.dto.AdminDTO;
import com.groupeisi.minisystemebancaire.models.Admin;

public class AdminMapper {

    public static AdminDTO toDTO(Admin admin) {
        return new AdminDTO(
                admin.getId(),
                admin.getUsername(),
                admin.getRole(),
                admin.getPassword() // ✅ Ajout du password ici
        );
    }


    public static Admin toEntity(AdminDTO adminDTO) {
        Admin admin = new Admin();
        admin.setId(adminDTO.getId());
        admin.setUsername(adminDTO.getUsername());
        admin.setRole(adminDTO.getRole());
        return admin;
    }
}
