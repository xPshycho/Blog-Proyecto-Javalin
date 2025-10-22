package edu.pucmm.eict.services;

import edu.pucmm.eict.modelos.Usuario;
import java.util.List;

public class UserService {

    private GestionDb<Usuario> gestion = new GestionDb<>(Usuario.class);

    public Usuario crearUsuario(Usuario usuario) {
        return gestion.crear(usuario);
    }

    public Usuario editarUsuario(Usuario usuario) {
        return gestion.editar(usuario);
    }

    public boolean eliminarUsuario(String username) {
        return gestion.eliminar(username);
    }

    public Usuario findUsuario(String username) {
        return gestion.find(username);
    }

    public List<Usuario> findAllUsuarios() {
        return gestion.findAll();
    }

    public void registrarUsuario(String username, String nombre, String password, boolean administrador, boolean autor) {
        Usuario usuario = new Usuario(username, nombre, password, administrador, autor);
        crearUsuario(usuario);
    }
    public void actualizarUsuario(Usuario usuario) {
        gestion.editar(usuario);
    }

}
