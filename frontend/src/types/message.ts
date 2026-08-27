export type MessageStatus = 'PENDING' | 'SENT' | 'FAILED';

export interface Message {
  id: number;
  tempId?: string; // Used for UI optimism during pending state
  channelId: string;
  senderId: string;
  senderName: string;
  content: string;
  status: MessageStatus;
  isEdited: boolean;
  isDeleted: boolean;
  createdAt: string;
}

export interface SendMessageRequest {
  channelId: string;
  content: string;
}

export interface KeysetPageResponse<T> {
  items: T[];
  nextAfterId?: number;
  hasMore: boolean;
}
