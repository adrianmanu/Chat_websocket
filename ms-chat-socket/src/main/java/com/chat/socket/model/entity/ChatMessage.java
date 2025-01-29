package com.chat.socket.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class ChatMessage {
    @Id
    private Long id;
    private String message;  // El mensaje enviado
    private String user;     // El usuario que envió el mensaje
    private String recipient; // destinatario del mensaje (si es privado)
    private String time;     // La hora de envío del mensaje

    // Constructor con todos los parámetros
    public ChatMessage(String message, String user, String recipient, String time) {
        this.message = message;
        this.user = user;
        this.recipient = recipient;
        this.time = time;
    }

    // Constructor sin destinatario (para mensajes públicos)
    public ChatMessage(String message, String user, String time) {
        this.message = message;
        this.user = user;
        this.time = time;
    }

    // Constructor sin parámetros
    public ChatMessage() {}

    // Getter y Setter para 'message'
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // Getter y Setter para 'user'
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    // Getter y Setter para 'recipient'
    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    // Getter y Setter para 'time'
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
