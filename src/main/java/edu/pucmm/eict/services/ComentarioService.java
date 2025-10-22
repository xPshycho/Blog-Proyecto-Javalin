package edu.pucmm.eict.services;

import edu.pucmm.eict.modelos.Comentario;
import java.util.List;

public class ComentarioService {

    private GestionDb<Comentario> gestion = new GestionDb<>(Comentario.class);

    public Comentario crearComentario(Comentario comentario) {
        return gestion.crear(comentario);
    }

    public Comentario editarComentario(Comentario comentario) {
        return gestion.editar(comentario);
    }

    public boolean eliminarComentario(Long id) {
        return gestion.eliminar(id);
    }

    public Comentario findComentario(Long id) {
        return gestion.find(id);
    }

    public List<Comentario> findAllComentarios() {
        return gestion.findAll();
    }
}
