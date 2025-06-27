package com.groupeisi.minisystemebancaire.repositories;

import com.groupeisi.minisystemebancaire.models.Compte;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.List;

public class CompteRepository {
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("banquePU");

    public void save(Compte compte) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(compte);
        em.getTransaction().commit();
        em.close();
    }

    public Compte findById(Long id) {
        EntityManager em = emf.createEntityManager();
        Compte compte = em.find(Compte.class, id);
        em.close();
        return compte;
    }

    public Compte findByNumero(String numero) {
        EntityManager em = emf.createEntityManager();
        return em.createQuery("SELECT c FROM Compte c WHERE c.numero = :numero", Compte.class)
                .setParameter("numero", numero)
                .getSingleResult();
    }


    public List<Compte> findByClientId(Long clientId) {
        EntityManager em = emf.createEntityManager();
        List<Compte> comptes = em.createQuery("SELECT c FROM Compte c WHERE c.client.id = :clientId", Compte.class)
                .setParameter("clientId", clientId)
                .getResultList();
        em.close();
        return comptes;
    }

    public void update(Compte compte) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.merge(compte);
        em.getTransaction().commit();
        em.close();
    }

    public void delete(Compte compte) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.remove(em.contains(compte) ? compte : em.merge(compte));
        em.getTransaction().commit();
        em.close();
    }
    public List<Compte> findAll() {
        EntityManager em = emf.createEntityManager();
        List<Compte> comptes = em.createQuery("SELECT c FROM Compte c", Compte.class).getResultList();
        em.close();
        return comptes;
    }

}
