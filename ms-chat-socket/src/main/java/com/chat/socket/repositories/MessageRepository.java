package com.chat.socket.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.chat.socket.model.entity.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {
}
