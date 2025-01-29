package com.chat.socket.services;

import com.chat.socket.model.entity.ChatMessage;
import com.chat.socket.repositories.ChatMessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessageServiceImpl(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    @Override
    public ChatMessage saveMessage(ChatMessage chatMessage) {
        return chatMessageRepository.save(chatMessage);  // Guardar el mensaje
    }

    @Override
    public List<ChatMessage> getAllMessages() {
        return (List<ChatMessage>) chatMessageRepository.findAll();  // Obtener todos los mensajes
    }

    @Override
    public List<ChatMessage> getMessagesByUser(String user) {
        return chatMessageRepository.findByUser(user);  // Buscar mensajes por usuario
    }
}
