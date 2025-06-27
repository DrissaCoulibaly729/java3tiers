package com.groupeisi.minisystemebancaire.repositories;

import com.groupeisi.minisystemebancaire.models.TicketSupport;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.List;

public class TicketSupportRepository {
    private final EntityManagerFactory emf = Persistence.createEntityManagerFactory("banquePU");

    // ✅ Sauvegarder un ticket
    public void save(TicketSupport ticket) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(ticket);
        em.getTransaction().commit();
        em.close();
    }

    // ✅ Trouver un ticket par ID
    public TicketSupport findById(Long id) {
        EntityManager em = emf.createEntityManager();
        TicketSupport ticket = em.find(TicketSupport.class, id);
        em.close();
        return ticket;
    }

    // ✅ Trouver un ticket par ID client
    public List<TicketSupport> findByClientId(Long clientId) {
        EntityManager em = emf.createEntityManager();
        List<TicketSupport> tickets = em.createQuery(
                        "SELECT t FROM TicketSupport t WHERE t.client.id = :clientId", TicketSupport.class)
                .setParameter("clientId", clientId)
                .getResultList();
        em.close();
        return tickets;
    }

    // ✅ Trouver un ticket par statut ("Ouvert", "Répondu", "Résolu")
    public List<TicketSupport> findByStatut(String statut) {
        EntityManager em = emf.createEntityManager();
        List<TicketSupport> tickets = em.createQuery(
                        "SELECT t FROM TicketSupport t WHERE t.statut = :statut", TicketSupport.class)
                .setParameter("statut", statut)
                .getResultList();
        em.close();
        return tickets;
    }

    // ✅ Trouver un ticket par ID ou sujet
    public List<TicketSupport> findBySujetOrId(String recherche) {
        EntityManager em = emf.createEntityManager();
        List<TicketSupport> tickets = em.createQuery(
                        "SELECT t FROM TicketSupport t WHERE t.sujet LIKE :recherche OR CAST(t.id AS string) = :recherche",
                        TicketSupport.class)
                .setParameter("recherche", "%" + recherche + "%")
                .getResultList();
        em.close();
        return tickets;
    }

    // ✅ Mettre à jour un ticket
    public void update(TicketSupport ticket) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.merge(ticket);
        em.getTransaction().commit();
        em.close();
    }

    // ✅ Supprimer un ticket
    public void delete(TicketSupport ticket) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.remove(em.contains(ticket) ? ticket : em.merge(ticket));
        em.getTransaction().commit();
        em.close();
    }

    // ✅ Trouver tous les tickets
    public List<TicketSupport> findAll() {
        EntityManager em = emf.createEntityManager();
        List<TicketSupport> tickets = em.createQuery("SELECT t FROM TicketSupport t", TicketSupport.class)
                .getResultList();
        em.close();
        return tickets;
    }
}
