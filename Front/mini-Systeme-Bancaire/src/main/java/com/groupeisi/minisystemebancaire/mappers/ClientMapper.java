package com.groupeisi.minisystemebancaire.mappers;

import com.groupeisi.minisystemebancaire.dto.ClientDTO;
import com.groupeisi.minisystemebancaire.models.Client;

public class ClientMapper {

    public static ClientDTO toDTO(Client client) {
        return new ClientDTO(
                client.getId(),
                client.getNom(),
                client.getPrenom(),
                client.getEmail(),
                client.getTelephone(),
                client.getAdresse(),
                client.getStatut(),
                client.getPassword()
        );
    }

    public static Client toEntity(ClientDTO clientDTO) {
        Client client = new Client();
        client.setId(clientDTO.getId());
        client.setNom(clientDTO.getNom());
        client.setPrenom(clientDTO.getPrenom());
        client.setEmail(clientDTO.getEmail());
        client.setTelephone(clientDTO.getTelephone());
        client.setAdresse(clientDTO.getAdresse());
        client.setStatut(clientDTO.getStatut());
        client.setPassword(clientDTO.getPassword());
        return client;
    }
}
