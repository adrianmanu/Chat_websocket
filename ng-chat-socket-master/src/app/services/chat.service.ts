import { Injectable } from '@angular/core';
import { Stomp } from '@stomp/stompjs';
import * as SockJS from 'sockjs-client';
import { BehaviorSubject, Observable } from 'rxjs';
import { ChatMessage } from '../models/chat-message';

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private stompClient: any;
  public isConnected: boolean = false;

  private messageSubject: BehaviorSubject<ChatMessage[]> = new BehaviorSubject<ChatMessage[]>([]);
  private connectedUsersSubject: BehaviorSubject<string[]> = new BehaviorSubject<string[]>([]);
  private notificationsSubject: BehaviorSubject<string[]> = new BehaviorSubject<string[]>([]);

  constructor() {}

  /**
   * Inicializa la conexión WebSocket si no está conectada.
   */
  private initConnectionSocket(callback?: () => void): void {
    if (this.isConnected || this.stompClient) return;

    const url = 'http://10.1.150.68:3000/chat-socket';
    const socket = new SockJS(url);
    this.stompClient = Stomp.over(socket);
    this.stompClient.debug = () => {}; // Deshabilitar logs de depuración

    this.stompClient.connect({}, () => {
      console.log('✅ Conectado al WebSocket');
      this.isConnected = true;
      
      if (callback) callback(); // Ejecutar callback cuando esté conectado

    }, (error: any) => {
      console.error('❌ Error de conexión WebSocket:', error);
      this.isConnected = false;
      setTimeout(() => this.initConnectionSocket(callback), 5000); // Reintentar conexión
    });
  }

  /**
   * Unirse a una sala de chat.
   */
  joinRoom(roomId: string, user: string): void {
    this.initConnectionSocket(() => {
      if (!this.stompClient) {
        console.error('❌ El cliente WebSocket no está inicializado.');
        return;
      }

      console.log(`👤 ${user} se unió a la sala ${roomId}`);

      const joinMessage: ChatMessage = {
        message: `${user} se ha unido al chat.`,
        user,
        time: new Date().toLocaleTimeString()
      };
      this.sendJoinMessage(joinMessage);

      // 🔔 Suscripción a mensajes del chat
      this.stompClient.subscribe(`/topic/${roomId}`, (message: any) => {
        this.addMessage(JSON.parse(message.body));
      });

      // 🔔 Suscripción a notificaciones
      this.stompClient.subscribe('/topic/notifications', (notification: any) => {
        console.log('🔔 Notificación recibida:', notification.body);
        this.addNotification(notification.body);
      });

      // 👥 Suscripción a la lista de usuarios conectados
      this.stompClient.subscribe('/topic/connected-users', (users: any) => {
        try {
          const parsedUsers = JSON.parse(users.body);
          if (Array.isArray(parsedUsers)) {
            console.log('👥 Usuarios conectados recibidos:', parsedUsers);
            this.connectedUsersSubject.next(parsedUsers);
          } else {
            console.error('❌ Formato inválido de usuarios conectados:', parsedUsers);
          }
        } catch (error) {
          console.error('❌ Error al parsear usuarios conectados:', error);
        }
      });
    });
  }

  /**
   * Suscribirse a mensajes privados.
   */
  subscribeToPrivateMessages(user: string): void {
    if (!this.isConnected || !this.stompClient) {
      console.error('❌ WebSocket no está conectado o no está inicializado.');
      return;
    }

    const topic = `/topic/private/${user}`;
    this.stompClient.subscribe(topic, (message: any) => {
      this.addMessage(JSON.parse(message.body));
    });

    console.log(`📬 Suscrito al canal privado: ${topic}`);
  }

  /**
   * Enviar un mensaje al chat público o privado.
   */
  sendMessage(roomId: string, message: ChatMessage): void {
    if (!this.isConnected || !this.stompClient) {
      console.error('❌ No se pudo enviar el mensaje: WebSocket no conectado.');
      return;
    }
  
    const destination = message.recipient
      ? `/app/chat/private/${message.recipient}`
      : `/app/chat/${roomId}`;
  
    this.stompClient.send(destination, {}, JSON.stringify(message)); // Envia el mensaje
  }
  

  /**
   * Enviar mensaje de ingreso a la sala.
   */
  private sendJoinMessage(message: ChatMessage): void {
    if (this.isConnected && this.stompClient) {
      this.stompClient.send(`/app/chat/join`, {}, JSON.stringify(message));
    }
  }

  /**
   * Salir de la sala de chat.
   */
  leaveRoom(user: string): void {
    if (!this.isConnected || !this.stompClient) return;

    const leaveMessage: ChatMessage = {
      message: `${user} ha salido del chat.`,
      user,
      time: new Date().toLocaleTimeString()
    };

    this.stompClient.send(`/app/chat/leave`, {}, JSON.stringify(leaveMessage));
    
    this.stompClient.disconnect(() => {
      console.log('🔌 Desconectado del WebSocket');
      this.isConnected = false;
      this.stompClient = null;
    });

    this.addNotification(`${user} ha salido del chat.`);
    this.removeUser(user);
  }

  /**
   * Agregar un nuevo mensaje recibido.
   */
  private addMessage(newMessage: ChatMessage): void {
    this.messageSubject.next([...this.messageSubject.getValue(), newMessage]);
  }

  /**
   * Agregar una notificación nueva.
   */
  private addNotification(notification: string): void {
    this.notificationsSubject.next([...this.notificationsSubject.getValue(), notification]);
  }

  /**
   * Remover un usuario de la lista de conectados.
   */
  private removeUser(user: string): void {
    const updatedUsers = this.connectedUsersSubject.getValue().filter(u => u !== user);
    this.connectedUsersSubject.next(updatedUsers);
  }

  /**
   * Obtener mensajes como Observable.
   */
  getMessages(): Observable<ChatMessage[]> {
    return this.messageSubject.asObservable();
  }

  /**
   * Obtener notificaciones como Observable.
   */
  getNotifications(): Observable<string[]> {
    return this.notificationsSubject.asObservable();
  }

  /**
   * Obtener la lista de usuarios conectados como Observable.
   */
  getConnectedUsers(): Observable<string[]> {
    return this.connectedUsersSubject.asObservable();
  }
}
