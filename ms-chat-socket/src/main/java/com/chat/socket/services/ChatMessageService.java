package com.chat.socket.services;

import com.chat.socket.model.entity.ChatMessage;

import java.util.List;

public interface ChatMessageService {
    ChatMessage saveMessage(ChatMessage chatMessage); // Guardar un mensaje
    List<ChatMessage> getAllMessages(); // Obtener todos los mensajes
    List<ChatMessage> getMessagesByUser(String user); // Obtener mensajes por usuario
}
