package com.chat.socket.repositories;

import com.chat.socket.model.entity.ChatMessage;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends CrudRepository<ChatMessage, Long> {
    List<ChatMessage> findByUser(String user);  // Buscar mensajes por usuario
    Optional<ChatMessage> findById(Long id);    // Buscar mensaje por ID
}
