package com.chat.socket.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String sender;
    private String receiver; // Null si es mensaje grupal
    private String content;
    private String timestamp;

    // Getter y Setter para 'id'
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // Getter y Setter para 'sender'
    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    // Getter y Setter para 'receiver'
    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    // Getter y Setter para 'content'
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    // Getter y Setter para 'timestamp'
    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
