package com.groupeisi.minisystemebancaire.repositories;

import com.groupeisi.minisystemebancaire.models.Admin;
import com.groupeisi.minisystemebancaire.models.CarteBancaire;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.List;

public class CarteBancaireRepository {
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("banquePU");

    public CarteBancaire findByNumero(String numero) {
        EntityManager em = emf.createEntityManager();
        List<CarteBancaire> cartes = em.createQuery("SELECT c FROM CarteBancaire c WHERE c.numero = :numero", CarteBancaire.class)
                .setParameter("numero", numero)
                .getResultList();
        em.close();
        return cartes.isEmpty() ? null : cartes.get(0);
    }
    public void save(CarteBancaire carte) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(carte);
        em.getTransaction().commit();
        em.close();
    }

    public CarteBancaire findById(Long id) {
        EntityManager em = emf.createEntityManager();
        CarteBancaire carte = em.find(CarteBancaire.class, id);
        em.close();
        return carte;
    }

    public List<CarteBancaire> findByCompteId(Long compteId) {
        EntityManager em = emf.createEntityManager();
        List<CarteBancaire> cartes = em.createQuery("SELECT c FROM CarteBancaire c WHERE c.compte.id = :compteId", CarteBancaire.class)
                .setParameter("compteId", compteId)
                .getResultList();
        em.close();
        return cartes;
    }

    public void update(CarteBancaire carte) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.merge(carte);
        em.getTransaction().commit();
        em.close();
    }

    public void delete(CarteBancaire carte) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.remove(em.contains(carte) ? carte : em.merge(carte));
        em.getTransaction().commit();
        em.close();
    }
    public List<CarteBancaire> findAll() {
        EntityManager em = emf.createEntityManager();
        List<CarteBancaire> cartes = em.createQuery("SELECT c FROM CarteBancaire c", CarteBancaire.class).getResultList();
        em.close();
        return cartes;
    }
    // ✅ Ajout de la méthode pour récupérer toutes les cartes d’un client via ses comptes
    public List<CarteBancaire> findByClientId(Long clientId) {
        EntityManager em = emf.createEntityManager();
        List<CarteBancaire> cartes = em.createQuery(
                        "SELECT c FROM CarteBancaire c WHERE c.compte.client.id = :clientId", CarteBancaire.class)
                .setParameter("clientId", clientId)
                .getResultList();
        em.close();
        return cartes;
    }

    public static class AdminRepository {
        private EntityManagerFactory emf = Persistence.createEntityManagerFactory("banquePU");

        public void save(Admin admin) {
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            em.persist(admin);
            em.getTransaction().commit();
            em.close();
        }

        public Admin findById(Long id) {
            EntityManager em = emf.createEntityManager();
            Admin admin = em.find(Admin.class, id);
            em.close();
            return admin;
        }

        public List<Admin> findAll() {
            EntityManager em = emf.createEntityManager();
            List<Admin> admins = em.createQuery("SELECT a FROM Admin a", Admin.class).getResultList();
            em.close();
            return admins;
        }

        public Admin findByUsername(String username) {
            EntityManager em = emf.createEntityManager();
            List<Admin> admins = em.createQuery("SELECT a FROM Admin a WHERE a.username = :username", Admin.class)
                    .setParameter("username", username)
                    .getResultList();
            em.close();
            return admins.isEmpty() ? null : admins.get(0);
        }

        public void update(Admin admin) {
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            em.merge(admin);
            em.getTransaction().commit();
            em.close();
        }

        public void delete(Admin admin) {
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            em.remove(em.contains(admin) ? admin : em.merge(admin));
            em.getTransaction().commit();
            em.close();
        }
    }
}
