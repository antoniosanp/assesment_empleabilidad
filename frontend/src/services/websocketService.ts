import { Client, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Message } from '../types/message';

class WebSocketService {
  private client: Client | null = null;
  private currentSubscription: StompSubscription | null = null;
  private activeChannelId: string | null = null;
  private messageCallback: ((message: Message) => void) | null = null;

  connect(onConnected?: () => void) {
    if (this.client && this.client.active) {
      if (onConnected && this.client.connected) onConnected();
      return;
    }

    const wsUrl = window.location.hostname === 'localhost' 
      ? 'http://localhost:8080/ws' 
      : `${window.location.protocol}//${window.location.host}/ws`;

    this.client = new Client({
      webSocketFactory: () => new SockJS(wsUrl),
      reconnectDelay: 3000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        console.log('[WebSocket] STOMP connected successfully.');
        if (this.activeChannelId && this.messageCallback) {
          this.resubscribe();
        }
        if (onConnected) onConnected();
      },
      onStompError: (frame) => {
        console.error('[WebSocket] STOMP error:', frame.headers['message']);
      },
    });

    this.client.activate();
  }

  subscribeToChannel(channelId: string, onMessageReceived: (message: Message) => void) {
    this.activeChannelId = channelId;
    this.messageCallback = onMessageReceived;

    if (!this.client || !this.client.connected) {
      this.connect(() => this.resubscribe());
      return;
    }

    this.resubscribe();
  }

  private resubscribe() {
    if (!this.activeChannelId || !this.messageCallback || !this.client || !this.client.connected) return;

    if (this.currentSubscription) {
      this.currentSubscription.unsubscribe();
      this.currentSubscription = null;
    }

    const topic = `/topic/channels/${this.activeChannelId}`;
    const cb = this.messageCallback;

    this.currentSubscription = this.client.subscribe(topic, (stompMessage) => {
      try {
        const receivedMsg: Message = JSON.parse(stompMessage.body);
        cb(receivedMsg);
      } catch (err) {
        console.error('[WebSocket] Failed to parse message body:', err);
      }
    });
    console.log(`[WebSocket] Subscribed to STOMP topic: ${topic}`);
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
    this.activeChannelId = null;
    this.messageCallback = null;
  }
}

export const websocketService = new WebSocketService();
