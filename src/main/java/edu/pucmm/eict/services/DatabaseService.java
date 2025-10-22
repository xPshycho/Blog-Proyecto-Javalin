package edu.pucmm.eict.services;

import edu.pucmm.eict.modelos.Articulo;
import edu.pucmm.eict.modelos.Usuario;
import edu.pucmm.eict.modelos.Comentario;
import edu.pucmm.eict.modelos.Etiqueta;

import java.util.*;
import java.util.stream.Collectors;

import edu.pucmm.eict.services.UserService;

/**
 * Servicio que actúa como base de datos en memoria.
 * Almacena y gestiona usuarios, artículos, comentarios y etiquetas.
 */
public class DatabaseService {

    private static DatabaseService instance;

    private List<Usuario> usuarios = new ArrayList<>();
    private List<Articulo> articulos = new ArrayList<>();
    private List<Comentario> comentarios = new ArrayList<>();
    private List<Etiqueta> etiquetas = new ArrayList<>();
    private GestionDb<Usuario> gestion = new GestionDb<>(Usuario.class);
    private UserService userService = new UserService();

    /**
     * Constructor privado para aplicar el patrón Singleton.
     * Inicializa la base de datos con un usuario administrador por defecto.
     */
    public void inicializarDatos() {
        if (userService.findUsuario("admin") == null) {
            gestion.crear(new Usuario("admin", "Administrador", "admin", true, true));
        }
    }



    /**
     * Obtiene la instancia única de DatabaseService.
     * @return La instancia única de la base de datos.
     */
    public static DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    /**
     * Guarda un artículo en la base de datos asignándole un ID único.
     * @param articulo El artículo a guardar.
     */
    public void guardarArticulo(Articulo articulo) {
        articulo.setId((long) (articulos.size() + 1));
        articulos.add(articulo);
    }

    /**
     * Obtiene un artículo por su ID.
     * @param id ID del artículo.
     * @return El artículo encontrado o null si no existe.
     */
    public Articulo getArticuloById(long id) {
        return articulos.stream().filter(a -> a.getId() == id).findFirst().orElse(null);
    }

    /**
     * Obtiene un artículo por su ID en formato String.
     * @param id ID del artículo como String.
     * @return El artículo encontrado o null si no existe.
     */
    public Articulo getArticuloById(String id) {
        long articuloId = Long.parseLong(id);
        return articulos.stream().filter(a -> a.getId() == articuloId).findFirst().orElse(null);
    }

    /**
     * Obtiene los artículos creados por un autor específico.
     * @param usuario Usuario autor de los artículos.
     * @return Lista de artículos del usuario.
     */
    public List<Articulo> getArticulosByAutor(Usuario usuario) {
        return articulos.stream()
                .filter(articulo -> articulo.getAutor().equals(usuario))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todos los artículos almacenados en la base de datos.
     * @return Lista de artículos.
     */
    public List<Articulo> obtenerArticulos() {
        return articulos;
    }

    /**
     * Obtiene la lista de usuarios registrados.
     * @return Lista de usuarios.
     */
    public List<Usuario> getUsuarios() {
        return usuarios;
    }

    /**
     * Obtiene la lista de comentarios registrados.
     * @return Lista de comentarios.
     */
    public List<Comentario> getComentarios() {
        return comentarios;
    }

    /**
     * Obtiene la lista de etiquetas registradas.
     * @return Lista de etiquetas.
     */
    public List<Etiqueta> getEtiquetas() {
        return etiquetas;
    }

    /**
     * Obtiene un usuario por su nombre de usuario.
     * @param username Nombre de usuario.
     * @return Usuario encontrado o un Optional vacío si no existe.
     */
    public Optional<Usuario> getUsuarioPorUsername(String username) {
        return usuarios.stream().filter(u -> u.getUsername().equals(username)).findFirst();
    }

    /**
     * Agrega un nuevo usuario a la base de datos.
     * @param usuario Usuario a agregar.
     */
    public void agregarUsuario(Usuario usuario) {
        usuarios.add(usuario);
    }

    /**
     * Agrega un nuevo comentario a la base de datos.
     * @param comentario Comentario a agregar.
     */
    public void agregarComentario(Comentario comentario) {
        comentarios.add(comentario);
    }

    /**
     * Agrega una nueva etiqueta a la base de datos.
     * @param etiqueta Etiqueta a agregar.
     */
    public void agregarEtiqueta(Etiqueta etiqueta) {
        etiquetas.add(etiqueta);
    }
}
