package com.groupeisi.minisystemebancaire.repositories;

import com.groupeisi.minisystemebancaire.models.Remboursement;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.List;

public class RemboursementRepository {
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("banquePU");

    public void save(Remboursement remboursement) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(remboursement);
        em.getTransaction().commit();
        em.close();
    }

    public Remboursement findById(Long id) {
        EntityManager em = emf.createEntityManager();
        Remboursement remboursement = em.find(Remboursement.class, id);
        em.close();
        return remboursement;
    }

    public List<Remboursement> findByCreditId(Long creditId) {
        EntityManager em = emf.createEntityManager();
        List<Remboursement> remboursements = em.createQuery("SELECT r FROM Remboursement r WHERE r.credit.id = :creditId", Remboursement.class)
                .setParameter("creditId", creditId)
                .getResultList();
        em.close();
        return remboursements;
    }

    public void update(Remboursement remboursement) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.merge(remboursement);
        em.getTransaction().commit();
        em.close();
    }

    public void delete(Remboursement remboursement) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.remove(em.contains(remboursement) ? remboursement : em.merge(remboursement));
        em.getTransaction().commit();
        em.close();
    }
    public List<Remboursement> findAll() {
        EntityManager em = emf.createEntityManager();
        List<Remboursement> remboursements = em.createQuery("SELECT r FROM Remboursement r", Remboursement.class).getResultList();
        em.close();
        return remboursements;
    }

}
