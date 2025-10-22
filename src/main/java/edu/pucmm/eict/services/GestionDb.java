package edu.pucmm.eict.services;

import jakarta.persistence.*;
import jakarta.persistence.criteria.*;

import java.util.List;

public class GestionDb<T> {

    private static EntityManagerFactory emf;
    private Class<T> claseEntidad;

    public GestionDb(Class<T> claseEntidad) {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("AjaxPU");
        }
        this.claseEntidad = claseEntidad;
    }

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public T crear(T entidad) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(entidad);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
        return entidad;
    }

    public T editar(T entidad) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            T resultado = em.merge(entidad);
            em.getTransaction().commit();
            return resultado;
        } catch (Exception e) {
            em.getTransaction().rollback();
            e.printStackTrace();
            return null;
        } finally {
            em.close();
        }
    }

    public boolean eliminar(Object entidadId) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            T entidad = em.find(claseEntidad, entidadId);
            if (entidad != null) {
                em.remove(entidad);
                em.getTransaction().commit();
                return true;
            }
            em.getTransaction().rollback();
            return false;
        } catch (Exception e) {
            em.getTransaction().rollback();
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    public T find(Object id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(claseEntidad, id);
        } finally {
            em.close();
        }
    }

    public List<T> findAll() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery<T> criteriaQuery = em.getCriteriaBuilder().createQuery(claseEntidad);
            criteriaQuery.select(criteriaQuery.from(claseEntidad));
            return em.createQuery(criteriaQuery).getResultList();
        } finally {
            em.close();
        }
    }

    public T findByField(String fieldName, Object value) {
        EntityManager em = getEntityManager();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<T> query = cb.createQuery(claseEntidad);
            Root<T> root = query.from(claseEntidad);

            // Crear la condición de búsqueda
            Predicate predicate = cb.equal(root.get(fieldName), value);
            query.where(predicate);

            // Ejecutar la consulta
            TypedQuery<T> typedQuery = em.createQuery(query);
            return typedQuery.getSingleResult();
        } catch (NoResultException e) {
            return null; // No se encontró el resultado
        } finally {
            em.close();
        }
    }

    public List<T> findAllByField(String fieldName, Object value) {
        EntityManager em = getEntityManager();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<T> query = cb.createQuery(claseEntidad);
            Root<T> root = query.from(claseEntidad);

            // Crear la condición de búsqueda
            Predicate predicate = cb.equal(root.get(fieldName), value);
            query.where(predicate);

            // Ejecutar la consulta
            TypedQuery<T> typedQuery = em.createQuery(query);
            return typedQuery.getResultList();
        } finally {
            em.close();
        }
    }
}
