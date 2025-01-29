import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ChatService } from 'src/app/services/chat.service';
import { ChatMessage } from 'src/app/models/chat-message';
import { trigger, transition, style, animate } from '@angular/animations';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.scss'],
  animations: [
    trigger('fadeInOut', [
      transition(':enter', [
        style({ opacity: 0 }),
        animate('500ms', style({ opacity: 1 })),
      ]),
      transition(':leave', [
        animate('500ms', style({ opacity: 0 })),
      ]),
    ]),
  ],
})
export class ChatComponent implements OnInit, OnDestroy {
  messageInput: string = '';
  userId: string = '';
  recipientId: string = '';
  messageList: ChatMessage[] = [];
  notifications: string[] = [];
  connectedUsers: string[] = [];
  private messageSubscription!: Subscription;

  constructor(private chatService: ChatService, private route: ActivatedRoute) {}

  ngOnInit(): void {
    this.userId = this.route.snapshot.params['userId'];
    this.chatService.joinRoom('ABC', this.userId);

    this.listenForUpdates();
  }

  ngOnDestroy(): void {
    this.chatService.leaveRoom(this.userId);
    if (this.messageSubscription) {
      this.messageSubscription.unsubscribe();
    }
  }

  sendMessage() {
    if (!this.messageInput.trim()) {
      this.showNotification("El mensaje no puede estar vacío.");
      return;
    }

    if (this.messageInput.length > 100) {
      this.showNotification("El mensaje no puede tener más de 100 caracteres.");
      return;
    }

    const chatMessage: ChatMessage = {
      message: this.messageInput.trim(),
      user: this.userId,
      recipient: this.recipientId.trim() || '',
      time: new Date().toLocaleTimeString()
    };

    console.log("📤 Enviando mensaje:", chatMessage);  // ✅ Verifica en consola si se está enviando correctamente

    this.chatService.sendMessage('ABC', chatMessage);
    this.messageInput = '';
  }

  listenForUpdates() {
    this.messageSubscription = this.chatService.getMessages()
      .subscribe((newMessages: ChatMessage[]) => {
        newMessages.forEach((newMessage: ChatMessage) => {
          const messageExists = this.messageList.some(msg => msg.message === newMessage.message && msg.time === newMessage.time);
          if (!messageExists) {
            this.messageList.push({
              ...newMessage,
              message_side: newMessage.user === this.userId ? 'sender' : 'receiver'
            });
            console.log("📥 Mensaje recibido en el frontend:", newMessage);  // ✅ Depuración
          }
        });
      });

    this.chatService.getNotifications().subscribe((notifications: string[]) => {
      notifications.forEach(notification => this.showNotification(notification));
    });

    this.chatService.getConnectedUsers().subscribe((users: string[]) => {
      this.connectedUsers = [...new Set(users)];
    });
  }

  selectRecipient(user: string) {
    if (!this.chatService.isConnected) {
      console.error('❌ No se puede seleccionar el destinatario, WebSocket no está conectado.');
      return;
    }

    this.recipientId = user;
    console.log(`📩 Ahora chateas con: ${user}`);
    this.chatService.subscribeToPrivateMessages(user);
  }

  showNotification(notification: string) {
    if (!this.notifications.includes(notification)) {
      this.notifications.push(notification);
      setTimeout(() => {
        this.notifications = this.notifications.filter(n => n !== notification);
      }, 2000);
    }
  }
}
