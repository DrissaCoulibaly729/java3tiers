package com.groupeisi.minisystemebancaire.repositories;

import com.groupeisi.minisystemebancaire.models.Credit;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.List;

public class CreditRepository {
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("banquePU");

    public void save(Credit credit) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(credit);
        em.getTransaction().commit();
        em.close();
    }

    public Credit findById(Long id) {
        EntityManager em = emf.createEntityManager();
        Credit credit = em.find(Credit.class, id);
        em.close();
        return credit;
    }

    public List<Credit> findByClientId(Long clientId) {
        EntityManager em = emf.createEntityManager();
        List<Credit> credits = em.createQuery("SELECT c FROM Credit c WHERE c.client.id = :clientId", Credit.class)
                .setParameter("clientId", clientId)
                .getResultList();
        em.close();
        return credits;
    }

    public List<Credit> findAll() {
        EntityManager em = emf.createEntityManager();
        List<Credit> credits = em.createQuery("SELECT c FROM Credit c", Credit.class).getResultList();
        em.close();
        return credits;
    }

    public void update(Credit credit) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.merge(credit);
        em.getTransaction().commit();
        em.close();
    }

    public void delete(Credit credit) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.remove(em.contains(credit) ? credit : em.merge(credit));
        em.getTransaction().commit();
        em.close();
    }
    // ✅ Recherche des crédits par statut (En attente, Accepté, Refusé)
    public List<Credit> findByStatut(String statut) {
        EntityManager em = emf.createEntityManager();
        List<Credit> credits = em.createQuery("SELECT c FROM Credit c WHERE c.statut = :statut", Credit.class)
                .setParameter("statut", statut)
                .getResultList();
        em.close();
        return credits;
    }


}
