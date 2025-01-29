export interface ChatMessage {
  message: string;
  user: string;
  recipient?: string;
  time: string;
  message_side?: 'sender' | 'receiver'; // Nueva propiedad para el lado del mensaje
}
