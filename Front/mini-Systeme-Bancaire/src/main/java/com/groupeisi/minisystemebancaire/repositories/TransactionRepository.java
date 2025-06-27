package com.groupeisi.minisystemebancaire.repositories;

import com.groupeisi.minisystemebancaire.models.Transaction;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.time.LocalDateTime;
import java.util.List;

public class TransactionRepository {
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("banquePU");

    public void save(Transaction transaction) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(transaction);
        em.getTransaction().commit();
        em.close();
    }

    public List<Transaction> findByCompteSourceId(Long compteId) {
        EntityManager em = emf.createEntityManager();
        List<Transaction> transactions = em.createQuery("SELECT t FROM Transaction t WHERE t.compteSource.id = :compteId", Transaction.class)
                .setParameter("compteId", compteId)
                .getResultList();
        em.close();
        return transactions;
    }

    public Transaction findById(Long id) {
        EntityManager em = emf.createEntityManager();
        Transaction transaction = em.find(Transaction.class, id);
        em.close();
        return transaction;
    }

    public void update(Transaction transaction) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.merge(transaction);
        em.getTransaction().commit();
        em.close();
    }

    public void delete(Transaction transaction) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.remove(em.contains(transaction) ? transaction : em.merge(transaction));
        em.getTransaction().commit();
        em.close();
    }
    public List<Transaction> findAll() {
        EntityManager em = emf.createEntityManager();
        List<Transaction> transactions = em.createQuery("SELECT t FROM Transaction t", Transaction.class).getResultList();
        em.close();
        return transactions;
    }
    /**
     * ✅ Compte les transactions effectuées par un compte dans les X dernières minutes
     */
    public int countRecentTransactions(Long compteId, int minutes) {
        EntityManager em = emf.createEntityManager();
        LocalDateTime timeLimit = LocalDateTime.now().minusMinutes(minutes);

        Long count = em.createQuery("SELECT COUNT(t) FROM Transaction t WHERE t.compteSource.id = :compteId AND t.date >= :timeLimit", Long.class)
                .setParameter("compteId", compteId)
                .setParameter("timeLimit", timeLimit)
                .getSingleResult();

        em.close();
        return count.intValue();
    }

    public List<Transaction> findByCompteSourceIdOrCompteDestId(Long sourceId, Long destId) {
        EntityManager em = emf.createEntityManager();
        return em.createQuery(
                        "SELECT t FROM Transaction t WHERE t.compteSource.id = :sourceId OR t.compteDest.id = :destId",
                        Transaction.class)
                .setParameter("sourceId", sourceId)
                .setParameter("destId", destId)
                .getResultList();
    }


}
