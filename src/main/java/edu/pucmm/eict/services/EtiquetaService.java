package edu.pucmm.eict.services;

import edu.pucmm.eict.modelos.Etiqueta;
import java.util.List;

public class EtiquetaService {

    private GestionDb<Etiqueta> gestion = new GestionDb<>(Etiqueta.class);

    public Etiqueta crearEtiqueta(Etiqueta etiqueta) {
        return gestion.crear(etiqueta);
    }

    public Etiqueta editarEtiqueta(Etiqueta etiqueta) {
        return gestion.editar(etiqueta);
    }

    public boolean eliminarEtiqueta(Long id) {
        return gestion.eliminar(id);
    }

    public Etiqueta findEtiqueta(Long id) {
        return gestion.find(id);
    }

    public List<Etiqueta> findAllEtiquetas() {
        return gestion.findAll();
    }

    // Buscar etiqueta por nombre usando el metodo genérico
    public Etiqueta findEtiquetaPorNombre(String nombre) {
        return gestion.findByField("etiqueta", nombre);
    }

    // Si quieres obtener varias etiquetas con el mismo nombre
    public List<Etiqueta> findEtiquetasPorNombre(String nombre) {
        return gestion.findAllByField("etiqueta", nombre);
    }
}
