package edu.pucmm.eict.modelos;

import jakarta.persistence.*;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "articulos")
public class Articulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;

    @Lob
    private String cuerpo;

    @ManyToOne
    @JoinColumn(name = "autor_username")
    private Usuario autor;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;

    // Cambiamos de List a Set para evitar duplicados.
    @OneToMany(mappedBy = "articulo", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comentario> comentarioList = new LinkedHashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "articulo_etiqueta",
            joinColumns = @JoinColumn(name = "articulo_id"),
            inverseJoinColumns = @JoinColumn(name = "etiqueta_id")
    )
    private List<Etiqueta> etiquetaList = new ArrayList<>();

    public Articulo() {
        // Las colecciones se inicializan al declararlas
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getCuerpo() {
        return cuerpo;
    }

    public void setCuerpo(String cuerpo) {
        this.cuerpo = cuerpo;
    }

    public Usuario getAutor() {
        return autor;
    }

    public void setAutor(Usuario autor) {
        this.autor = autor;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Set<Comentario> getComentarioList() {
        return comentarioList;
    }

    public void setComentarioList(Set<Comentario> comentarioList) {
        this.comentarioList = comentarioList;
    }

    public List<Etiqueta> getEtiquetaList() {
        return etiquetaList;
    }

    public void setEtiquetaList(List<Etiqueta> etiquetaList) {
        this.etiquetaList = etiquetaList;
    }

    // Métodos de conveniencia para manejar etiquetas
    public void addEtiqueta(Etiqueta etiqueta) {
        if (!this.etiquetaList.contains(etiqueta)) {
            this.etiquetaList.add(etiqueta);
            etiqueta.getArticulos().add(this);
        }
    }

    public void removeEtiqueta(Etiqueta etiqueta) {
        if (this.etiquetaList.contains(etiqueta)) {
            this.etiquetaList.remove(etiqueta);
            etiqueta.getArticulos().remove(this);
        }
    }
}
