package edu.pucmm.eict.modelos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "comentarios")
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    private String comentario;

    @ManyToOne
    @JoinColumn(name = "autor_username")
    private Usuario autor;

    @ManyToOne
    @JoinColumn(name = "articulo_id")
    @JsonIgnore  // Evita la referencia cíclica
    private Articulo articulo;

    public Comentario() {
        // Constructor vacío
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public Usuario getAutor() {
        return autor;
    }

    public void setAutor(Usuario autor) {
        this.autor = autor;
    }

    public Articulo getArticulo() {
        return articulo;
    }

    public void setArticulo(Articulo articulo) {
        this.articulo = articulo;
    }
}
