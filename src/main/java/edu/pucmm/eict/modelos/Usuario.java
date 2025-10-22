package edu.pucmm.eict.modelos;

import jakarta.persistence.*;

import static edu.pucmm.eict.controladores.FotoController.FOTO_POR_DEFECTO;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    private String username;

    private String nombre;

    private String password;

    private boolean administrador;

    private boolean autor;

    @Lob
    @Column(length = 1000000)
    private String foto; // Almacena la imagen en base64

    private String fotoContentType; // Almacena el tipo de contenido (ej. image/jpeg)

    public Usuario() {
        // Constructor vacío
    }

    public Usuario(String username, String nombre, String password, boolean administrador, boolean autor) {
        this.username = username;
        this.nombre = nombre;
        this.password = password;
        this.administrador = administrador;
        this.autor = autor;
    }

    // Getters y Setters

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAdministrador() {
        return administrador;
    }

    public void setAdministrador(boolean administrador) {
        this.administrador = administrador;
    }

    public boolean isAutor() {
        return autor;
    }

    public void setAutor(boolean autor) {
        this.autor = autor;
    }

    public String getFoto() {
        return (foto != null) ? foto : FOTO_POR_DEFECTO;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getFotoContentType() {
        return fotoContentType;
    }

    public void setFotoContentType(String fotoContentType) {
        this.fotoContentType = fotoContentType;
    }

}
