package com.chat.socket.controller;

import com.chat.socket.model.entity.ChatMessage;
import com.chat.socket.model.entity.Message;
import com.chat.socket.repositories.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
public class WebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);
    private final Set<String> connectedUsers = Collections.synchronizedSet(new HashSet<>());
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MessageRepository messageRepository;

    public WebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // ------------------------ MENSAJES PÚBLICOS ------------------------

    @MessageMapping("/chat/{roomId}")
    @SendTo("/topic/{roomId}")
    public ChatMessage chat(@DestinationVariable String roomId, @Payload ChatMessage message) {
        validateMessage(message);
        message.setTime(getCurrentTime());
        logger.info("Mensaje en sala {} de {}: {}", roomId, message.getUser(), message.getMessage());
        return message;
    }

    // ------------------------ MENSAJES PRIVADOS ------------------------

    @MessageMapping("/chat/private/{recipient}")
    public void privateChat(@DestinationVariable String recipient, @Payload ChatMessage message) {
        validateMessage(message);

        if (recipient == null || recipient.trim().isEmpty()) {
            logger.warn("Intento de enviar mensaje privado sin destinatario válido.");
            return;
        }

        message.setTime(getCurrentTime());
        logger.info("Mensaje privado de {} para {}: {}", message.getUser(), recipient, message.getMessage());

        messagingTemplate.convertAndSend("/topic/private/" + recipient, message);
    }

    // ------------------------ MANEJO DE CONEXIONES ------------------------

    // Unirse al chat (actualizar lista de usuarios)
    @MessageMapping("/chat/join")
    @SendTo("/topic/connected-users")
    public Set<String> join(@Payload ChatMessage message) {
        connectedUsers.add(message.getUser());
        logger.info("{} se ha conectado.", message.getUser());
        messagingTemplate.convertAndSend("/topic/notifications", message.getUser() + " se ha conectado.");
        return connectedUsers;
    }

    // Gestionar conexión de un usuario
    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = headerAccessor.getFirstNativeHeader("username");

        if (username != null) {
            connectedUsers.add(username);
            logger.info("{} se ha conectado.", username);
            headerAccessor.getSessionAttributes().put("username", username);
            messagingTemplate.convertAndSend("/topic/connected-users", connectedUsers);
        }
    }

    // Gestionar desconexión de un usuario
    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) headerAccessor.getSessionAttributes().get("username");

        if (username != null) {
            connectedUsers.remove(username);
            logger.info("{} se ha desconectado.", username);
            messagingTemplate.convertAndSend("/topic/notifications", username + " se ha desconectado.");
            messagingTemplate.convertAndSend("/topic/connected-users", connectedUsers);
        } else {
            logger.info("Usuario desconectado: Desconocido");
        }
    }

    // Guardar mensajes en la base de datos
    @MessageMapping("/message")
    @SendTo("/chatroom/public")
    public Message receiveMessage(@Payload Message message) {
        validateMessage(message);
        message.setTimestamp(LocalDateTime.now().toString());  // Establece la marca de tiempo.

        messageRepository.save(message); // Guarda el mensaje en la base de datos.

        logger.info("Mensaje guardado de {}: {}", message.getSender(), message.getContent());
        return message;
    }

    // Recuperar todos los mensajes
    @GetMapping("/messages")
    public List<Message> getAllMessages() {
        logger.info("Recuperando todos los mensajes de la base de datos");
        return messageRepository.findAll();
    }

    // Validar contenido del mensaje
    private void validateMessage(ChatMessage message) {
        if (message.getMessage() == null || message.getMessage().trim().isEmpty()) {
            throw new IllegalArgumentException("El mensaje no puede estar vacío");
        }
        if (message.getMessage().length() > 100) {
            throw new IllegalArgumentException("El mensaje no puede superar los 100 caracteres");
        }
    }

    private void validateMessage(Message message) {
        if (message.getContent() == null || message.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("El mensaje no puede estar vacío");
        }
        if (message.getContent().length() > 100) {
            throw new IllegalArgumentException("El mensaje no puede superar los 100 caracteres");
        }
    }

    private String getCurrentTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private Set<String> filterConnectedUsers(String currentUser) {
        Set<String> filteredUsers = new HashSet<>(connectedUsers);
        filteredUsers.remove(currentUser);
        return filteredUsers;
    }

    // ------------------------ MANEJO DE ERRORES ------------------------

    @ControllerAdvice
    public static class WebSocketErrorHandler {
        @MessageExceptionHandler
        public String handleException(Throwable exception) {
            LoggerFactory.getLogger(WebSocketErrorHandler.class).error("Error en WebSocket: ", exception);
            return exception.getMessage();
        }
    }
}
