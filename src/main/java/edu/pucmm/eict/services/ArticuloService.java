package edu.pucmm.eict.services;

import edu.pucmm.eict.modelos.Articulo;
import edu.pucmm.eict.modelos.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class ArticuloService {

    private GestionDb<Articulo> gestion = new GestionDb<>(Articulo.class);

    public Articulo findArticulo(Long id) {
        EntityManager em = gestion.getEntityManager();
        try {
            TypedQuery<Articulo> query = em.createQuery(
                    "SELECT DISTINCT a FROM Articulo a " +
                            "LEFT JOIN FETCH a.etiquetaList " +
                            "LEFT JOIN FETCH a.comentarioList " +
                            "WHERE a.id = :id", Articulo.class);
            query.setParameter("id", id);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public List<Articulo> findAllArticulos() {
        EntityManager em = gestion.getEntityManager();
        try {
            TypedQuery<Articulo> query = em.createQuery(
                    "SELECT DISTINCT a FROM Articulo a " +
                            "LEFT JOIN FETCH a.etiquetaList " +
                            "LEFT JOIN FETCH a.comentarioList", Articulo.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Articulo crearArticulo(Articulo articulo) {
        return gestion.crear(articulo);
    }

    public Articulo editarArticulo(Articulo articulo) {
        return gestion.editar(articulo);
    }

    public boolean eliminarArticulo(Long id) {
        return gestion.eliminar(id);
    }


    public List<Articulo> findArticulosPaginated(int page, int pageSize) {
        EntityManager em = gestion.getEntityManager();
        try {
            TypedQuery<Articulo> query = em.createQuery(
                    "SELECT DISTINCT a FROM Articulo a " +
                            "LEFT JOIN FETCH a.etiquetaList " +
                            "LEFT JOIN FETCH a.comentarioList " +
                            "ORDER BY a.fecha DESC", Articulo.class);
            query.setFirstResult((page - 1) * pageSize);
            query.setMaxResults(pageSize);
            return query.getResultList();
        } finally {
            em.close();
        }
    }


    public long countAllArticulos() {
        EntityManager em = gestion.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery("SELECT COUNT(a) FROM Articulo a", Long.class);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    // Metodo para obtener artículos asociados a una etiqueta específica
    public List<Articulo> findArticulosByEtiqueta(String etiqueta) {
        EntityManager em = gestion.getEntityManager();
        try {
            TypedQuery<Articulo> query = em.createQuery(
                    "SELECT DISTINCT a FROM Articulo a JOIN a.etiquetaList e " +
                            "WHERE e.etiqueta = :etiqueta ORDER BY a.fecha DESC", Articulo.class);
            query.setParameter("etiqueta", etiqueta.toLowerCase());
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Articulo> findArticulosByEtiquetasPaginated(List<String> tagNames, int page, int pageSize) {
        EntityManager em = gestion.getEntityManager();
        try {
            String jpql = "SELECT a FROM Articulo a JOIN a.etiquetaList e " +
                    "WHERE e.etiqueta IN :tagNames " +
                    "GROUP BY a " +
                    "HAVING COUNT(DISTINCT e.etiqueta) = :numTags " +
                    "ORDER BY a.fecha DESC";
            TypedQuery<Articulo> query = em.createQuery(jpql, Articulo.class);
            query.setParameter("tagNames", tagNames);
            query.setParameter("numTags", (long) tagNames.size());
            query.setFirstResult((page - 1) * pageSize);
            query.setMaxResults(pageSize);
            List<Articulo> articulos = query.getResultList();
            // Forzamos la carga de la colección etiquetaList para cada artículo
            for (Articulo a : articulos) {
                a.getEtiquetaList().size();
            }
            return articulos;
        } finally {
            em.close();
        }
    }

    public long countArticulosByEtiquetas(List<String> tagNames) {
        EntityManager em = gestion.getEntityManager();
        try {
            String jpql = "SELECT a.id FROM Articulo a JOIN a.etiquetaList e " +
                    "WHERE e.etiqueta IN :tagNames " +
                    "GROUP BY a.id " +
                    "HAVING COUNT(DISTINCT e.etiqueta) = :numTags";
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            query.setParameter("tagNames", tagNames);
            query.setParameter("numTags", (long) tagNames.size());
            List<Long> ids = query.getResultList();
            return ids.size();
        } finally {
            em.close();
        }
    }

    public List<Articulo> findArticulosByAutorPaginated(Usuario autor, int page, int pageSize) {
        EntityManager em = gestion.getEntityManager();
        try {
            TypedQuery<Articulo> query = em.createQuery(
                    "SELECT DISTINCT a FROM Articulo a " +
                            "LEFT JOIN FETCH a.etiquetaList " +
                            "LEFT JOIN FETCH a.comentarioList " +
                            "WHERE a.autor = :autor " +
                            "ORDER BY a.fecha DESC", Articulo.class);
            query.setParameter("autor", autor);
            query.setFirstResult((page - 1) * pageSize);
            query.setMaxResults(pageSize);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public long countArticulosByAutor(Usuario autor) {
        EntityManager em = gestion.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(a) FROM Articulo a WHERE a.autor = :autor", Long.class);
            query.setParameter("autor", autor);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }
}
