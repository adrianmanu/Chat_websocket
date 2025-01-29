package com.chat.socket.services;

import com.chat.socket.model.entity.Message;
import com.chat.socket.repositories.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    @Autowired
    private MessageRepository messageRepository;

    // Método que maneja el envío de un mensaje
    public void saveMessage(String message, String sender, String recipient, String timestamp) {
        // Si el destinatario es null, es un mensaje grupal
        Message newMessage = new Message();
        newMessage.setSender(sender);
        newMessage.setReceiver(recipient != null ? recipient : null);
        newMessage.setContent(message);
        newMessage.setTimestamp(timestamp);

        // Guardar el mensaje en la base de datos
        messageRepository.save(newMessage);
    }
}
