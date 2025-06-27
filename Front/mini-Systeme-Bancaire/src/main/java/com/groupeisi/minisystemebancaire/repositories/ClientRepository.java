package com.groupeisi.minisystemebancaire.repositories;

import com.groupeisi.minisystemebancaire.models.Client;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.List;

public class ClientRepository {
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("banquePU");

    public void save(Client client) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(client);
        em.getTransaction().commit();
        em.close();
    }

    public Client findById(Long id) {
        EntityManager em = emf.createEntityManager();
        Client client = em.find(Client.class, id);
        em.close();
        return client;
    }

    public Client findByEmail(String email) {
        EntityManager em = emf.createEntityManager();
        List<Client> clients = em.createQuery("SELECT c FROM Client c WHERE c.email = :email", Client.class)
                .setParameter("email", email)
                .getResultList();
        em.close();
        return clients.isEmpty() ? null : clients.get(0);
    }

    public List<Client> findAll() {
        EntityManager em = emf.createEntityManager();
        List<Client> clients = em.createQuery("SELECT c FROM Client c", Client.class).getResultList();
        em.close();
        return clients;
    }

    public void update(Client client) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.merge(client);
        em.getTransaction().commit();
        em.close();
    }

    public void delete(Client client) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.remove(em.contains(client) ? client : em.merge(client));
        em.getTransaction().commit();
        em.close();
    }
}
