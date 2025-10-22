package edu.pucmm.eict.modelos;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mensajes")
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String senderId;
    private String mensaje;

    private LocalDateTime timestamp;

    // Constructor por defecto requerido por JPA
    public Mensaje() { }

    public Mensaje(String senderId, String mensaje, LocalDateTime timestamp) {
        this.senderId = senderId;
        this.mensaje = mensaje;
        this.timestamp = timestamp;
    }

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
