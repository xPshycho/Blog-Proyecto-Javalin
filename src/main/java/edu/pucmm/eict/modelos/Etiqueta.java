package edu.pucmm.eict.modelos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "etiquetas")
public class Etiqueta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String etiqueta;

    // Relación inversa a Articulo.etiquetaList
    @ManyToMany(mappedBy = "etiquetaList")
    @JsonIgnore // Se ignora la serialización de la relación inversa para evitar LazyInitializationException
    private Set<Articulo> articulos = new HashSet<>();

    public Etiqueta() {
        // Constructor vacío
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public void setEtiqueta(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public Set<Articulo> getArticulos() {
        return articulos;
    }

    public void setArticulos(Set<Articulo> articulos) {
        this.articulos = articulos;
    }
}
