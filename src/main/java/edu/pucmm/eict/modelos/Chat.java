package edu.pucmm.eict.modelos;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chats")
public class Chat {

    @Id
    private String id;

    private String nombreCompleto;
    private String correo;
    private String telefono;
    private String razonConsulta;
    private String articleId;
    private String authorId;

    // Se modifica para que la colección se cargue EAGERmente
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "chat_id")
    private List<Mensaje> mensajes = new ArrayList<>();

    // Constructor por defecto requerido por JPA
    public Chat() { }

    public Chat(String id, String nombreCompleto, String correo, String telefono, String razonConsulta, String articleId, String authorId) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.correo = correo;
        this.telefono = telefono;
        this.razonConsulta = razonConsulta;
        this.articleId = articleId;
        this.authorId = authorId;
    }

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getRazonConsulta() { return razonConsulta; }
    public void setRazonConsulta(String razonConsulta) { this.razonConsulta = razonConsulta; }

    public String getArticleId() { return articleId; }
    public void setArticleId(String articleId) { this.articleId = articleId; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public List<Mensaje> getMensajes() { return mensajes; }
    public void setMensajes(List<Mensaje> mensajes) { this.mensajes = mensajes; }
}
