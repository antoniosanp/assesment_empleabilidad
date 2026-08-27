import { Client, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Message } from '../types/message';

class WebSocketService {
  private client: Client | null = null;
  private currentSubscription: StompSubscription | null = null;

  connect(onConnected?: () => void) {
    if (this.client && this.client.active) return;

    this.client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        console.log('[WebSocket] STOMP connected successfully.');
        if (onConnected) onConnected();
      },
      onStompError: (frame) => {
        console.error('[WebSocket] STOMP error:', frame.headers['message']);
      },
    });

    this.client.activate();
  }

  subscribeToChannel(channelId: string, onMessageReceived: (message: Message) => void) {
    if (this.currentSubscription) {
      this.currentSubscription.unsubscribe();
      this.currentSubscription = null;
    }

    if (!this.client || !this.client.connected) {
      this.connect(() => this.subscribeToChannel(channelId, onMessageReceived));
      return;
    }

    const topic = `/topic/channels/${channelId}`;
    this.currentSubscription = this.client.subscribe(topic, (stompMessage) => {
      try {
        const receivedMsg: Message = JSON.parse(stompMessage.body);
        onMessageReceived(receivedMsg);
      } catch (err) {
        console.error('[WebSocket] Failed to parse message body:', err);
      }
    });
  }

  disconnect() {
    if (this.currentSubscription) {
      this.currentSubscription.unsubscribe();
      this.currentSubscription = null;
    }
    if (this.client) {
      this.client.deactivate();
      this.client = null;
    }
  }
}

export const websocketService = new WebSocketService();
